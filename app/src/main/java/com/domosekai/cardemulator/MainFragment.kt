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
    private lateinit var cu_more: LinearLayout
    private lateinit var tu_more: LinearLayout
    private lateinit var cu_generic: LinearLayout
    private lateinit var tu_generic: LinearLayout
    private lateinit var metro: LinearLayout

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
        cu_more = root.findViewById(R.id.cu_more)
        tu_more = root.findViewById(R.id.tu_more)
        cu_generic = root.findViewById(R.id.cu_generic)
        tu_generic = root.findViewById(R.id.tu_generic)
        metro = root.findViewById(R.id.metro)

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

        // CU more
        val cuIssuer = root.findViewById<EditText>(R.id.cu_issuer)
        HCEService.cuIssuer = cuIssuer.text.toString()
        val cuAddButton = root.findViewById<Button>(R.id.cu_add)
        cuAddButton.setOnClickListener {
            val line = inflater.inflate(R.layout.custom_data, container, false)
            cu_more.addView(line, cu_more.childCount - 1)
        }
        val cuClearButton = root.findViewById<Button>(R.id.cu_clear)
        cuClearButton.setOnClickListener {
            cu_more.removeViews(1, cu_more.childCount - 2)
            HCEService.cuCustom = emptyMap()
        }
        val cuApplyButton = root.findViewById<Button>(R.id.cu_apply)
        cuApplyButton.setOnClickListener {
            val map = mutableMapOf<String, String>()
            var i = 1
            while (i < cu_more.childCount - 1) {
                val q = cu_more.getChildAt(i).findViewById<EditText>(R.id.custom_query)
                val r = cu_more.getChildAt(i).findViewById<EditText>(R.id.custom_result)
                if (q.text.toString() != "" && r.text.toString() != "") {
                    map[q.text.toString().toUpperCase()] = r.text.toString().toUpperCase()
                }
                i++
            }
            HCEService.cuCustom = map
        }

        // TU more
        val tuIssuer = root.findViewById<EditText>(R.id.tu_issuer)
        HCEService.tuIssuer = tuIssuer.text.toString()
        val tuAddButton = root.findViewById<Button>(R.id.tu_add)
        tuAddButton.setOnClickListener {
            val line = inflater.inflate(R.layout.custom_data, container, false)
            tu_more.addView(line, tu_more.childCount - 1)
        }
        val tuClearButton = root.findViewById<Button>(R.id.tu_clear)
        tuClearButton.setOnClickListener {
            tu_more.removeViews(1, tu_more.childCount - 2)
            HCEService.tuCustom = emptyMap()
        }
        val tuApplyButton = root.findViewById<Button>(R.id.tu_apply)
        tuApplyButton.setOnClickListener {
            val map = mutableMapOf<String, String>()
            var i = 1
            while (i < tu_more.childCount - 1) {
                val q = tu_more.getChildAt(i).findViewById<EditText>(R.id.custom_query)
                val r = tu_more.getChildAt(i).findViewById<EditText>(R.id.custom_result)
                if (q.text.toString() != "" && r.text.toString() != "") {
                    map[q.text.toString().toUpperCase()] = r.text.toString().toUpperCase()
                }
                i++
            }
            HCEService.tuCustom = map
        }

        // Metro stations
        val metroLine = root.findViewById<EditText>(R.id.metro_line)
        val lineWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                HCEService.metroLine = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        metroLine.addTextChangedListener(lineWatcher)
        val metroStation = root.findViewById<EditText>(R.id.metro_station)
        val stationWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                HCEService.metroStation = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        metroStation.addTextChangedListener(stationWatcher)
        val metroRemark = root.findViewById<EditText>(R.id.metro_remark)
        val remarkWatcher = object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                HCEService.metroRemark = p0.toString()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        metroRemark.addTextChangedListener(remarkWatcher)
        val rg = root.findViewById<RadioGroup>(R.id.radio_gate)
        rg.setOnCheckedChangeListener { radioGroup, i ->
            HCEService.inGate = i == R.id.radio_exit
        }

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
                cu_more.visibility =
                    if (p2 > 0) View.VISIBLE else View.GONE
                cu_generic.visibility =
                    if (p0.getItemAtPosition(p2) == "CU") View.VISIBLE else View.GONE
            }
            spinner2 -> {
                when (p0.getItemAtPosition(p2)) {
                    "None" -> HCEService.tuType = 0
                    "TU BJ" -> HCEService.tuType = TU_BJ
                    "TU" -> HCEService.tuType = TU_GEN
                }
                tu_more.visibility =
                    if (p2 > 0) View.VISIBLE else View.GONE
                tu_generic.visibility =
                    if (p0.getItemAtPosition(p2) == "TU") View.VISIBLE else View.GONE
            }
            spinner3 -> {
                when (p0.getItemAtPosition(p2)) {
                    "SH Metro" -> {
                        spinner1.setSelection(8, true)
                        spinner2.setSelection(0, true)
                    }
                    "TU Metro" -> {
                        spinner1.setSelection(0, true)
                        spinner2.setSelection(2, true)
                    }
                }
                psm.visibility =
                    if (p0.getItemAtPosition(p2) == "PSM Scan") View.VISIBLE else View.GONE
                if (p0.getItemAtPosition(p2).toString().contains("Metro")) {
                    metro.visibility = View.VISIBLE
                    HCEService.metro = true
                } else {
                    metro.visibility = View.GONE
                    HCEService.metro = false
                }
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {

    }
}