package com.jndapp.minimal.widgets.widgets.contacts

import android.content.Context
import android.graphics.*
import java.io.File

object ContactWidgetHelper {

    fun getContactPhotoBitmap(context: Context, appWidgetId: Int, slot: Int = 0): Bitmap? {
        val file = File(context.filesDir, "contact_photo_${appWidgetId}_$slot.png")
        if (file.exists()) {
            try {
                val orig = BitmapFactory.decodeFile(file.absolutePath)
                if (orig != null) return getCircularBitmap(orig)
            } catch (_: Exception) {}
        }
        return null
    }

    fun getRawContactPhotoBitmap(context: Context, appWidgetId: Int, slot: Int = 0): Bitmap? {
        val file = File(context.filesDir, "contact_photo_${appWidgetId}_$slot.png")
        if (file.exists()) {
            try {
                return BitmapFactory.decodeFile(file.absolutePath)
            } catch (_: Exception) {}
        }
        return null
    }

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = Math.min(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)
        canvas.drawOval(rectF, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2
        val srcRect = Rect(left, top, left + size, top + size)
        canvas.drawBitmap(bitmap, srcRect, rect, paint)
        return output
    }

    fun createInitialAvatarBitmap(name: String, bgColor: Int, textColor: Int = Color.WHITE): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = bgColor
            style = Paint.Style.FILL
        }
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        val initial = if (name.isNotBlank()) name.trim().first().uppercase() else "C"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            this.textSize = size * 0.44f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val bounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, bounds)
        canvas.drawText(initial, size / 2f, size / 2f - bounds.exactCenterY(), textPaint)
        return bitmap
    }
}
