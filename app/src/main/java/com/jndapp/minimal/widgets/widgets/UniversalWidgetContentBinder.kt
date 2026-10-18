package com.jndapp.minimal.widgets.widgets

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.view.KeyEvent
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.MainActivity
import com.jndapp.minimal.widgets.R
import java.text.SimpleDateFormat
import java.util.*

object UniversalWidgetContentBinder {

    fun bindContent(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        widgetClassName: String,
        layoutId: Int,
        accentColor: Int,
        primaryTextColor: Int,
        secondaryTextColor: Int,
        isDark: Boolean
    ) {
        val simpleName = widgetClassName.substringAfterLast('.')
        val pkg = widgetClassName.substringBeforeLast('.', "").lowercase()

        // 1. Health & Pedometer
        if (pkg.contains("health") || simpleName.contains("Pedometer", ignoreCase = true) || simpleName.contains("Water", ignoreCase = true)) {
            bindHealthWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 2. Weather & Forecast
        if (pkg.contains("weather") || simpleName.contains("Weather", ignoreCase = true) ||
            simpleName.contains("Sun", ignoreCase = true) || simpleName.contains("Moon", ignoreCase = true) ||
            simpleName.contains("Wind", ignoreCase = true) || simpleName.contains("AirQ", ignoreCase = true) ||
            simpleName.contains("UV", ignoreCase = true)
        ) {
            bindWeatherWidget(context, views, appWidgetId, simpleName, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 3. AI & Search
        if (pkg.contains("ai") || simpleName.contains("chatgpt", ignoreCase = true) ||
            simpleName.contains("claude", ignoreCase = true) || simpleName.contains("grok", ignoreCase = true) ||
            simpleName.contains("gemini", ignoreCase = true) || simpleName.contains("deepseek", ignoreCase = true) ||
            simpleName.contains("perplexity", ignoreCase = true) || simpleName.contains("aibar", ignoreCase = true) ||
            simpleName.contains("aifolder", ignoreCase = true)
        ) {
            bindAiWidget(context, views, appWidgetId, accentColor, primaryTextColor)
        }

        // 4. App Launcher
        if (pkg.contains("applauncher") || simpleName.contains("Launcher", ignoreCase = true)) {
            bindAppLauncherWidget(context, views, appWidgetId, primaryTextColor, secondaryTextColor)
        }

        // 5. Calculator
        if (pkg.contains("calculator") || simpleName.contains("Calc", ignoreCase = true)) {
            bindCalculatorWidget(context, views, appWidgetId, primaryTextColor)
        }

        // 6. Compass
        if (pkg.contains("compass") || simpleName.contains("Compass", ignoreCase = true)) {
            bindCompassWidget(context, views, appWidgetId, accentColor, primaryTextColor)
        }

        // 7. Games
        if (pkg.contains("games") || simpleName.contains("Dice", ignoreCase = true) ||
            simpleName.contains("TicTac", ignoreCase = true) || simpleName.contains("Puzzle", ignoreCase = true) ||
            simpleName.contains("Dino", ignoreCase = true) || simpleName.contains("Snake", ignoreCase = true) ||
            simpleName.contains("Breakout", ignoreCase = true)
        ) {
            bindGamesWidget(context, views, appWidgetId, accentColor, primaryTextColor)
        }

        // 8. Quotes, Advice, Jokes
        if (pkg.contains("quotes") || simpleName.contains("Quote", ignoreCase = true) ||
            simpleName.contains("Advice", ignoreCase = true) || simpleName.contains("Joke", ignoreCase = true)
        ) {
            bindQuoteWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 9. Notes & Reminders
        if (pkg.contains("notes") || simpleName.contains("Note", ignoreCase = true) || simpleName.contains("Reminder", ignoreCase = true)) {
            bindNotesWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 10. Music Widgets
        if (pkg.contains("music") || simpleName.contains("Music", ignoreCase = true)) {
            bindMusicWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 11. Social & App Folders
        if (pkg.contains("social") || pkg.contains("folder") || simpleName.contains("Folder", ignoreCase = true)) {
            bindFolderWidget(context, views, appWidgetId, accentColor, primaryTextColor)
        }

        // 12. Google Widgets
        if (pkg.contains("google") || simpleName.contains("Google", ignoreCase = true)) {
            bindGoogleWidget(context, views, appWidgetId)
        }

        // 13. Settings & Quick Settings
        if (pkg.contains("settings") || simpleName.contains("Settings", ignoreCase = true) ||
            simpleName.contains("Wifi", ignoreCase = true) || simpleName.contains("Bluetooth", ignoreCase = true) ||
            simpleName.contains("Torch", ignoreCase = true) || simpleName.contains("Airplane", ignoreCase = true) ||
            simpleName.contains("Autorotate", ignoreCase = true) || simpleName.contains("Sound", ignoreCase = true) ||
            simpleName.contains("Hotspot", ignoreCase = true)
        ) {
            bindSettingsWidget(context, views, appWidgetId, simpleName, accentColor, primaryTextColor)
        }

        // 14. Productivity (Bookmarks, Pomodoro, Screentime)
        if (pkg.contains("productivity") || simpleName.contains("Bookmark", ignoreCase = true) ||
            simpleName.contains("Pomodoro", ignoreCase = true) || simpleName.contains("Screentime", ignoreCase = true)
        ) {
            bindProductivityWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 15. Hybrid Clocks
        if (pkg.contains("hybrid") || simpleName.contains("Clock", ignoreCase = true)) {
            bindHybridWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }

        // 16. PhotoFrame & Personalise
        if (pkg.contains("photoframe") || pkg.contains("personalise") ||
            simpleName.contains("Photo", ignoreCase = true) || simpleName.contains("Frame", ignoreCase = true) ||
            simpleName.contains("Text", ignoreCase = true) || simpleName.contains("Countdown", ignoreCase = true)
        ) {
            bindPhotoAndPersonaliseWidget(context, views, appWidgetId, accentColor, primaryTextColor, secondaryTextColor)
        }
    }

    // --- 1. HEALTH & PEDOMETER ---
    private fun bindHealthWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        val prefs = context.getSharedPreferences("health_prefs", Context.MODE_PRIVATE)
        val steps = prefs.getInt("daily_steps", 6420)
        val goal = prefs.getInt("steps_goal", 10000)
        val progress = ((steps * 100) / goal).coerceIn(0, 100)

        try {
            views.setTextViewText(R.id.steps_count, String.format(Locale.US, "%,d", steps))
            views.setTextColor(R.id.steps_count, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.steps_label, "STEPS TODAY")
            views.setTextColor(R.id.steps_label, accentColor)
        } catch (_: Exception) {}

        try {
            views.setProgressBar(R.id.progress_bar, 100, progress, false)
        } catch (_: Exception) {}

        try {
            views.setProgressBar(R.id.steps_progress, 100, progress, false)
        } catch (_: Exception) {}

        // Bitmaps for image-based text slots
        try {
            val stepBitmap = drawTextBitmap(context, String.format(Locale.US, "%,d", steps), 72f, textColor)
            views.setImageViewBitmap(R.id.steps_count_image, stepBitmap)
        } catch (_: Exception) {}

        try {
            val labelBitmap = drawTextBitmap(context, "STEPS", 32f, accentColor)
            views.setImageViewBitmap(R.id.steps_label_image, labelBitmap)
        } catch (_: Exception) {}

        try {
            val statusBitmap = drawTextBitmap(context, "$progress% of $goal", 28f, subTextColor)
            views.setImageViewBitmap(R.id.steps_status_image, statusBitmap)
        } catch (_: Exception) {}

        // Water widgets
        try {
            views.setTextViewText(R.id.water_amount, "1.75 L")
            views.setTextColor(R.id.water_amount, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.water_remaining, "Goal: 2.5 L")
            views.setTextColor(R.id.water_remaining, accentColor)
        } catch (_: Exception) {}

        // Health launcher intent
        val healthIntent = Intent(Intent.ACTION_MAIN).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            healthIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.widget_root, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 2. WEATHER & FORECAST ---
    private fun bindWeatherWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        simpleName: String,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        val weatherPrefs = context.getSharedPreferences("WeatherWidgetPrefs", Context.MODE_PRIVATE)
        val cityName = weatherPrefs.getString("city_$appWidgetId", "San Francisco") ?: "San Francisco"

        try {
            views.setTextViewText(R.id.temperature, "24°")
            views.setTextColor(R.id.temperature, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.feels_like_temp, "Feels like 25°")
            views.setTextColor(R.id.feels_like_temp, subTextColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.city_name, cityName)
            views.setTextColor(R.id.city_name, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.cityNameText, cityName)
            views.setTextColor(R.id.cityNameText, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.weather_text, "Partly Cloudy")
            views.setTextColor(R.id.weather_text, subTextColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.low_high_temp, "H: 26°  L: 18°")
            views.setTextColor(R.id.low_high_temp, subTextColor)
        } catch (_: Exception) {}

        try {
            views.setImageViewResource(R.id.weather_icon, R.drawable.ic_weather_partly_cloudy)
        } catch (_: Exception) {}

        // Wind views
        try {
            views.setTextViewText(R.id.wind_direction, "NW")
            views.setTextColor(R.id.wind_direction, accentColor)
            views.setTextViewText(R.id.wind_speed, "14 km/h")
            views.setTextColor(R.id.wind_speed, textColor)
            views.setTextViewText(R.id.wind_info, "14 km/h NW")
            views.setTextColor(R.id.wind_info, textColor)
        } catch (_: Exception) {}

        try {
            val windSpeedBmp = drawTextBitmap(context, "14 km/h", 48f, textColor)
            views.setImageViewBitmap(R.id.wind_speed_bitmap, windSpeedBmp)
            val windDirBmp = drawTextBitmap(context, "NW", 44f, accentColor)
            views.setImageViewBitmap(R.id.wind_direction_bitmap, windDirBmp)
            val windLabelBmp = drawTextBitmap(context, "WIND SPEED", 24f, subTextColor)
            views.setImageViewBitmap(R.id.wind_speed_label_bitmap, windLabelBmp)
        } catch (_: Exception) {}

        // Air Quality & UV
        try {
            views.setTextViewText(R.id.aqi_value, "38")
            views.setTextColor(R.id.aqi_value, textColor)
            views.setTextViewText(R.id.aqi_value_text, "38")
            views.setTextColor(R.id.aqi_value_text, textColor)
            views.setTextViewText(R.id.aqi_level_text, "Good")
            views.setTextColor(R.id.aqi_level_text, accentColor)
            views.setTextViewText(R.id.aqi_label, "AIR QUALITY")
            views.setTextColor(R.id.aqi_label, subTextColor)
        } catch (_: Exception) {}

        // Sunrise & Sunset
        try {
            views.setTextViewText(R.id.sunriseTimeText, "06:14 AM")
            views.setTextColor(R.id.sunriseTimeText, textColor)
            views.setTextViewText(R.id.sunsetTimeText, "06:48 PM")
            views.setTextColor(R.id.sunsetTimeText, textColor)
        } catch (_: Exception) {}

        try {
            val sunriseBmp = drawTextBitmap(context, "06:14 AM", 40f, textColor)
            views.setImageViewBitmap(R.id.sunriseTimeImage, sunriseBmp)
            val sunsetBmp = drawTextBitmap(context, "06:48 PM", 40f, textColor)
            views.setImageViewBitmap(R.id.sunsetTimeImage, sunsetBmp)
        } catch (_: Exception) {}

        // Moonphase
        try {
            views.setTextViewText(R.id.moonPhaseText, "Waxing Gibbous")
            views.setTextColor(R.id.moonPhaseText, accentColor)
        } catch (_: Exception) {}

        if (simpleName.contains("Moon", ignoreCase = true)) {
            try {
                views.setImageViewResource(R.id.iv_widget_other, R.drawable.ic_full_moon)
                views.setImageViewResource(R.id.content_image, R.drawable.ic_full_moon)
            } catch (_: Exception) {}
        }

        // Weather click intent
        val weatherIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://weather.com")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            weatherIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.widget_root, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 3. AI & SEARCH ---
    private fun bindAiWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int
    ) {
        try {
            val askBmp = drawTextBitmap(context, "Ask ChatGPT...", 36f, textColor)
            views.setImageViewBitmap(R.id.chatgpt_text_image, askBmp)
        } catch (_: Exception) {}

        fun urlIntent(url: String, requestCode: Int): PendingIntent {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            return PendingIntent.getActivity(
                context,
                appWidgetId * 100 + requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        try { views.setOnClickPendingIntent(R.id.chatgpt_container, urlIntent("https://chatgpt.com", 1)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.chatgpt_icon, urlIntent("https://chatgpt.com", 1)) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.gemini_container, urlIntent("https://gemini.google.com", 2)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.gemini_icon, urlIntent("https://gemini.google.com", 2)) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.claude_container, urlIntent("https://claude.ai", 3)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.claude_icon, urlIntent("https://claude.ai", 3)) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.grok_container, urlIntent("https://x.com/i/grok", 4)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.grok_icon, urlIntent("https://x.com/i/grok", 4)) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.perplexity_container, urlIntent("https://perplexity.ai", 5)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.perplexity_icon, urlIntent("https://perplexity.ai", 5)) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.deepseek_icon, urlIntent("https://chat.deepseek.com", 6)) } catch (_: Exception) {}

        // Root container fallback to ChatGPT
        try { views.setOnClickPendingIntent(R.id.widget_container, urlIntent("https://chatgpt.com", 1)) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_root, urlIntent("https://chatgpt.com", 1)) } catch (_: Exception) {}
    }

    // --- 4. APP LAUNCHER ---
    private fun bindAppLauncherWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        try {
            views.setTextViewText(R.id.app_launcher_text, "Applications")
            views.setTextColor(R.id.app_launcher_text, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.app_launcher_custom_text, "Launch")
            views.setTextColor(R.id.app_launcher_custom_text, subTextColor)
        } catch (_: Exception) {}

        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            homeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.app_launcher_container, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 5. CALCULATOR ---
    private fun bindCalculatorWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        textColor: Int
    ) {
        try {
            views.setTextViewText(R.id.display, "0")
            views.setTextColor(R.id.display, textColor)
        } catch (_: Exception) {}

        val calcIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_CALCULATOR)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            calcIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.widget_root, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 6. COMPASS ---
    private fun bindCompassWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int
    ) {
        try {
            views.setTextViewText(R.id.label_north, "N")
            views.setTextColor(R.id.label_north, Color.RED)
            views.setTextViewText(R.id.label_south, "S")
            views.setTextColor(R.id.label_south, textColor)
            views.setTextViewText(R.id.label_east, "E")
            views.setTextColor(R.id.label_east, textColor)
            views.setTextViewText(R.id.label_west, "W")
            views.setTextColor(R.id.label_west, textColor)
        } catch (_: Exception) {}

        try {
            val compassBmp = drawCompassBitmap(accentColor, textColor)
            views.setImageViewBitmap(R.id.compass_dial, compassBmp)
            views.setImageViewBitmap(R.id.main_image, compassBmp)
        } catch (_: Exception) {}

        val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            mapsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try { views.setOnClickPendingIntent(R.id.widget_root, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 7. GAMES ---
    private fun bindGamesWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int
    ) {
        try {
            views.setTextViewText(R.id.tv_dice_text, "Roll: 6")
            views.setTextColor(R.id.tv_dice_text, textColor)
            views.setImageViewResource(R.id.iv_dice, R.drawable.ic_dice6)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.game_status, "Tap to Play")
            views.setTextColor(R.id.game_status, accentColor)
        } catch (_: Exception) {}

        val xo = listOf("X", "O", "X", "O", "X", "", "", "O", "")
        for (i in 0..8) {
            val resId = context.resources.getIdentifier("cell_$i", "id", context.packageName)
            if (resId != 0) {
                try {
                    views.setTextViewText(resId, xo[i])
                    views.setTextColor(resId, if (xo[i] == "X") accentColor else textColor)
                } catch (_: Exception) {}
            }
            val tileId = context.resources.getIdentifier("tile_$i", "id", context.packageName)
            if (tileId != 0 && i < 8) {
                try {
                    views.setTextViewText(tileId, "${i + 1}")
                    views.setTextColor(tileId, textColor)
                } catch (_: Exception) {}
            }
        }
    }

    // --- 8. QUOTES, ADVICE, JOKES ---
    private fun bindQuoteWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        val quotes = listOf(
            "Small steps every day lead to big results." to "Anonymous",
            "Focus on the step in front of you, not the whole staircase." to "Proverb",
            "Simplicity is the ultimate sophistication." to "Leonardo da Vinci",
            "The secret of getting ahead is getting started." to "Mark Twain",
            "Action is the foundational key to all success." to "Pablo Picasso"
        )
        val index = (appWidgetId.coerceAtLeast(0)) % quotes.size
        val (quote, author) = quotes[index]

        try {
            views.setTextViewText(R.id.tv_quote_text, "\"$quote\"")
            views.setTextColor(R.id.tv_quote_text, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.tv_author_text, "— $author")
            views.setTextColor(R.id.tv_author_text, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.tv_advice_text, "Take a deep breath and start small.")
            views.setTextColor(R.id.tv_advice_text, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.tv_setup, "Why do programmers prefer dark mode?")
            views.setTextColor(R.id.tv_setup, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.tv_delivery, "Because light attracts bugs!")
            views.setTextColor(R.id.tv_delivery, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.tv_title, "Daily Inspiration")
            views.setTextColor(R.id.tv_title, accentColor)
        } catch (_: Exception) {}
    }

