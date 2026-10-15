package com.ryanjames.sheetmusic.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * The bundled Bravura font, or `null` while it is still loading.
 *
 * Fonts load synchronously on Android, JVM and iOS, but asynchronously on the web. Glyphs drawn before
 * the font is ready would use a fallback font with different metrics, so they must wait.
 */
@Composable
internal expect fun rememberBravuraFontFamily(): FontFamily?
