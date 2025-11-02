package com.spowlo.musicdownloader.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.spowlo.musicdownloader.R
import com.spowlo.musicdownloader.data.remote.Quality

@Composable
fun QualitySelector(
    selectedQuality: Quality,
    onQualitySelected: (Quality) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.select_quality),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QualityOption(
                    quality = Quality.M4A_320,
                    label = stringResource(R.string.quality_m4a_320),
                    selected = selectedQuality == Quality.M4A_320,
                    onClick = { onQualitySelected(Quality.M4A_320) }
                )
                
                QualityOption(
                    quality = Quality.OPUS_160,
                    label = stringResource(R.string.quality_opus_160),
                    selected = selectedQuality == Quality.OPUS_160,
                    onClick = { onQualitySelected(Quality.OPUS_160) }
                )
                
                QualityOption(
                    quality = Quality.BEST,
                    label = stringResource(R.string.quality_best),
                    selected = selectedQuality == Quality.BEST,
                    onClick = { onQualitySelected(Quality.BEST) }
                )
            }
        }
    }
}

@Composable
private fun QualityOption(
    quality: Quality,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because we handle click on the Row
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
