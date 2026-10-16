package com.jndapp.minimal.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class QuickNoteEditActivity : AppCompatActivity() {

    private var widgetId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetId = intent?.getIntExtra("appWidgetId", 0) ?: 0
        if (widgetId == 0) {
            finish()
            return
        }

        val prefs = getSharedPreferences("NotesWidgetPrefs", Context.MODE_PRIVATE)
        val existingNote = prefs.getString("note_text_$widgetId", "") ?: ""

        val input = EditText(this).apply {
            setText(existingNote)
            hint = "Enter note…"
            setSelection(text.length)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newNote = input.text.toString()
                prefs.edit().putString("note_text_$widgetId", newNote).apply()

                val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(widgetId))
                }
                sendBroadcast(updateIntent)
                finish()
            }
            .setNegativeButton("Cancel") { _, _ -> finish() }
            .setOnDismissListener { finish() }
            .show()
    }
}
