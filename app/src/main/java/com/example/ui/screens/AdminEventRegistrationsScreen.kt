package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.EventRegistration
import com.example.data.model.SocietyEvent
import com.example.data.model.StudentProfile
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import com.example.util.PdfExportManager

@Composable
fun AdminEventRegistrationsScreen(
  canManageRegistrations: Boolean,
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  students: List<StudentProfile>,
  initialEventId: String?,
  onRegisterStudentAdmin: (SocietyEvent, StudentProfile, String) -> Unit,
  onUpdateRegistration: (EventRegistration, String, String) -> Unit,
  onDeleteRegistration: (EventRegistration, SocietyEvent?) -> Unit,
  onMessage: (String) -> Unit,
) {
  val context = LocalContext.current

  var selectedEventId by remember(initialEventId, events) {
    mutableStateOf(
      if (!initialEventId.isNullOrBlank() && events.any { it.id == initialEventId }) {
        initialEventId
      } else {
        "OVERVIEW"
      }
    )
  }
  var statusFilter by remember { mutableStateOf("ALL") }
  var searchQuery by remember { mutableStateOf("") }
  var showManualRegisterDialog by remember { mutableStateOf(false) }
  var deletingRegistration by remember { mutableStateOf<EventRegistration?>(null) }
  var zoomReceipt by remember { mutableStateOf<Pair<String, String>?>(null) }

  val selectedEvent = remember(events, selectedEventId) {
    events.firstOrNull { it.id == selectedEventId }
  }

  val eventRegistrations = remember(registrations, selectedEvent, statusFilter, searchQuery) {
    if (selectedEvent == null) emptyList()
    else {
      registrations
        .filter { it.eventId == selectedEvent.id }
        .filter { reg ->
          val statusOk = when (statusFilter) {
            "APPROVED" -> reg.registrationStatus == "APPROVED"
            "PENDING" -> reg.registrationStatus == "PENDING"
            "PAID" -> reg.paymentStatus == "PAID" || reg.paymentStatus == "FREE"
            "UNPAID" -> reg.paymentStatus == "UNPAID" || reg.paymentStatus == "RECEIPT_SUBMITTED"
            else -> true
          }
          val q = searchQuery.trim().lowercase()
          val queryOk = q.isEmpty() ||
            reg.studentName.lowercase().contains(q) ||
            reg.registrationNumber.lowercase().contains(q) ||
            reg.department.lowercase().contains(q)
          statusOk && queryOk
        }
    }
  }

  val exportPdfLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("application/pdf")
  ) { uri: Uri? ->
    val ev = selectedEvent
    if (uri != null && ev != null) {
      val rows = registrations.filter { it.eventId == ev.id }.map {
        listOf(it.studentName, it.registrationNumber, it.department, "PKR ${it.feeAmount.toInt()}", it.paymentStatus, it.registrationStatus)
      }
      PdfExportManager.writeSummaryReportPdfToUri(
        context = context,
        uri = uri,
        reportTitle = "Event Registrations — ${ev.title}",
        subtitle = "Date: ${ev.eventDate} • Venue: ${ev.venue} • Participants: ${rows.size}/${ev.maxParticipants}",
        headers = listOf("Student Name", "Reg No", "Department", "Fee", "Payment", "Status"),
        rows = rows,
      ).onSuccess { onMessage("Registrations PDF saved for ${ev.title}!") }
        .onFailure { onMessage("PDF export failed: ${it.localizedMessage}") }
    }
  }

  val exportCsvLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.CreateDocument("text/csv")
  ) { uri: Uri? ->
    val ev = selectedEvent
    if (uri != null && ev != null) {
      val rows = registrations.filter { it.eventId == ev.id }.map {
        listOf(ev.title, ev.eventDate, it.studentName, it.registrationNumber, it.department, it.feeAmount.toInt().toString(), it.paymentStatus, it.registrationStatus, it.transactionRef)
      }
      PdfExportManager.writeCsvReportToUri(
        context = context,
        uri = uri,
        headers = listOf("Event", "Date", "Student Name", "Reg No", "Department", "Fee_PKR", "Payment_Status", "Registration_Status", "Transaction_Ref"),
        rows = rows,
      ).onSuccess { onMessage("Registrations CSV saved for ${ev.title}!") }
        .onFailure { onMessage("CSV export failed: ${it.localizedMessage}") }
    }
  }

  if (zoomReceipt != null) {
    val (title, src) = zoomReceipt!!
    FullScreenImagePreviewDialog(
      title = title,
      imageSource = src,
      onDismiss = { zoomReceipt = null },
    )
  }

  if (deletingRegistration != null) {
    val target = deletingRegistration!!
    ConfirmDeleteDialog(
      itemTitle = "${target.studentName} (${target.registrationNumber})",
      itemSubtitle = "Remove registration from \"${target.eventTitle}\" and free 1 participant seat?",
      onConfirmDelete = {
        val parentEvent = events.firstOrNull { it.id == target.eventId }
        onDeleteRegistration(target, parentEvent)
      },
      onDismiss = { deletingRegistration = null },
    )
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    if (events.isEmpty()) {
      item {
        EmptyStateCard(
          title = "No Events Created Yet",
          subtitle = "Create events in the Events tab first. Each event automatically gets its own separate registration sheet here.",
          icon = Icons.Filled.Event,
        )
      }
      return@LazyColumn
    }

    // Event Registration Sheet Dropdown Selector
    item {
      YesDropdownField(
        label = "Separate Event Registration Sheet",
        options = listOf("OVERVIEW") + events.map { it.id },
        selectedOption = selectedEventId,
        onOptionSelected = { selectedEventId = it },
        optionLabel = { opt ->
          if (opt == "OVERVIEW") {
            "All Event Registration Sheets (${events.size} Events)"
          } else {
            val ev = events.firstOrNull { it.id == opt }
            val count = registrations.count { it.eventId == opt }
            if (ev != null) "${ev.title} ($count Registered)" else opt
          }
        },
        testTag = "event_reg_sheet_dropdown",
      )
    }

    if (selectedEvent == null) {
      item {
        SectionTitleRow(title = "Select an Event to Manage Its Registrations")
      }
      items(events, key = { it.id }) { ev ->
        val evRegs = registrations.filter { it.eventId == ev.id }
        val approvedCount = evRegs.count { it.registrationStatus == "APPROVED" }
        val pendingCount = evRegs.count { it.registrationStatus == "PENDING" }
        val paidOrFreeCount = evRegs.count { it.paymentStatus == "PAID" || it.paymentStatus == "FREE" }
        val seatRatio = (evRegs.size.toFloat() / ev.maxParticipants.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedEventId = ev.id }
            .testTag("separate_event_reg_card_${ev.id}"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              StatusPill(label = ev.category)
              Text(
                text = if (ev.registrationFee <= 0.0) "FREE EVENT" else "Fee: PKR ${ev.registrationFee.toInt()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(ev.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("${ev.eventDate} • ${ev.venue}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            LinearProgressIndicator(
              progress = { seatRatio },
              modifier = Modifier.fillMaxWidth().height(8.dp),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Registered: ${evRegs.size}/${ev.maxParticipants}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
              Text("Approved: $approvedCount • Pending: $pendingCount • Paid/Free: $paidOrFreeCount", style = MaterialTheme.typography.labelSmall)
            }

            Button(
              onClick = { selectedEventId = ev.id },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(Icons.Filled.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(Modifier.width(8.dp))
              Text("Open ${ev.title} Registrations (${evRegs.size})")
            }
          }
        }
      }
    } else {
      val allForThisEvent = registrations.filter { it.eventId == selectedEvent.id }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              StatusPill(label = selectedEvent.category)
              Text(
                text = "Seats: ${allForThisEvent.size} / ${selectedEvent.maxParticipants}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Text(selectedEvent.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
              "Date: ${selectedEvent.eventDate} • Venue: ${selectedEvent.venue} • Fee: ${if (selectedEvent.registrationFee <= 0.0) "FREE" else "PKR ${selectedEvent.registrationFee.toInt()}"}",
              style = MaterialTheme.typography.bodySmall,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              if (canManageRegistrations) {
                Button(
                  onClick = { showManualRegisterDialog = true },
                  modifier = Modifier.weight(1f).testTag("admin_manual_register_student_btn"),
                ) {
                  Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Add Student")
                }
              }
              OutlinedButton(
                onClick = { exportPdfLauncher.launch("YES_${selectedEvent.id}_Registrations.pdf") },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("PDF")
              }
              OutlinedButton(
                onClick = { exportCsvLauncher.launch("YES_${selectedEvent.id}_Registrations.csv") },
                modifier = Modifier.weight(1f),
              ) {
                Icon(Icons.Filled.TableView, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("CSV")
              }
            }
          }
        }
      }

      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Search participants in ${selectedEvent.title}") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        YesDropdownField(
          label = "Filter Participant Status",
          options = listOf("ALL", "APPROVED", "PENDING", "PAID", "UNPAID"),
          selectedOption = statusFilter,
          onOptionSelected = { statusFilter = it },
          optionLabel = {
            when (it) {
              "ALL" -> "All Participants (${allForThisEvent.size})"
              "APPROVED" -> "Approved"
              "PENDING" -> "Pending Verification"
              "PAID" -> "Paid / Free"
              "UNPAID" -> "Unpaid / Receipt Submitted"
              else -> it
            }
          },
          testTag = "event_reg_status_dropdown",
        )
      }

      if (eventRegistrations.isEmpty()) {
        item {
          EmptyStateCard(
            title = "No Registrations in This View",
            subtitle = "Students who register for \"${selectedEvent.title}\" appear exclusively in this event's sheet.",
            icon = Icons.Filled.HowToReg,
          )
        }
      } else {
        items(eventRegistrations, key = { it.id }) { reg ->
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
                  Text("${reg.studentName} (${reg.registrationNumber})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                  Text("${reg.department} • Fee: PKR ${reg.feeAmount.toInt()}", style = MaterialTheme.typography.bodySmall)
                }
                if (canManageRegistrations) {
                  IconButton(
                    onClick = { deletingRegistration = reg },
                    modifier = Modifier.testTag("delete_reg_${reg.id}"),
                  ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Registration", tint = MaterialTheme.colorScheme.error)
                  }
                }
              }

              if (reg.transactionRef.isNotBlank()) {
                Text("Payment Ref: ${reg.transactionRef}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }

              if (reg.receiptUri.isNotBlank()) {
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { zoomReceipt = "${reg.studentName} — Payment Receipt" to reg.receiptUri },
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                ) {
                  SocietyImage(
                    imageSource = reg.receiptUri,
                    contentDescription = "Receipt of ${reg.studentName}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                  )
                }
                Text("Tap receipt screenshot to inspect full image", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(label = reg.registrationStatus)
                StatusPill(label = reg.paymentStatus)
              }

              if (canManageRegistrations) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  Button(
                    onClick = {
                      onUpdateRegistration(
                        reg,
                        "APPROVED",
                        if (reg.feeAmount > 0.0) "PAID" else "FREE",
                      )
                    },
                  ) {
                    Text("Approve & Mark Paid")
                  }
                  OutlinedButton(
                    onClick = { onUpdateRegistration(reg, "REJECTED", reg.paymentStatus) },
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
  }

  if (showManualRegisterDialog && selectedEvent != null) {
    val ev = selectedEvent
    var selectedStudentId by remember(students) { mutableStateOf(students.firstOrNull()?.id ?: "") }
    val chosenStudent = students.firstOrNull { it.id == selectedStudentId }
    var paymentState by remember(ev) { mutableStateOf(if (ev.registrationFee <= 0.0) "FREE" else "PAID") }

    AlertDialog(
      onDismissRequest = { showManualRegisterDialog = false },
      title = { Text("Register Student — ${ev.title}") },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          if (students.isEmpty()) {
            Text("No student records found. Add students in the Students tab first.")
          } else {
            YesDropdownField(
              label = "Select Registered Student",
              options = students.map { it.id },
              selectedOption = selectedStudentId,
              onOptionSelected = { selectedStudentId = it },
              optionLabel = { id ->
                val st = students.firstOrNull { it.id == id }
                if (st != null) "${st.fullName} (${st.registrationNumber})" else id
              },
            )
            if (ev.registrationFee > 0.0) {
              YesDropdownField(
                label = "Payment Status (PKR ${ev.registrationFee.toInt()})",
                options = listOf("PAID", "UNPAID"),
                selectedOption = paymentState,
                onOptionSelected = { paymentState = it },
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (chosenStudent != null) {
              onRegisterStudentAdmin(ev, chosenStudent, paymentState)
              showManualRegisterDialog = false
            }
          },
          enabled = chosenStudent != null,
        ) {
          Text("Register Participant")
        }
      },
      dismissButton = {
        TextButton(onClick = { showManualRegisterDialog = false }) { Text("Cancel") }
      },
    )
  }
}
