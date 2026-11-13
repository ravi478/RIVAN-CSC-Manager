package com.rivan.cscmanager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LocalStore(app)
    private val _data = MutableStateFlow(store.load())
    val data: StateFlow<AppData> = _data.asStateFlow()

    fun mutate(action: (AppData) -> Unit, audit: String? = null, module: String = "System", detail: String = "") {
        val d = _data.value
        action(d)
        if (audit != null) {
            d.auditLogs.add(0, AuditLog("AUD-${System.currentTimeMillis()}", d.auth.userName, d.auth.role, audit, module, detail, now()))
        }
        store.save(d)
        _data.value = d.copy()
    }

    fun loginAdmin(id:String, password:String):Boolean {
        if (id.trim().equals("ADMIN-01", true) && password == "1234") {
            mutate({ it.auth = AuthState(true,"admin","ADMIN-01","Ravi Kumar","ALL") }, "Admin Login", "Security", "Admin logged in")
            return true
        }
        return false
    }

    fun loginOperator(id:String, password:String):String? {
        val op = _data.value.operators.firstOrNull { it.id.equals(id.trim(), true) }
            ?: return "Operator ID not found"
        if (op.status != "Active") return "Access blocked: ${op.status}"
        if (op.password != password) return "Wrong password"
        mutate({ it.auth = AuthState(true,"operator",op.id,op.name,op.cscId) }, "Operator Login", "Security", "${op.name} logged in")
        return null
    }

    fun logout() { mutate({ it.auth.loggedIn=false }, "Logout", "Security", "User logged out") }

    fun addCsc(c:Csc) = mutate({ it.cscs.add(c) }, "CSC Added", "CSC Centers", c.name)
    fun toggleCsc(id:String) = mutate({ it.cscs.find { c -> c.id==id }?.let { c -> c.status = if(c.status=="Active") "Inactive" else "Active" } }, "CSC Status Changed", "CSC Centers", id)

    fun addOperator(o:Operator) = mutate({ it.operators.add(o) }, "Operator Added", "Operators", o.id)
    fun approveOperator(id:String) = mutate({ it.operators.find { o -> o.id==id }?.apply { status="Active"; deviceApproved=true } }, "Operator Approved", "Operators", id)
    fun toggleOperator(id:String) = mutate({ it.operators.find { o -> o.id==id }?.let { o -> o.status=if(o.status=="Active") "Inactive" else "Active" } }, "Operator Status Changed", "Operators", id)

    fun addCustomer(c:Customer) = mutate({ it.customers.add(c) }, "Customer Added", "Customers", c.name)
    fun addService(s:ServiceItem) = mutate({ it.services.add(s) }, "Service Added", "Services", s.name)
    fun toggleService(id:String) = mutate({ it.services.find { s -> s.id==id }?.let { s -> s.status=if(s.status=="Active") "Inactive" else "Active" } }, "Service Status Changed", "Services", id)

    fun addApplication(a:Application) {
        mutate({
            it.applications.add(0,a)
            it.incomes.add(0, Income("INC-${System.currentTimeMillis()}","App: ${a.id} (${a.serviceName})","Service Income",a.amount,a.paymentMode,a.paymentRef,a.cscId,a.operatorName,a.date,true))
        }, "Application Created", "Applications", a.id)
    }
    fun updateApplicationStatus(id:String,status:String) { val user=_data.value.auth.userName; mutate({ it.applications.find { a -> a.id==id }?.apply { this.status=status; history.add("$status • $user") } }, "Application Status Changed", "Applications", "$id → $status") }

    fun addIncome(i:Income) = mutate({ it.incomes.add(0,i) }, "Income Added", "Finance", i.title)
    fun addExpense(e:Expense) = mutate({ it.expenses.add(0,e) }, "Expense Added", "Finance", e.title)

    fun addRequest(r:RequestItem) = mutate({ it.requests.add(0,r) }, "Request Created", "Refunds", r.id)
    fun approveRequest(id:String) = mutate({
        val r=it.requests.find { x->x.id==id } ?: return@mutate
        r.status="Approved"
        if(r.type=="Refund") it.expenses.add(0,Expense("EXP-${System.currentTimeMillis()}","Refund ${r.appId}","Refund",r.amount,"Original Payment","${r.appId}",r.cscId,r.by,r.date,"Approved"))
    }, "Request Approved", "Refunds", id)
    fun rejectRequest(id:String) = mutate({ it.requests.find { x->x.id==id }?.status="Rejected" }, "Request Rejected", "Refunds", id)

    fun checkIn(opId:String, cscId:String, photo:String="") {
        val op=_data.value.operators.firstOrNull { it.id==opId } ?: return
        mutate({
            val today=day(); val existing=it.attendance.firstOrNull { a->a.operatorId==opId && a.date==today }
            if(existing==null) it.attendance.add(0,Attendance("ATT-${System.currentTimeMillis()}",op.id,op.name,cscId,today,timeNow(),"",attendanceStatus(timeNow(),it.settings.graceMinutes),photo))
            else { existing.checkIn=timeNow(); existing.status=attendanceStatus(existing.checkIn,it.settings.graceMinutes); existing.photoPath=photo }
        }, "Attendance Check-in", "Attendance", op.name)
    }
    fun checkOut(opId:String) = mutate({ it.attendance.firstOrNull { a->a.operatorId==opId && a.date==day() }?.checkOut=timeNow() }, "Attendance Check-out", "Attendance", opId)

    fun addLeave(l:LeaveRequest) = mutate({ it.leaves.add(0,l) }, "Leave Requested", "Leave", l.operatorName)
    fun approveLeave(id:String) = mutate({ it.leaves.find { x->x.id==id }?.status="Approved" }, "Leave Approved", "Leave", id)
    fun rejectLeave(id:String) = mutate({ it.leaves.find { x->x.id==id }?.status="Rejected" }, "Leave Rejected", "Leave", id)

    fun addPayroll(p:Payroll) = mutate({ it.payrolls.add(0,p) }, "Payroll Saved", "Payroll", p.operatorName)
    fun markPayrollPaid(id:String) = mutate({ it.payrolls.find { p->p.id==id }?.status="Paid" }, "Payroll Paid", "Payroll", id)

    fun addDocument(doc:DocumentRecord) = mutate({ it.documents.add(0,doc) }, "Document Added", "Documents", doc.customerName)
    fun markNotificationRead(id:String) = mutate({ it.notifications.find { n->n.id==id }?.read=true }, "Notification Read", "Notifications", id)

    fun saveBranding(b:Branding) = mutate({ it.branding=b }, "Branding Updated", "Branding", b.appName)
    fun saveSettings(s:Settings) = mutate({ it.settings=s }, "Settings Updated", "Security", "System rules updated")
    fun snapshot() = mutate({ it.backups.add(0,BackupRecord("BKP-${System.currentTimeMillis()}",now(),"Manual Local Snapshot",it.auth.userName,"Saved")) }, "Backup Snapshot", "Security", "Manual snapshot created")
    fun resetDemo() { store.clear(); _data.value = store.load() }

    fun exportBytes():ByteArray = store.exportBytes(_data.value)
    fun importBytes(bytes:ByteArray) { val imported=store.importBytes(bytes); _data.value=imported; store.save(imported) }

    fun nextId(prefix:String, existing:Int):String = "$prefix-${String.format("%03d", existing+1)}"
    private fun now()=SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(Date())
    private fun day()=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date())
    private fun timeNow()=SimpleDateFormat("HH:mm",Locale.US).format(Date())
    private fun attendanceStatus(time:String,grace:Int):String { val p=time.split(":"); val mins=p[0].toInt()*60+p[1].toInt(); return if(mins<=9*60+grace) "Present" else "Late" }
}
