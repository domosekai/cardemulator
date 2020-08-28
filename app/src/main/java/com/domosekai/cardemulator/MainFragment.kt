package com.domosekai.cardemulator

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment

class MainFragment : Fragment(), AdapterView.OnItemSelectedListener {

    private lateinit var spinner1: Spinner
    private lateinit var spinner2: Spinner
    private lateinit var spinner3: Spinner
    private lateinit var psm: ConstraintLayout
    private lateinit var cu_generic: LinearLayout
    private lateinit var tu_generic: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_main, container, false)
        spinner1 = root.findViewById(R.id.card_type1)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            requireContext(), R.array.card_type1, android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner1.adapter = adapter
            spinner1.onItemSelectedListener = this
        }
        spinner2 = root.findViewById(R.id.card_type2)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.card_type2, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner2.adapter = adapter
            spinner2.onItemSelectedListener = this
        }
        spinner3 = root.findViewById(R.id.special_feature)
        ArrayAdapter.createFromResource(
            requireContext(), R.array.features, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner3.adapter = adapter
            spinner3.onItemSelectedListener = this
        }
        psm = root.findViewById(R.id.psm_scan)
        cu_generic = root.findViewById(R.id.cu_generic)
        tu_generic = root.findViewById(R.id.tu_generic)

        // PSM Scan
        val pos = root.findViewById<EditText>(R.id.pos)
        val minus10 = root.findViewById<Button>(R.id.minus10)
        val plus10 = root.findViewById<Button>(R.id.plus10)
        val minus50 = root.findViewById<Button>(R.id.minus50)
        val plus50 = root.findViewById<Button>(R.id.plus50)
        HCEService.prefix = pos.text.toString().take(7)
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
                HCEService.prefix = p0.toString().take(7)
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        pos.addTextChangedListener(posWatcher)

        // CU generic card
        val cuIssuer = root.findViewById<EditText>(R.id.cu_issuer)
        HCEService.cuIssuer = cuIssuer.text.toString()
        val cuWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                HCEService.cuIssuer = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        cuIssuer.addTextChangedListener(cuWatcher)

        // TU generic card
        val tuIssuer = root.findViewById<EditText>(R.id.tu_issuer)
        HCEService.tuIssuer = tuIssuer.text.toString()
        val tuWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                HCEService.tuIssuer = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        tuIssuer.addTextChangedListener(tuWatcher)

        return root
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        when (p0) {
            spinner1 -> {
                when (p0.getItemAtPosition(p2)) {
                    "None" -> HCEService.cardType = 0
                    "YCT" -> HCEService.cardType = TYPE_YCT
                    "TFT" -> HCEService.cardType = TYPE_TFT
                    "SZT" -> HCEService.cardType = TYPE_SZT
                    "CU" -> HCEService.cardType = TYPE_CU
                    "Suzhou" -> HCEService.cardType = TYPE_SUZ
                    "Suzhou Cika" -> HCEService.cardType = TYPE_SUZ_CIKA
                    "BJ" -> HCEService.cardType = TYPE_BJ
                    "SPTC" -> HCEService.cardType = TYPE_SPTC
                    "Zhenjiang" -> HCEService.cardType = TYPE_ZHENJIANG
                }
                cu_generic.visibility =
                    if (p0.getItemAtPosition(p2) == "CU") View.VISIBLE else View.GONE
            }
            spinner2 -> {
                when (p0.getItemAtPosition(p2)) {
                    "None" -> HCEService.tuType = 0
                    "TU BJ" -> HCEService.tuType = TU_BJ
                    "TU" -> HCEService.tuType = TU_GEN
                }
                tu_generic.visibility =
                    if (p0.getItemAtPosition(p2) == "TU") View.VISIBLE else View.GONE
            }
            spinner3 -> {
                psm.visibility =
                    if (p0.getItemAtPosition(p2) == "PSM Scan") View.VISIBLE else View.GONE
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}