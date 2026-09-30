package com.gulshan.ai

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val textView = TextView(this).apply {
            text = "Gulshan AI\n\nNamaste! Main Gulshan hoon."
            textSize = 24f
            setPadding(40, 80, 40, 40)
        }

        setContentView(textView)
    }
}
