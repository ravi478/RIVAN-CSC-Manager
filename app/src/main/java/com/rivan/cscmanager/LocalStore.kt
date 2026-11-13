package com.rivan.cscmanager

import android.content.Context
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("rivan_csc_store", Context.MODE_PRIVATE)
    private val key = "app_data"

    fun load(): AppData {
        val raw = prefs.getString(key, null) ?: return seed().also { save(it) }
        return try {
            val bytes = Base64.getDecoder().decode(raw)
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as AppData }
        } catch (_: Exception) { seed().also { save(it) } }
    }

    fun save(data: AppData) {
        val out = ByteArrayOutputStream()
        ObjectOutputStream(out).use { it.writeObject(data) }
        prefs.edit().putString(key, Base64.getEncoder().encodeToString(out.toByteArray())).apply()
    }

    fun exportBytes(data: AppData): ByteArray {
        val out = ByteArrayOutputStream()
        ObjectOutputStream(out).use { it.writeObject(data) }
        return out.toByteArray()
    }

    fun importBytes(bytes: ByteArray): AppData = ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as AppData }
    fun clear() { prefs.edit().clear().apply() }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun seed(): AppData {
        val d = AppData()
        d.auth = AuthState(false)
        d.cscs += listOf(
            Csc("CSC-001","Gudhan CSC (Main)","Gudhan","Rohtak",mobile="9812011111",email="gudhan@rivancsc.in",manager="Ravi Kumar"),
            Csc("CSC-002","Kalanaur Digital Kendra","Kalanaur","Rohtak",mobile="9812022222",email="kalanaur@rivancsc.in",manager="Sandeep Sharma"),
            Csc("CSC-003","Julana Seva Center","Julana","Jind",open="09:30",close="17:30",status="Inactive")
        )
        d.operators += listOf(
            Operator("OP-001","Sandeep Sharma","9812022222","sandeep@rivancsc.in","CSC-002","1234","2026-01-15",12000.0,"Active",true),
            Operator("OP-002","Pooja Rani","9812044444","pooja@rivancsc.in","CSC-001","1234","2026-03-01",11000.0,"Active",true),
            Operator("OP-003","Vikram Singh","9812055555","vikram@rivancsc.in","CSC-001","1234","2026-11-01",10000.0,"Pending",false)
        )
        d.services += listOf(
            ServiceItem("SRV-01","PM Kisan Registration","Agriculture",30.0,"New farmer registration & eKYC"),
            ServiceItem("SRV-02","Ayushman Card","Health",30.0,"PM-JAY Golden Card"),
            ServiceItem("SRV-03","PAN Card (New/Correction)","Identity",110.0,"NSDL / UTI PAN application"),
            ServiceItem("SRV-04","Income Certificate","Revenue",40.0,"Saral Haryana Income Certificate"),
            ServiceItem("SRV-05","Caste / Domicile Certificate","Revenue",40.0,"Haryana Resident & Caste"),
            ServiceItem("SRV-06","Parivar Pehchan Patra (PPP)","State",30.0,"Family ID update & correction"),
            ServiceItem("SRV-07","e-Shram / Pension","Welfare",30.0,"Old age / Widow pension & e-Shram"),
            ServiceItem("SRV-08","Printing / Photo / Lamination","Retail",20.0,"Urgent documents & passport photo"),
            ServiceItem("SRV-09","Passport","Documents",200.0,"Passport application assistance")
        )
        d.customers += listOf(
            Customer("CUST-1001","Ramesh Chander","9813010101",village="Gudhan",district="Rohtak",pincode="124113",notes="Regular farmer beneficiary",cscId="CSC-001",createdAt=today()),
            Customer("CUST-1002","Sunita Devi","9813020202","9813020203","Kalanaur","Rohtak","Haryana","124113",true,"Family ID verified","CSC-002",today())
        )
        d.applications += listOf(
            Application("RIVAN-2026-0001","CUST-1001","Ramesh Chander","9813010101","SRV-01","PM Kisan Registration",30.0,"UPI","UPI8849201","Verified","Completed","CSC-001","ADMIN-01","Ravi Kumar",today(),"eKYC done",mutableListOf("Completed • Ravi Kumar")),
            Application("RIVAN-2026-0002","CUST-1002","Sunita Devi","9813020202","SRV-04","Income Certificate",40.0,"Cash","CASH-02","Pending Verification","Processing","CSC-002","OP-001","Sandeep Sharma",today(),"Patwari report uploaded",mutableListOf("Processing • Sandeep Sharma"))
        )
        d.incomes += Income("INC-001","App: RIVAN-2026-0001 (PM Kisan)","Service Income",30.0,"UPI","UPI8849201","CSC-001","Ravi Kumar",today())
        d.expenses += Expense("EXP-001","A4 Paper Rim & Ink","Stationery",450.0,"Cash","BILL-19","CSC-001","Ravi Kumar",today())
        d.requests += RequestItem("REQ-001","Refund","RIVAN-2026-0002",40.0,"Duplicate fee paid by customer","Sandeep Sharma","CSC-002",today())
        d.attendance += listOf(
            Attendance("ATT-01","OP-001","Sandeep Sharma","CSC-002",today(),"08:58","","Present"),
            Attendance("ATT-02","OP-002","Pooja Rani","CSC-001",today(),"09:22","","Late")
        )
        d.leaves += LeaveRequest("LV-01","OP-002","Pooja Rani","Casual Leave",today(),today(),"Family function",true)
        d.payrolls += Payroll("PAY-01","OP-001","Sandeep Sharma","2026-10",12000.0,500.0,0.0,1000.0,11500.0,"Paid")
        d.documents += DocumentRecord("DOC-01","CUST-1001","Ramesh Chander","PM Kisan","Aadhaar & Land Jamabandi",expiry="2027-12-31",uploadedBy="Ravi Kumar",date=today())
        d.notifications += listOf(
            NotificationItem("NOT-01","New Refund Request","Sandeep Sharma requested ₹40 refund for RIVAN-2026-0002","10 mins ago"),
            NotificationItem("NOT-02","Operator Approval Pending","Vikram Singh (OP-003) awaiting admin activation","1 hour ago")
        )
        d.backups += BackupRecord("BKP-01",today()+" 08:00","Auto Local Snapshot","System","Success")
        return d
    }
}
