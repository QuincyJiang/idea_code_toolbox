package model

/**
 * 代码模板实体类
 * @param templateName 模板名
 * @param targetClassNameTemp 生成的目标类名模板（会被Decoder正确解析）
 * @param codeLanguage 模板目标语言
 * @param tempStr 模板内容
 * */
data class CodeTemplate(var templateName: String, var targetClassNameTemp: String, var codeLanguage: CodeLanguage, var tempStr: String)

/**
 * 生成代码目标语言 支持java 与 kotlin
 * */
enum class CodeLanguage{
    Java, Kotlin
}

/**
 * 模板类型 新建文件 还是 生成一个代码块
 * */
enum class TemplateType {
    File, CodeBlock
}

/**
 * 解析模板和选中PSI Class File 生成对应的目标代码实体
 * */
data class GeneratedSourceCode(var className: String, var sourceCode: String)

/**
 * 模板解析占位符的源参数
 * @param className 生成类名
 * @param bindParams 源参数map 模板中需要用到的所有占位符的真实数据资源均放在该map中 由具体的模板解析器去映射
 * */
data class BindingSource (
    var className: String,
    var bindParams: LinkedHashMap<String, Any>)