package model

/**
 * 代码模板实体类
 * @param templateName 模板名
 * @param codeLanguage 模板目标语言
 * @param tempStr 模板内容
 * */
data class CodeTemplate(var templateName: String, var codeLanguage: CodeLanguage, var tempStr: String)

/**
 * 生成代码目标语言 支持java 与 kotlin
 * */
enum class CodeLanguage{
    Java, Kotlin
}