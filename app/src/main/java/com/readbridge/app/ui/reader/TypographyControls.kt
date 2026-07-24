package com.readbridge.app.ui.reader

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.readbridge.app.domain.reader.model.ContentWidth
import com.readbridge.app.domain.reader.model.ReaderFontFamily
import com.readbridge.app.domain.reader.model.ReaderFontWeight
import com.readbridge.app.domain.reader.model.ReaderTextAlign
import com.readbridge.app.domain.reader.model.ReadingPreferences
import com.readbridge.app.domain.reader.model.ReadingTheme
import kotlin.math.roundToInt

/** The live "Aa" panel. Every change writes through immediately so it persists globally. */
@Composable
fun TypographyControls(
    prefs: ReadingPreferences,
    onChange: (ReadingPreferences) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Leitura", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { onChange(ReadingPreferences()) }) { Text("Restaurar") }
        }

        Section("Tema") {
            ChipRow(
                options = ReadingTheme.entries,
                selected = prefs.theme,
                label = ::themeLabel,
                onSelect = { onChange(prefs.copy(theme = it)) },
            )
        }

        Section("Fonte") {
            ChipRow(
                options = ReaderFontFamily.entries,
                selected = prefs.fontFamily,
                label = ::fontFamilyLabel,
                onSelect = { onChange(prefs.copy(fontFamily = it)) },
            )
        }

        SliderSection(
            label = "Tamanho",
            value = "${prefs.fontSizeSp} sp",
            sliderValue = prefs.fontSizeSp.toFloat(),
            range = ReadingPreferences.MIN_FONT_SIZE.toFloat()..ReadingPreferences.MAX_FONT_SIZE.toFloat(),
            steps = ReadingPreferences.MAX_FONT_SIZE - ReadingPreferences.MIN_FONT_SIZE - 1,
            onValueChange = { onChange(prefs.copy(fontSizeSp = it.roundToInt())) },
        )

        SliderSection(
            label = "Entrelinha",
            value = String.format("%.1f", prefs.lineHeight),
            sliderValue = prefs.lineHeight,
            range = ReadingPreferences.MIN_LINE_HEIGHT..ReadingPreferences.MAX_LINE_HEIGHT,
            steps = 0,
            onValueChange = { onChange(prefs.copy(lineHeight = it)) },
        )

        SliderSection(
            label = "Espaço entre letras",
            value = String.format("%.2f em", prefs.letterSpacingEm),
            sliderValue = prefs.letterSpacingEm,
            range = ReadingPreferences.MIN_LETTER_SPACING..ReadingPreferences.MAX_LETTER_SPACING,
            steps = 0,
            onValueChange = { onChange(prefs.copy(letterSpacingEm = it)) },
        )

        Section("Peso") {
            ChipRow(
                options = ReaderFontWeight.entries,
                selected = prefs.fontWeight,
                label = ::fontWeightLabel,
                onSelect = { onChange(prefs.copy(fontWeight = it)) },
            )
        }

        Section("Largura") {
            ChipRow(
                options = ContentWidth.entries,
                selected = prefs.contentWidth,
                label = ::contentWidthLabel,
                onSelect = { onChange(prefs.copy(contentWidth = it)) },
            )
        }

        Section("Alinhamento") {
            ChipRow(
                options = ReaderTextAlign.entries,
                selected = prefs.textAlign,
                label = ::textAlignLabel,
                onSelect = { onChange(prefs.copy(textAlign = it)) },
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Hifenização", style = MaterialTheme.typography.bodyLarge)
            Switch(
                checked = prefs.hyphenation,
                onCheckedChange = { onChange(prefs.copy(hyphenation = it)) },
            )
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun <T> ChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(label(option)) },
            )
        }
    }
}

@Composable
private fun SliderSection(
    label: String,
    value: String,
    sliderValue: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
) {
    Section(title = "$label  •  $value") {
        Slider(
            value = sliderValue,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
        )
    }
}

private fun themeLabel(theme: ReadingTheme): String = when (theme) {
    ReadingTheme.Light -> "Claro"
    ReadingTheme.Sepia -> "Sépia"
    ReadingTheme.Gray -> "Cinza"
    ReadingTheme.Oled -> "OLED"
    ReadingTheme.System -> "Sistema"
}

private fun fontFamilyLabel(family: ReaderFontFamily): String = when (family) {
    ReaderFontFamily.Serif -> "Serifada"
    ReaderFontFamily.SansSerif -> "Sem serifa"
    ReaderFontFamily.Slab -> "Slab"
    ReaderFontFamily.Monospace -> "Mono"
    ReaderFontFamily.Dyslexic -> "Dislexia"
}

private fun fontWeightLabel(weight: ReaderFontWeight): String = when (weight) {
    ReaderFontWeight.Light -> "Leve"
    ReaderFontWeight.Normal -> "Normal"
    ReaderFontWeight.Medium -> "Médio"
    ReaderFontWeight.Bold -> "Negrito"
}

private fun contentWidthLabel(width: ContentWidth): String = when (width) {
    ContentWidth.Narrow -> "Estreita"
    ContentWidth.Medium -> "Média"
    ContentWidth.Wide -> "Larga"
    ContentWidth.Full -> "Cheia"
}

private fun textAlignLabel(align: ReaderTextAlign): String = when (align) {
    ReaderTextAlign.Start -> "Esquerda"
    ReaderTextAlign.Justify -> "Justificado"
}
