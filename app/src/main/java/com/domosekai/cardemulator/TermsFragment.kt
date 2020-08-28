package com.domosekai.cardemulator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer

class TermsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_terms, container, false)
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

        val scrollView = root.findViewById<ScrollView>(R.id.terms_scroll)
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

}