package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.EventRegistration
import com.example.data.model.SocietyConstants
import com.example.data.model.SocietyEvent
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.FullScreenImagePreviewDialog
import com.example.ui.components.SectionTitleRow
import com.example.ui.components.SocietyImage
import com.example.ui.components.StatusPill
import com.example.ui.components.YesDropdownField
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private data class ParsedEventDate(
  val year: Int,
  val monthZeroBased: Int,
  val dayOfMonth: Int,
) {
  val key: String
    get() = "%04d-%02d-%02d".format(Locale.US, year, monthZeroBased + 1, dayOfMonth)
}

private fun parseSocietyEventDate(raw: String): ParsedEventDate? {
  val trimmed = raw.trim()
  if (trimmed.isBlank()) return null
  val formats = listOf("yyyy-MM-dd", "dd-MM-yyyy", "yyyy/MM/dd", "dd/MM/yyyy")
  for (pattern in formats) {
    try {
      val sdf = SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }
      val parsed = sdf.parse(trimmed) ?: continue
      val cal = Calendar.getInstance()
      cal.time = parsed
      return ParsedEventDate(
        year = cal.get(Calendar.YEAR),
        monthZeroBased = cal.get(Calendar.MONTH),
        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
      )
    } catch (_: Exception) {
      // try next format
    }
  }
  return null
}

private fun isMeetingCategory(category: String): Boolean =
  category.equals("Society Meetings", ignoreCase = true)

private fun isFieldTripCategory(category: String): Boolean =
  category.equals("Study Tours", ignoreCase = true) ||
    category.equals("Educational Trips", ignoreCase = true) ||
    category.equals("Industrial Visits", ignoreCase = true)

