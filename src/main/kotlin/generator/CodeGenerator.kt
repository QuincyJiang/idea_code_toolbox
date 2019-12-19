package generator

import groovy.text.GStringTemplateEngine
import model.BindingSource
import model.ClassStruct
import model.CodeTemplate
import model.GeneratedSourceCode
import org.apache.commons.lang.time.DateFormatUtils
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.log.NullLogChute
import java.io.StringWriter
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * 根据模板文件名解析规则 解析出的目标java文件名
 * */
const val CLASS_NAME_KEY = "ClassName"

/**
 * 模板解析器
 * 输入一个模板实体类CodeTemplate，输入一个抽象Class实体类，输出一个生成的GeneratedSourceCode文件
 * */
interface ISourceGenerator {
    fun combine(template: CodeTemplate, selectedClass: ClassStruct, currentClass: ClassStruct?): GeneratedSourceCode
}

abstract class AbsSourceGenerator: ISourceGenerator {
    override fun combine(template: CodeTemplate, selectedClass: ClassStruct, currentClass: ClassStruct?): GeneratedSourceCode {
        val bindingSource = createBindingSource(template,selectedClass, currentClass)
        val source = doCombine(template, bindingSource)
        return GeneratedSourceCode(bindingSource.className, source)
    }

    /**
     * 见下面实现类 模板的具体解析工作由IDEA的 Groovy 解析器 和  Vm解析器完成
     * 我们只要把需要绑定的参数提前提供好就行了
     * BindingParams是为模板的占位符号提供具体数据的 具体说明
     * @see BindingSource
     * */
    protected abstract fun doCombine(template: CodeTemplate, bindingSource: BindingSource): String

    /**
     * 添加绑定参数
     * @param template 当前模板文件
     * @param selectedClass 当前右键所在的类 它是contextClass 目标生成类是基于它来生成的
     * @param targetClass 目标生成的类
     * */
    private fun createBindingSource(
        template: CodeTemplate,
        selectedClass: ClassStruct,
        targetClass: ClassStruct?
    ): BindingSource {
        val map = LinkedHashMap<String, Any>()
        map["contextClass"] = selectedClass
        map["class"] = targetClass ?: ""
        map["TIME"] = DateFormatUtils.format(Date(), "yyyy-MM-dd HH:mm:ss")
        map["USER"] = System.getProperty("user.name")
        map["BR"] = "\n"
        map["QT"] = "\""
        val className = tryParseTargetClassName(template, map)
        map[CLASS_NAME_KEY] = className
        return BindingSource(className, map)
    }

    /**
     * 根据模板的文件名模板生成对应目标class文件名
     * 比如说 选中的类是 IChargeCore.class
     * 生成的目标java文件名模板为  #set($end = ${contextClass.name.length()} - 1)${contextClass.name.substring(0,${end})}Imp
     * 那么生成的目标文件名就是  ChargeCoreImp.java
     * */
    private fun tryParseTargetClassName(template: CodeTemplate, map: LinkedHashMap<String, Any>): String {
        val className: String
        try {
            className = generateClassName(template.targetClassNameTemp, map)
        } catch (e: Exception) {
            val reported = RuntimeException(
                "Generate class name: ${template.targetClassNameTemp} failed. Error msg : ${e.message}", e)
            reported.stackTrace = e.stackTrace
            throw reported
        }
        return className
    }

    protected abstract fun generateClassName(classNameTemplate: String, environment: LinkedHashMap<String, Any>): String
}

/**
 * Groovy模板解析器 选择Groovy作为模板语言的原因是idea对groovy文件有较好的语法高亮支持
 * */

class GroovySourceGenerator: AbsSourceGenerator() {
    override fun doCombine(template: CodeTemplate, bindingSource: BindingSource): String {
        return decodeGroovy(template.tempStr, adjustBindings(bindingSource.bindParams))
    }

    private fun adjustBindings(env: LinkedHashMap<String, Any>): LinkedHashMap<String, Any> {
        val map = LinkedHashMap(env)
        map["clazz"] = env["class"]
        return map
    }

    override fun generateClassName(classNameTemplate: String, environment: LinkedHashMap<String, Any>): String {
        return decodeGroovy(classNameTemplate, adjustBindings(environment))
    }
}

/**
 * vm模板解析器
 * */
class VelocityTemplateGenerator : AbsSourceGenerator() {

    override fun generateClassName(classNameTemplate: String, environment: LinkedHashMap<String, Any>): String {
        return decodeVelocity(classNameTemplate, environment)
    }

    override fun doCombine(template: CodeTemplate, bindingSource: BindingSource): String {
        return decodeVelocity(template.tempStr, bindingSource.bindParams)
    }

}


/**
 * 全局单例velocity解析器 同时初始化参数
 * */
val velocityTempEngine = VelocityEngine().apply {
    setProperty( org.apache.velocity.runtime.RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
        NullLogChute::class.java.name)
    init()
}

val groovyTemplateEngine = GStringTemplateEngine()


/**
 * 填充参数到velocity模板中
 * */
fun decodeVelocity(template: String, map: LinkedHashMap<String, Any>): String{
    val context = VelocityContext()
    map.forEach { key, value -> context.put(key, value) }
    val writer = StringWriter()
    velocityTempEngine.evaluate(context, writer, "", template)
    return writer.toString()
}
/**
 * 填充参数到Groovy模板中
 * */

fun decodeGroovy(template: String, map: LinkedHashMap<String, Any>): String {
    try {
        val writer = StringWriter()
        groovyTemplateEngine.createTemplate(template).make(map).writeTo(writer)
        return writer.toString()
    } catch (e: Exception) {
        val reported = RuntimeException(e)
        reported.stackTrace = e.stackTrace
        throw reported
    }
}






