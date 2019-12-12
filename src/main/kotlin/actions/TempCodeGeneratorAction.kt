package actions

import ToolboxSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService

/**
 * 动态注册的模板代码解析action
 * */
class TempCodeGeneratorAction internal constructor(private val templateKey: String) : AnAction(), DumbAware {

    private val settings: ToolboxSettings = ServiceManager.getService(ToolboxSettings::class.java)
     init {
         templatePresentation.description = "CodeGenerator Dynamic Actions"
         templatePresentation.setText(templateKey, false)
     }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        // 如果在index 或者 sync 给个友好提示
        val dumbService = DumbService.getInstance(project)
        if (dumbService.isDumb) {
            dumbService.showDumbModeNotification("Wait until index is finish")
            return
        }
        val codeTemplate = settings.mCodeTemplates[templateKey]
        //todo 根据选中template解析 模板并生成代码
    }
}
