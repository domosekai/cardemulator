package com.domosekai.cardemulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class RawFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_raw, container, false)

        val saveButton = root.findViewById<Button>(R.id.save_message)
        saveButton.setOnClickListener {
            val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
            try {
                val file =
                    File(context?.getExternalFilesDir(null), "raw-" + sdf.format(Date()) + ".log")
                val os = FileOutputStream(file)
                os.write(HCEService.rawMessage.value?.toByteArray())
                os.close()
                Toast.makeText(context, "Save OK", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Save error: $e", Toast.LENGTH_SHORT).show()
            }
        }

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

        // NestedScrollView supports auto hiding toolbar
        val scrollView = root.findViewById<NestedScrollView>(R.id.raw_scroll)
        val bottomButton = root.findViewById<Button>(R.id.bottom_message)
        bottomButton.setOnClickListener {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }

        val checkbox = root.findViewById<CheckBox>(R.id.check_enable)
        val rawTextView = root.findViewById<TextView>(R.id.raw_message)
        val rawObserver = Observer<String> { new ->
            rawTextView.text = new
        }
        checkbox.setOnCheckedChangeListener { _, b ->
            if (b) {
                HCEService.rawMessage.observe(viewLifecycleOwner, rawObserver)
            } else {
                HCEService.rawMessage.removeObservers(viewLifecycleOwner)
            }
        }
        if (checkbox.isChecked) {
            HCEService.rawMessage.observe(viewLifecycleOwner, rawObserver)
        } else {
            HCEService.rawMessage.removeObservers(viewLifecycleOwner)
        }
        return root
    }

}