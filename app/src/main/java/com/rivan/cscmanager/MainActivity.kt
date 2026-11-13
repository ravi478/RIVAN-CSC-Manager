
package com.rivan.cscmanager

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val vm by viewModels<AppViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RivanApp(vm)
        }
    }
}

enum class Section(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    CENTERS("Centers", Icons.Default.Business),
    WORK("Work", Icons.Default.Description),
    FINANCE("Finance", Icons.Default.Payments),
    HR("HR", Icons.Default.People),
    SYSTEM("System", Icons.Default.Settings)
}

@Composable
fun RivanApp(vm: AppViewModel) {
    val data by vm.data.collectAsStateWithLifecycle()
    var dark by rememberSaveable {
        mutableStateOf(data.branding.theme == "dark")
    }

    LaunchedEffect(data.branding.theme) {
        dark = data.branding.theme == "dark"
    }

    MaterialTheme(
        colorScheme = if (dark) {
            darkColorScheme(
                primary = Color(0xFF48D597),
                secondary = Color(0xFF7BDCB5)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF087F45),
                secondary = Color(0xFF0F9D68)
            )
        }
    ) {
        Surface(Modifier.fillMaxSize()) {
            AnimatedContent(
                targetState = data.auth.loggedIn,
                transitionSpec = {
                    fadeIn() + scaleIn(initialScale = 0.98f) togetherWith fadeOut()
                },
                label = "auth"
            ) { logged ->
                if (!logged) {
                    LoginScreen(vm)
                } else {
                    MainShell(vm, dark) { dark = it }
                }
            }
        }
    }
}

