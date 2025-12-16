package com.eventos.app.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Dialog de confirmación con estilo consistente
 */
@Composable
fun EventosConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirmar",
    dismissText: String = "Cancelar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Dialog informativo simple
 */
@Composable
fun EventosInfoDialog(
    title: String,
    message: String,
    buttonText: String = "Entendido",
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(buttonText)
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Dialog de error
 */
@Composable
fun EventosErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.errorContainer,
        titleContentColor = MaterialTheme.colorScheme.onErrorContainer,
        textContentColor = MaterialTheme.colorScheme.onErrorContainer
    )
}

