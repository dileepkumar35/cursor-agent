package com.spowlo.musicdownloader.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.spowlo.musicdownloader.R
import com.spowlo.musicdownloader.data.remote.Quality
import com.spowlo.musicdownloader.ui.components.MetadataCard
import com.spowlo.musicdownloader.ui.components.QualitySelector

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDownloads: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQualityDialog by remember { mutableStateOf(false) }
    
    // Show download dialog when download starts
    LaunchedEffect(uiState.downloadStarted) {
        if (uiState.downloadStarted) {
            onNavigateToDownloads()
            viewModel.resetAfterDownload()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.home_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // URL Input
        OutlinedTextField(
            value = uiState.url,
            onValueChange = { viewModel.updateUrl(it) },
            label = { Text(stringResource(R.string.url_hint)) },
            placeholder = { Text("https://open.spotify.com/track/...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 3,
            enabled = !uiState.isLoading
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Fetch Metadata Button
        Button(
            onClick = { viewModel.fetchMetadata() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.url.isNotBlank()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.btn_fetch_metadata))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Metadata Display
        if (uiState.metadata != null) {
            MetadataCard(
                metadata = uiState.metadata!!,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Quality Selection
        QualitySelector(
            selectedQuality = uiState.selectedQuality,
            onQualitySelected = { viewModel.updateQuality(it) },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Download Button
        Button(
            onClick = { viewModel.startDownload() },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.url.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = if (uiState.isLoading) "Processing..." else stringResource(R.string.btn_download)
            )
        }
        
        // Error Message
        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = uiState.error!!,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
