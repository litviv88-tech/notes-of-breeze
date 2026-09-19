package com.breez.notes.ui.navigation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.breez.notes.ui.editor.EditorScreen
import com.breez.notes.ui.folders.FolderDetailScreen
import com.breez.notes.ui.folders.FoldersScreen
import com.breez.notes.ui.notes.NotesScreen
import com.breez.notes.ui.settings.SettingsScreen
import com.breez.notes.ui.settings.ThemePickerScreen
import com.breez.notes.ui.settings.WallpaperPickerScreen
import com.breez.notes.ui.update.UpdateBanner
import com.breez.notes.ui.update.UpdateViewModel
import com.breez.notes.ui.web.WebNotesActivity

@Composable
fun NavGraph(
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val installPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        updateViewModel.onInstallPermissionResult()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        updateViewModel.checkForUpdate()
    }

    LaunchedEffect(updateState.needsInstallPermission) {
        if (!updateState.needsInstallPermission) return@LaunchedEffect
        updateViewModel.consumeInstallPermissionRequest()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            installPermissionLauncher.launch(intent)
        } else {
            updateViewModel.startUpdate()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Routes.Notes.route,
            modifier = Modifier.weight(1f)
        ) {
            composable(Routes.Notes.route) {
                NotesScreen(
                    onOpenNote = { id -> navController.navigate(Routes.Editor.create(id)) },
                    onCreateNote = { navController.navigate(Routes.Editor.create()) },
                    onOpenFolders = { navController.navigate(Routes.Folders.route) },
                    onOpenTheme = { navController.navigate(Routes.ThemePicker.route) },
                    onOpenWallpaper = { navController.navigate(Routes.WallpaperPicker.route) },
                    onOpenSettings = { navController.navigate(Routes.Settings.route) }
                )
            }
            composable(
                route = Routes.Editor.route,
                arguments = listOf(
                    navArgument("noteId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) {
                EditorScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.Folders.route) {
                FoldersScreen(
                    onBack = { navController.popBackStack() },
                    onOpenFolder = { id -> navController.navigate(Routes.FolderDetail.create(id)) }
                )
            }
            composable(
                route = Routes.FolderDetail.route,
                arguments = listOf(navArgument("folderId") { type = NavType.LongType })
            ) {
                FolderDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenNote = { id -> navController.navigate(Routes.Editor.create(id)) }
                )
            }
            composable(Routes.Settings.route) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTheme = { navController.navigate(Routes.ThemePicker.route) },
                    onOpenWallpaper = { navController.navigate(Routes.WallpaperPicker.route) },
                    onOpenWeb = {
                        context.startActivity(Intent(context, WebNotesActivity::class.java))
                    },
                    updateState = updateState,
                    onCheckUpdate = updateViewModel::checkForUpdate,
                    onStartUpdate = updateViewModel::startUpdate
                )
            }
            composable(Routes.ThemePicker.route) {
                ThemePickerScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.WallpaperPicker.route) {
                WallpaperPickerScreen(onBack = { navController.popBackStack() })
            }
        }
        if (updateState.showBanner && currentRoute != Routes.Settings.route) {
            UpdateBanner(
                state = updateState,
                onUpdate = updateViewModel::startUpdate,
                onDismiss = updateViewModel::dismissBanner,
                onRetry = updateViewModel::startUpdate
            )
        }
    }
}
