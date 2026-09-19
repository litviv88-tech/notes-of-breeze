package com.breez.notes.domain.usecase

import com.breez.notes.domain.fakes.FakeNoteRepository
import com.breez.notes.domain.model.Note
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CopyNoteTest {

    @Test
    fun copiesNoteToAnotherFolderAndKeepsOriginal() = runTest {
        val notes = FakeNoteRepository()
        val sourceId = notes.upsert(
            Note(title = "Список", body = "молоко", folderId = 1L, reminderAt = 99L, isPinned = true)
        )
        val copyNote = CopyNote(notes)

        val copyId = copyNote(sourceId, 2L)

        assertTrue(copyId > 0L)
        assertNotEquals(sourceId, copyId)
        assertEquals(2, notes.getAll().size)
        val original = notes.getById(sourceId)
        val copy = notes.getById(copyId)
        assertEquals(1L, original?.folderId)
        assertEquals("Список", original?.title)
        assertTrue(original?.isPinned == true)
        assertEquals(2L, copy?.folderId)
        assertEquals("Список", copy?.title)
        assertEquals("молоко", copy?.body)
        assertNull(copy?.reminderAt)
        assertTrue(copy?.isPinned == false)
    }

    @Test
    fun renameAndMoveKeepTheSameNote() = runTest {
        val notes = FakeNoteRepository()
        val id = notes.upsert(Note(title = "Старое", body = "текст", folderId = null))

        notes.rename(id, "Новое")
        notes.moveToFolder(id, 7L)

        val saved = notes.getById(id)
        assertEquals("Новое", saved?.title)
        assertEquals(7L, saved?.folderId)
        assertEquals(1, notes.getAll().size)
    }
}
