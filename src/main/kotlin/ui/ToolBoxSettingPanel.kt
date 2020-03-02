package ui

import com.intellij.openapi.components.ServiceManager
import model.CodeLanguage
import model.CodeTemplate
import model.TemplateLanguage
import model.TemplateType
import settings.ToolboxSettings
import javax.swing.*

class ToolBoxSettingPanel(template: CodeTemplate?) {
    var mainPanel: JPanel? = null
    var editorPanel: JPanel? = null
    var configPanel: JPanel? = null
    var templateEditor: JTextArea? = null
    var btnSave: JButton? = null
    var cancelBtn: JButton? = null
    var targetType: JComboBox<*>? = null
    var tempLanguage: JComboBox<*>? = null
    var targetLanguage: JComboBox<*>? = null
    var tempName: JTextField? = null
    var targetName: JTextField? = null

    init {
        if (template != null) {
            initView(template)
        }

        btnSave!!.addActionListener {
            val templateLanguage: TemplateLanguage
            val templateType: TemplateType
            val codeLanguage: CodeLanguage
            val tempStr = templateEditor!!.text
            val targetNameStr = targetName!!.text
            val tempNameStr = tempName!!.text
            when (this@ToolBoxSettingPanel.tempLanguage!!.selectedIndex) {
                0 -> templateLanguage = TemplateLanguage.Vm
                1 -> templateLanguage = TemplateLanguage.Groovy
                else -> templateLanguage = TemplateLanguage.Vm
            }
            when (this@ToolBoxSettingPanel.targetType!!.selectedIndex) {
                0 -> templateType = TemplateType.CodeBlock
                1 -> templateType = TemplateType.File
                2 -> templateType = TemplateType.Clipboard
                else -> templateType = TemplateType.CodeBlock
            }
            when (this@ToolBoxSettingPanel.targetLanguage!!.selectedIndex) {
                0 -> codeLanguage = CodeLanguage.Java
                1 -> codeLanguage = CodeLanguage.Kotlin
                else -> codeLanguage = CodeLanguage.Java
            }
            val result = CodeTemplate(templateType, tempNameStr, targetNameStr, codeLanguage, tempStr, templateLanguage)
            ServiceManager.getService(ToolboxSettings::class.java).mCodeTemplates[result.templateName] = result
        }

        cancelBtn!!.addActionListener { }
    }

    private fun initView(template: CodeTemplate) {
        templateEditor!!.text = template.tempStr
        if (template.codeLanguage === CodeLanguage.Java) {
            targetLanguage!!.setSelectedIndex(0)
        } else {
            targetLanguage!!.setSelectedIndex(1)
        }
        if (template.templateLanguage === TemplateLanguage.Groovy) {
            tempLanguage!!.setSelectedIndex(1)
        } else {
            tempLanguage!!.setSelectedIndex(0)
        }
        when (template.templateType) {
            TemplateType.File -> targetType!!.setSelectedIndex(1)
            TemplateType.Clipboard -> targetType!!.setSelectedIndex(2)
            TemplateType.CodeBlock -> targetType!!.setSelectedIndex(0)
        }
        targetName!!.text = template.targetClassNameTemp
        tempName!!.text = template.templateName
    }
}
