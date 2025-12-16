package com.eventos.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eventos.app.ui.theme.*

/**
 * Badge de estado con colores predefinidos
 */
@Composable
fun EventosStatusBadge(
    text: String,
    type: StatusType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (type) {
        StatusType.SUCCESS -> Success.copy(alpha = 0.15f)
        StatusType.WARNING -> Warning.copy(alpha = 0.15f)
        StatusType.ERROR -> Error.copy(alpha = 0.15f)
        StatusType.INFO -> Info.copy(alpha = 0.15f)
        StatusType.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
    }
    
    val textColor = when (type) {
        StatusType.SUCCESS -> Success
        StatusType.WARNING -> Warning
        StatusType.ERROR -> Error
        StatusType.INFO -> Info
        StatusType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Badge personalizado con colores propios
 */
@Composable
fun EventosCustomBadge(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

enum class StatusType {
    SUCCESS,
    WARNING,
    ERROR,
    INFO,
    NEUTRAL
}

