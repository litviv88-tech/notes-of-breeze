package com.breez.notes.ui.navigation

sealed class Routes(val route: String) {
    data object Notes : Routes("notes")
    data object Editor : Routes("editor?noteId={noteId}") {
        fun create(noteId: Long = -1L): String = "editor?noteId=$noteId"
    }
    data object Folders : Routes("folders")
    data object FolderDetail : Routes("folders/{folderId}") {
        fun create(folderId: Long): String = "folders/$folderId"
    }
    data object Settings : Routes("settings")
    data object ThemePicker : Routes("settings/theme")
    data object WallpaperPicker : Routes("settings/wallpaper")
}
