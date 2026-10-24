package com.ryanjames.sheetmusic.score

/** Semantic text roles, independent of a source format's element names or nesting. */
public enum class MetadataKind {
    WORK_NUMBER, WORK_TITLE, MOVEMENT_NUMBER, MOVEMENT_TITLE,
    CREATOR, RIGHTS, SOURCE, RELATION,
    ENCODING_DATE, ENCODER, SOFTWARE, ENCODING_DESCRIPTION, MISCELLANEOUS,
}

/** Ordered descriptive metadata. Repeated entries and empty text are retained. */
public sealed interface MetadataEntry {
    /**
     * One text value. [qualifier] is an optional creator/rights/encoder role or miscellaneous
     * field name. Text preserves whitespace; container nesting and typography are not represented.
     */
    public data class Text(
        val kind: MetadataKind,
        val text: String,
        val qualifier: String? = null,
    ) : MetadataEntry

    /**
     * Ordered visible credit text and its semantic types, associated with a one-based page. Each
     * of the [words] keeps its text exactly, including line breaks. [visuals] holds the position,
     * colour and text formatting of each of the [words] at the same index, or is empty when none
     * has any.
     */
    public data class Credit(
        val words: List<String>,
        val types: List<String> = emptyList(),
        val page: Int = 1,
        val visuals: List<VisualHints?> = emptyList(),
    ) : MetadataEntry {
        init {
            require(page > 0)
            require(visuals.isEmpty() || visuals.size == words.size)
        }
    }
}