@Composable
fun StudentEventCalendarContent(
  events: List<SocietyEvent>,
  registrations: List<EventRegistration>,
  onRegisterClick: (SocietyEvent) -> Unit,
  onCancelRegistration: (EventRegistration, SocietyEvent) -> Unit,
) {
  val initialCal = remember(events) {
    val firstUpcomingParsed = events
      .filter { it.upcoming }
      .mapNotNull { parseSocietyEventDate(it.eventDate) }
      .minByOrNull { it.key }
    val now = Calendar.getInstance()
    if (firstUpcomingParsed != null) {
      now.set(Calendar.YEAR, firstUpcomingParsed.year)
      now.set(Calendar.MONTH, firstUpcomingParsed.monthZeroBased)
      now.set(Calendar.DAY_OF_MONTH, 1)
    }
    now
  }

  var displayedYear by remember { mutableIntStateOf(initialCal.get(Calendar.YEAR)) }
  var displayedMonth by remember { mutableIntStateOf(initialCal.get(Calendar.MONTH)) }
  var selectedDateKey by remember { mutableStateOf<String?>(null) }
  var filterCategory by remember { mutableStateOf("All Categories") }
  var posterPreview by remember { mutableStateOf<Pair<String, String>?>(null) }
  var regToCancel by remember { mutableStateOf<Pair<EventRegistration, SocietyEvent>?>(null) }

  val regMap = remember(registrations) { registrations.associateBy { it.eventId } }

  if (posterPreview != null) {
    FullScreenImagePreviewDialog(
      title = posterPreview!!.first,
      imageSource = posterPreview!!.second,
      onDismiss = { posterPreview = null },
    )
  }

  if (regToCancel != null) {
    val (reg, ev) = regToCancel!!
    ConfirmDeleteDialog(
      itemTitle = ev.title,
      itemSubtitle = "Cancel and remove your registration for this event?",
      onConfirmDelete = { onCancelRegistration(reg, ev) },
      onDismiss = { regToCancel = null },
    )
  }

  val filterOptions = remember {
    listOf(
      "All Categories",
      "Meetings & Field Trips",
    ) + SocietyConstants.EVENT_CATEGORIES
  }

  val filteredEvents = remember(events, filterCategory) {
    events.filter { ev ->
      when (filterCategory) {
        "All Categories" -> true
        "Meetings & Field Trips" -> isMeetingCategory(ev.category) || isFieldTripCategory(ev.category)
        else -> ev.category.equals(filterCategory, ignoreCase = true)
      }
    }
  }

  val eventsByDateKey = remember(filteredEvents) {
    val map = mutableMapOf<String, MutableList<SocietyEvent>>()
    filteredEvents.forEach { ev ->
      val parsed = parseSocietyEventDate(ev.eventDate)
      if (parsed != null) {
        map.getOrPut(parsed.key) { mutableListOf() }.add(ev)
      }
    }
    map
  }

  val monthEvents = remember(filteredEvents, displayedYear, displayedMonth) {
    filteredEvents.filter { ev ->
      val parsed = parseSocietyEventDate(ev.eventDate)
      parsed != null && parsed.year == displayedYear && parsed.monthZeroBased == displayedMonth
    }.sortedBy { it.eventDate }
  }

  val selectedDayEvents = remember(selectedDateKey, eventsByDateKey, monthEvents) {
    if (selectedDateKey != null) {
      eventsByDateKey[selectedDateKey].orEmpty()
    } else {
      monthEvents
    }
  }

  val monthTitle = remember(displayedYear, displayedMonth) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, displayedYear)
      set(Calendar.MONTH, displayedMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    SimpleDateFormat("MMMM yyyy", Locale.US).format(cal.time)
  }

  val calendarCells = remember(displayedYear, displayedMonth) {
    val cal = Calendar.getInstance().apply {
      set(Calendar.YEAR, displayedYear)
      set(Calendar.MONTH, displayedMonth)
      set(Calendar.DAY_OF_MONTH, 1)
    }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // Sunday = 0
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = mutableListOf<Int?>()
    repeat(firstDayOfWeek) { cells.add(null) }
    for (day in 1..daysInMonth) {
      cells.add(day)
    }
    while (cells.size % 7 != 0) {
      cells.add(null)
    }
    cells
  }

  val upcomingMeetingsCount = remember(events) {
    events.count { it.upcoming && isMeetingCategory(it.category) }
  }
  val upcomingFieldTripsCount = remember(events) {
    events.count { it.upcoming && isFieldTripCategory(it.category) }
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize().testTag("student_event_calendar_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // Firestore Live Sync Summary Banner for Meetings & Field Trips
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Column {
                Text("YES Society Event Calendar", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                  "Live Firestore sync for Society Meetings, Field Trips & Seminars",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Filled.Groups, contentDescription = null, tint = Color(0xFF146C38), modifier = Modifier.size(20.dp))
                Column {
                  Text("$upcomingMeetingsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                  Text("Society Meetings", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White,
              modifier = Modifier.weight(1f),
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(Icons.Filled.Explore, contentDescription = null, tint = Color(0xFFB8860B), modifier = Modifier.size(20.dp))
                Column {
                  Text("$upcomingFieldTripsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                  Text("Field Trips & Tours", style = MaterialTheme.typography.labelSmall)
                }
              }
            }
          }
        }
      }
    }

    // Category Dropdown Filter
    item {
      YesDropdownField(
        label = "Filter Calendar by Event Category",
        options = filterOptions,
        selectedOption = filterCategory,
        onOptionSelected = {
          filterCategory = it
          selectedDateKey = null
        },
        testTag = "calendar_category_dropdown",
      )
    }

    // Interactive Monthly Grid Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Month Header Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = {
                selectedDateKey = null
                if (displayedMonth == 0) {
                  displayedMonth = 11
                  displayedYear -= 1
                } else {
                  displayedMonth -= 1
                }
              },
              modifier = Modifier.testTag("calendar_prev_month_btn"),
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(monthTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
              if (selectedDateKey != null) {
                Text(
                  text = "Showing $selectedDateKey • Tap here for whole month",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.clickable { selectedDateKey = null },
                )
              }
            }

            IconButton(
              onClick = {
                selectedDateKey = null
                if (displayedMonth == 11) {
                  displayedMonth = 0
                  displayedYear += 1
                } else {
                  displayedMonth += 1
                }
              },
              modifier = Modifier.testTag("calendar_next_month_btn"),
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
            }
          }

          // Weekday Headers
          Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { dayName ->
              Text(
                text = dayName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
              )
            }
          }

          // Calendar Weeks
          calendarCells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              week.forEach { dayNum ->
                if (dayNum == null) {
                  Spacer(modifier = Modifier.weight(1f).aspectRatio(1f))
                } else {
                  val cellKey = "%04d-%02d-%02d".format(Locale.US, displayedYear, displayedMonth + 1, dayNum)
                  val dayEvents = eventsByDateKey[cellKey].orEmpty()
                  val isSelected = selectedDateKey == cellKey
                  val hasMeeting = dayEvents.any { isMeetingCategory(it.category) }
                  val hasFieldTrip = dayEvents.any { isFieldTripCategory(it.category) }
                  val hasOtherEvent = dayEvents.isNotEmpty() && !hasMeeting && !hasFieldTrip

                  val bgColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else -> Color.Transparent
                  }
                  val textColor = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    dayEvents.isNotEmpty() -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                  }

                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(10.dp))
                      .background(bgColor)
                      .clickable {
                        selectedDateKey = if (isSelected) null else cellKey
                      }
                      .testTag("calendar_day_$cellKey"),
                    contentAlignment = Alignment.Center,
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center,
                    ) {
                      Text(
                        text = "$dayNum",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (dayEvents.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                      )
                      if (dayEvents.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                          if (hasMeeting) {
                            Box(
                              modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(0xFF146C38))
                            )
                          }
                          if (hasFieldTrip) {
                            Box(
                              modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFFFDF80) else Color(0xFFB8860B))
                            )
                          }
                          if (hasOtherEvent) {
                            Box(
                              modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color.White else Color(0xFF006D77))
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }

          // Color Legend
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            CalendarLegendItem(color = Color(0xFF146C38), label = "Society Meeting")
            CalendarLegendItem(color = Color(0xFFB8860B), label = "Field Trip / Tour")
            CalendarLegendItem(color = Color(0xFF006D77), label = "Seminar / Event")
          }
        }
      }
    }

    // Selected Date or Month Schedule Header
    item {
      SectionTitleRow(
        title = if (selectedDateKey != null) "Events on $selectedDateKey" else "Schedule for $monthTitle (${selectedDayEvents.size})",
        actionLabel = if (selectedDateKey != null) "Show Full Month" else null,
        onAction = if (selectedDateKey != null) ({ selectedDateKey = null }) else null,
      )
    }

    if (selectedDayEvents.isEmpty()) {
      item {
        EmptyStateCard(
          title = if (selectedDateKey != null) "No Society Activity on $selectedDateKey" else "No Scheduled Events in $monthTitle",
          subtitle = "Use the arrows above or select another month to view upcoming society meetings, field trips, and seminars.",
          icon = Icons.Filled.CalendarMonth,
        )
      }
    } else {
      items(selectedDayEvents, key = { "cal_${it.id}" }) { ev ->
        val myReg = regMap[ev.id]
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(
            width = 1.dp,
            color = when {
              isMeetingCategory(ev.category) -> Color(0xFF146C38).copy(alpha = 0.4f)
              isFieldTripCategory(ev.category) -> Color(0xFFB8860B).copy(alpha = 0.5f)
              else -> MaterialTheme.colorScheme.surfaceVariant
            },
          ),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ev.posterUri.isNotBlank()) {
              SocietyImage(
                imageSource = ev.posterUri,
                contentDescription = "${ev.title} poster",
                modifier = Modifier
                  .fillMaxWidth()
                  .height(170.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { posterPreview = ev.title to ev.posterUri },
                contentScale = ContentScale.Crop,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              StatusPill(label = ev.category)
              Text(
                text = if (ev.registrationFee <= 0.0) "FREE" else "PKR ${ev.registrationFee.toInt()}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
            }
            Text(ev.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(ev.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
              Text("${ev.eventDate} • ${ev.eventTime}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
              Text("${ev.venue} • Seats: ${ev.registeredCount}/${ev.maxParticipants}", style = MaterialTheme.typography.bodySmall)
            }

            if (myReg != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                  StatusPill(label = "REG: ${myReg.registrationStatus}")
                }
                OutlinedButton(
                  onClick = { regToCancel = myReg to ev },
                  modifier = Modifier.testTag("calendar_cancel_reg_${ev.id}"),
                ) {
                  Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(4.dp))
                  Text("Cancel", color = MaterialTheme.colorScheme.error)
                }
              }
            } else if (ev.upcoming && ev.registeredCount < ev.maxParticipants) {
              Button(
                onClick = { onRegisterClick(ev) },
                modifier = Modifier.fillMaxWidth().testTag("calendar_register_event_${ev.id}"),
              ) {
                Text("Register for ${ev.title}")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalendarLegendItem(color: Color, label: String) {
  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
