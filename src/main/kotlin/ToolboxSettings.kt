import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.xmlb.XmlSerializerUtil
import model.CodeLanguage
import model.CodeTemplate

/**
 * 插件配置中心
 * */
@State(name = "ToolBoxSettings", storages = [Storage("\$APP_CONFIG$/ToolBoxSettings.xml")])
class ToolboxSettings: PersistentStateComponent<ToolboxSettings> {
    var mCodeTemplates =  HashMap<String, CodeTemplate>(4)

    // 初始化预置的默认模板
    init {
        // 根据选中IXXCore.java 生成XXCoreImp.java
        mCodeTemplates["CoreImp"] = getDefaultTemplates("CoreImp",
            CodeLanguage.Java, "#set(\$end = \${contextClass.name.length()} - 1)\${contextClass.name.substring(1,\${end})}Imp" ,"CoreImpTemp.vm")
    }
    override fun getState(): ToolboxSettings? {
       return this
    }

    override fun loadState(state: ToolboxSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun getTemplate(templateName: String): CodeTemplate? {
        return mCodeTemplates[templateName]
    }

    fun removeTemplate(templateName: String) {
        mCodeTemplates.remove(templateName)
    }
    fun addTemplate(name: String, template: CodeTemplate) {
        mCodeTemplates[name] = template
    }
}

fun getDefaultTemplates(templateName: String,
                        codeLanguage: CodeLanguage,
                        targetClassName: String,
                        templateFileName: String): CodeTemplate {
    val velocityTemplate =
        FileUtil.loadTextAndClose(ToolboxSettings::class.java.getResourceAsStream("/temp/$templateFileName"))
    return CodeTemplate(
        templateName,
        targetClassName,
        codeLanguage,
        velocityTemplate
    )
}