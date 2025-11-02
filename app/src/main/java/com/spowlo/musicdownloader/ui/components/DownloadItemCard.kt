package com.spowlo.musicdownloader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spowlo.musicdownloader.domain.DownloadItem
import com.spowlo.musicdownloader.domain.DownloadStatus
import com.spowlo.musicdownloader.ui.theme.Error
import com.spowlo.musicdownloader.ui.theme.Success
import com.spowlo.musicdownloader.ui.theme.Warning

@Composable
fun DownloadItemCard(
    downloadItem: DownloadItem,
    onRetry: (String) -> Unit,
    onCancel: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cover Image
                if (downloadItem.coverImageUrl != null) {
                    AsyncImage(
                        model = downloadItem.coverImageUrl,
                        contentDescription = "Album cover",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "♪",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Download Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = downloadItem.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                    
                    Text(
                        text = downloadItem.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                    
                    // Status Badge
                    StatusBadge(status = downloadItem.status)
                }
                
                // Action Button
                when (downloadItem.status) {
                    DownloadStatus.FAILED -> {
                        IconButton(onClick = { onRetry(downloadItem.jobId) }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    DownloadStatus.PROCESSING -> {
                        IconButton(onClick = { onCancel(downloadItem.jobId) }) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Cancel",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    else -> {}
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress Bar
            if (downloadItem.status == DownloadStatus.PROCESSING) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = downloadItem.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text(
                        text = downloadItem.currentLine,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
            
            // Error Message
            if (downloadItem.status == DownloadStatus.FAILED && downloadItem.error != null) {
                Text(
                    text = downloadItem.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: DownloadStatus) {
    val (icon, color, text) = when (status) {
        DownloadStatus.COMPLETED -> Triple(Icons.Default.CheckCircle, Success, "Completed")
        DownloadStatus.FAILED -> Triple(Icons.Default.Error, Error, "Failed")
        DownloadStatus.PROCESSING -> Triple(null, Warning, "Processing")
        DownloadStatus.PENDING -> Triple(null, Warning, "Pending")
        DownloadStatus.CANCELLED -> Triple(Icons.Default.Cancel, Color.Gray, "Cancelled")
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }
        
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
