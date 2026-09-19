package com.breez.notes.domain.model

data class ChecklistItem(
    val text: String,
    val done: Boolean = false
)

object ChecklistFormat {
    private val donePrefix = Regex("""^\s*(?:[-*•]\s*)?\[(?:x|X|✓)\]\s*""")
    private val openPrefix = Regex("""^\s*(?:[-*•]\s*)?\[\s*\]\s*""")
    private val bulletPrefix = Regex("""^\s*[-*•]\s+""")

    fun parse(body: String): List<ChecklistItem> {
        val lines = body.lineSequence().map { it.trimEnd() }.toList()
        if (lines.all { it.isBlank() }) return listOf(ChecklistItem(""))
        return lines.map { line ->
            val trimmed = line.trim()
            when {
                donePrefix.containsMatchIn(trimmed) ->
                    ChecklistItem(trimmed.replaceFirst(donePrefix, ""), true)
                openPrefix.containsMatchIn(trimmed) ->
                    ChecklistItem(trimmed.replaceFirst(openPrefix, ""), false)
                trimmed.isBlank() -> ChecklistItem("")
                else -> ChecklistItem(trimmed.replaceFirst(bulletPrefix, ""), false)
            }
        }
    }

    fun encode(items: List<ChecklistItem>): String {
        val source = items.ifEmpty { listOf(ChecklistItem("")) }
        return source.joinToString("\n") { item ->
            val mark = if (item.done) "x" else " "
            "- [$mark] ${item.text}"
        }
    }

    fun preview(body: String, limit: Int = 3): List<ChecklistItem> {
        return parse(body).filter { it.text.isNotBlank() }.take(limit)
    }
}
