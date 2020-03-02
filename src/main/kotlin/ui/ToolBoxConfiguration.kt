package ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ui.Messages
import model.CodeLanguage
import model.CodeTemplate
import model.TemplateLanguage
import model.TemplateType
import settings.ToolboxSettings
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTabbedPane

class ToolBoxConfiguration(settings: ToolboxSettings?) {
    var tabbedPanel : JTabbedPane? = null
    var mainPanel: JPanel? = null

    init {
        settings?.mCodeTemplates?.forEach { _, u ->
            addNewTab(u)
        }
        addPlusTab()
        selectTab(0)
    }
    private fun addNewTab(codeTemplate: CodeTemplate): Int {
        return addNewTab(codeTemplate, 0)
    }

    private fun addNewTab(codeTemplate: CodeTemplate, indexFromEnd: Int): Int {
        val index = tabbedPanel!!.tabCount - indexFromEnd
        val editPannel = ToolBoxSettingPanel(codeTemplate)
        tabbedPanel!!.insertTab(codeTemplate.templateName, null, editPannel.mainPanel, null, index)
        tabbedPanel!!.setTabComponentAt(index, tabTitleComponent(codeTemplate))
        return index
    }

    private fun tabTitleComponent(codeTemplate: CodeTemplate): Component {
        return JPanel(
            FlowLayout(
                FlowLayout.CENTER, 0, 0)
        ).apply {
            add(JLabel(codeTemplate.templateName).apply { isOpaque = false })
            isOpaque = false
            add(JLabel(AllIcons.Actions.Close).apply {
                isOpaque = false
                toolTipText = "删除模板"
                addMouseListener(object : MouseListener {
                    override fun mouseClicked(e: MouseEvent?) {
                        deleteTemplate(codeTemplate.templateName)
                    }

                    override fun mouseReleased(e: MouseEvent?) {

                    }

                    override fun mouseEntered(e: MouseEvent?) {
                    }

                    override fun mouseExited(e: MouseEvent?) {
                    }

                    override fun mousePressed(e: MouseEvent?) {
                    }
                })
            })
        }
    }

    private fun deleteTemplate(title: String) {
        val result = Messages.showYesNoDialog("删除模板 '$title' ?", "Delete", null)
        if (result == Messages.OK) {
            ServiceManager.getService(ToolboxSettings::class.java).mCodeTemplates.remove(title)
            refresh()
        }
    }

    private fun refresh() {
        tabbedPanel!!.removeAll()
        ServiceManager.getService(ToolboxSettings::class.java).mCodeTemplates.forEach { _, u ->
            addNewTab(u)
        }
        addPlusTab()
    }

    private fun addPlusTab() {
        tabbedPanel?.addTab("+", JPanel())
        val addBtn = JLabel(AllIcons.Welcome.CreateNewProject)
        addBtn.toolTipText = "创建新模板"
        addBtn.addMouseListener(object : MouseListener{
            override fun mouseClicked(e: MouseEvent?) {
                createNewTemplate()
            }

            override fun mouseReleased(e: MouseEvent?) {

            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseExited(e: MouseEvent?) {
            }

            override fun mousePressed(e: MouseEvent?) {
            }
        })
        val index = tabbedPanel!!.tabCount - 1
        tabbedPanel!!.setTabComponentAt(index, addBtn)
        tabbedPanel!!.setEnabledAt(index, false)
    }
    private fun createNewTemplate() {
        val template = CodeTemplate(
            TemplateType.CodeBlock, "Untitled", "", CodeLanguage.Java,
            "", TemplateLanguage.Vm)
        val index = addNewTab(template, 1)
        selectTab(index)
    }
    private fun selectTab(index: Int) {
        var _index = index
        if (_index >= (tabbedPanel!!.tabCount - 1)) {
            _index = tabbedPanel!!.tabCount -2
        }
        if (_index < 0) {
            _index = 0
        }
        tabbedPanel?.selectedIndex = _index
    }
    fun createUIComponents() {
        tabbedPanel = JTabbedPane()
    }
}