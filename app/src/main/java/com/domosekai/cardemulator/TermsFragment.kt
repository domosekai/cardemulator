package com.domosekai.cardemulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class TermsFragment : Fragment() {

    private lateinit var rg: RadioGroup
    private lateinit var rg2: RadioGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_terms, container, false)
        Log.i("TermsFragment", "created view with ${HCEService.cuOnly} ${HCEService.tuOnly}")

        val saveButton = root.findViewById<Button>(R.id.save_terms)
        saveButton.setOnClickListener {
            val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
            try {
                val file =
                    File(context?.getExternalFilesDir(null), "terms-" + sdf.format(Date()) + ".log")
                val os = FileOutputStream(file)
                os.write(HCEService.terminals.value?.toByteArray())
                os.close()
                Toast.makeText(context, "Save OK", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Save error: $e", Toast.LENGTH_SHORT).show()
            }
        }

        val clearButton = root.findViewById<Button>(R.id.clear_terms)
        clearButton.setOnClickListener {
            HCEService.terminals.value = ""
        }

        val copyAllButton = root.findViewById<Button>(R.id.copy_terms)
        copyAllButton.setOnClickListener {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("simple text", HCEService.terminals.value)
            clipboard.setPrimaryClip(clip)
        }

        rg = root.findViewById<RadioGroup>(R.id.radio_gate)
        rg2 = root.findViewById<RadioGroup>(R.id.radio_card)

        val scrollView = root.findViewById<NestedScrollView>(R.id.terms_scroll)
        val bottomButton = root.findViewById<Button>(R.id.bottom_terms)
        bottomButton.setOnClickListener {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }

        val termsTextView = root.findViewById<TextView>(R.id.terms_message)
        val termsObserver = Observer<String> { new ->
            termsTextView.text = new
        }
        HCEService.terminals.observe(this, termsObserver)
        return root
    }

    override fun onResume() {
        super.onResume()
        // view states restored after onCreatedView, so do not register onChange listeners there
        // otherwise listeners will get fired when states restored
        if (HCEService.cuOnly) rg2.check(R.id.radio_cu)
        else if (HCEService.tuOnly) rg2.check(R.id.radio_tu)
        rg2.setOnCheckedChangeListener { radioGroup, i ->
            Log.i("TermsFragment", "clicked")
            HCEService.cuOnly = i == R.id.radio_cu
            HCEService.tuOnly = i == R.id.radio_tu
        }
        if (HCEService.inGate) rg.check(R.id.radio_exit) else rg.check(R.id.radio_entrance)
        rg.setOnCheckedChangeListener { radioGroup, i ->
            HCEService.inGate = i == R.id.radio_exit
        }
    }

}