@Composable
fun LoginScreen(vm: AppViewModel) {
    val context = LocalContext.current
    var role by rememberSaveable { mutableStateOf("admin") }
    var id by rememberSaveable { mutableStateOf("ADMIN-01") }
    var pass by rememberSaveable { mutableStateOf("1234") }
    var error by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                    )
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically() + fadeIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(78.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "R",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        "RIVAN CSC Manager",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Digital Seva • 100 CSC Network",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(28.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleChip("Admin", role == "admin") {
                            role = "admin"
                            id = "ADMIN-01"
                            error = ""
                        }
                        RoleChip("Operator", role == "operator") {
                            role = "operator"
                            id = "OP-001"
                            error = ""
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = id,
                        onValueChange = { id = it },
                        label = { Text("Login ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val ok = if (role == "admin") {
                                vm.loginAdmin(id, pass)
                            } else {
                                vm.loginOperator(id, pass) == null
                            }

                            if (!ok) {
                                error = "Login failed. Check ID/password or approval status."
                            } else {
                                Toast.makeText(
                                    context,
                                    "Welcome to RIVAN CSC Manager",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Secure Login", fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        "Demo Admin: ADMIN-01 / 1234 • Operator: OP-001 / 1234",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.RoleChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Box(
            Modifier.padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun MainShell(
    vm: AppViewModel,
    dark: Boolean,
    onTheme: (Boolean) -> Unit
) {
    val data by vm.data.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf(Section.HOME) }
    var page by rememberSaveable { mutableStateOf("dashboard") }

    val operator = data.auth.role == "operator"

    val navItems = if (operator) {
        listOf(
            Section.HOME,
            Section.WORK,
            Section.FINANCE,
            Section.HR,
            Section.SYSTEM
        )
    } else {
        Section.values().toList()
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    NavigationBarItem(
                        selected = section == item,
                        onClick = {
                            section = item
                            page = when (item) {
                                Section.HOME -> "dashboard"
                                Section.CENTERS -> "cscs"
                                Section.WORK -> "work"
                                Section.FINANCE -> "finance"
                                Section.HR -> "hr"
                                Section.SYSTEM -> "system"
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) },
                        alwaysShowLabel = true
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TopBar(data, onTheme, dark, vm)

            AnimatedContent(
                targetState = page,
                label = "page"
            ) { selectedPage ->
                PageHost(selectedPage, data, vm) {
                    page = it
                }
            }
        }
    }
}

@Composable
fun TopBar(
    data: AppData,
    onTheme: (Boolean) -> Unit,
    dark: Boolean,
    vm: AppViewModel
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                data.branding.appName,
                fontWeight = FontWeight.Black,
                fontSize = 19.sp
            )
            Text(
                if (data.auth.role == "admin") {
                    "Admin • Network Control"
                } else {
                    "Operator • ${data.auth.cscId}"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = { onTheme(!dark) }) {
            Icon(
                if (dark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Change theme"
            )
        }

        IconButton(onClick = vm::logout) {
            Icon(Icons.Default.Logout, contentDescription = "Logout")
        }
    }
}

@Composable
fun PageHost(
    page: String,
    data: AppData,
    vm: AppViewModel,
    setPage: (String) -> Unit
) {
    when (page) {
        "dashboard" -> Dashboard(data, setPage)
        "cscs" -> CscScreen(data, vm)
        "operators" -> OperatorScreen(data, vm)
        "customers" -> CustomerScreen(data, vm)
        "services" -> ServiceScreen(data, vm)
        "applications" -> ApplicationScreen(data, vm)
        "work" -> WorkHub(setPage)
        "finance" -> FinanceScreen(data, vm)
        "payments" -> PaymentsScreen(data)
        "requests" -> RequestsScreen(data, vm)
        "attendance" -> AttendanceScreen(data, vm)
        "leaves" -> LeaveScreen(data, vm)
        "payroll" -> PayrollScreen(data, vm)
        "documents" -> DocumentScreen(data, vm)
        "reports" -> ReportsScreen(data)
        "notifications" -> NotificationScreen(data, vm)
        "system" -> SystemHub(setPage)
        "branding" -> BrandingScreen(data, vm)
        "security" -> SecurityScreen(data, vm)
        "profile" -> ProfileScreen(data)
        else -> Dashboard(data, setPage)
    }
}

@Composable
fun Dashboard(data: AppData, setPage: (String) -> Unit) {
    val scope = data.auth.cscId
    val apps = data.applications.filter {
        scope == "ALL" || it.cscId == scope
    }
    val incomes = data.incomes.filter {
        scope == "ALL" || it.cscId == scope
    }.sumOf { it.amount }
    val expenses = data.expenses.filter {
        scope == "ALL" || it.cscId == scope
    }.sumOf { it.amount }
    val pending = data.requests.count { it.status == "Pending" }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "Good ${greet()} 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "Your CSC operations at a glance",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    "Applications",
                    apps.size.toString(),
                    Icons.Default.Description,
                    Modifier.weight(1f)
                )
                MetricCard(
                    "Income",
                    "₹${money(incomes)}",
                    Icons.Default.TrendingUp,
                    Modifier.weight(1f)
                )
                MetricCard(
                    "Expenses",
                    "₹${money(expenses)}",
                    Icons.Default.TrendingDown,
                    Modifier.weight(1f)
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    "Profit",
                    "₹${money(incomes - expenses)}",
                    Icons.Default.AccountBalanceWallet,
                    Modifier.weight(1f)
                )
                MetricCard(
                    "Pending",
                    pending.toString(),
                    Icons.Default.PendingActions,
                    Modifier.weight(1f)
                )
            }
        }

        item { SectionTitle("Quick Actions") }

        item {
            QuickGrid(
                listOf(
                    "New Customer" to "customers",
                    "New Application" to "applications",
                    "Attendance" to "attendance",
                    "Payment" to "payments",
                    "Refund Request" to "requests",
                    "Reports" to "reports"
                ),
                setPage
            )
        }

        item { SectionTitle("Recent Applications") }

        items(apps.take(5), key = { it.id }) {
            AppRow(it)
        }

        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier
) {
    Card(
        modifier,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(7.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(
                title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
}

@Composable
fun QuickGrid(
    items: List<Pair<String, String>>,
    setPage: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { (title, page) ->
                    Card(
                        Modifier
                            .weight(1f)
                            .clickable { setPage(page) },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(title, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AppRow(a: Application) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(a.serviceName, fontWeight = FontWeight.Bold)
                Text(
                    "${a.id} • ${a.customerName}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusPill(a.status)
        }
    }
}

@Composable
fun StatusPill(status: String) {
    val color = when (status) {
        "Completed", "Active", "Approved", "Paid" ->
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        "Pending", "Processing", "Pending Verification", "NEW" ->
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
        "Rejected", "Inactive" ->
            MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(99.dp)
    ) {
        Text(
            status,
            Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun WorkHub(setPage: (String) -> Unit) {
    Hub(
        "Work & Services",
        listOf(
            "Customers" to "customers",
            "Applications" to "applications",
            "Services" to "services",
            "Payments & Receipts" to "payments",
            "Refunds & Cancels" to "requests",
            "Document Vault" to "documents"
        ),
        setPage
    )
}

@Composable
fun SystemHub(setPage: (String) -> Unit) {
    Hub(
        "System & Administration",
        listOf(
            "Operators" to "operators",
            "Reports & Export" to "reports",
            "Notifications" to "notifications",
            "Branding" to "branding",
            "Security & Backup" to "security",
            "Profile" to "profile"
        ),
        setPage
    )
}

@Composable
fun Hub(
    title: String,
    items: List<Pair<String, String>>,
    setPage: (String) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Text("Choose a module", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        items(items) { (text, page) ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .clickable { setPage(page) },
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text.take(1),
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(text, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun CscScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("100 CSC Centers", data.cscs.size, { show = true }, "Add CSC") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.cscs) { c ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Row {
                            Text(c.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            StatusPill(c.status)
                        }
                        Text(
                            "${c.village}, ${c.district} • ${c.mobile}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Manager: ${c.manager}", fontSize = 12.sp)
                        TextButton(onClick = { vm.toggleCsc(c.id) }) {
                            Text(if (c.status == "Active") "Deactivate" else "Activate")
                        }
                    }
                }
            }
        }
    }

    if (show) {
        CscForm {
            vm.addCsc(it)
            show = false
        }
    }
}

@Composable
fun OperatorScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Operators", data.operators.size, { show = true }, "Add Operator") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.operators) { o ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Row {
                            Text(o.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            StatusPill(o.status)
                        }
                        Text(
                            "${o.id} • ${o.cscId} • ${o.mobile}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("Salary ₹${money(o.basicSalary)}", fontSize = 12.sp)

                        Row {
                            if (o.status == "Pending") {
                                TextButton(onClick = { vm.approveOperator(o.id) }) {
                                    Text("Approve")
                                }
                            }
                            TextButton(onClick = { vm.toggleOperator(o.id) }) {
                                Text(if (o.status == "Active") "Deactivate" else "Activate")
                            }
                        }
                    }
                }
            }
        }
    }

    if (show) {
        OperatorForm(data.cscs) {
            vm.addOperator(it)
            show = false
        }
    }
}

@Composable
fun CustomerScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Customers", data.customers.size, { show = true }, "Add Customer") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.customers) { c ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Text(c.name, fontWeight = FontWeight.Bold)
                        Text(
                            "${c.mobile} • ${c.village}, ${c.district}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text("CSC: ${c.cscId}", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (show) {
        CustomerForm(data.cscs) {
            vm.addCustomer(it)
            show = false
        }
    }
}

@Composable
fun ServiceScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Services", data.services.size, { show = true }, "Add Service") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.services) { s ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Row(
                        Modifier.padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(s.name, fontWeight = FontWeight.Bold)
                            Text(
                                "${s.category} • ₹${money(s.price)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusPill(s.status)
                        TextButton(onClick = { vm.toggleService(s.id) }) {
                            Text(if (s.status == "Active") "Off" else "On")
                        }
                    }
                }
            }
        }
    }

    if (show) {
        ServiceForm {
            vm.addService(it)
            show = false
        }
    }
}

