package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldSecondary

@Composable
fun AdminScreen(
    viewModel: MainViewModel
) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val faqs by viewModel.faqs.collectAsState()
    val services by viewModel.serviceMenu.collectAsState()
    val messages by viewModel.chatMessages.collectAsState()

    var passwordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var selectedAdminTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: FAQs, 2: Services, 3: Settings/Dir 755

    if (!isAdminLoggedIn) {
        // Admin Login Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_login_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = EmeraldPrimary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = GoldSecondary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "CRRSA Admin Portal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Authentication required for service management",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Admin Password") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_password_input"),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Default password: admin123",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (viewModel.loginAdmin(passwordInput)) {
                                passwordInput = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log In as Admin")
                    }
                }
            }
        }
    } else {
        // Authenticated Admin Dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            // Admin Top Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CRRSA Admin Control Center",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Logged in as System Administrator",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldPrimary
                    )
                }

                IconButton(
                    onClick = { viewModel.logoutAdmin() },
                    modifier = Modifier.testTag("admin_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Log out",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Admin Tabs
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedAdminTab == 0,
                    onClick = { selectedAdminTab = 0 },
                    text = { Text("Analytics", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 1,
                    onClick = { selectedAdminTab = 1 },
                    text = { Text("FAQs (${faqs.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 2,
                    onClick = { selectedAdminTab = 2 },
                    text = { Text("Services (${services.size})", fontSize = 11.sp) }
                )
                Tab(
                    selected = selectedAdminTab == 3,
                    onClick = { selectedAdminTab = 3 },
                    text = { Text("Storage (755)", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedAdminTab) {
                0 -> AdminAnalyticsTab(messagesCount = messages.size, faqsCount = faqs.size, servicesCount = services.size, viewModel = viewModel)
                1 -> AdminFaqManagerTab(faqs = faqs, viewModel = viewModel)
                2 -> AdminServiceManagerTab(services = services, viewModel = viewModel)
                3 -> AdminStorageTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AdminAnalyticsTab(
    messagesCount: Int,
    faqsCount: Int,
    servicesCount: Int,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Inquiries",
                value = "$messagesCount",
                icon = Icons.Default.Chat,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Active FAQs",
                value = "$faqsCount",
                icon = Icons.Default.HelpCenter,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Services Menu",
                value = "$servicesCount",
                icon = Icons.Default.MiscellaneousServices,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "System Mode",
                value = "Active (755)",
                icon = Icons.Default.FolderZip,
                modifier = Modifier.weight(1f)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Uploads Directory Security Inspection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = viewModel.getUploadDirPermissionStatus(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = EmeraldPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminFaqManagerTab(
    faqs: List<com.example.data.local.FaqItem>,
    viewModel: MainViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_faq_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New FAQ Entry")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(faqs, key = { it.id }) { faq ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "[${faq.category}] ${faq.questionEn}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = faq.questionAm, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deleteFaq(faq.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete FAQ", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var category by remember { mutableStateOf("Residency") }
        var qEn by remember { mutableStateOf("") }
        var qAm by remember { mutableStateOf("") }
        var aEn by remember { mutableStateOf("") }
        var aAm by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add FAQ Entry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") })
                    OutlinedTextField(value = qEn, onValueChange = { qEn = it }, label = { Text("Question (English)") })
                    OutlinedTextField(value = qAm, onValueChange = { qAm = it }, label = { Text("Question (Amharic)") })
                    OutlinedTextField(value = aEn, onValueChange = { aEn = it }, label = { Text("Answer (English)") })
                    OutlinedTextField(value = aAm, onValueChange = { aAm = it }, label = { Text("Answer (Amharic)") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (qEn.isNotBlank() && aEn.isNotBlank()) {
                        viewModel.addFaq(category, qEn, qAm, aEn, aAm)
                        showAddDialog = false
                    }
                }) {
                    Text("Save FAQ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminServiceManagerTab(
    services: List<com.example.data.local.ServiceMenuItem>,
    viewModel: MainViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("add_service_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Service Item")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(services, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.titleEn, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Fee: ${item.feeEtb} ETB • ${item.processingTimeDays} day(s)", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary)
                        }
                        IconButton(onClick = { viewModel.deleteService(item.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Service", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var titleEn by remember { mutableStateOf("") }
        var titleAm by remember { mutableStateOf("") }
        var descEn by remember { mutableStateOf("") }
        var descAm by remember { mutableStateOf("") }
        var docsEn by remember { mutableStateOf("") }
        var docsAm by remember { mutableStateOf("") }
        var feeStr by remember { mutableStateOf("100") }
        var daysStr by remember { mutableStateOf("2") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = titleEn, onValueChange = { titleEn = it }, label = { Text("Title (English)") })
                    OutlinedTextField(value = titleAm, onValueChange = { titleAm = it }, label = { Text("Title (Amharic)") })
                    OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") })
                    OutlinedTextField(value = docsEn, onValueChange = { docsEn = it }, label = { Text("Required Documents (EN)") })
                    OutlinedTextField(value = feeStr, onValueChange = { feeStr = it }, label = { Text("Fee (ETB)") })
                    OutlinedTextField(value = daysStr, onValueChange = { daysStr = it }, label = { Text("Processing Days") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (titleEn.isNotBlank()) {
                        val fee = feeStr.toDoubleOrNull() ?: 100.0
                        val days = daysStr.toIntOrNull() ?: 2
                        viewModel.addService(titleEn, titleAm, descEn, descAm, docsEn, docsAm, fee, days)
                        showAddDialog = false
                    }
                }) {
                    Text("Create Service")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminStorageTab(
    viewModel: MainViewModel
) {
    val status = viewModel.getUploadDirPermissionStatus()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = EmeraldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uploads Directory 755 Mode Verification",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "As requested, the directory permissions for uploaded document files are set to 755 (rwxr-xr-x). Owner has read/write/execute rights, while group and public users have read/execute rights.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
