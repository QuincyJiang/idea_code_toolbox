package utils

import com.intellij.openapi.diagnostic.Logger

fun Any.logi(msg: String) {
   Logger.getInstance(this::class.java).info(msg)
}

fun Any.loge(msg: String) {
    Logger.getInstance(this::class.java).error(msg)
}

fun Any.logd(msg: String) {
    Logger.getInstance(this::class.java).debug(msg)
}