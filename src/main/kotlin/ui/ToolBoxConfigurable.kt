package ui

import ToolboxSettings
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.SearchableConfigurable
import javax.swing.JComponent

/**
 *  ToolBox插件在idea Settings-> OtherSettings 中出现的配置页
 * */
class ToolBoxConfigurable: SearchableConfigurable, Configurable.NoScroll {
    val settings = ServiceManager.getService(ToolboxSettings::class.java)
    var configuration: ToolBoxSettingPanel? = null

    // 校验设置之后数据是否有变动更新
    override fun isModified(): Boolean {
        return false
    }

    override fun getId(): String {
        return "com.jiangxq.toolbox.configurable"
    }

    override fun getDisplayName(): String {
        return "GVToolBox"
    }

    override fun apply() {
    }

    override fun createComponent(): JComponent? {
        if (configuration == null) {
            configuration = ToolBoxSettingPanel()
        }
        return configuration!!.mainPanel
    }
}