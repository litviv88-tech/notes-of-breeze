package com.breez.notes.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PalettesTest {

    @Test
    fun catalogDoesNotDependOnAndroidResources() {
        assertEquals(1, Palettes.all.size)
        Palettes.all.forEach { palette ->
            assertTrue(palette.id.isNotBlank())
            assertTrue(palette.nameKey.startsWith("palette_"))
            assertTrue(palette.primaryHex.startsWith("#"))
        }
    }

    @Test
    fun unknownIdFallsBackToFirstPalette() {
        assertEquals(Palettes.all.first(), Palettes.find("missing"))
        assertEquals("breez_blue", Palettes.find("breez_blue").id)
    }
}
