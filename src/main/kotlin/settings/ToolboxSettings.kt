package settings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.xmlb.XmlSerializerUtil
import generator.GroovySourceGenerator
import generator.VelocityTemplateGenerator
import model.CodeLanguage
import model.CodeTemplate
import model.TemplateLanguage
import model.TemplateType

/**
 * 插件配置中心
 * */
@State(name = "ToolBoxSettings", storages = [Storage("\$APP_CONFIG$/ToolBoxSettings.xml")])
class ToolboxSettings: PersistentStateComponent<ToolboxSettings> {
    var mCodeTemplates =  HashMap<String, CodeTemplate>(4)
    // 两个generator 也是全局单例
    var mGroovySourceGenerator = GroovySourceGenerator()
    var mVmSourceGenerator = VelocityTemplateGenerator()

    // 初始化预置的默认模板
    init {
        // 根据选中IXXCore.java 生成XXCoreImp.java
        mCodeTemplates["IXXCore实现类模板"] = getDefaultTemplates(
            TemplateType.File,
            "IXXCore实现类模板",
            CodeLanguage.Java,
            "#set(\$end = \${contextClass.name.length()})\${contextClass.name.substring(1,\${end})}Imp",
            "CoreImpTemp.vm"
        )
        //快速生成模板接口代码
        mCodeTemplates["埋点接口代码"] = getDefaultTemplates(
            TemplateType.CodeBlock, "埋点接口代码",
            CodeLanguage.Java, "Default", "IHiidoStatic.vm"
        )
        //快速生成模板实现代码
        mCodeTemplates["埋点实现类代码"] = getDefaultTemplates(
            TemplateType.CodeBlock, "埋点实现类代码",
            CodeLanguage.Java, "Default", "HiidoStaticImp.vm"
        )
    }
    override fun getState(): ToolboxSettings? {
       return this
    }

    override fun loadState(state: ToolboxSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
}

fun getDefaultTemplates( templateType: TemplateType,
                        templateName: String,
                        codeLanguage: CodeLanguage,
                        targetClassName: String,
                        templateFileName: String): CodeTemplate {
    val velocityTemplate =
        FileUtil.loadTextAndClose(ToolboxSettings::class.java.getResourceAsStream("/temp/$templateFileName"))
    return CodeTemplate(
        templateType,
        templateName,
        targetClassName,
        codeLanguage,
        velocityTemplate,
        TemplateLanguage.Vm
    )
}