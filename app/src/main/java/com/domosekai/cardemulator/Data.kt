package com.domosekai.cardemulator

data class Transaction(
    var query: String = "",
    var command: String = "",
    var p1: Int = 0,
    var p2: Int = 0,
    var lc: Int = 0,
    var data: String = "",
    var le: Int = 0,
    var result: String = ""
)

const val TYPE_YCT = 1
const val TYPE_BJ = 2
const val TYPE_SUZ = 3
const val TYPE_SUZ_CIKA = 4
const val TYPE_SZT = 5
const val TYPE_TFT = 6
const val TYPE_SPTC = 7
const val TYPE_HZ = 8
const val TYPE_ZHENJIANG = 9
const val TYPE_CU = 99

const val TU_BJ = 1
const val TU_NANTONG = 2
const val TU_SH = 3
const val TU_GEN = 99

fun parseAPDU(apdu: ByteArray): Transaction {
    val tran = Transaction()
    tran.query = byteArrayToHexString(apdu)
    if (apdu.size < 5) return tran
    tran.command = byteArrayToHexString(apdu.sliceArray(0..1))
    tran.p1 = apdu[2].toInt() and 0xFF
    tran.p2 = apdu[3].toInt() and 0xFF
    tran.lc = apdu[4].toInt() and 0xFF
    if (tran.lc > 0 && apdu.size >= tran.lc + 5) {
        tran.data = byteArrayToHexString(apdu.sliceArray(5..tran.lc + 4))
        if (apdu.size > tran.lc + 5)
            tran.le = apdu[tran.lc + 5].toInt() and 0xFF
    } else if (tran.lc > 0) {
        tran.le = tran.lc
        tran.lc = 0
    }
    return tran
}

fun getRecordNo(p1: Int, p2: Int): String {
    return if (p2 % 8 == 0) {
        "starts with ${"%02X".format(p1)}"
    } else {
        "#$p1"
    }
}

fun byteArrayToHexString(bytes: ByteArray): String {
    val hexArray = charArrayOf(
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    )
    val hexChars = CharArray(bytes.size * 2)
    var v: Int
    for (j in bytes.indices) {
        v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = hexArray[v.ushr(4)]
        hexChars[j * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

fun hexStringToByteArray(s: String): ByteArray {
    val len = s.length
    val data = ByteArray(len / 2)
    var i = 0
    while (i < len) {
        data[i / 2] =
            ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }
    return data
}