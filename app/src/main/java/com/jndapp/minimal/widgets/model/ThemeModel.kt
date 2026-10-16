package com.jndapp.minimal.widgets.model

import androidx.compose.ui.graphics.Color

data class StudioTheme(
    val name: String,
    val description: String,
    val accentColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val isPro: Boolean = false
)

object StudioThemes {
    val darkThemes = listOf(
        StudioTheme("Default", "Warm amber on pure black", Color(0xFFFFBF00), Color(0xFF000000), Color(0xFF222225), false),
        StudioTheme("Pure Black", "Pure black with white accent", Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFF1A1B20), false),
        StudioTheme("Midnight Blue", "Deep dark theme with blue accents", Color(0xFF2196F3), Color(0xFF000000), Color(0xFF001122), true),
        StudioTheme("Nothing Red", "Nothing's signature red on pure black", Color(0xFFD71921), Color(0xFF000000), Color(0xFF121212), true),
        StudioTheme("Neon Green", "Vibrant neon green on pure black", Color(0xFF00FF00), Color(0xFF000000), Color(0xFF0A0A0A), true),
        StudioTheme("Electric Purple", "Electric purple on pure black", Color(0xFF8A2BE2), Color(0xFF000000), Color(0xFF0F001F), true),
        StudioTheme("Crimson Red", "Deep crimson red on pure black", Color(0xFFDC143C), Color(0xFF000000), Color(0xFF1A0000), true),
        StudioTheme("Golden Yellow", "Bright golden yellow on pure black", Color(0xFFFFD700), Color(0xFF000000), Color(0xFF1A1A00), true),
        StudioTheme("Ocean Blue", "Deep ocean blue on pure black", Color(0xFF0066CC), Color(0xFF000000), Color(0xFF000A1A), true),
        StudioTheme("Hot Pink", "Vibrant hot pink on pure black", Color(0xFFFF69B4), Color(0xFF000000), Color(0xFF1A0010), true),
        StudioTheme("Turquoise", "Bright turquoise on pure black", Color(0xFF40E0D0), Color(0xFF000000), Color(0xFF001A1A), true),
        StudioTheme("Orange Fire", "Burning orange on pure black", Color(0xFFFF4500), Color(0xFF000000), Color(0xFF1A0A00), true),
        StudioTheme("Matrix", "Matrix green on deep black", Color(0xFF00FF41), Color(0xFF0A0A0A), Color(0xFF000000), true),
        StudioTheme("Cyberpunk Red", "Cyberpunk red on dark background", Color(0xFFFF0040), Color(0xFF1A0A0A), Color(0xFF000000), true),
        StudioTheme("Dracula", "Classic Dracula theme with purple accent", Color(0xFFBD93F9), Color(0xFF282A36), Color(0xFF44475A), true)
    )

    val lightThemes = listOf(
        StudioTheme("White Blue", "Pure white background with blue accent", Color(0xFF2196F3), Color(0xFFFFFFFF), Color(0xFFD8D8E4), false),
        StudioTheme("White Red", "Pure white background with red accent", Color(0xFFF44336), Color(0xFFFFFFFF), Color(0xFFFDECEA), true),
        StudioTheme("White Green", "Pure white background with green accent", Color(0xFF4CAF50), Color(0xFFFFFFFF), Color(0xFFE8F5E9), true),
        StudioTheme("White Pink", "Pure white background with pink accent", Color(0xFFE91E63), Color(0xFFFFFFFF), Color(0xFFFCE4EC), true),
        StudioTheme("White Orange", "Pure white background with orange accent", Color(0xFFFF9800), Color(0xFFFFFFFF), Color(0xFFFFF3E0), true),
        StudioTheme("White Purple", "Pure white background with purple accent", Color(0xFF9C27B0), Color(0xFFFFFFFF), Color(0xFFF3E5F5), true),
        StudioTheme("White Teal", "Pure white background with teal accent", Color(0xFF009688), Color(0xFFFFFFFF), Color(0xFFE0F2F1), true),
        StudioTheme("Ash White", "White background with charcoal accent", Color(0xFF212121), Color(0xFFFFFFFF), Color(0xFFF8F8F8), true),
        StudioTheme("Nord Light", "White background with nord blue accent", Color(0xFF5E81AC), Color(0xFFFFFFFF), Color(0xFFF0F4F8), true),
        StudioTheme("Solarized Light", "White background with solarized brown accent", Color(0xFFB58900), Color(0xFFFFFFFF), Color(0xFFFDFCF0), true)
    )

    val pastelThemes = listOf(
        StudioTheme("Baby Blue", "Soft sky blue with white accent", Color(0xFF1976D2), Color(0xFFBBDEFB), Color(0xFFE3F2FD), false),
        StudioTheme("Sweet Pink", "Soft pink with rose accent", Color(0xFFC2185B), Color(0xFFFCE4EC), Color(0xFFF8BBD9), true),
        StudioTheme("Minty", "Light mint with forest accent", Color(0xFF00796B), Color(0xFFE0F2F1), Color(0xFFB2DFDB), true),
        StudioTheme("Lavender", "Soft purple with indigo accent", Color(0xFF512DA8), Color(0xFFF3E5F5), Color(0xFFE1BEE7), true),
        StudioTheme("Creamy Sun", "Warm yellow with orange accent", Color(0xFFF57C00), Color(0xFFFFF9C4), Color(0xFFFFF176), true),
        StudioTheme("Peach Puff", "Soft peach with coral accent", Color(0xFFE64A19), Color(0xFFFFE0B2), Color(0xFFFFCCBC), true),
        StudioTheme("Lilac Dream", "Whimsical lilac with violet accent", Color(0xFF7B1FA2), Color(0xFFEDE7F6), Color(0xFFD1C4E9), true),
        StudioTheme("Cotton Candy", "Duo pastel pink and blue look", Color(0xFF2196F3), Color(0xFFFCE4EC), Color(0xFFBBDEFB), true)
    )

    val mutedThemes = listOf(
        StudioTheme("Ash & Lemon", "Volcanic ash with lemon zest", Color(0xFFFFF59D), Color(0xFF212121), Color(0xFF121212), false),
        StudioTheme("Midnight Rose", "Deep charcoal with dusty rose accent", Color(0xFFF48FB1), Color(0xFF121212), Color(0xFF1E1E1E), true),
        StudioTheme("Deep Lavender", "Dark slate with soft lavender accent", Color(0xFFB39DDB), Color(0xFF1A1A2E), Color(0xFF16213E), true),
        StudioTheme("Night Mint", "Obsidian with pale mint accent", Color(0xFF80CBC4), Color(0xFF1E1E1E), Color(0xFF121212), true),
        StudioTheme("Deep Sky", "Midnight blue with sky blue accent", Color(0xFF81D4FA), Color(0xFF0D47A1), Color(0xFF01579B), true),
        StudioTheme("Muted Forest", "Dark moss with sage green accent", Color(0xFFC5E1A5), Color(0xFF1B2E1E), Color(0xFF121E14), true),
        StudioTheme("Ember Dust", "Charcoal with faint amber accent", Color(0xFFFFB74D), Color(0xFF1E1E1E), Color(0xFF121212), true),
        StudioTheme("Coffee Night", "Deep brown with cream accent", Color(0xFFFFE0B2), Color(0xFF3E2723), Color(0xFF1B0000), true)
    )
}
