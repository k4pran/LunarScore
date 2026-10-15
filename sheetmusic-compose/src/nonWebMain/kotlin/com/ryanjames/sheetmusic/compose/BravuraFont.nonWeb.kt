package com.ryanjames.sheetmusic.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import com.ryanjames.sheetmusic.compose.generated.resources.Res
import com.ryanjames.sheetmusic.compose.generated.resources.bravura
import org.jetbrains.compose.resources.Font

@Composable
internal actual fun rememberBravuraFontFamily(): FontFamily? {
    val font = Font(Res.font.bravura)
    return remember(font) { FontFamily(font) }
}
