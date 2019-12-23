package utils

interface OnConfirmListener<T> {
    fun onConfirm(result: T)
}