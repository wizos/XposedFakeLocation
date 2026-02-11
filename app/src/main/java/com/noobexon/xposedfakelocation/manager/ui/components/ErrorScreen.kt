package com.noobexon.xposedfakelocation.manager.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.noobexon.xposedfakelocation.R

/**
 * Displays an error dialog when the Xposed module is not active.
 *
 * @param onDismiss Callback to be invoked when the user dismisses the dialog.
 * @param onConfirm Callback to be invoked when the user confirms the dialog.
 */
@Composable
fun ErrorScreen(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.error_module_not_active)) },
        text = {
            Text(stringResource(R.string.error_module_not_active_message))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}