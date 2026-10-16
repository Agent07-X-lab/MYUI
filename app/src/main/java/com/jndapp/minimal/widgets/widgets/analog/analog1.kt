package com.jndapp.minimal.widgets.widgets.analog

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

class analog1 : BaseAnalogWidget() {

    override fun getLayoutId(): Int = R.layout.analog1_layout

    override fun configureViews(context: Context, views: RemoteViews, appWidgetManager: AppWidgetManager) {
        val studioPrefs = context.getSharedPreferences("studio_prefs", Context.MODE_PRIVATE)
        val widgetPrefs = context.getSharedPreferences("widget_settings", Context.MODE_PRIVATE)

        val accentColor = studioPrefs.getInt("theme_accent_color", Color.parseColor("#FFBF00"))
        val bgColor = studioPrefs.getInt("theme_background_color", Color.parseColor("#000000"))
        val opacity = widgetPrefs.getFloat("widget_opacity", 1.0f)

        views.setInt(R.id.widget_container, "setBackgroundColor", bgColor)
        views.setFloat(R.id.widget_container, "setAlpha", opacity.coerceIn(0.0f, 1.0f))

        val accentColorList = ColorStateList.valueOf(accentColor)
        val whiteColorList = ColorStateList.valueOf(Color.WHITE)

        views.setColorStateList(R.id.analog_clock, "setDialTintList", accentColorList)
        views.setColorStateList(R.id.analog_clock, "setHourHandTintList", whiteColorList)
        views.setColorStateList(R.id.analog_clock, "setMinuteHandTintList", whiteColorList)
        views.setColorStateList(R.id.analog_clock, "setSecondHandTintList", accentColorList)

        views.setTextColor(R.id.daydate, Color.WHITE)
        views.setTextViewText(R.id.daydate, getFormattedDayDate())
    }
}