@Composable
fun ApplicationScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Applications", data.applications.size, { show = true }, "New Application") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.applications) { a ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Row {
                            Column(Modifier.weight(1f)) {
                                Text(a.serviceName, fontWeight = FontWeight.Bold)
                                Text(
                                    a.id,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusPill(a.status)
                        }
                        Text(
                            "${a.customerName} • ₹${money(a.amount)} • ${a.paymentMode}",
                            fontSize = 12.sp
                        )
                        Row {
                            if (a.status != "Completed") {
                                TextButton(
                                    onClick = {
                                        vm.updateApplicationStatus(a.id, "Completed")
                                    }
                                ) {
                                    Text("Mark Completed")
                                }
                            }
                            TextButton(
                                onClick = {
                                    vm.updateApplicationStatus(a.id, "Processing")
                                }
                            ) {
                                Text("Processing")
                            }
                        }
                    }
                }
            }
        }
    }

    if (show) {
        ApplicationForm(data) {
            vm.addApplication(it)
            show = false
        }
    }
}

@Composable
fun FinanceScreen(data: AppData, vm: AppViewModel) {
    var tab by rememberSaveable { mutableStateOf(0) }
    var show by remember { mutableStateOf(false) }

    val income = data.incomes.sumOf { it.amount }
    val expense = data.expenses.sumOf { it.amount }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Income, Expense & Profit", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tab == 0,
                onClick = { tab = 0 },
                label = { Text("Income") }
            )
            FilterChip(
                selected = tab == 1,
                onClick = { tab = 1 },
                label = { Text("Expense") }
            )
            FilterChip(
                selected = tab == 2,
                onClick = { tab = 2 },
                label = { Text("Profit") }
            )
        }

        Spacer(Modifier.height(10.dp))

        if (tab < 2) {
            Button(
                onClick = { show = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (tab == 0) "+ Add Income" else "+ Add Expense")
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            "Income ₹${money(income)}   •   Expense ₹${money(expense)}   •   Profit ₹${money(income - expense)}",
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (tab == 0) {
                items(data.incomes) {
                    FinanceRow(it.title, it.amount, it.category)
                }
            } else if (tab == 1) {
                items(data.expenses) {
                    FinanceRow(it.title, it.amount, it.category)
                }
            } else {
                item {
                    FinanceRow("Net Profit", income - expense, "Summary")
                }
            }
        }
    }

    if (show) {
        if (tab == 0) {
            IncomeForm(data.cscs) {
                vm.addIncome(it)
                show = false
            }
        } else {
            ExpenseForm(data.cscs) {
                vm.addExpense(it)
                show = false
            }
        }
    }
}

