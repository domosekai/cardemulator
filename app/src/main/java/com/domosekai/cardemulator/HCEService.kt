package com.domosekai.cardemulator

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class HCEService : HostApduService() {

    companion object {
        const val TAG = "Host Card Emulator"
        const val STATUS_SUCCESS = "9000"
        const val STATUS_FAILED = "6A82"
        const val CLA_NOT_SUPPORTED = "6E00"
        const val INS_NOT_SUPPORTED = "6D00"
    }

    private var cardType = 0

    override fun onDeactivated(reason: Int) {
        Log.i(TAG, "Deactivated: $reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return hexStringToByteArray(STATUS_FAILED)
        }
        val hexCommandApdu = byteArrayToHexString(commandApdu)

        Log.i(TAG, "Query: $hexCommandApdu")
        val tran = Transaction()
        tran.txQuery = hexCommandApdu
        var result = ""
        when (hexCommandApdu.substring(0, 4)) {
            // 1 YCT, 2 BJ TU, 3 Suzhou, 4 Shenzhen, 5 BJ OLD, 6 CU Fake, 7 Nantong TU
            "00A4" -> when (hexCommandApdu) {
                "00A4040008A000000632010105", "00A4040008A00000063201010500" -> {
                    tran.txResult =
                        "6F318408A000000632010105A5259F0801029F0C1E01011000FFFFFFFF020103105170010170907984201908062040123100009000"
                    cardType = 2
                }
                "00A404001091560000144D4F542E424D4143303031", "00A404001091560000144D4F542E424D414330303100" ->
                    tran.txResult = "6F12841091560000144D4F542E424D41433030319000"
                "00A40000021001", "00A40400075041592E535A54", "00A40400075041592E535A5400" -> {
                    tran.txResult =
                        "6F3284075041592E535A54A5279F0801029F0C200000000000000000FD44000051800000E63C1D292017120120271201101000009000"
                    cardType = 4
                }
                "00A40400085041592E41505059", "00A40400085041592E4150505900" -> {
                    tran.txResult = "6F0A84085041592E415050599000"
                    cardType = 1
                }
                /*"00A40000023F00" -> {
                    tran.txResult = "6F15840E315041592E5359532E4444463031A5038801019000"
                }*/
                "00A404000B535558494E2E4444463031", "00A404000B535558494E2E444446303100" -> {
                    tran.txResult =
                        "6F30840B535558494E2E4444463031A5219F0C1E21500909283991510202860000215000283991512019010120501231FFFF9000"
                    cardType = 3
                }
                "00A40400085041592E5449434C", "00A40400085041592E5449434C36" ->
                    tran.txResult =
                        "6F3484085041592E5449434CA5289F0801029F0C21FFFFFFFFFFFFFFFF000000000000000000000000000000002019120700000186A09000"
                "00A4000002ADF1" ->
                    tran.txResult = "6F0A84085041592E4D4631586A81"
                "00A40400085041592E4D463158" ->
                    tran.txResult = "6F0A84085041592E4D4631586A81"
                /*"00A4040009A00000000386980701", "00A4040009A0000000038698070100" -> {
                    tran.txResult = "6F328409A00000000386980701A5259F0801029F0C1E869820007590FFFF820520007D93847E0526E0B8201807242024101602149000"
                    cardType = 6
                }
                "00A4040008A000000632010105", "00A4040008A00000063201010500" -> {
                    tran.txResult = "9000"
                    cardType = 7
                }*/
            }
            "0020" -> {
                // suzhou
                tran.txResult = "9000"
            }
            "00B0" -> {
                // switch by P1, P2 is offset (because bit 8 of P1 is 1)
                result = when (hexCommandApdu.substring(4, 6)) {
                    // 0x15
                    "95" -> when (cardType) {
                        1 -> "FFFFFFFFFFFFFFFF5100000756695739030002FFFFFFFF201509162025123120150916218121010100000000052B000101015100000756695739030000000000FF8000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                        2 -> "01011000FFFFFFFF02010310517001017090798420190806204012310000"
                        3 -> "21500909283991510202860000215000283991512019010120501231FFFF"
                        4 -> "0000000000000000FD44000051800000D63C1D29201712012027120110100000"
                        6 -> "869820007590FFFF820520007D93847E0526E0B820180724202410160214"
                        7 -> "12343060FFFFFFFF02010310517001017090798420190806204012310000"
                        else -> ""
                    }
                    // 0x11
                    "91" ->
                        "00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    // 0x16
                    "96" ->
                        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    // 0x04 beijing old
                    "84" ->
                        "1000751136141285060200300000000000000000000000002018030920240309000000000000009156000014FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    // 0x05 beijing old
                    "85" ->
                        "000000001400000000302001141204000100FFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    else -> ""
                }
                if (commandApdu.count() == 5) {
                    val offset = commandApdu[3].toInt() and 0xFF
                    if (result.length / 2 > offset) {
                        result = result.substring(offset * 2)
                    }
                }
            }
            "00B2" ->
                // switch by P1 P2
                result = when (cardType) {
                    1 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x08 R1
                        "0144" -> "1814001912010E0F0000000000000000000000000000"
                        // 0x08 R17 (last 3 bits of 0 means P1 is identifier: find first C8 in 0x08)
                        // 0x08 shl 3 = 0x40
                        "1144", "C840" ->
                            //"C8540000000102003BCEC2EE010000169261" + posList[1] + "04101610" + posList[1] + "0410163BCEC2EE010000169261000000000000000000000000000000000000000000000000000000000000000000000000000000000000000014105C"
                            "C8540000000002003BCEB67101000020061830010430062002150430103BCE9E63010000203169000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000C4E7"
                        // 0x18
                        "01C4" -> "000A00000000000299090100" + prefix + "002BC1207112517"
                        "02C4" -> "000900000000000000090100" + prefix + "100001207095711"
                        "03C4" -> "000800000000000299090100" + prefix + "202BC1207095717"
                        "04C4" -> "000700000000000000090100" + prefix + "300001207095311"
                        "05C4" -> "000600000000000000090100" + prefix + "400001207095311"
                        "06C4" -> "000500000000000000090100" + prefix + "500001207095311"
                        "07C4" -> "000400000000000000090100" + prefix + "600001207095311"
                        "08C4" -> "000300000000000000090100" + prefix + "700001207095311"
                        "09C4" -> "000200000000000000090100" + prefix + "800001207095311"
                        "0AC4" -> "000100000000000000090100" + prefix + "900001207095311"
                        "0BC4" -> "0002000000000013880288012002549113881207095314"
                        "0CC4" -> "0001000000000000000200001600202920190715085953"
                        else -> ""
                    }
                    2 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x02 R1
                        "0114" -> "0112060000000100000000000000000000000000"
                        // 0x03 R1
                        "011C" -> "013020011320320627200110001DB00000000000000000000000000000000000000000000000000000000000000000000000"
                        // 0x18...
                        else -> ""
                    }
                    4 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x08 R1
                        "0144" -> "011E0000B1A0AD0F486A484BD600304F00000000000000000000000000000000"
                        // 0x18
                        "01C4" -> "001A000000000000A00900003101165220191024184245"
                        "02C4" -> "0019000000000002EE0900008600135920191024173608"
                        "03C4" -> "0018000000000002EE0900008600235920191024173608"
                        "04C4" -> "0017000000000002EE0900008600335920191024173608"
                        "05C4" -> "0016000000000002EE0900008600435920191024173608"
                        "06C4" -> "0015000000000002EE0900008600535920191024173608"
                        "07C4" -> "0014000000000002EE0900008600635920191024173608"
                        "08C4" -> "0013000000000002EE0900008600735920191024173608"
                        "09C4" -> "0012000000000002EE0900008600835920191024173608"
                        "0AC4" -> "0011000000000002EE0900008600935920191024173608"
                        else -> ""
                    }
                    else -> ""
                }
            // balance
            "805C" -> when (hexCommandApdu.substring(4, 8)) {
                "0502" -> result = when (cardType) {
                    1 -> "000000640007A1200000000000000000"
                    2 -> "00000B220007A1200000000000000000"
                    4 -> ""
                    else -> "000000640007A1200000000000000000"
                }
                "0001", "0002" -> result = when (cardType) {
                    1 -> "00000064"
                    2 -> "00000B22"
                    3 -> "00000064"
                    4 -> "80000064"
                    6 -> "00000064"
                    else -> "00000064"
                }
            }
            "C4FE" -> result = "41070B16BEF8F42110010184"
            //"C4FE" -> result = "BA9A8B9F34000035C0490032"
            // transaction #
            "C4FB" -> result = "0002000A"
        }

        if (commandApdu.count() == 5) {
            // truncate according to Le
            val length = commandApdu[4].toInt() and 0xFF
            if (length > 0 && result.length / 2 > length) {
                result = result.take(length * 2)
            }
        }

        if (result != "") {
            tran.txResult = result + STATUS_SUCCESS
        }
        Log.i(TAG, "Result: " + (tran.txResult ?: "null"))
        return hexStringToByteArray(tran.txResult ?: STATUS_FAILED)

    }
}