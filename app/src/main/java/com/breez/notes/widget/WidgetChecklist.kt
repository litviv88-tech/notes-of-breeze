package com.breez.notes.widget

data class ChecklistLine(
    val index: Int,
    val text: String,
    val done: Boolean
)

object WidgetChecklist {
    private val donePrefix = Regex("""^\s*(?:[-*•]\s*)?\[(?:x|X|✓)\]\s*""")
    private val openPrefix = Regex("""^\s*(?:[-*•]\s*)?\[\s*\]\s*""")
    private val bulletPrefix = Regex("""^\s*[-*•]\s+""")

    fun lines(body: String, title: String): List<ChecklistLine> {
        val source = body.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()
            .ifEmpty { listOf(title.ifBlank { "•" }) }
        return source.mapIndexed { index, line ->
            when {
                donePrefix.containsMatchIn(line) ->
                    ChecklistLine(index, line.replaceFirst(donePrefix, ""), true)
                openPrefix.containsMatchIn(line) ->
                    ChecklistLine(index, line.replaceFirst(openPrefix, ""), false)
                else ->
                    ChecklistLine(index, line.replaceFirst(bulletPrefix, ""), false)
            }
        }
    }

    fun toggle(body: String, title: String, index: Int): String {
        val hasBody = body.lineSequence().any { it.trim().isNotEmpty() }
        val raw = if (hasBody) body.split("\n") else listOf(title)
        if (index !in raw.indices) return body
        val trimmed = raw[index].trim()
        val updated = if (donePrefix.containsMatchIn(trimmed)) {
            "- [ ] ${trimmed.replaceFirst(donePrefix, "")}"
        } else {
            val text = trimmed.replaceFirst(openPrefix, "").replaceFirst(bulletPrefix, "")
            "- [x] $text"
        }
        return raw.toMutableList().also { it[index] = updated }.joinToString("\n")
    }
}
