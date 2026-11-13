package com.rivan.cscmanager

import java.io.Serializable

data class Branding(var appName:String="RIVAN CSC Manager", var tagline:String="Digital Seva & 100 CSC Network", var primary:String="#087F45", var theme:String="light", var footer:String="Powered by RIVAN Digital Services") : Serializable

data class Settings(var checkInTime:String="09:00", var checkOutTime:String="18:00", var graceMinutes:Int=15, var expenseApprovalLimit:Double=1000.0, var autoBackup:String="Daily", var smsEnabled:Boolean=true, var whatsappEnabled:Boolean=true) : Serializable

data class AuthState(var loggedIn:Boolean=false, var role:String="", var userId:String="", var userName:String="", var cscId:String="ALL") : Serializable

data class Csc(var id:String, var name:String, var village:String, var district:String, var state:String="Haryana", var mobile:String="", var email:String="", var manager:String="", var open:String="09:00", var close:String="18:00", var status:String="Active") : Serializable

data class Operator(var id:String, var name:String, var mobile:String, var email:String, var cscId:String, var password:String="1234", var joining:String="", var basicSalary:Double=0.0, var status:String="Pending", var deviceApproved:Boolean=false) : Serializable

data class Customer(var id:String, var name:String, var mobile:String, var altMobile:String="", var village:String="", var district:String="", var state:String="Haryana", var pincode:String="", var consent:Boolean=true, var notes:String="", var cscId:String="", var createdAt:String="") : Serializable

data class ServiceItem(var id:String, var name:String, var category:String, var price:Double, var desc:String="", var status:String="Active") : Serializable

data class Application(var id:String, var customerId:String, var customerName:String, var customerMobile:String, var serviceId:String, var serviceName:String, var amount:Double, var paymentMode:String, var paymentRef:String, var paymentStatus:String="Verified", var status:String="Pending", var cscId:String, var operatorId:String, var operatorName:String, var date:String, var notes:String="", var history:MutableList<String> = mutableListOf()) : Serializable

data class Income(var id:String, var title:String, var category:String, var amount:Double, var mode:String, var ref:String, var cscId:String, var operator:String, var date:String, var auto:Boolean=true) : Serializable

data class Expense(var id:String, var title:String, var category:String, var amount:Double, var mode:String, var ref:String, var cscId:String, var operator:String, var date:String, var status:String="Approved") : Serializable

data class RequestItem(var id:String, var type:String, var appId:String, var amount:Double, var reason:String, var by:String, var cscId:String, var date:String, var status:String="Pending") : Serializable

data class Attendance(var id:String, var operatorId:String, var operatorName:String, var cscId:String, var date:String, var checkIn:String="", var checkOut:String="", var status:String="Present", var photoPath:String="") : Serializable

data class LeaveRequest(var id:String, var operatorId:String, var operatorName:String, var type:String, var from:String, var to:String, var reason:String, var paid:Boolean, var status:String="Pending") : Serializable

data class Payroll(var id:String, var operatorId:String, var operatorName:String, var month:String, var basic:Double, var bonus:Double, var deduction:Double, var advance:Double, var net:Double, var status:String="Draft") : Serializable

data class DocumentRecord(var id:String, var customerId:String, var customerName:String, var service:String, var docType:String, var uri:String="", var expiry:String="", var version:String="v1", var uploadedBy:String="", var date:String="") : Serializable

data class NotificationItem(var id:String, var title:String, var message:String, var time:String, var read:Boolean=false) : Serializable

data class AuditLog(var id:String, var user:String, var role:String, var action:String, var module:String, var detail:String, var time:String) : Serializable

data class BackupRecord(var id:String, var date:String, var type:String, var by:String, var status:String) : Serializable

data class AppData(
    var branding:Branding=Branding(), var settings:Settings=Settings(), var auth:AuthState=AuthState(),
    var cscs:MutableList<Csc> = mutableListOf(), var operators:MutableList<Operator> = mutableListOf(),
    var customers:MutableList<Customer> = mutableListOf(), var services:MutableList<ServiceItem> = mutableListOf(),
    var applications:MutableList<Application> = mutableListOf(), var incomes:MutableList<Income> = mutableListOf(),
    var expenses:MutableList<Expense> = mutableListOf(), var requests:MutableList<RequestItem> = mutableListOf(),
    var attendance:MutableList<Attendance> = mutableListOf(), var leaves:MutableList<LeaveRequest> = mutableListOf(),
    var payrolls:MutableList<Payroll> = mutableListOf(), var documents:MutableList<DocumentRecord> = mutableListOf(),
    var notifications:MutableList<NotificationItem> = mutableListOf(), var auditLogs:MutableList<AuditLog> = mutableListOf(),
    var backups:MutableList<BackupRecord> = mutableListOf()
) : Serializable
