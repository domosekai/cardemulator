package com.domosekai.cardemulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

class RawFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_raw, container, false)
        val clearButton = root.findViewById<Button>(R.id.clear_message)
        clearButton.setOnClickListener {
            HCEService.rawMessage.value = ""
            HCEService.commands = ""
        }

        val copyCommandsButton = root.findViewById<Button>(R.id.copy_commands)
        copyCommandsButton.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData =
                ClipData.newPlainText("simple text", HCEService.commands)
            clipboard.setPrimaryClip(clip)
        }

        val copyAllButton = root.findViewById<Button>(R.id.copy_message)
        copyAllButton.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("simple text", HCEService.rawMessage.value)
            clipboard.setPrimaryClip(clip)
        }

        val scrollView = root.findViewById<ScrollView>(R.id.raw_scroll)
        val bottomButton = root.findViewById<Button>(R.id.bottom_message)
        bottomButton.setOnClickListener {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }

        val rawTextView = root.findViewById<TextView>(R.id.raw_message)
        val rawObserver = Observer<String> { new ->
            rawTextView.text = new
        }
        HCEService.rawMessage.observe(this, rawObserver)
        return root
    }

}