package com.ryanjames.sheetmusic.smufl

/**
 * SMuFL glyphs used by the renderer.
 *
 * A SMuFL font is scaled so that one em equals four staff spaces (the height of a five-line staff),
 * and every glyph is drawn relative to its origin on the text baseline.
 */
public enum class SmuflGlyph(public val codepoint: Char) {
    G_CLEF(''),
    NOTEHEAD_WHOLE(''),
    NOTEHEAD_HALF(''),
    NOTEHEAD_BLACK(''),
}

/** A point in SMuFL glyph coordinates: staff spaces, relative to the glyph origin, with y pointing **up**. */
public data class SmuflPoint(val x: Float, val y: Float)

/** Glyph bounding box and anchors, as published in a SMuFL font's metadata file. */
public data class GlyphMetrics(
    val bBoxSW: SmuflPoint,
    val bBoxNE: SmuflPoint,
    val stemUpSE: SmuflPoint? = null,
    val stemDownNW: SmuflPoint? = null,
) {
    val width: Float get() = bBoxNE.x - bBoxSW.x
}

/** The subset of a SMuFL font's `engravingDefaults` used by the layout, in staff spaces. */
public data class EngravingDefaults(
    val staffLineThickness: Float,
    val stemThickness: Float,
    val legerLineThickness: Float,
    val legerLineExtension: Float,
    val thinBarlineThickness: Float,
    val thickBarlineThickness: Float,
    val thinThickBarlineSeparation: Float,
)

/** Metrics for one SMuFL font. */
public class SmuflFontMetrics(
    public val fontName: String,
    public val engravingDefaults: EngravingDefaults,
    private val glyphs: Map<SmuflGlyph, GlyphMetrics>,
) {
    public operator fun get(glyph: SmuflGlyph): GlyphMetrics =
        glyphs[glyph] ?: error("$fontName has no metrics for $glyph")
}

/** Metrics copied from `Bravura.json` of Bravura 1.482, the font bundled by `sheetmusic-compose`. */
public val BravuraMetrics: SmuflFontMetrics = SmuflFontMetrics(
    fontName = "Bravura",
    engravingDefaults = EngravingDefaults(
        staffLineThickness = 0.13f,
        stemThickness = 0.12f,
        legerLineThickness = 0.16f,
        legerLineExtension = 0.4f,
        thinBarlineThickness = 0.16f,
        thickBarlineThickness = 0.5f,
        thinThickBarlineSeparation = 0.4f,
    ),
    glyphs = mapOf(
        SmuflGlyph.G_CLEF to GlyphMetrics(
            bBoxSW = SmuflPoint(0.0f, -2.632f),
            bBoxNE = SmuflPoint(2.684f, 4.392f),
        ),
        SmuflGlyph.NOTEHEAD_WHOLE to GlyphMetrics(
            bBoxSW = SmuflPoint(0.0f, -0.5f),
            bBoxNE = SmuflPoint(1.688f, 0.5f),
        ),
        SmuflGlyph.NOTEHEAD_HALF to GlyphMetrics(
            bBoxSW = SmuflPoint(0.0f, -0.5f),
            bBoxNE = SmuflPoint(1.18f, 0.5f),
            stemUpSE = SmuflPoint(1.18f, 0.168f),
            stemDownNW = SmuflPoint(0.0f, -0.168f),
        ),
        SmuflGlyph.NOTEHEAD_BLACK to GlyphMetrics(
            bBoxSW = SmuflPoint(0.0f, -0.5f),
            bBoxNE = SmuflPoint(1.18f, 0.5f),
            stemUpSE = SmuflPoint(1.18f, 0.168f),
            stemDownNW = SmuflPoint(0.0f, -0.168f),
        ),
    ),
)