@Composable
fun FinanceRow(title: String, amount: Double, category: String) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(
                    category,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("₹${money(amount)}", fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun PaymentsScreen(data: AppData) {
    ListScreen("Payments & Receipts", data.applications.size, {}, "") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.applications) { a ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Text(a.id, fontWeight = FontWeight.Bold)
                        Text("${a.customerName} • ${a.serviceName}", fontSize = 12.sp)
                        Text(
                            "₹${money(a.amount)} • ${a.paymentMode} • ${a.paymentRef}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        StatusPill(a.paymentStatus)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestsScreen(data: AppData, vm: AppViewModel) {
    val admin = data.auth.role == "admin"

    ListScreen("Refunds & Cancels", data.requests.size, {}, "") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(data.requests) { r ->
                Card(shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(15.dp)) {
                        Row {
                            Text(
                                "${r.type} • ${r.appId}",
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            StatusPill(r.status)
                        }
                        Text("₹${money(r.amount)} • ${r.reason}", fontSize = 12.sp)
                        Text(
                            "By ${r.by} • ${r.cscId}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (admin && r.status == "Pending") {
                            Row {
                                TextButton(onClick = { vm.approveRequest(r.id) }) {
                                    Text("Approve")
                                }
                                TextButton(onClick = { vm.rejectRequest(r.id) }) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AttendanceScreen(data: AppData, vm: AppViewModel) {
    val context = LocalContext.current
    val me = data.auth

    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val file = File(
                context.filesDir,
                "attendance_${System.currentTimeMillis()}.jpg"
            )
            file.outputStream().use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
            }
            vm.checkIn(me.userId, me.cscId, file.absolutePath)
        }
    }

    val permission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) camera.launch(null)
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Attendance", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(10.dp))

        if (me.role == "operator") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            camera.launch(null)
                        } else {
                            permission.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📸 Check In")
                }

                Button(
                    onClick = { vm.checkOut(me.userId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Check Out")
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(
                data.attendance.filter {
                    me.cscId == "ALL" || it.cscId == me.cscId
                }
            ) { a ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(a.operatorName, fontWeight = FontWeight.Bold)
                            Text("${a.date} • ${a.cscId}", fontSize = 11.sp)
                            Text(
                                "In ${a.checkIn} • Out ${a.checkOut.ifBlank { "—" }}",
                                fontSize = 12.sp
                            )
                        }
                        StatusPill(a.status)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaveScreen(data: AppData, vm: AppViewModel) {
    val admin = data.auth.role == "admin"
    var show by remember { mutableStateOf(false) }

    ListScreen(
        "Leave Management",
        data.leaves.size,
        { show = true },
        if (admin) "" else "Request Leave"
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data.leaves) { leave ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row {
                            Text(
                                leave.operatorName,
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            StatusPill(leave.status)
                        }
                        Text(
                            "${leave.type} • ${leave.from} → ${leave.to}",
                            fontSize = 12.sp
                        )
                        Text(leave.reason, fontSize = 12.sp)

                        if (admin && leave.status == "Pending") {
                            Row {
                                TextButton(
                                    onClick = { vm.approveLeave(leave.id) }
                                ) {
                                    Text("Approve")
                                }
                                TextButton(
                                    onClick = { vm.rejectLeave(leave.id) }
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (show) {
        LeaveForm(
            data.operators.firstOrNull { it.id == data.auth.userId }
        ) {
            vm.addLeave(it)
            show = false
        }
    }
}

@Composable
fun PayrollScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Salary / Payroll", data.payrolls.size, { show = true }, "Create Payroll") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data.payrolls) { p ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Row {
                            Text(
                                p.operatorName,
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            StatusPill(p.status)
                        }
                        Text("${p.month} • Net ₹${money(p.net)}", fontSize = 12.sp)
                        Text(
                            "Basic ₹${money(p.basic)} + Bonus ₹${money(p.bonus)} − Deduction ₹${money(p.deduction)} − Advance ₹${money(p.advance)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (p.status != "Paid") {
                            TextButton(
                                onClick = { vm.markPayrollPaid(p.id) }
                            ) {
                                Text("Mark Paid")
                            }
                        }
                    }
                }
            }
        }
    }

    if (show) {
        PayrollForm(data.operators) {
            vm.addPayroll(it)
            show = false
        }
    }
}

@Composable
fun DocumentScreen(data: AppData, vm: AppViewModel) {
    var show by remember { mutableStateOf(false) }

    ListScreen("Document Vault", data.documents.size, { show = true }, "Add Document") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data.documents) { document ->
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text(document.customerName, fontWeight = FontWeight.Bold)
                        Text(
                            "${document.service} • ${document.docType}",
                            fontSize = 12.sp
                        )
                        Text(
                            "Expiry: ${document.expiry.ifBlank { "Not set" }} • ${document.version}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (show) {
        DocumentForm(data.customers) {
            vm.addDocument(it)
            show = false
        }
    }
}

@Composable
fun ReportsScreen(data: AppData) {
    val context = LocalContext.current
    val income = data.incomes.sumOf { it.amount }
    val expense = data.expenses.sumOf { it.amount }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Reports & Export", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))

        ReportCard("Applications", data.applications.size.toString())
        ReportCard("Customers", data.customers.size.toString())
        ReportCard("Income", "₹${money(income)}")
        ReportCard("Expense", "₹${money(expense)}")
        ReportCard("Profit", "₹${money(income - expense)}")

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_TEXT, buildCsv(data))
                }
                context.startActivity(
                    Intent.createChooser(sendIntent, "Export CSV")
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export CSV")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Phase 3 report layer is local/offline. Online scheduled reports are reserved for Phase 4.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportCard(title: String, value: String) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 7.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(15.dp)) {
            Text(title, Modifier.weight(1f), fontWeight = FontWeight.Bold)
            Text(value, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun NotificationScreen(data: AppData, vm: AppViewModel) {
    ListScreen("Notifications", data.notifications.size, {}, "") {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(data.notifications) { notification ->
                Card(
                    Modifier.clickable {
                        vm.markNotificationRead(notification.id)
                    },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row {
                            Text(
                                notification.title,
                                Modifier.weight(1f),
                                fontWeight = FontWeight.Bold
                            )
                            if (!notification.read) StatusPill("NEW")
                        }
                        Text(notification.message, fontSize = 12.sp)
                        Text(
                            notification.time,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BrandingScreen(data: AppData, vm: AppViewModel) {
    var name by remember(data.branding) { mutableStateOf(data.branding.appName) }
    var tagline by remember(data.branding) { mutableStateOf(data.branding.tagline) }
    var color by remember(data.branding) { mutableStateOf(data.branding.primary) }
    var theme by remember(data.branding) { mutableStateOf(data.branding.theme) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Branding", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            name,
            { name = it },
            label = { Text("App Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            tagline,
            { tagline = it },
            label = { Text("Tagline") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            color,
            { color = it },
            label = { Text("Primary Color HEX") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))

        Text("Theme")
        Row {
            FilterChip(
                selected = theme == "light",
                onClick = { theme = "light" },
                label = { Text("Light") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = theme == "dark",
                onClick = { theme = "dark" },
                label = { Text("Dark") }
            )
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick = {
                vm.saveBranding(
                    Branding(name, tagline, color, theme, data.branding.footer)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Branding")
        }
    }
}

@Composable
fun SecurityScreen(data: AppData, vm: AppViewModel) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(vm.exportBytes())
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.use {
                vm.importBytes(it.readBytes())
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Security & Backup", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))

        InfoCard(
            "Local storage",
            "App data is stored on this device. Phase 4 will add server/cloud sync."
        )

        Button(
            onClick = { vm.snapshot() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Local Snapshot")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                exportLauncher.launch("RIVAN_CSC_Backup.bin")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export Backup File")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                importLauncher.launch(
                    arrayOf(
                        "application/octet-stream",
                        "application/*",
                        "*/*"
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Import Backup File")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(onClick = { vm.resetDemo() }) {
            Text("Reset Demo Data", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Text("Recent audit logs", fontWeight = FontWeight.Bold)

        LazyColumn {
            items(data.auditLogs.take(12)) { log ->
                Text(
                    "${log.time} • ${log.action} • ${log.detail}",
                    fontSize = 11.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun InfoCard(title: String, body: String) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(15.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                body,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileScreen(data: AppData) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Profile", fontSize = 23.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        InfoCard(
            data.auth.userName,
            "${data.auth.userId} • ${data.auth.role.uppercase()}"
        )
        Spacer(Modifier.height(10.dp))
        InfoCard("CSC Access", data.auth.cscId)
        Spacer(Modifier.height(10.dp))
        InfoCard(
            "App",
            "${data.branding.appName}\n${data.branding.tagline}"
        )
    }
}

@Composable
fun ListScreen(
    title: String,
    count: Int,
    onAdd: () -> Unit,
    addText: String,
    content: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 23.sp, fontWeight = FontWeight.Black)
                Text(
                    "$count records",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (addText.isNotBlank()) {
                Button(onClick = onAdd) {
                    Text(addText)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        content()
    }
}

// -------------------- FORMS --------------------

@Composable
fun FormSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black)
            content()
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
fun CscForm(done: (Csc) -> Unit) {
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Rohtak") }
    var mobile by remember { mutableStateOf("") }
    var manager by remember { mutableStateOf("") }

    FormSheet("Add CSC", {}) {
        OutlinedTextField(name, { name = it }, label = { Text("CSC Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(village, { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(district, { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(manager, { manager = it }, label = { Text("Manager") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    done(
                        Csc(
                            "CSC-${System.currentTimeMillis() % 100000}",
                            name,
                            village,
                            district,
                            mobile = mobile,
                            manager = manager
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save CSC")
        }
    }
}

@Composable
fun OperatorForm(cscs: List<Csc>, done: (Operator) -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var csc by remember { mutableStateOf(cscs.firstOrNull()?.id ?: "CSC-001") }
    var password by remember { mutableStateOf("1234") }
    var salary by remember { mutableStateOf("10000") }

    FormSheet("Add Operator", {}) {
        OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(csc, { csc = it }, label = { Text("CSC ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(password, { password = it }, label = { Text("Initial Password") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(salary, { salary = it }, label = { Text("Basic Salary") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    done(
                        Operator(
                            "OP-${System.currentTimeMillis() % 100000}",
                            name,
                            mobile,
                            email,
                            csc,
                            password,
                            basicSalary = salary.toDoubleOrNull() ?: 0.0
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Operator")
        }
    }
}

@Composable
fun CustomerForm(cscs: List<Csc>, done: (Customer) -> Unit) {
    var name by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("Rohtak") }
    var csc by remember { mutableStateOf(cscs.firstOrNull()?.id ?: "CSC-001") }

    FormSheet("Add Customer", {}) {
        OutlinedTextField(name, { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mobile, { mobile = it }, label = { Text("Mobile") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(village, { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(district, { district = it }, label = { Text("District") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(csc, { csc = it }, label = { Text("CSC ID") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (name.isNotBlank() && mobile.isNotBlank()) {
                    done(
                        Customer(
                            "CUST-${System.currentTimeMillis() % 100000}",
                            name,
                            mobile,
                            village = village,
                            district = district,
                            cscId = csc,
                            createdAt = today()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Customer")
        }
    }
}

@Composable
fun ServiceForm(done: (ServiceItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var price by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }

    FormSheet("Add Service", {}) {
        OutlinedTextField(name, { name = it }, label = { Text("Service Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(price, { price = it }, label = { Text("Customer Price") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(description, { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (name.isNotBlank()) {
                    done(
                        ServiceItem(
                            "SRV-${System.currentTimeMillis() % 10000}",
                            name,
                            category,
                            price.toDoubleOrNull() ?: 0.0,
                            description
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Service")
        }
    }
}

@Composable
fun ApplicationForm(data: AppData, done: (Application) -> Unit) {
    var customer by remember { mutableStateOf(data.customers.firstOrNull()?.id ?: "") }
    var service by remember { mutableStateOf(data.services.firstOrNull()?.id ?: "") }
    var mode by remember { mutableStateOf("Cash") }
    var reference by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    FormSheet("New Application", {}) {
        OutlinedTextField(customer, { customer = it }, label = { Text("Customer ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(service, { service = it }, label = { Text("Service ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mode, { mode = it }, label = { Text("Payment Mode: Cash / UPI / Bank Transfer") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(reference, { reference = it }, label = { Text("Payment Reference") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(note, { note = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val c = data.customers.firstOrNull { it.id == customer }
                val s = data.services.firstOrNull { it.id == service }

                if (c != null && s != null) {
                    done(
                        Application(
                            "RIVAN-${SimpleDateFormat("yyyy", Locale.US).format(Date())}-${System.currentTimeMillis() % 100000}",
                            c.id,
                            c.name,
                            c.mobile,
                            s.id,
                            s.name,
                            s.price,
                            mode,
                            reference,
                            "Verified",
                            "Pending",
                            c.cscId,
                            data.auth.userId,
                            data.auth.userName,
                            today(),
                            note,
                            mutableListOf("Pending • ${data.auth.userName}")
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Application")
        }
    }
}

@Composable
fun IncomeForm(cscs: List<Csc>, done: (Income) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other Income") }
    var mode by remember { mutableStateOf("Cash") }
    var csc by remember { mutableStateOf(cscs.firstOrNull()?.id ?: "ALL") }

    FormSheet("Add Income", {}) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(mode, { mode = it }, label = { Text("Payment Mode") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(csc, { csc = it }, label = { Text("CSC ID") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    done(
                        Income(
                            "INC-${System.currentTimeMillis()}",
                            title,
                            category,
                            amount.toDoubleOrNull() ?: 0.0,
                            mode,
                            "",
                            csc,
                            "Manual",
                            today(),
                            false
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Income")
        }
    }
}

@Composable
fun ExpenseForm(cscs: List<Csc>, done: (Expense) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var csc by remember { mutableStateOf(cscs.firstOrNull()?.id ?: "ALL") }

    FormSheet("Add Expense", {}) {
        OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(category, { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(csc, { csc = it }, label = { Text("CSC ID") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    done(
                        Expense(
                            "EXP-${System.currentTimeMillis()}",
                            title,
                            category,
                            amount.toDoubleOrNull() ?: 0.0,
                            "Cash",
                            "",
                            csc,
                            "Manual",
                            today()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Expense")
        }
    }
}

@Composable
fun LeaveForm(op: Operator?, done: (LeaveRequest) -> Unit) {
    var type by remember { mutableStateOf("Casual Leave") }
    var from by remember { mutableStateOf(today()) }
    var to by remember { mutableStateOf(today()) }
    var reason by remember { mutableStateOf("") }

    FormSheet("Request Leave", {}) {
        OutlinedTextField(type, { type = it }, label = { Text("Leave Type") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(from, { from = it }, label = { Text("From YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(to, { to = it }, label = { Text("To YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(reason, { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                if (op != null) {
                    done(
                        LeaveRequest(
                            "LV-${System.currentTimeMillis()}",
                            op.id,
                            op.name,
                            type,
                            from,
                            to,
                            reason,
                            true
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Leave")
        }
    }
}

@Composable
fun PayrollForm(ops: List<Operator>, done: (Payroll) -> Unit) {
    var operatorId by remember { mutableStateOf(ops.firstOrNull()?.id ?: "") }
    var month by remember { mutableStateOf("2026-11") }
    var basic by remember { mutableStateOf("10000") }
    var bonus by remember { mutableStateOf("0") }
    var deduction by remember { mutableStateOf("0") }
    var advance by remember { mutableStateOf("0") }

    FormSheet("Create Payroll", {}) {
        OutlinedTextField(operatorId, { operatorId = it }, label = { Text("Operator ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(month, { month = it }, label = { Text("Month YYYY-MM") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(basic, { basic = it }, label = { Text("Basic") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(bonus, { bonus = it }, label = { Text("Bonus") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(deduction, { deduction = it }, label = { Text("Deduction") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(advance, { advance = it }, label = { Text("Advance") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val op = ops.firstOrNull { it.id == operatorId }

                if (op != null) {
                    val b = basic.toDoubleOrNull() ?: 0.0
                    val bo = bonus.toDoubleOrNull() ?: 0.0
                    val de = deduction.toDoubleOrNull() ?: 0.0
                    val ad = advance.toDoubleOrNull() ?: 0.0

                    done(
                        Payroll(
                            "PAY-${System.currentTimeMillis()}",
                            op.id,
                            op.name,
                            month,
                            b,
                            bo,
                            de,
                            ad,
                            b + bo - de - ad
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Payroll")
        }
    }
}

@Composable
fun DocumentForm(customers: List<Customer>, done: (DocumentRecord) -> Unit) {
    var customerId by remember { mutableStateOf(customers.firstOrNull()?.id ?: "") }
    var service by remember { mutableStateOf("General") }
    var type by remember { mutableStateOf("Identity Document") }
    var expiry by remember { mutableStateOf("") }

    FormSheet("Add Document", {}) {
        OutlinedTextField(customerId, { customerId = it }, label = { Text("Customer ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(service, { service = it }, label = { Text("Service") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(type, { type = it }, label = { Text("Document Type") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(expiry, { expiry = it }, label = { Text("Expiry YYYY-MM-DD") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val customer = customers.firstOrNull { it.id == customerId }

                if (customer != null) {
                    done(
                        DocumentRecord(
                            "DOC-${System.currentTimeMillis()}",
                            customer.id,
                            customer.name,
                            service,
                            type,
                            expiry = expiry,
                            uploadedBy = "Current User",
                            date = today()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Document")
        }
    }
}

// -------------------- HELPERS --------------------

fun today(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

fun greet(): String =
    when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }

fun money(value: Double): String =
    String.format(Locale.US, "%.0f", value)

fun buildCsv(data: AppData): String {
    val builder = StringBuilder()
    builder.append("Application ID,Customer,Service,Amount,Payment,Status,Date\n")

    data.applications.forEach {
        builder.append(
            "${it.id},${it.customerName},${it.serviceName},${it.amount},${it.paymentMode},${it.status},${it.date}\n"
        )
    }

    return builder.toString()
}
