package com.breez.notes.ui.navigation

sealed class Routes(val route: String) {
    data object Notes : Routes("notes")
    data object Editor : Routes("editor?noteId={noteId}&checklist={checklist}") {
        fun create(noteId: Long = -1L, checklist: Boolean = false): String =
            "editor?noteId=$noteId&checklist=${if (checklist) 1 else 0}"
    }
    data object Folders : Routes("folders")
    data object FolderDetail : Routes("folders/{folderId}") {
        fun create(folderId: Long): String = "folders/$folderId"
    }
    data object Settings : Routes("settings")
    data object ThemePicker : Routes("settings/theme")
    data object WallpaperPicker : Routes("settings/wallpaper")
}
