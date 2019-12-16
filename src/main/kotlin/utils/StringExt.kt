package utils

import model.HiidoStaticKey
import model.Param

fun String.adjustSlash(): String {
    return this.replace("-","_")
}

fun List<Param>.toParmsStr(): String {
    return this.toString().replace("[", "").replace("]", "")
}
fun ArrayList<Param>.toParmsStr(): String {
    return this.toString().replace("[", "").replace("]", "")
}

fun ArrayList<HiidoStaticKey>.toKeyStr(): String {
    val result = StringBuilder()
    this.forEach{
        result.append(it.key + ",")
    }
    return result.toString()
}

fun List<HiidoStaticKey>.toKeyStr(): String {
    val result = StringBuilder()
    this.forEach{
        result.append(it.key + ",")
    }
    return result.toString()
}

fun String.splitSlash(): List<String> {
    return this.split("-")
}