    // --- 9. NOTES & REMINDERS ---
    private fun bindNotesWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        val prefs = context.getSharedPreferences("widget_notes", Context.MODE_PRIVATE)
        val title = prefs.getString("note_title_$appWidgetId", "Quick Note") ?: "Quick Note"
        val content = prefs.getString("note_content_$appWidgetId", "• Drink water\n• Review tasks\n• Take a walk") ?: "• Drink water\n• Review tasks"

        try {
            views.setTextViewText(R.id.notes_title, title)
            views.setTextColor(R.id.notes_title, accentColor)
            views.setTextViewText(R.id.reminders_title, "Reminders")
            views.setTextColor(R.id.reminders_title, accentColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.notes_content, content)
            views.setTextColor(R.id.notes_content, textColor)
            views.setTextViewText(R.id.reminders_content, content)
            views.setTextColor(R.id.reminders_content, textColor)
            views.setTextViewText(R.id.tv_note, content)
            views.setTextColor(R.id.tv_note, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.reminders_count, "3")
            views.setTextColor(R.id.reminders_count, accentColor)
            views.setTextViewText(R.id.reminders_header, "REMINDERS")
            views.setTextColor(R.id.reminders_header, subTextColor)
        } catch (_: Exception) {}
    }

    // --- 10. MUSIC WIDGETS ---
    private fun bindMusicWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        try {
            views.setTextViewText(R.id.widget_music4_title, "Minimal Beats")
            views.setTextColor(R.id.widget_music4_title, textColor)
            views.setTextViewText(R.id.widget_music4_artist, "Now Playing")
            views.setTextColor(R.id.widget_music4_artist, accentColor)

            views.setTextViewText(R.id.widget_music5_title, "Daily Mix")
            views.setTextColor(R.id.widget_music5_title, textColor)
            views.setTextViewText(R.id.widget_music5_artist, "MyUI Audio")
            views.setTextColor(R.id.widget_music5_artist, accentColor)

            views.setTextViewText(R.id.artist_name, "Kendrick Lamar")
            views.setTextColor(R.id.artist_name, accentColor)
            views.setTextViewText(R.id.song_title, "Not Like Us")
            views.setTextColor(R.id.song_title, textColor)
        } catch (_: Exception) {}

        try {
            views.setImageViewResource(R.id.album_art, R.drawable.default_album_art)
            views.setImageViewResource(R.id.album_art2, R.drawable.default_album_art)
            views.setImageViewResource(R.id.album_art3, R.drawable.default_album_art)
        } catch (_: Exception) {}

        // Media key intents
        val playIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE))
        }
        val playPending = PendingIntent.getBroadcast(context, appWidgetId * 100 + 1, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT))
        }
        val nextPending = PendingIntent.getBroadcast(context, appWidgetId * 100 + 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
            putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS))
        }
        val prevPending = PendingIntent.getBroadcast(context, appWidgetId * 100 + 3, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        try { views.setOnClickPendingIntent(R.id.play_pause_button, playPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_music4_play_pause_button, playPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_music5_play_pause_button, playPending) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.next_button, nextPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_music4_next_button, nextPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_music5_next_button, nextPending) } catch (_: Exception) {}

        try { views.setOnClickPendingIntent(R.id.prev_button, prevPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_music4_prev_button, prevPending) } catch (_: Exception) {}
    }

    // --- 11. FOLDERS & SOCIAL ---
    private fun bindFolderWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int
    ) {
        val appPackages = listOf(
            "com.google.android.dialer",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.google.android.GoogleCamera",
            "com.whatsapp",
            "com.google.android.youtube",
            "com.instagram.android",
            "org.telegram.messenger",
            "com.google.android.gm",
            "com.android.settings"
        )

        for (i in 0..9) {
            val pkg = appPackages[i % appPackages.size]
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg) ?: Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = pkg
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pending = PendingIntent.getActivity(
                context,
                appWidgetId * 100 + i,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val slotContainerId = context.resources.getIdentifier("slot_container_$i", "id", context.packageName)
            if (slotContainerId != 0) {
                try { views.setOnClickPendingIntent(slotContainerId, pending) } catch (_: Exception) {}
            }

            val appIconId = context.resources.getIdentifier("app_icon_$i", "id", context.packageName)
            if (appIconId != 0) {
                try { views.setOnClickPendingIntent(appIconId, pending) } catch (_: Exception) {}
            }
        }
    }

    // --- 12. GOOGLE WIDGETS ---
    private fun bindGoogleWidget(context: Context, views: RemoteViews, appWidgetId: Int) {
        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val searchPending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 1,
            searchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val voiceIntent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val voicePending = PendingIntent.getActivity(
            context,
            appWidgetId * 10 + 2,
            voiceIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try { views.setOnClickPendingIntent(R.id.widget_container, searchPending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_root, searchPending) } catch (_: Exception) {}
    }

    // --- 13. SETTINGS & QUICK SETTINGS ---
    private fun bindSettingsWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        simpleName: String,
        accentColor: Int,
        textColor: Int
    ) {
        try {
            views.setTextViewText(R.id.settings_status, "ON")
            views.setTextColor(R.id.settings_status, accentColor)
            views.setTextViewText(R.id.settings_state, "Enabled")
            views.setTextColor(R.id.settings_state, textColor)
            views.setTextViewText(R.id.profile_text, "Normal")
            views.setTextColor(R.id.profile_text, textColor)
        } catch (_: Exception) {}

        val action = when {
            simpleName.contains("Wifi", ignoreCase = true) -> Settings.ACTION_WIFI_SETTINGS
            simpleName.contains("Bluetooth", ignoreCase = true) -> Settings.ACTION_BLUETOOTH_SETTINGS
            simpleName.contains("Sound", ignoreCase = true) || simpleName.contains("Vibrate", ignoreCase = true) -> Settings.ACTION_SOUND_SETTINGS
            simpleName.contains("Airplane", ignoreCase = true) -> Settings.ACTION_AIRPLANE_MODE_SETTINGS
            simpleName.contains("DarkMode", ignoreCase = true) -> Settings.ACTION_DISPLAY_SETTINGS
            simpleName.contains("Location", ignoreCase = true) -> Settings.ACTION_LOCATION_SOURCE_SETTINGS
            simpleName.contains("Alarm", ignoreCase = true) -> AlarmClock.ACTION_SHOW_ALARMS
            simpleName.contains("Hotspot", ignoreCase = true) -> Settings.ACTION_WIRELESS_SETTINGS
            simpleName.contains("Torch", ignoreCase = true) -> MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
            else -> Settings.ACTION_SETTINGS
        }
        val intent = Intent(action).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
        val pending = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try { views.setOnClickPendingIntent(R.id.widget_root, pending) } catch (_: Exception) {}
        try { views.setOnClickPendingIntent(R.id.widget_container, pending) } catch (_: Exception) {}
    }

    // --- 14. PRODUCTIVITY ---
    private fun bindProductivityWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        try {
            views.setTextViewText(R.id.tv_browser_title, "Quick Links")
            views.setTextColor(R.id.tv_browser_title, textColor)
        } catch (_: Exception) {}

        try {
            views.setTextViewText(R.id.timer_text, "25:00")
            views.setTextColor(R.id.timer_text, textColor)
            views.setTextViewText(R.id.status_text, "Focus Session")
            views.setTextColor(R.id.status_text, accentColor)
            views.setProgressBar(R.id.progress_bar, 100, 65, false)
        } catch (_: Exception) {}

        try {
            val screenTimeBmp = drawTextBitmap(context, "3h 42m", 60f, textColor)
            views.setImageViewBitmap(R.id.screentime_time_image, screenTimeBmp)
            val titleBmp = drawTextBitmap(context, "SCREEN TIME", 26f, accentColor)
            views.setImageViewBitmap(R.id.screentime_title_image, titleBmp)
        } catch (_: Exception) {}
    }

    // --- 15. HYBRID CLOCKS ---
    private fun bindHybridWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        val cal = Calendar.getInstance()
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)
        val dateStr = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(cal.time)
        val dayStr = SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time).uppercase()

        try {
            views.setTextViewText(R.id.widget_time, timeStr)
            views.setTextColor(R.id.widget_time, textColor)
            views.setTextViewText(R.id.widget_date, dateStr)
            views.setTextColor(R.id.widget_date, accentColor)
            views.setTextViewText(R.id.day_text, dayStr)
            views.setTextColor(R.id.day_text, accentColor)
            views.setTextViewText(R.id.widget_day, dayStr)
            views.setTextColor(R.id.widget_day, subTextColor)
            views.setTextViewText(R.id.chat_bubble, "Have a great day!")
            views.setTextColor(R.id.chat_bubble, textColor)
        } catch (_: Exception) {}
    }

    // --- 16. PHOTO & PERSONALISE ---
    private fun bindPhotoAndPersonaliseWidget(
        context: Context,
        views: RemoteViews,
        appWidgetId: Int,
        accentColor: Int,
        textColor: Int,
        subTextColor: Int
    ) {
        try {
            views.setTextViewText(R.id.config_text, "Cherish Moments")
            views.setTextColor(R.id.config_text, textColor)
            views.setTextViewText(R.id.custom_text, "Minimal Widgets")
            views.setTextColor(R.id.custom_text, accentColor)
            views.setTextViewText(R.id.custom_text_top, "Daily Life")
            views.setTextColor(R.id.custom_text_top, subTextColor)
            views.setTextViewText(R.id.custom_text_center, "Memories")
            views.setTextColor(R.id.custom_text_center, textColor)
            views.setTextViewText(R.id.custom_text_bottom, "Forever")
            views.setTextColor(R.id.custom_text_bottom, accentColor)
        } catch (_: Exception) {}

        try {
            views.setImageViewResource(R.id.widget_photo, R.drawable.ic_photoframe1)
            views.setImageViewResource(R.id.widget_photo_left, R.drawable.ic_photoframe1)
            views.setImageViewResource(R.id.widget_photo_right, R.drawable.ic_photoframe2)
        } catch (_: Exception) {}

        try {
            val countBmp = drawTextBitmap(context, "42", 90f, accentColor)
            views.setImageViewBitmap(R.id.iv_countdown_number, countBmp)
            val eventBmp = drawTextBitmap(context, "DAYS UNTIL GOAL", 26f, subTextColor)
            views.setImageViewBitmap(R.id.iv_event_name, eventBmp)
            val textBmp = drawTextBitmap(context, "Simplicity is beauty.", 44f, textColor)
            views.setImageViewBitmap(R.id.iv_widget_text, textBmp)
        } catch (_: Exception) {}
    }

    // --- DRAWING HELPERS ---
    private fun drawTextBitmap(
        context: Context,
        text: String,
        textSize: Float,
        textColor: Int
    ): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.textSize = textSize.coerceAtLeast(12f)
            color = textColor
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val bounds = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        val w = (bounds.width() + 30).coerceAtLeast(1)
        val h = (bounds.height() + 30).coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, w / 2f, h / 2f - bounds.exactCenterY(), paint)
        return bitmap
    }

    private fun drawCompassBitmap(accentColor: Int, textColor: Int): Bitmap {
        val size = 260
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = size / 2f
        val cy = size / 2f

        val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#33FFFFFF")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawCircle(cx, cy, cx - 10f, dialPaint)

        // North needle (Red)
        val northPath = Path().apply {
            moveTo(cx, cy - (size * 0.38f))
            lineTo(cx - 14f, cy)
            lineTo(cx + 14f, cy)
            close()
        }
        val northPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E53935")
            style = Paint.Style.FILL
        }
        canvas.drawPath(northPath, northPaint)

        // South needle (Accent/White)
        val southPath = Path().apply {
            moveTo(cx, cy + (size * 0.38f))
            lineTo(cx - 14f, cy)
            lineTo(cx + 14f, cy)
            close()
        }
        val southPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(southPath, southPaint)

        // Center pivot
        val pivotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 8f, pivotPaint)
        return bitmap
    }
}
