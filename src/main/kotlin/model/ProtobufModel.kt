package model

/**
 * 抽象pb model类
 * @param requests 请求
 * @param responses 响应
 * @param pbName 解析的pb大类名s
 * */
data class PbModel(var requests: List<String>, var responses: List<String>, var pbName: String)

