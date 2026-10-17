package com.jndapp.minimal.widgets.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.widget.RemoteViews

enum class WidgetRatio(val widthRatio: Float, val heightRatio: Float) {
    SQUARE(1.0f, 1.0f),
    GRID_1X1(1.0f, 1.0f),
    GRID_1X2(1.0f, 2.3f),
    GRID_2X1(2.3f, 1.0f),
    GRID_2X2(2.3f, 2.3f),
    GRID_2X3(2.3f, 3.6f),
    GRID_3X1(3.6f, 1.0f),
    GRID_3X2(3.6f, 2.3f),
    GRID_3X4(3.6f, 4.9f),
    GRID_4X1(4.9f, 1.0f),
    GRID_4X2(4.9f, 2.3f),
    GRID_4X4(4.9f, 4.9f),
    RATIO_2X1(2.3f, 1.0f),
    RATIO_2_2_1(2.3f, 1.0f),
    LANDSCAPE_3_2(3.6f, 2.3f)
}

data class WidgetDimensions(
    val widthDp: Int,
    val heightDp: Int,
    val targetDimensionDp: Int,
    val widthPaddingPx: Int,
    val heightPaddingPx: Int,
    val targetDimensionPx: Float,
    val density: Float
)

object WidgetScaleHelper {
    private const val TAG = "WidgetScaleHelper"

    fun applyWidgetScale(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        rootViewId: Int,
        ratio: WidgetRatio = WidgetRatio.SQUARE
    ): WidgetDimensions {
        val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
        val density = context.resources.displayMetrics.density

        var minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH) ?: 0
        var minHeight = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT) ?: 0

        if (minWidth <= 0 || minHeight <= 0) {
            val info = appWidgetManager.getAppWidgetInfo(appWidgetId)
            if (info != null) {
                minWidth = if (minWidth <= 0) (info.minWidth / density).toInt().coerceAtLeast(100) else minWidth
                minHeight = if (minHeight <= 0) (info.minHeight / density).toInt().coerceAtLeast(100) else minHeight
            } else {
                minWidth = 180
                minHeight = 180
            }
        }

        val prefs = context.getSharedPreferences("widget_ratio_preferences", Context.MODE_PRIVATE)
        val isFixedRatio = prefs.getBoolean("widget_fixed_ratio_$appWidgetId", false)

        val dimensions: WidgetDimensions
        if (isFixedRatio) {
            val targetRatio = ratio.widthRatio / ratio.heightRatio
            val currentRatio = minWidth.toFloat() / minHeight.toFloat()
            val (targetW, targetH) = if (currentRatio > targetRatio) {
                Pair((minHeight * targetRatio).toInt(), minHeight)
            } else {
                Pair(minWidth, (minWidth / targetRatio).toInt())
            }

            val padX = (((minWidth - targetW) / 2f) * density).toInt().coerceAtLeast(0)
            val padY = (((minHeight - targetH) / 2f) * density).toInt().coerceAtLeast(0)

            try {
                views.setViewPadding(rootViewId, padX, padY, padX, padY)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to set view padding: ${e.message}")
            }

            val targetDim = minOf(targetW, targetH)
            dimensions = WidgetDimensions(
                widthDp = minWidth,
                heightDp = minHeight,
                targetDimensionDp = targetDim,
                widthPaddingPx = padX,
                heightPaddingPx = padY,
                targetDimensionPx = targetDim * density,
                density = density
            )
        } else {
            try {
                views.setViewPadding(rootViewId, 0, 0, 0, 0)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to reset view padding: ${e.message}")
            }

            val targetDim = minOf(minWidth, minHeight)
            dimensions = WidgetDimensions(
                widthDp = minWidth,
                heightDp = minHeight,
                targetDimensionDp = targetDim,
                widthPaddingPx = 0,
                heightPaddingPx = 0,
                targetDimensionPx = targetDim * density,
                density = density
            )
        }

        return dimensions
    }

    fun setViewSizePx(views: RemoteViews, viewId: Int, sizePx: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                views.setViewLayoutWidth(viewId, sizePx, TypedValue.COMPLEX_UNIT_PX)
                views.setViewLayoutHeight(viewId, sizePx, TypedValue.COMPLEX_UNIT_PX)
            } catch (e: Exception) {
                Log.w(TAG, "Could not set view size on Android 12+: ${e.message}")
            }
        }
    }
}
