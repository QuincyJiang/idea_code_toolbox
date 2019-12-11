package actions

import ToolboxSettings
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.project.DumbAware
import java.util.*

class CodeGeneratorGroup: ActionGroup(), DumbAware {
    private val settings = ServiceManager.getService(ToolboxSettings::class.java)

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        if (e == null) {
            return AnAction.EMPTY_ARRAY
        }
        PlatformDataKeys.PROJECT.getData(e.dataContext) ?: return AnAction.EMPTY_ARRAY
        val children = ArrayList<AnAction>()
        // fixme npe
        settings.mCodeTemplates.forEach { key, (_) -> children.add(getOrCreateAction(key)) }

        return children.toTypedArray()
    }

    /**
     * 解析Settings已保存的生成模板 动态注册到ActionManager中
     * */
    private fun getOrCreateAction(key: String): AnAction {
        val actionId = "com.jiangxq.toolbox.CodeGeneratorGroup.Action.$key"
        var action: AnAction? = ActionManager.getInstance().getAction(actionId)
        if (action == null) {
            action = TempCodeGeneratorAction(key)
            ActionManager.getInstance().registerAction(actionId, action)
        }
        return action
    }
}