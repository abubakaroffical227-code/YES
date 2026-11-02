package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AdminAccount
import com.example.data.model.SocietyConstants

@Composable
fun AdminPermissionManagementScreen(
  admin: AdminAccount,
  onPermissionChanged: (AdminAccount, String, Boolean) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Text("Manage Permissions for ${admin.fullName}", style = MaterialTheme.typography.titleLarge)
      Text("Role: ${admin.adminRole}", style = MaterialTheme.typography.bodyMedium)
      Spacer(modifier = Modifier.height(16.dp))
    }

    items(SocietyConstants.ALL_PERMISSIONS) { permission ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(permission, style = MaterialTheme.typography.bodyMedium)
        Switch(
          checked = admin.permissions.contains(permission),
          onCheckedChange = { isChecked ->
            onPermissionChanged(admin, permission, isChecked)
          }
        )
      }
    }
  }
}
