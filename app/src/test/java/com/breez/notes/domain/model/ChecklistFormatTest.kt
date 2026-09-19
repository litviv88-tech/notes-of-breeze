package com.breez.notes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChecklistFormatTest {

    @Test
    fun encodesAndParsesItems() {
        val encoded = ChecklistFormat.encode(
            listOf(
                ChecklistItem("Молоко", false),
                ChecklistItem("Хлеб", true)
            )
        )
        val parsed = ChecklistFormat.parse(encoded)
        assertEquals(2, parsed.size)
        assertEquals("Молоко", parsed[0].text)
        assertFalse(parsed[0].done)
        assertEquals("Хлеб", parsed[1].text)
        assertTrue(parsed[1].done)
    }

    @Test
    fun togglesVisibleItem() {
        val body = ChecklistFormat.encode(
            listOf(ChecklistItem("Первое"), ChecklistItem("Второе"))
        )
        val toggled = ChecklistFormat.toggleVisible(body, 1)
        val parsed = ChecklistFormat.parse(toggled)
        assertFalse(parsed[0].done)
        assertTrue(parsed[1].done)
    }

    @Test
    fun shareTextUsesMarks() {
        val body = ChecklistFormat.encode(
            listOf(ChecklistItem("Купить", true), ChecklistItem("Позвонить", false))
        )
        val share = ChecklistFormat.asShareText(body)
        assertTrue(share.contains("☑ Купить"))
        assertTrue(share.contains("☐ Позвонить"))
    }
}
