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
            // 1 YCT, 2 BJ TU, 3 Suzhou, 4 Shenzhen, 5 BJ OLD, 6 CU Fake, 7 Nantong TU, 8 TFT, 9 TFT Cika, 10 SPTC, 11 Suzhou Cika
            "00A4" -> when (hexCommandApdu) {
                /*"00A4040008A000000632010105", "00A4040008A00000063201010500" -> {
                    tran.txResult =
                        "6F318408A000000632010105A5259F0801029F0C1E01011000FFFFFFFF020103105170010170907984201908062040123100009000"
                    cardType = 2
                }*/
                /*"00A404001091560000144D4F542E424D4143303031", "00A404001091560000144D4F542E424D414330303100" ->
                    tran.txResult = "6F12841091560000144D4F542E424D41433030319000"
                "00A40000021001", "00A40400075041592E535A54", "00A40400075041592E535A5400" -> {
                    tran.txResult =
                        "6F3284075041592E535A54A5279F0801029F0C200000000000000000FD44000051800000E63C1D292017120120271201101000009000"
                    cardType = 4
                }*/
                /*"00A40400085041592E41505059", "00A40400085041592E4150505900" -> {
                    tran.txResult = "6F0A84085041592E415050599000"
                    cardType = 1
                }
                "00A40400085041592E5449434C", "00A40400085041592E5449434C36" ->
                    tran.txResult =
                        "6F3484085041592E5449434CA5289F0801029F0C21FFFFFFFFFFFFFFFF000000000000000000000000000000002019120700000186A09000"
                "00A40000023F00" -> {
                    tran.txResult = "6F15840E315041592E5359532E4444463031A5038801019000"
                }*/
                /*"00A404000B535558494E2E4444463031", "00A404000B535558494E2E444446303100", "00A4000002DF01" -> {
                    tran.txResult =
                        "6F30840B535558494E2E4444463031A5219F0C1E21500909283991510202860000215000283991512019010120501231FFFF9000"
                    cardType = 3
                }*/
                "00A40000020006", "00A4040008535A504B5F5A5959", "00A4040008535A504B5F5A595900", "00A40000023F00" -> {
                    tran.txResult = "9000"
                    cardType = 11
                }
                /*"00A4000002ADF1" ->
                    tran.txResult = "6F0A84085041592E4D4631586A81"
                "00A40400085041592E4D463158" ->
                    tran.txResult = "6F0A84085041592E4D4631586A81"*/
                /*"00A4040009A00000000386980701", "00A4040009A0000000038698070100" -> {
                    tran.txResult =
                        "6F328409A00000000386980701A5259F0801029F0C1E869820007590FFFF820520007D93847E0526E0B8201807242024101602149000"
                    cardType = 6
                }
                "00A4040008A000000632010105", "00A4040008A00000063201010500" -> {
                    tran.txResult = "9000"
                    cardType = 7
                }*/
                /*"00A404000DD156000015B9ABB9B2D3A6D3C3", "00A404000DD156000015B9ABB9B2D3A6D3C300" -> {
                    tran.txResult = "9000"
                    cardType = 8
                }
                "00A40000021002" -> {
                    tran.txResult = "9000"
                    cardType = 9
                }*/
                /*"00A4040009A00000000386980701", "00A4040009A0000000038698070100" -> {
                    tran.txResult =
                        "6F328409A00000000386980701A5259F0801029F0C1E869820007590FFFF820520008983D02F2495D0EC20200103202505111B149000"
                    cardType = 10
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
                        6 -> "869821207590FFFF820520007D93847E0526E0B820180724202410160214"
                        7 -> "12343060FFFFFFFF02010310517001017090798420190806204012310000"
                        8 -> "544661000201000003000000610002018010238120170927202709270102000000"
                        9 -> "544661000201000003000000610002018010238120170927203003040102000000"
                        10 -> "869820007590FFFF820520008983D02F2495D0EC20200103202505111B14"
                        else -> ""
                    }
                    // 0x05
                    "85" -> when (cardType) {
                        5 -> "000000001400000000302001141204000100FFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                        6 -> "869821207590FFFF050021203030343739313930201902220000000000000000333231313030343739313930"
                        //11 -> "06004743010F650501"
                        11 -> "06004477010E100201" // BALANCE 0, EXPIRY 2019.12
                        else -> ""
                    }
                    // 0x06
                    "86" -> when (cardType) {
                        //11 -> "3C00000000000247434A3102EE00000000474301490000040002" // BALANCE 50, EXPIRY 2021
                        //11 -> "3C000000000002447745E8017200000000447701490000040002" // BALANCE 0, EXPIRY 2019.12
                        11 -> "3E00000000000245DC48C302E80000000045DC01490000040002" // EXPIRY 2020.12
                        else -> ""
                    }
                    // 0x11
                    "91" ->
                        "00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    // 0x16
                    "96" ->
                        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    // 0x17
                    "97" ->
                        "000001561000100000010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                    // 0x1A
                    "9A" ->
                        "190000001900000020200000202000002020000C202000142020001E1900000019000000190000001900000019000000"
                    // 0x04 beijing old
                    "84" ->
                        "1000751136141285060200300000000000000000000000002018030920240309000000000000009156000014FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
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
                        // 0x18
                        //"01C4" -> "0002000000000000000941310064482220200523135920" // suzhou tram
                        "01C4" -> "001A000000000000A00900003101165220200301002723"
                        "02C4" -> "0001000000000000000941310043055120151031095400"
                        "03C4" -> "001A000000000000A00900003101165220111111111111"
                        "04C4" -> "001A000000000000A00900003101165220101010101010"
                        // 0x1E
                        //"01F4" -> "060000413100644822020000020002310000000000E803000020200523135920305011423050FFFFFFFF000000000000" // suzhou tram
                        "01F4" -> "0630500000024904070215960000000000000001900000044C20200301002723333011063330FFFFFFFF000000000000"
                        "02F4" -> "0630500000014004130200010000000000000001900000044C19900301190734712003282330FFFFFFFF000000000000"
                        "03F4" -> "0630500000014004130288440000000000000000000000044C20101010101010314011023140FFFFFFFF000000000000"
                        "04F4" -> "0630500000014004130200000000710000000000000000044C19900301190734100001021000FFFFFFFF000000000000"
                        "05F4" -> "0630500000014004130200000501000000000000000000044C19900301190734667014256670FFFFFFFF000000000000"
                        "06F4" -> "0630500000014004130260010000000000000000000000044C19900301190734710012017910FFFFFFFF000000000000"
                        "07F4" -> "0630500000014004130208000000000000000000000000044C19900301190734307011473070FFFFFFFF000000000000"
                        "08F4" -> "0830500000014004130100080021000000000000000000044C19900301190734581012205810FFFFFFFF000000000000"
                        "09F4" -> "03305000000140041301000A040B000300000000000000044C19900301190734221000002210FFFFFFFF000000000000"
                        "0AF4" -> "0630500000014004130200150001000100000000000000044C20200516055013337013493370FFFFFFFF000000000000"
                        "0BF4" -> "0630500000014004130200070000000000000000000000044C20200512082655341003503410FFFFFFFF000000000000"
                        "0CF4" -> "0630500000014004130200030001000100000000000000044C20190807080753884014678810FFFFFFFF000000000000"
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
                    8 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x18
                        "01C4" -> "0002000000000000A00900003101165220191024184245"
                        "02C4" -> "0001000000000002EE0900008600135900000000173608"
                        else -> ""
                    }
                    9 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x09
                        "014C" -> "0002000000000000021200010003684420200405180244"
                        "024C" -> "0001000000000000021200010006025220200304100910"
                        // 0x18
                        "01C4" -> "0003000000000000000900010022345820200304163658"
                        "02C4" -> "0002000000000000000900010022285220200304162908"
                        "03C4" -> "0001000000000000020900010021322520200304161002"
                        else -> ""
                    }
                    10 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x07
                        "013C" -> "FFFFFFFFC8006C0200007E456B6670E0"
                        "023C" -> "FFFFFFFFC800340300007E456B6060AF"
                        "033C" -> "95070001E803FC0300007E455B66C849"
                        "043C" -> "FFFFFFFF6400640000007E4413B0AC35"
                        "053C" -> "FFFFFFFFC800580200007E41ABD88C52"
                        "063C" -> "FFFFFFFFC800200300007E41AB884CBB"
                        "073C" -> "95070001E803E80300007E41AB749CEF"
                        "083C" -> "999900230000000000007E411AA8309E"
                        "093C" -> "929900230000000000007E411AA83095"
                        "0A3C" -> "00000000000000000000000000000000"
                        // 0x18
                        "01C4" -> "000D00017C000000F00621420100058120200513145536"
                        "02C4" -> "000C00026C000000C80941310119838520200513133828"
                        "03C4" -> "000B000334000000C80941310119772220200513133224"
                        "04C4" -> "000A0003FC000003E80220009507000120200511133850"
                        "05C4" -> "0009000014000000500621420100033420200402155807"
                        "06C4" -> "0008000064000000640941310119775420200402144843"
                        "07C4" -> "00070000C8000000F00621420100058420200316155017"
                        "08C4" -> "00060001B8000000A00621420100058120200122153459"
                        "09C4" -> "0005000258000000C80941310119722720200121152435"
                        "0AC4" -> "0004000320000000C80941310119722220200121140819"
                        else -> ""
                    }
                    11 -> when (hexCommandApdu.substring(4, 8)) {
                        // 0x19 (suzhou metro in/out)
                        "01CC" -> "0030000001610403514556990000000000000002000000000000000004540405514560C747D0010000000000000000000000"
                        // 0x18
                        "01C4" -> "0003000000000000010921500040055220200502220307"
                        "02C4" -> "0002000000000000000921500001027820200502212625"
                        "03C4" -> "0001000000000000320200000000000020120416132322"
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
                "0001", "0002", "0302" -> result = when (cardType) {
                    1 -> "00000064"
                    2 -> "00000B22"
                    3 -> "00000064"
                    4 -> "80000064"
                    6 -> "00000064"
                    8 -> "00000064"
                    9 -> "00000002"
                    10 -> "0000017C"
                    11 -> "00000031"
                    else -> "00000064"
                }
            }
            /*// Init for load
            "8050" -> result = when (cardType) {
                2 -> "00000B2200020003200100E9F0DEEA"
                else -> ""
            }*/
            "C4FE" -> result = "41070B16BEF8F42110010184"
            //"C4FE" -> result = "BA9A8B9F34000035C0490032"
            // transaction #
            "C4FB" -> result = "0002000A"
            //else -> if (hexCommandApdu.substring(0,2) == "80") tran.txResult = "9000"
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