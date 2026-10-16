package com.jndapp.minimal.widgets.widgets.analog

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.RemoteViews
import com.jndapp.minimal.widgets.R

open class GenericAnalogWidget(private val layoutResId: Int) : BaseAnalogWidget() {
    override fun getLayoutId(): Int = layoutResId

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

class analog2 : GenericAnalogWidget(R.layout.analog2_layout)
class analog3 : GenericAnalogWidget(R.layout.analog3_layout)
class analog4 : GenericAnalogWidget(R.layout.analog4_layout)
class analog5 : GenericAnalogWidget(R.layout.analog5_layout)
class analog6 : GenericAnalogWidget(R.layout.analog6_layout)
class analog7 : GenericAnalogWidget(R.layout.analog7_layout)
class analog8 : GenericAnalogWidget(R.layout.analog8_layout)
class analog9 : GenericAnalogWidget(R.layout.analog9_layout)
class analog10 : GenericAnalogWidget(R.layout.analog10_layout)
class analog11 : GenericAnalogWidget(R.layout.analog11_layout)
class analog12 : GenericAnalogWidget(R.layout.analog12_layout)
class analog13 : GenericAnalogWidget(R.layout.analog13_layout)
class analog14 : GenericAnalogWidget(R.layout.analog14_layout)
class analog15 : GenericAnalogWidget(R.layout.analog15_layout)
class analog16 : GenericAnalogWidget(R.layout.analog16_layout)
class analog17 : GenericAnalogWidget(R.layout.analog17_layout)
class analog18 : GenericAnalogWidget(R.layout.analog18_layout)
class analog19 : GenericAnalogWidget(R.layout.analog19_layout)
class analog20 : GenericAnalogWidget(R.layout.analog20_layout)
class analog21 : GenericAnalogWidget(R.layout.analog21_layout)
class analog22 : GenericAnalogWidget(R.layout.analog22_layout)
class analog23 : GenericAnalogWidget(R.layout.analog23_layout)
class analog24 : GenericAnalogWidget(R.layout.analog24_layout)
class analog25 : GenericAnalogWidget(R.layout.analog25_layout)
class analog26 : GenericAnalogWidget(R.layout.analog26_layout)
class analog27 : GenericAnalogWidget(R.layout.analog27_layout)
class analog28 : GenericAnalogWidget(R.layout.analog28_layout)
class analog29 : GenericAnalogWidget(R.layout.analog29_layout)
class analog30 : GenericAnalogWidget(R.layout.analog30_layout)
class analog31 : GenericAnalogWidget(R.layout.analog31_layout)
