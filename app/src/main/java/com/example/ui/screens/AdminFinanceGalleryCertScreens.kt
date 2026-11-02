package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.EventRegistration
import com.example.data.model.FinanceTransaction
import com.example.data.model.GalleryItem
import com.example.data.model.IdCardDesignTheme
import com.example.data.model.MembershipApplication
import com.example.data.model.PaymentAccount
import com.example.data.model.SocietyCertificate
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentPayment
import com.example.data.model.StudentProfile
import com.example.data.model.hasPermission
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.OfficialCertificateCard
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import com.example.util.ImageEncodingUtils
import com.example.util.PdfExportManager

@Composable
fun AdminFinanceModuleScreen(
  admin: AdminAccount,
  events: List<SocietyEvent>,
  transactions: List<FinanceTransaction>,
  payments: List<StudentPayment>,
  registrations: List<EventRegistration>,
  paymentAccounts: List<PaymentAccount>,
  onRecordTransaction: (String, String, String, Double, String, String, String) -> Unit,
  onDeleteTransaction: (FinanceTransaction) -> Unit,
  onSavePaymentAccount: (PaymentAccount?, String, String, String, String, String, String, Boolean) -> Unit,
  onDeletePaymentAccount: (PaymentAccount) -> Unit,
  onVerifyPayment: (StudentPayment, Boolean) -> Unit,
  onDeletePayment: (StudentPayment) -> Unit,
  onDeleteRegistration: (EventRegistration, SocietyEvent?) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val canManageFinance = admin.hasPermission("FINANCE_MANAGEMENT")
  if (!canManageFinance) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
      EmptyStateCard(
        title = "Finance Access Restricted",
        subtitle = "Zero-Trust Security Policy: Financial ledgers, society balances, account details, and payment verifications are strictly restricted on the backend to Finance Secretaries, President, Convener, and Portal Administrators.",
        icon = Icons.Filled.Lock,
      )
    }
    return
  }

  var tabMode by remember { mutableStateOf("LEDGER") }
  var showAddTxDialog by remember { mutableStateOf(false) }
  var editingAccount by remember { mutableStateOf<PaymentAccount?>(null) }
  var showAccountDialog by remember { mutableStateOf(false) }
  var txToDelete by remember { mutableStateOf<FinanceTransaction?>(null) }
  var accountToDelete by remember { mutableStateOf<PaymentAccount?>(null) }
  var paymentToDelete by remember { mutableStateOf<StudentPayment?>(null) }
  var regToDelete by remember { mutableStateOf<EventRegistration?>(null) }
  var zoomReceipt by remember { mutableStateOf<Pair<String, String>?>(null) }

  if (zoomReceipt != null) {
    val (title, src) = zoomReceipt!!
    FullScreenImagePreviewDialog(
      title = title,
      imageSource = src,
      onDismiss = { zoomReceipt = null },
    )
  }

  if (txToDelete != null) {
    val target = txToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.title} (PKR ${target.amount.toInt()})",
      itemSubtitle = "Permanently delete this ${target.type.lowercase()} entry from the Finance Ledger?",
      onConfirmDelete = { onDeleteTransaction(target) },
      onDismiss = { txToDelete = null },
    )
  }

  if (accountToDelete != null) {
    val target = accountToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.bankOrWalletName} — ${target.accountTitle}",
      itemSubtitle = "Remove this official society payment account (${target.accountNumber})?",
      onConfirmDelete = { onDeletePaymentAccount(target) },
      onDismiss = { accountToDelete = null },
    )
  }

  if (paymentToDelete != null) {
    val target = paymentToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.studentName} — ${target.purposeTitle}",
      itemSubtitle = "Delete this student fee receipt record (PKR ${target.amount.toInt()})?",
      onConfirmDelete = { onDeletePayment(target) },
      onDismiss = { paymentToDelete = null },
    )
  }

  if (regToDelete != null) {
    val target = regToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.studentName} — ${target.eventTitle}",
      itemSubtitle = "Delete this participant registration record?",
      onConfirmDelete = {
        val parentEvent = events.firstOrNull { it.id == target.eventId }
        onDeleteRegistration(target, parentEvent)
      },
      onDismiss = { regToDelete = null },
    )
  }

  val totalIncome = remember(transactions) { transactions.filter { it.type == "INCOME" }.sumOf { it.amount } }
  val totalExpense = remember(transactions) { transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount } }
  val netBalance = totalIncome - totalExpense

  val exportPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    if (uri != null) {
      val rows = transactions.map {
        listOf(it.transactionDate, it.type, it.category, it.title, "PKR ${it.amount.toInt()}")
      }
      PdfExportManager.writeSummaryReportPdfToUri(
        context = context,
        uri = uri,
        reportTitle = "Financial Income & Expense Statement",
        subtitle = "Income: PKR ${totalIncome.toInt()} | Expense: PKR ${totalExpense.toInt()} | Net: PKR ${netBalance.toInt()}",
        headers = listOf("Date", "Type", "Category", "Description", "Amount"),
        rows = rows,
      ).onSuccess { onMessage("Financial Statement PDF exported!") }
        .onFailure { onMessage("PDF export failed: ${it.localizedMessage}") }
    }
  }

  val exportCsvLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/csv")
  ) { uri: Uri? ->
    if (uri != null) {
      val rows = transactions.map {
        listOf(it.transactionDate, it.type, it.category, it.title, it.amount.toString(), it.referenceNumber, it.recordedByName)
      }
      PdfExportManager.writeCsvReportToUri(
        context = context,
        uri = uri,
        headers = listOf("Date", "Type", "Category", "Title", "Amount_PKR", "Reference", "Recorded_By"),
        rows = rows,
      ).onSuccess { onMessage("Financial Ledger CSV exported!") }
        .onFailure { onMessage("CSV export failed: ${it.localizedMessage}") }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
          StatMetricCard(
            title = "Total Income",
            value = "PKR ${totalIncome.toInt()}",
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            accentColor = Color(0xFF0E5A34),
            modifier = Modifier.weight(1f),
          )
          StatMetricCard(
            title = "Total Expenses",
            value = "PKR ${totalExpense.toInt()}",
            icon = Icons.AutoMirrored.Filled.TrendingDown,
            accentColor = Color(0xFFBA1A1A),
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(Modifier.height(10.dp))
        StatMetricCard(
          title = "Available Society Balance",
          value = "PKR ${netBalance.toInt()}",
          icon = Icons.Filled.AccountBalanceWallet,
          accentColor = MaterialTheme.colorScheme.primary,
          modifier = Modifier.fillMaxWidth(),
        )
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          Button(
            onClick = { tabMode = "ACCOUNTS" },
            modifier = Modifier.weight(1.2f).testTag("finance_accounts_tab_btn"),
          ) {
            Icon(Icons.Filled.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Account Details (${paymentAccounts.size})")
          }
          OutlinedButton(
            onClick = { exportPdfLauncher.launch("YES_Financial_Statement.pdf") },
            modifier = Modifier.weight(0.9f),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("PDF")
          }
          OutlinedButton(
            onClick = { exportCsvLauncher.launch("YES_Financial_Ledger.csv") },
            modifier = Modifier.weight(0.9f),
          ) {
            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("CSV")
          }
        }
      }

      item {
        YesDropdownField(
          label = "Select Finance Section",
          options = listOf("LEDGER", "ACCOUNTS", "RECEIPTS", "PAID_UNPAID"),
          selectedOption = tabMode,
          onOptionSelected = { tabMode = it },
          optionLabel = { opt ->
            when (opt) {
              "LEDGER" -> "Income & Expense Ledger (${transactions.size})"
              "ACCOUNTS" -> "Official Account Details by Administrator (${paymentAccounts.size})"
              "RECEIPTS" -> "Student Fee Receipts (${payments.size})"
              "PAID_UNPAID" -> "Event Paid vs Unpaid (${registrations.size})"
              else -> opt
            }
          },
          testTag = "finance_section_dropdown",
        )
      }

      when (tabMode) {
        "ACCOUNTS" -> {
          item {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text("Official Society Payment Accounts", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text(
                    "Active accounts added here are displayed automatically to students when paying event or membership fees.",
                    style = MaterialTheme.typography.bodySmall,
                  )
                }
                Spacer(Modifier.width(8.dp))
                Button(
                  onClick = {
                    editingAccount = null
                    showAccountDialog = true
                  },
                  modifier = Modifier.testTag("add_payment_account_btn"),
                ) {
                  Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Add Account")
                }
              }
            }
          }

          if (paymentAccounts.isEmpty()) {
            item {
              EmptyStateCard(
                title = "No Account Details Configured",
                subtitle = "Tap + or 'Add Account' to publish Bank Account, JazzCash, EasyPaisa, or SadaPay details for students.",
                icon = Icons.Filled.AccountBalance,
              )
            }
          } else {
            items(paymentAccounts, key = { it.id }) { acc ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      StatusPill(label = acc.accountType)
                      StatusPill(label = if (acc.active) "ACTIVE" else "INACTIVE")
                    }
                    Row {
                      IconButton(onClick = {
                        editingAccount = acc
                        showAccountDialog = true
                      }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Account")
                      }
                      IconButton(
                        onClick = { accountToDelete = acc },
                        modifier = Modifier.testTag("delete_account_${acc.id}"),
                      ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Account", tint = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                  Text(acc.bankOrWalletName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text("Account Title: ${acc.accountTitle}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                  Text("Account / Wallet No: ${acc.accountNumber}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                  if (acc.ibanOrBranch.isNotBlank()) {
                    Text("IBAN / Branch: ${acc.ibanOrBranch}", style = MaterialTheme.typography.bodySmall)
                  }
                  if (acc.instructions.isNotBlank()) {
                    Text("Instructions: ${acc.instructions}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }
        }

        "LEDGER" -> {
          if (transactions.isEmpty()) {
            item {
              EmptyStateCard(title = "No Financial Entries", subtitle = "Tap + to record registration collections, sponsorships, or society expenses.")
            }
          } else {
            items(transactions, key = { it.id }) { tx ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                      StatusPill(label = tx.type)
                      StatusPill(label = tx.category)
                    }
                    Text(tx.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${tx.transactionDate} • Ref: ${tx.referenceNumber.ifBlank { "N/A" }} • By ${tx.recordedByName}", style = MaterialTheme.typography.bodySmall)
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      text = (if (tx.type == "INCOME") "+ " else "- ") + "PKR ${tx.amount.toInt()}",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = if (tx.type == "INCOME") Color(0xFF0E5A34) else MaterialTheme.colorScheme.error,
                    )
                    IconButton(
                      onClick = { txToDelete = tx },
                      modifier = Modifier.testTag("delete_tx_${tx.id}"),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete Entry", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }

        "RECEIPTS" -> {
          if (payments.isEmpty()) {
            item {
              EmptyStateCard(title = "No Student Fee Records", subtitle = "Student fee submissions and payment receipts appear here.")
            }
          } else {
            items(payments, key = { it.id }) { pay ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
              ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text("${pay.studentName} (${pay.registrationNumber})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                      Text("${pay.purposeTitle} • PKR ${pay.amount.toInt()}", style = MaterialTheme.typography.bodySmall)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      StatusPill(label = pay.status)
                      IconButton(
                        onClick = { paymentToDelete = pay },
                        modifier = Modifier.testTag("delete_payment_${pay.id}"),
                      ) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Payment Record", tint = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                  if (pay.transactionRef.isNotBlank()) {
                    Text("Ref: ${pay.transactionRef} • Date: ${pay.paymentDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                  }
                  if (pay.receiptUri.isNotBlank()) {
                    Surface(
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { zoomReceipt = "${pay.studentName} — Fee Receipt" to pay.receiptUri },
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                    ) {
                      SocietyImage(
                        imageSource = pay.receiptUri,
                        contentDescription = "Receipt of ${pay.studentName}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                      )
                    }
                  }
                  if (pay.status != "PAID" && pay.status != "VERIFIED") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Button(onClick = { onVerifyPayment(pay, true) }) { Text("Verify & Post Income") }
                      OutlinedButton(onClick = { onVerifyPayment(pay, false) }) { Text("Reject") }
                    }
                  }
                }
              }
            }
          }
        }

        else -> {
          if (registrations.isEmpty()) {
            item {
              EmptyStateCard(title = "No Event Registration Fee Records", subtitle = "Participant payment statuses appear here.")
            }
          } else {
            items(registrations, key = { it.id }) { reg ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(14.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text("${reg.studentName} (${reg.registrationNumber})", fontWeight = FontWeight.Bold)
                    Text("${reg.eventTitle} • Fee: PKR ${reg.feeAmount.toInt()}", style = MaterialTheme.typography.bodySmall)
                  }
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusPill(label = reg.paymentStatus)
                    IconButton(onClick = { regToDelete = reg }) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete Registration", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (tabMode == "LEDGER") {
      FloatingActionButton(
        onClick = { showAddTxDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("add_finance_tx_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Record Income or Expense")
      }
    } else if (tabMode == "ACCOUNTS") {
      FloatingActionButton(
        onClick = {
          editingAccount = null
          showAccountDialog = true
        },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("add_payment_account_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Official Payment Account")
      }
    }
  }

  if (showAccountDialog) {
    val existing = editingAccount
    var accountType by remember(existing) { mutableStateOf(existing?.accountType ?: SocietyConstants.PAYMENT_ACCOUNT_TYPES.first()) }
    var bankName by remember(existing) { mutableStateOf(existing?.bankOrWalletName ?: "") }
    var accountTitle by remember(existing) { mutableStateOf(existing?.accountTitle ?: "") }
    var accountNumber by remember(existing) { mutableStateOf(existing?.accountNumber ?: "") }
    var ibanOrBranch by remember(existing) { mutableStateOf(existing?.ibanOrBranch ?: "") }
    var instructions by remember(existing) { mutableStateOf(existing?.instructions ?: "Upload screenshot of transfer receipt with transaction ID.") }
    var active by remember(existing) { mutableStateOf(existing?.active ?: true) }

    AlertDialog(
      onDismissRequest = { showAccountDialog = false },
      title = { Text(if (existing == null) "Add Official Account Details" else "Edit Account Details") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          YesDropdownField(
            label = "Account / Wallet Type",
            options = SocietyConstants.PAYMENT_ACCOUNT_TYPES,
            selectedOption = accountType,
            onOptionSelected = { accountType = it },
          )
          OutlinedTextField(
            value = bankName,
            onValueChange = { bankName = it },
            label = { Text("Bank or Wallet Name * (e.g. HBL UAF Branch / JazzCash)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = accountTitle,
            onValueChange = { accountTitle = it },
            label = { Text("Account Title *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = accountNumber,
            onValueChange = { accountNumber = it },
            label = { Text("Account / Mobile Wallet Number *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = ibanOrBranch,
            onValueChange = { ibanOrBranch = it },
            label = { Text("IBAN / Branch Code (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Payment Instructions for Students") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Visible to Students")
            Switch(checked = active, onCheckedChange = { active = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSavePaymentAccount(
              existing,
              accountType,
              bankName,
              accountTitle,
              accountNumber,
              ibanOrBranch,
              instructions,
              active,
            )
            showAccountDialog = false
          },
          enabled = bankName.trim().length >= 2 && accountTitle.trim().length >= 2 && accountNumber.trim().length >= 3,
        ) {
          Text("Save Account")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAccountDialog = false }) { Text("Cancel") }
      },
    )
  }

  if (showAddTxDialog) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("INCOME") }
    var category by remember { mutableStateOf(SocietyConstants.FINANCE_INCOME_CATEGORIES.first()) }
    var amountStr by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-11-12") }
    var refNo by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val categoriesForType = if (type == "INCOME") {
      SocietyConstants.FINANCE_INCOME_CATEGORIES
    } else {
      SocietyConstants.FINANCE_EXPENSE_CATEGORIES
    }

    AlertDialog(
      onDismissRequest = { showAddTxDialog = false },
      title = { Text("Record Financial Transaction") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          YesDropdownField(
            label = "Transaction Type",
            options = listOf("INCOME", "EXPENSE"),
            selectedOption = type,
            onOptionSelected = {
              type = it
              category = if (it == "INCOME") SocietyConstants.FINANCE_INCOME_CATEGORIES.first()
              else SocietyConstants.FINANCE_EXPENSE_CATEGORIES.first()
            },
          )
          YesDropdownField(
            label = "Ledger Category",
            options = categoriesForType,
            selectedOption = category,
            onOptionSelected = { category = it },
            optionLabel = { it.replace("_", " ") },
          )
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Description / Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Amount (PKR) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD) *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = refNo, onValueChange = { refNo = it }, label = { Text("Voucher / Bank Ref") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        val amt = amountStr.toDoubleOrNull() ?: 0.0
        Button(
          onClick = {
            onRecordTransaction(title, type, category, amt, date, refNo, notes)
            showAddTxDialog = false
          },
          enabled = title.trim().length >= 2 && amt > 0.0,
        ) {
          Text("Save Entry")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddTxDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
fun AdminGalleryModuleScreen(
  canManageGallery: Boolean,
  itemsList: List<GalleryItem>,
  onSaveItem: (GalleryItem?, String, String, String, String, String, String, Boolean) -> Unit,
  onDeleteItem: (GalleryItem) -> Unit,
) {
  val context = LocalContext.current
  var albumFilter by remember { mutableStateOf("ALL") }
  var editingItem by remember { mutableStateOf<GalleryItem?>(null) }
  var showDialog by remember { mutableStateOf(false) }
  var itemToDelete by remember { mutableStateOf<GalleryItem?>(null) }
  var zoomGalleryItem by remember { mutableStateOf<GalleryItem?>(null) }

  val filteredItems = remember(itemsList, albumFilter) {
    if (albumFilter == "ALL") itemsList
    else itemsList.filter { it.albumCategory == albumFilter }
  }

  if (zoomGalleryItem != null) {
    val g = zoomGalleryItem!!
    FullScreenImagePreviewDialog(
      title = g.title,
      imageSource = g.imageUri,
      onDismiss = { zoomGalleryItem = null },
    )
  }

  if (itemToDelete != null) {
    val target = itemToDelete!!
    ConfirmDeleteDialog(
      itemTitle = target.title,
      itemSubtitle = "Permanently delete this photograph from the Society Gallery?",
      onConfirmDelete = { onDeleteItem(target) },
      onDismiss = { itemToDelete = null },
    )
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        YesDropdownField(
          label = "Filter Gallery Album",
          options = listOf("ALL") + SocietyConstants.GALLERY_ALBUMS,
          selectedOption = albumFilter,
          onOptionSelected = { albumFilter = it },
          optionLabel = { if (it == "ALL") "All Albums (${itemsList.size})" else it },
          testTag = "admin_gallery_album_dropdown",
        )
      }

      if (filteredItems.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Gallery Highlights Yet",
            subtitle = "Upload photographs and captions from Annual Dinner, Sports Gala, Study Tours, and Workshops.",
            icon = Icons.Filled.PhotoLibrary,
          )
        }
      } else {
        items(filteredItems, key = { it.id }) { item ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              if (item.imageUri.isNotBlank()) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { zoomGalleryItem = item },
                ) {
                  SocietyImage(
                    imageSource = item.imageUri,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                  )
                }
              }
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                StatusPill(label = item.albumCategory)
                StatusPill(label = if (item.approved) "APPROVED" else "PENDING")
              }
              Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(item.caption, style = MaterialTheme.typography.bodySmall)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(item.eventDate, style = MaterialTheme.typography.labelSmall)
                if (canManageGallery) {
                  Row {
                    IconButton(onClick = { editingItem = item; showDialog = true }) {
                      Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                      onClick = { itemToDelete = item },
                      modifier = Modifier.testTag("delete_gallery_${item.id}"),
                    ) {
                      Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    if (canManageGallery) {
      FloatingActionButton(
        onClick = { editingItem = null; showDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("add_gallery_item_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Add Gallery Photo")
      }
    }
  }

  if (showDialog) {
    val g = editingItem
    var title by remember(g) { mutableStateOf(g?.title ?: "") }
    var album by remember(g) { mutableStateOf(g?.albumCategory ?: SocietyConstants.GALLERY_ALBUMS.first()) }
    var caption by remember(g) { mutableStateOf(g?.caption ?: "") }
    var eventDate by remember(g) { mutableStateOf(g?.eventDate ?: "2026-10-30") }
    var imageUri by remember(g) { mutableStateOf(g?.imageUri ?: "") }
    var approved by remember(g) { mutableStateOf(g?.approved ?: true) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
      if (uri != null) {
        imageUri = ImageEncodingUtils.encodeUriToDataUrl(context, uri, maxDimension = 720, quality = 70) ?: uri.toString()
      }
    }

    AlertDialog(
      onDismissRequest = { showDialog = false },
      title = { Text(if (g == null) "Upload Event Photograph" else "Edit Gallery Item") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Photo Title *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          YesDropdownField(
            label = "Gallery Album",
            options = SocietyConstants.GALLERY_ALBUMS,
            selectedOption = album,
            onOptionSelected = { album = it },
          )
          OutlinedTextField(value = caption, onValueChange = { caption = it }, label = { Text("Event Description / Caption *") }, minLines = 2, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = eventDate, onValueChange = { eventDate = it }, label = { Text("Event Date") }, singleLine = true, modifier = Modifier.fillMaxWidth())
          OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (imageUri.isNotBlank()) "Photograph Attached ✓" else "Choose Photograph")
          }
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Approve for Public Student Gallery")
            Switch(checked = approved, onCheckedChange = { approved = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onSaveItem(g, title, album, caption.ifBlank { title }, eventDate, imageUri, "EMERALD", approved)
            showDialog = false
          },
          enabled = title.trim().length >= 2,
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
fun AdminCertificatesModuleScreen(
  admin: AdminAccount,
  certificates: List<SocietyCertificate>,
  students: List<StudentProfile>,
  events: List<SocietyEvent>,
  onIssueCertificate: (String, String, String, String, String, String, String, String) -> Unit,
  onApproveCertificate: (SocietyCertificate) -> Unit,
  onRejectCertificate: (SocietyCertificate, String) -> Unit,
  onDeleteCertificate: (SocietyCertificate) -> Unit,
  onOpenVerifyDialog: () -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current
  val canManageCertificates = admin.hasPermission("CERTIFICATE_GENERATION") || admin.hasPermission("ADMINISTRATOR_ACCESS")
  val canApproveAny = admin.hasPermission("CERTIFICATE_APPROVAL") || admin.hasPermission("ADMINISTRATOR_ACCESS")
  val isSeniorCabinet = admin.adminRole in setOf("PRESIDENT", "SENIOR_VICE_PRESIDENT", "VICE_PRESIDENT", "SUPER_ADMIN")

  var typeFilter by remember { mutableStateOf("ALL") }
  var showIssueDialog by remember { mutableStateOf(false) }
  var certToExport by remember { mutableStateOf<SocietyCertificate?>(null) }
  var certToDelete by remember { mutableStateOf<SocietyCertificate?>(null) }
  var certToReject by remember { mutableStateOf<SocietyCertificate?>(null) }
  var rejectionReason by remember { mutableStateOf("") }

  val filteredCertificates = remember(certificates, typeFilter) {
    if (typeFilter == "ALL") certificates
    else certificates.filter { it.certificateType == typeFilter }
  }

  if (certToDelete != null) {
    val target = certToDelete!!
    ConfirmDeleteDialog(
      itemTitle = "${target.certificateCode} — ${target.studentName}",
      itemSubtitle = "Permanently revoke and delete this issued certificate?",
      onConfirmDelete = { onDeleteCertificate(target) },
      onDismiss = { certToDelete = null },
    )
  }

  val pdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val cert = certToExport
    if (uri != null && cert != null) {
      PdfExportManager.writeCertificatePdfToUri(context, uri, cert)
        .onSuccess { onMessage("Dual-Logo Official Certificate PDF saved!") }
        .onFailure { onMessage("PDF generation failed: ${it.localizedMessage}") }
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        OutlinedButton(
          onClick = onOpenVerifyDialog,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Icon(Icons.Outlined.VerifiedUser, contentDescription = null)
          Spacer(Modifier.width(8.dp))
          Text("Verify Any Certificate by ID / QR Code")
        }
      }

      item {
        YesDropdownField(
          label = "Filter Certificate Category",
          options = listOf("ALL", "PARTICIPATION", "ORGANIZER", "MERIT", "MEMBERSHIP"),
          selectedOption = typeFilter,
          onOptionSelected = { typeFilter = it },
          optionLabel = { if (it == "ALL") "All Certificates (${certificates.size})" else it },
          testTag = "admin_certificates_type_dropdown",
        )
      }

      if (filteredCertificates.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Certificates Issued",
            subtitle = "Tap + to generate official UAF + YES dual-logo QR-verified certificates.",
            icon = Icons.Filled.WorkspacePremium,
          )
        }
      } else {
        items(filteredCertificates, key = { it.id }) { cert ->
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OfficialCertificateCard(
              certificate = cert,
              onDownloadPdf = {
                certToExport = cert
                pdfLauncher.launch("${cert.certificateCode}.pdf")
              },
              onDelete = if (canManageCertificates) ({ certToDelete = cert }) else null,
            )

            if (cert.status == "PENDING") {
              Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
              ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text("Approval Required", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                  }
                  Text("This certificate is generated and forwarded to the President, SVP, and VP for digital approval.", style = MaterialTheme.typography.bodySmall)
                  
                  if (isSeniorCabinet || canApproveAny) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                      Button(
                        onClick = { onApproveCertificate(cert) },
                        modifier = Modifier.weight(1f),
                      ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve")
                      }
                      OutlinedButton(
                        onClick = { certToReject = cert; rejectionReason = "" },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                      ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reject")
                      }
                    }
                  }
                }
              }
            } else if (cert.status == "REJECTED") {
              StatusPill(label = "REJECTED")
            }
          }
        }
      }
    }

    if (canManageCertificates) {
      FloatingActionButton(
        onClick = { showIssueDialog = true },
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .padding(18.dp)
          .testTag("issue_certificate_fab"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Issue Certificate")
      }
    }
  }

  if (showIssueDialog) {
    var selectedStudentId by remember(students) { mutableStateOf(students.firstOrNull()?.id ?: "") }
    val selectedStudent = students.firstOrNull { it.id == selectedStudentId }
    var selectedEventId by remember(events) { mutableStateOf(events.firstOrNull()?.id ?: "GENERAL_SESSION") }
    val selectedEvent = events.firstOrNull { it.id == selectedEventId }
    var certType by remember { mutableStateOf("PARTICIPATION") }
    var signatory by remember { mutableStateOf("President & Convener, Young Entomologist Society — Department of Entomology, UAF") }

    AlertDialog(
      onDismissRequest = { showIssueDialog = false },
      title = { Text("Issue Official Dual-Logo Certificate") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (students.isEmpty()) {
            Text("No registered students available yet. Add a student first.")
          } else {
            YesDropdownField(
              label = "Recipient Student",
              options = students.map { it.id },
              selectedOption = selectedStudentId,
              onOptionSelected = { selectedStudentId = it },
              optionLabel = { id ->
                val st = students.firstOrNull { it.id == id }
                if (st != null) "${st.fullName} (${st.registrationNumber})" else id
              },
            )
          }

          val eventOptions = listOf("GENERAL_SESSION") + events.map { it.id }
          YesDropdownField(
            label = "Event / Program",
            options = eventOptions,
            selectedOption = selectedEventId,
            onOptionSelected = { selectedEventId = it },
            optionLabel = { id ->
              if (id == "GENERAL_SESSION") "Young Entomologist Society Annual Session"
              else events.firstOrNull { it.id == id }?.title ?: id
            },
          )

          YesDropdownField(
            label = "Certificate Type",
            options = listOf("PARTICIPATION", "ORGANIZER", "MERIT", "MEMBERSHIP"),
            selectedOption = certType,
            onOptionSelected = { certType = it },
          )

          OutlinedTextField(
            value = signatory,
            onValueChange = { signatory = it },
            label = { Text("Authorized Signatory Line") },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (selectedStudent != null) {
              onIssueCertificate(
                selectedStudent.studentUid.ifBlank { selectedStudent.id },
                selectedStudent.fullName,
                selectedStudent.registrationNumber,
                selectedEvent?.id ?: "general",
                selectedEvent?.title ?: "Young Entomologist Society Annual Session",
                selectedEvent?.eventDate ?: "2026-10-30",
                certType,
                signatory,
              )
              showIssueDialog = false
            }
          },
          enabled = selectedStudent != null,
        ) {
          Text("Issue Certificate")
        }
      },
      dismissButton = {
        TextButton(onClick = { showIssueDialog = false }) { Text("Cancel") }
      },
    )
  }
  if (certToReject != null) {
    val target = certToReject!!
    AlertDialog(
      onDismissRequest = { certToReject = null },
      title = { Text("Reject Certificate Request") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("${target.studentName} — ${target.eventTitle}", fontWeight = FontWeight.Bold)
          OutlinedTextField(
            value = rejectionReason,
            onValueChange = { rejectionReason = it },
            label = { Text("Rejection Reason *") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onRejectCertificate(target, rejectionReason)
            certToReject = null
          },
          enabled = rejectionReason.trim().length >= 3,
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Confirm Rejection")
        }
      },
      dismissButton = {
        TextButton(onClick = { certToReject = null }) { Text("Cancel") }
      },
    )
  }
}
