package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.hasPermission


@Composable
fun PermissionGuard(
  admin: AdminAccount,
  requiredPermission: String,
  title: String = "Access Restricted",
  subtitle: String = "You do not have the required permissions to access this management module.",
  content: @Composable () -> Unit
) {
  if (admin.hasPermission(requiredPermission)) {
    content()
  } else {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
      EmptyStateCard(
        title = title,
        subtitle = subtitle,
        icon = androidx.compose.material.icons.Icons.Filled.Lock,
      )
    }
  }
}
