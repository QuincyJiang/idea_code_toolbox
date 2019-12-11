package actions
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.Messages

/**
 * 主菜单
 * */
class MainMenuAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val fileChooseDescriptor = FileChooserDescriptor(true, false,
            false, false, false, false)
        fileChooseDescriptor.description = "选择埋点excel文件路径"
        fileChooseDescriptor.title = "选择excel路径"
        FileChooser.chooseFile(fileChooseDescriptor, e.project, null) {
            Messages.showMessageDialog(e.project, "路径结果 ${it.path}", "路径", Messages.getInformationIcon() )
        }
    }
}
