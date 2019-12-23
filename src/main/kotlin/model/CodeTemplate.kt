package model

/**
 * 代码模板实体类
 * @param templateType 模板生成代码类型
 * @param templateName 模板名
 * @param targetClassNameTemp 生成的目标类名模板（会被Decoder正确解析）
 * @param codeLanguage 模板目标语言
 * @param tempStr 模板内容
 * @param templateLanguage 模板语言
 * */
data class CodeTemplate(var templateType: TemplateType, var templateName: String, var targetClassNameTemp: String,
                        var codeLanguage: CodeLanguage, var tempStr: String, var templateLanguage: TemplateLanguage)

/**
 * 生成代码目标语言 支持java 与 kotlin
 * */
enum class CodeLanguage{
    Java, Kotlin
}

/**
 * 模板文件语法 vm 或 groovy
 * */
enum class TemplateLanguage {
    Vm, Groovy
}

/**
 * 模板生成类型 新建文件 还是 生成一个代码块
 * File：根据className 生成一个新的.java文件
 * CodeBlock: 代码块
 * Clipboard： 将代码复制到剪切板
 * */
enum class TemplateType {
    File, CodeBlock, Clipboard
}

/**
 * 解析模板和选中PSI Class File 生成对应的目标代码实体
 * */
@NoArg data class GeneratedSourceCode(var className: String, var sourceCode: String)

@NoArg data class SourceCodeBundle(var classStruct: ClassStruct, var codeTemplate: CodeTemplate, var generatedSourceCode: GeneratedSourceCode)

/**
 * 模板解析占位符的源参数
 * @param className 生成类名
 * @param bindParams 源参数map 模板中需要用到的所有占位符的真实数据资源均放在该map中 由具体的模板解析器去映射
 * */
@NoArg data class BindingSource (
    var className: String,
    var bindParams: LinkedHashMap<String, Any>)