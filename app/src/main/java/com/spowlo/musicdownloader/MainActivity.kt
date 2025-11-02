package com.spowlo.musicdownloader

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spowlo.musicdownloader.data.local.AppDatabase
import com.spowlo.musicdownloader.data.remote.RetrofitClient
import com.spowlo.musicdownloader.data.repository.MusicRepository
import com.spowlo.musicdownloader.ui.screens.*
import com.spowlo.musicdownloader.ui.theme.SpowloMusicDownloaderTheme

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Handle permission denied
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request storage permission
        checkAndRequestPermissions()
        
        // Initialize repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MusicRepository(
            apiService = RetrofitClient.apiService,
            downloadDao = database.downloadDao()
        )
        
        setContent {
            SpowloMusicDownloaderTheme {
                SpowloApp(repository = repository)
            }
        }
    }
    
    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ - Request READ_MEDIA_AUDIO permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpowloApp(repository: MusicRepository) {
    val navController = rememberNavController()
    
    // Create ViewModels
    val homeViewModel = remember { HomeViewModel(repository) }
    val downloadViewModel = remember { DownloadViewModel(repository) }
    val historyViewModel = remember { HistoryViewModel(repository) }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                // Pop up to the start destination and save state
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToDownloads = {
                        navController.navigate(Screen.Downloads.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
            
            composable(Screen.Downloads.route) {
                DownloadScreen(viewModel = downloadViewModel)
            }
            
            composable(Screen.History.route) {
                HistoryScreen(viewModel = historyViewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Downloads : Screen("downloads", "Downloads", Icons.Default.Download)
    object History : Screen("history", "History", Icons.Default.History)
}

private val bottomNavItems = listOf(
    Screen.Home,
    Screen.Downloads,
    Screen.History
)
