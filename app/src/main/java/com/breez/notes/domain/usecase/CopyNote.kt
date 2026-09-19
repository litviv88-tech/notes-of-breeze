package com.breez.notes.domain.usecase

import com.breez.notes.domain.repository.NoteRepository
import javax.inject.Inject

class CopyNote @Inject constructor(
    private val notes: NoteRepository
) {
    suspend operator fun invoke(id: Long, folderId: Long?): Long {
        if (id <= 0L) return 0L
        return notes.copyToFolder(id, folderId)
    }
}
