package com.domosekai.cardemulator

import android.media.RingtoneManager
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class HCEService : HostApduService() {

    companion object {
        const val STATUS_SUCCESS = "9000"
        const val STATUS_FAILED = "6A82"
        const val CLA_NOT_SUPPORTED = "6E00"
        const val INS_NOT_SUPPORTED = "6D00"
        var cardType = 0
        var tuType = 0
        var cuOnly = false
        var tuOnly = false
        var cuIssuer = ""
        var tuIssuer = ""
        var app = 0
        var inTU = false
        var metro = false
        var inGate = false
        var metroLine = ""
        var metroStation = ""
        var metroRemark = ""
        var metroCity = ""
        var metroInstitution = ""
        var metroStationIn = ""
        var prefix = ""
        var wait = false
        var waitID = 0
        val rawMessage = MutableLiveData("")
        var commands = ""
        var terminals = MutableLiveData("")
        var cuCustom = emptyMap<String, String>()
        var tuCustom = emptyMap<String, String>()
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
        val df1 = SimpleDateFormat("yyyyMMddHHmmss")
        val df2 = SimpleDateFormat("yyyyMMdd")
    }

    override fun onDeactivated(reason: Int) {
        rawMessage.value += df.format(Date()) + "\n"
        rawMessage.value += "Deactivated: $reason\n\n"
        if (wait) {
            waitID++
            val current = waitID
            Timer().schedule(600) {
                if (current == waitID) wait = false
            }
        }
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) {
            return hexStringToByteArray(STATUS_FAILED)
        }
        val tran = parseAPDU(commandApdu)
        // Block duplicate read during waiting period
        if (wait && tran.command == "00A4" && (!metro || cardType == 0 || tuType == 0))
            return hexStringToByteArray(STATUS_FAILED)

        rawMessage.value += df.format(Date()) + "\n"
        when (tran.command) {
            "00B0" -> rawMessage.value += "READ INFO ${"%02X".format(tran.p1 and 0x1F)}\n"
            "00B2" -> rawMessage.value += "READ RECORD ${"%02X".format(tran.p2 shr 3)} " +
                    "${getRecordNo(tran.p1, tran.p2)}\n"
            "80CA" -> rawMessage.value += "GET DATA\n"
            "8050" -> rawMessage.value += "INIT\n"
            "805C" -> rawMessage.value += "GET BALANCE\n"
            "80DC" -> rawMessage.value += "UPDATE RECORD ${"%02X".format(tran.p2 shr 3)} " +
                    "${getRecordNo(tran.p1, tran.p2)}\n"
        }
        rawMessage.value += "Command: ${tran.query}\n"
        commands += tran.query + "\n"

        // Get custom response
        var result = if (inTU) {
            tuCustom[tran.query]
        } else {
            cuCustom[tran.query]
        }
        if (result != null) {
            rawMessage.value += "Response: ${result}\n\n"
            return hexStringToByteArray(result)
        }

        // Get predefined response
        when (tran.command) {
            // SELECT
            "00A4" -> {
                app = 0
                when (tran.data) {
                    "3F00", "1001" -> result = ""
                    "315055422E5359532E4444463031" ->
                        if (cardType == TYPE_HZ && !wait && !tuOnly) {
                            result = "6F15840E315055422E5359532E4444463031A503880101"
                            inTU = false
                        }
                    "325041592E5359532E4444463031" ->
                        if (tuType > 0 && !wait && !cuOnly) {
                            result =
                                "6F49840E325041592E5359532E4444463031A537BF0C3461194F08A000000632010106500A4D4F545F545F4341534887010161174F08A00000063201010550084D4F545F545F4550870102"
                        }
                    "A000000632010105" ->
                        if (tuType > 0 && !wait && !cuOnly) {
                            result =
                                "6F318408A000000632010105A5259F0801029F0C1E01011000FFFFFFFF02010310517001017090798420190806204012310000"
                            app = 2
                            inTU = true
                        }
                    "91560000144D4F542E424D4143303031" ->
                        if (tuType == TU_BJ) {
                            result = "6F12841091560000144D4F542E424D4143303031"
                            app = 3
                        }
                    "5041592E535A54" ->
                        if (cardType == TYPE_SZT) {
                            result =
                                "6F3284075041592E535A54A5279F0801029F0C200000000000000000FD44000051800000E63C1D29201712012027120110100000"
                            inTU = false
                        }
                    "5041592E41505059" ->
                        if (cardType == TYPE_YCT) {
                            result = "6F0A84085041592E41505059"
                            inTU = false
                        }
                    "5041592E5449434C" ->
                        if (cardType == TYPE_YCT) {
                            result =
                                "6F3484085041592E5449434CA5289F0801029F0C21FFFFFFFFFFFFFFFF000000000000000000000000000000002019120700000186A0"
                            app = 1
                            inTU = false
                        }
                    "535558494E2E4444463031", "DF01" ->
                        if (cardType == TYPE_SUZ) {
                            result =
                                "6F30840B535558494E2E4444463031A5219F0C1E21500909283991510202860000215000283991512019010120501231FFFF"
                            inTU = false
                        }
                    "0006", "535A504B5F5A5959" ->
                        if (cardType == TYPE_SUZ_CIKA) {
                            result = ""
                            inTU = false
                        }
                    "A00000000386980701" ->
                        if (!wait && !tuOnly) when (cardType) {
                            TYPE_CU, TYPE_SPTC -> {
                                result =
                                    "6F328409A00000000386980701A5259F0801029F0C1E869820007590FFFF820520007D93847E0526E0B820180724202410160214"
                                inTU = false
                            }
                            TYPE_HZ -> {
                                result =
                                    "6F2E8409A00000000386980701A5219F0C1E847531000000000000003100310000015107302820191225206912250000"
                                inTU = false
                            }
                        }
                    "A00000000386980702" ->
                        if (cardType == TYPE_HZ) {
                            result =
                                "6F308409A00000000386980702A5239F0C200001310000000000000000000000000000000000000000000000000000000000"
                            inTU = false
                            app = 2
                        }
                    "A00000000386980703" ->
                        if (cardType == TYPE_HZ) {
                            result =
                                "6F308409A00000000386980703A5239F0C20000131000000000000000000000000000000000000000000000000000000000000000000"
                            inTU = false
                            app = 1
                        }
                    "D156000015B9ABB9B2D3A6D3C3" ->
                        if (cardType == TYPE_TFT) {
                            result = ""
                            inTU = false
                        }
                    "1002" ->
                        if (cardType == TYPE_TFT) {
                            result = ""
                            app = 1
                            inTU = false
                        }
                }
            }
            // PIN
            "0020" -> {
                if (cardType == TYPE_SUZ)
                    result = ""
            }
            // READ INFO
            "00B0" -> {
                // switch by P1, P2 is offset (because bit 8 of P1 is 1)
                when (tran.p1 and 0x1F) {
                    0x15 -> result = if (inTU)
                        when (tuType) {
                            TU_BJ -> "01011000FFFFFFFF02010310517005678901234520190806204012310000"
                            TU_NANTONG -> "12343060FFFFFFFF02010310517001017090798420190806204012310000"
                            else -> tuIssuer + "FFFFFFFF02010310517005678901234520190806204012310000"
                        }
                    else when (cardType) {
                        TYPE_YCT -> "FFFFFFFFFFFFFFFF5100000756695739030002FFFFFFFF201509162025123120150916218121010100000000052B000101015100000756695739030000000000FF8000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                        TYPE_SUZ -> "21500909283991510202860000215000283991512019010120501231FFFF"
                        TYPE_SZT -> "0000000000000000FD44000051800000D63C1D29201712012027120110100000"
                        TYPE_CU -> cuIssuer + "000100000001116000116001013BBD5220200613202112310000"
                        TYPE_TFT ->
                            if (app == 0)
                                "544661000201000003000000610002018010238120170927202709270102000000"
                            else
                                "544661000201000003000000610002018010238120170927203003040102000000"
                        TYPE_SPTC -> "869820007590FFFF820520008983D02F2495D0EC20200103202505111B14"
                        TYPE_HZ -> "847531000000000000003100310000015107302820191225206912250000"
                        else -> null
                    }
                    0x05 -> result = when (cardType) {
                        TYPE_BJ -> "000000001400000000302001141204000100FFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                        TYPE_ZHENJIANG -> "869821207590FFFF050021203030343739313930201902220000000000000000333231313030343739313930"
                        //11 -> "06004743010F650501"
                        TYPE_SUZ_CIKA -> "06004477010E100201" // BALANCE 0, EXPIRY 2019.12
                        TYPE_HZ -> "1700002099010101E000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                        else -> null
                    }
                    0x06 -> result = when (cardType) {
                        //11 -> "3C00000000000247434A3102EE00000000474301490000040002" // BALANCE 50, EXPIRY 2021
                        //11 -> "3C000000000002447745E8017200000000447701490000040002" // BALANCE 0, EXPIRY 2019.12
                        TYPE_SUZ_CIKA -> "3E00000000000245DC48C302E80000000045DC01490000040002" // EXPIRY 2020.12
                        else -> null
                    }
                    0x11 ->
                        result = "00FFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    0x16 ->
                        result =
                            "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                    0x17 ->
                        if (inTU) result =
                            "000001561000100000010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                    0x1A ->
                        if (cardType == TYPE_TFT)
                            result =
                                "190000001900000020200000202000002020000C202000142020001E1900000019000000190000001900000019000000"
                    0x04 ->
                        if (cardType == TYPE_BJ)
                            result =
                                "1000751136141285060200300000000000000000000000002018030920240309000000000000009156000014FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"
                }
                if (result != null && tran.p2 > 0 && result.length / 2 > tran.p2) {
                    result = result.substring(tran.p2 * 2)
                }
            }
            // READ RECORD
            "00B2" -> {
                // switch by P1 P2
                if (inTU) when (tuType) {
                    TU_BJ -> when (tran.p2 shr 3) {
                        // 0x02 R1
                        0x02 -> if (tran.p1 == 1)
                            result = "0112060000000100000000000000000000000000"
                        0x03 -> if (tran.p1 == 1)
                            result =
                                "013020011320320627200110001DB00000000000000000000000000000000000000000000000000000000000000000000000"
                        0x18 -> result = when (tran.p1) {
                            //"01C4" -> "0002000000000000000941310064482220200523135920" // suzhou tram
                            1 -> "001A000000000000A00900003101165220200301002723"
                            2 -> "0001000000000000000941310043055120151031095400"
                            3 -> "001A000000000000A00900003101165220111111111111"
                            4 -> "001A000000000000A00900003101165220101010101010"
                            else -> null
                        }
                        0x1A -> result = when (tran.p1) {
                            1 -> "27017D0101000000000000043835025810581012205810FFFFFFFF122058100000000001007513090090251000000000000000000041310173800800004131013299462019120711350020191207115635000000BE1E0900000000000004B50000011D0000000000000000000000000000000000000000000000000000000000"
                            2 -> "27027D010100000000000007708502100011011000FFFFFFFF000100291030303033323834350000001009007597201911291325450000012CAB100011011000FFFFFFFF000100290D30303033323834350000001009007598201911291330460000006400C8000000000000000000000000000000000000000000000000DDF3"
                            else -> null
                        }
                        0x1E -> result = when (tran.p1) {
                            //"01F4" -> "060000413100644822020000020002310000000000E803000020200523135920305011423050FFFFFFFF000000000000" // suzhou tram
                            1 -> "0630500000024904070215960000000000000001900000044C20200301002723333011063330FFFFFFFF000000000000"
                            2 -> "0630500000014004130200010000000000000001900000044C19900301190734712003282330FFFFFFFF000000000000"
                            3 -> "0630500000014004130288440000000000000000000000044C20101010101010314011023140FFFFFFFF000000000000"
                            4 -> "0630500000014004130200000000710000000000000000044C19900301190734100001021000FFFFFFFF000000000000"
                            5 -> "0630500000014004130200000501000000000000000000044C19900301190734667014256670FFFFFFFF000000000000"
                            6 -> "0630500000014004130260010000000000000000000000044C19900301190734710012017910FFFFFFFF000000000000"
                            7 -> "0630500000014004130208000000000000000000000000044C19900301190734307011473070FFFFFFFF000000000000"
                            8 -> "0830500000014004130100080021000000000000000000044C19900301190734581012205810FFFFFFFF000000000000"
                            9 -> "03305000000140041301000A040B000300000000000000044C19900301190734221000002210FFFFFFFF000000000000"
                            10 -> "0630500000014004130200150001000100000000000000044C20200516055013337013493370FFFFFFFF000000000000"
                            11 -> "0630500000014004130200070000000000000000000000044C20200512082655341003503410FFFFFFFF000000000000"
                            12 -> "0630500000014004130200030001000100000000000000044C20190807080753884014678810FFFFFFFF000000000000"
                            13 -> "0300004131012613770104020841000000000000000000044C2020040817130739100003500050000001000000000000"
                            14 -> "034131060150761B370101FF0BFF000000000000000000044C20200721184504261011592610FFFFFFFF000000000000"
                            15 -> "0600000000137600120291000000001900000000000000044C20200713130002301010083010FFFFFFFF000000000000"
                            16 -> "0400004131061291960100020055000000000000000000044C20200725144407332003473320FFFFFFFF000000000000"
                            17 -> "037910000003511F010100030033000000000000000000044C20200723104639791012017910FFFFFFFF000000000000"
                            18 -> "0400004131007712370219080005000100000000000000044C20200716121327871011108710FFFFFFFF000000000000"
                            19 -> "0200004131012113140250000000000000000000000000044C20200714163016375502068040FFFFFFFF000000000000"
                            20 -> "0600004131014142590227020000000000000000000000044C20200719175704491011974910FFFFFFFF000000000000"
                            21 -> "061E00030B80032248FE00000000000000000000000000444C20191212130454301010083010FFFFFFFF000000000000"
                            22 -> "0600004331000022680100901010000000000001900000044C20200724110840581012205810FFFFFFFF000000000000"
                            23 -> "0400004131014188120101040306000000000001900000044C20200806160841393011343930FFFFFFFF000000000000"
                            24 -> "0300004131014246420200010023000000000000000000044C20200805192706393011343930FFFFFFFF000000000000"
                            25 -> "044131017340211b310101FF5AFF000000000001900000044C20200731165245261011592610FFFFFFFF000000000000"
                            26 -> "0600000000002200600210020001010100000001900000044C20200801083001100011131121FFFFFFFF000000000000"
                            27 -> "0800004131014186190101230703000000000000000000044C20200807171310393011343930FFFFFFFF000000000000"
                            28 -> "0600004131060486370200030000880009000001900000044C20200111200905221012172210FFFFFFFF000000000000"
                            29 -> "0600004131061155860206070001000100000001900000044C20200823163350338012963380FFFFFFFF000000000000"
                            30 -> "0400000002640191180100000000000016000001900000044C20200818194916584012215840FFFFFFFF000000000000"
                            31 -> "06000009000900060902006A0001900000000001900000044C20200801131135100011011000FFFFFFFF000000000000"
                            else -> null
                        }
                    }
                    TU_GEN -> when (tran.p2 shr 3) {
                        0x1A -> result = when (tran.p1) {
                            1 -> if (metro && inGate)
                                "27017D010100000000000000000001" + metroCity + "0000" +
                                        metroInstitution + "FFFFFFFF" + "0000000000000000" + metroStationIn +
                                        "000000000000000000000000000000000000000000000000" +
                                        df1.format(Date(Date().time - 60 * 60 * 1000)) +
                                        "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                            else
                                "27017D0101000000000000043835025810581012205810FFFFFFFF122058100000000001007513090090251000000000000000000041310173800800004131013299462019120711350020191207115635000000BE1E0900000000000004B50000011D0000000000000000000000000000000000000000000000000000000000"
                            2 -> "27027D010100000000000007708502100011011000FFFFFFFF000100291030303033323834350000001009007597201911291325450000012CAB100011011000FFFFFFFF000100290D30303033323834350000001009007598201911291330460000006400C8000000000000000000000000000000000000000000000000DDF3"
                            else -> null
                        }
                    }
                }
                else when (cardType) {
                    TYPE_HZ -> when (app) {
                        0 -> when (tran.p2) {
                            // 0x17 first occurence
                            0xB8 -> result = when (tran.p1) {
                                1 -> "012E000000310004001002000000005E04A8E90000000000000EE90100015E04A8E90100000020590000000104001002"
                                6 -> if (metro && inGate)
                                    "062E0031013100040253160000000005360118" + "%08X".format(Date().time / 1000 - 60 * 60 - 946656000) +
                                            "0000000005" + df2.format(Date(Date().time - 60 * 60 * 1000)) +
                                            "00000000000005360106000000B60000"
                                else "062E0031033100010525370000016C0106015026E12A7F0000016C092020090100000000000001010109000000B60000"
                                else -> null
                            }
                        }
                        else -> when (tran.p2) {
                            0xB8 -> result = when (tran.p1) {
                                1 -> "012E00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                                6 -> "062E00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                                else -> null
                            }
                        }
                    }
                    TYPE_YCT -> when (tran.p2 shr 3) {
                        // 0x08 R1 & R17 (p2's last 3 bits 0 means P1 is identifier: find first C8 in 0x08)
                        0x08 -> result = when (tran.p1) {
                            1 -> "1814001912010E0F0000000000000000000000000000"
                            17, 0xC8 ->
                                //"C8540000000102003BCEC2EE010000169261" + posList[1] + "04101610" + posList[1] + "0410163BCEC2EE010000169261000000000000000000000000000000000000000000000000000000000000000000000000000000000000000014105C"
                                "C8540000000002003BCEB67101000020061830010430062002150430103BCE9E63010000203169000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000C4E7"
                            else -> null
                        }
                        0x18 -> result = when (tran.p1) {
                            1 -> "000A00000000000299090100" + prefix + "002BC1207112517"
                            2 -> "000900000000000000090100" + prefix + "100001207095711"
                            3 -> "000800000000000299090100" + prefix + "202BC1207095717"
                            4 -> "000700000000000000090100" + prefix + "300001207095311"
                            5 -> "000600000000000000090100" + prefix + "400001207095311"
                            6 -> "000500000000000000090100" + prefix + "500001207095311"
                            7 -> "000400000000000000090100" + prefix + "600001207095311"
                            8 -> "000300000000000000090100" + prefix + "700001207095311"
                            9 -> "000200000000000000090100" + prefix + "800001207095311"
                            10 -> "000100000000000000090100" + prefix + "900001207095311"
                            11 -> "0002000000000013880288012002549113881207095314"
                            12 -> "0001000000000000000200001600202920190715085953"
                            else -> null
                        }
                    }
                    TYPE_SZT -> when (tran.p2 shr 3) {
                        0x08 -> if (tran.p1 == 1)
                            result =
                                "011E0000B1A0AD0F486A484BD600304F00000000000000000000000000000000"
                        0x18 -> result = when (tran.p1) {
                            1 -> "001A000000000000A00900003101165220191024184245"
                            2 -> "0019000000000002EE0900008600135920191024173608"
                            3 -> "0018000000000002EE0900008600235920191024173608"
                            4 -> "0017000000000002EE0900008600335920191024173608"
                            5 -> "0016000000000002EE0900008600435920191024173608"
                            6 -> "0015000000000002EE0900008600535920191024173608"
                            7 -> "0014000000000002EE0900008600635920191024173608"
                            8 -> "0013000000000002EE0900008600735920191024173608"
                            9 -> "0012000000000002EE0900008600835920191024173608"
                            10 -> "0011000000000002EE0900008600935920191024173608"
                            else -> null
                        }
                    }
                    TYPE_TFT -> if (app == 0) when (tran.p2 shr 3) {
                        0x18 -> result = when (tran.p1) {
                            1 -> "0002000000000000A00900003101165220191024184245"
                            2 -> "0001000000000002EE0900008600135900000000173608"
                            else -> null
                        }
                    }
                    // cika
                    else when (tran.p2 shr 3) {
                        0x09 -> result = when (tran.p1) {
                            1 -> "0002000000000000021200010003684420200405180244"
                            2 -> "0001000000000000021200010006025220200304100910"
                            else -> null
                        }
                        0x18 -> result = when (tran.p1) {
                            1 -> "0003000000000000000900010022345820200304163658"
                            2 -> "0002000000000000000900010022285220200304162908"
                            3 -> "0001000000000000020900010021322520200304161002"
                            else -> null
                        }
                    }
                    TYPE_SPTC -> when (tran.p2 shr 3) {
                        0x07 -> result = when (tran.p1) {
                            1 -> "FFFFFFFFC8006C0200007E456B6670E0"
                            2 -> "FFFFFFFFC800340300007E456B6060AF"
                            3 -> "95070001E803FC0300007E455B66C849"
                            4 -> "FFFFFFFF6400640000007E4413B0AC35"
                            5 -> "FFFFFFFFC800580200007E41ABD88C52"
                            6 -> "FFFFFFFFC800200300007E41AB884CBB"
                            7 -> "95070001E803E80300007E41AB749CEF"
                            8 -> "999900230000000000007E411AA8309E"
                            9 -> "929900230000000000007E411AA83095"
                            10 -> "00000000000000000000000000000000"
                            else -> null
                        }
                        0x18 -> result = when (tran.p1) {
                            1 -> "000D00017C000000F00621420100058120200513145536"
                            2 -> "000C00026C000000C80941310119838520200513133828"
                            3 -> "000B000334000000C80941310119772220200513133224"
                            4 -> "000A0003FC000003E80220009507000120200511133850"
                            5 -> "0009000014000000500621420100033420200402155807"
                            6 -> "0008000064000000640941310119775420200402144843"
                            7 -> "00070000C8000000F00621420100058420200316155017"
                            8 -> "00060001B8000000A00621420100058120200122153459"
                            9 -> "0005000258000000C80941310119722720200121152435"
                            10 -> "0004000320000000C80941310119722220200121140819"
                            else -> null
                        }
                    }
                    TYPE_SUZ_CIKA -> when (tran.p2 shr 3) {
                        // suzhou metro in/out
                        0x19 -> if (tran.p1 == 1)
                            result =
                                "0030000001610403514556990000000000000002000000000000000004540405514560C747D0010000000000000000000000"
                        0x18 -> result = when (tran.p1) {
                            1 -> "0003000000000000010921500040055220200502220307"
                            2 -> "0002000000000000000921500001027820200502212625"
                            3 -> "0001000000000000320200000000000020120416132322"
                            else -> null
                        }
                    }
                }
            }
            // Balance
            "805C" -> result = when (tran.p1) {
                0x05 -> "00000B220007A1200000000000000000"
                // 01 02 03
                else -> "00000B22"
            }
            // Get challenge
            "0084" -> result = "C620857FF5D967C2"
            // External auth
            "0082" -> result = ""
            // Get data
            "80CA" -> result =
                if (inTU) ""
                else if (cardType == TYPE_HZ) "04FFF83E302F6EB256"
                else "850526E0B885AC58FC"
            // Init for load
            "8050" -> {
                // Same format for TU and CU
                //result = "00000B2200020000000100" + "E9F0DEEA"
                if (tran.lc == 11 && metro) {
                    val s = if (inGate) "Out" else "In"
                    val t = if (inTU) "TU" else "CU"
                    terminals.value += "${df.format(Date())},$metroLine,$metroStation,$metroRemark,$t,$s," +
                            tran.data.takeLast(12) + "\n"
                    val notification =
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(applicationContext, notification)
                    r.play()
                    wait = true
                }
            }
            // Update record
            "80DC" -> result = ""
        }

        if (result != null) {
            if (tran.le > 0 && result.length / 2 > tran.le) {
                result = result.take(tran.le * 2)
            }
            tran.result = result + STATUS_SUCCESS
        } else {
            tran.result = STATUS_FAILED
        }

        rawMessage.value += "Response: ${tran.result}\n\n"
        return hexStringToByteArray(tran.result)

    }
}