package com.domosekai.cardemulator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var pos: EditText
    private lateinit var minus10: Button
    private lateinit var plus10: Button
    private lateinit var minus50: Button
    private lateinit var plus50: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        pos = findViewById(R.id.pos)
        minus10 = findViewById(R.id.minus10)
        plus10 = findViewById(R.id.plus10)
        minus50 = findViewById(R.id.minus50)
        plus50 = findViewById(R.id.plus50)
        prefix = pos.text.toString().take(7)
        minus10.setOnClickListener {
            pos.setText((pos.text.toString().toInt() - 10).toString().padStart(8, '0'))
        }
        plus10.setOnClickListener {
            pos.setText((pos.text.toString().toInt() + 10).toString().padStart(8, '0'))
        }
        minus50.setOnClickListener {
            pos.setText((pos.text.toString().toInt() - 50).toString().padStart(8, '0'))
        }
        plus50.setOnClickListener {
            pos.setText((pos.text.toString().toInt() + 50).toString().padStart(8, '0'))
        }
        val posWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                prefix = p0.toString().take(7)
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        pos.addTextChangedListener(posWatcher)
    }
}
