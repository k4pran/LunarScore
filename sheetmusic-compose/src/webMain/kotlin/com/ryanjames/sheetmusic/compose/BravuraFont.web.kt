package com.ryanjames.sheetmusic.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import com.ryanjames.sheetmusic.compose.generated.resources.Res
import com.ryanjames.sheetmusic.compose.generated.resources.bravura
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalResourceApi::class)
@Composable
internal actual fun rememberBravuraFontFamily(): FontFamily? {
    val font by preloadFont(Res.font.bravura)
    return remember(font) { font?.let { FontFamily(it) } }
}
