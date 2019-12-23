package actions

import ToolboxSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import generator.Excel2ClassInterfaceConvert
import model.GeneratedSourceCode
import model.HiidoStaticExcel
import model.HiidoStaticSheet
import ui.ChooseSheetDialog
import ui.ConfirmCodeDialog
import utils.OnConfirmListener
import utils.insertCode
import utils.readExcel
import java.io.File


/**
 * 动态注册的模板代码解析action
 * */
class HiidoCodeGeneratorAction internal constructor(private val templateKey: String) : AnAction(), DumbAware {

    private val settings: ToolboxSettings = ServiceManager.getService(ToolboxSettings::class.java)

    init {
        templatePresentation.description = "CodeGenerator HiidoStaticCode Actions"
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
        //选择埋点表格文件
        chooseExcelFile(e)
    }

    private fun chooseExcelFile(e: AnActionEvent) {
        val fileChooseDescriptor = FileChooserDescriptor(
            true, false,
            false, false, false, false
        )
        fileChooseDescriptor.description = "选择埋点模板Excel文件"
        fileChooseDescriptor.title = "选择Excel文件"
        FileChooser.chooseFile(fileChooseDescriptor, e.project, null) {
            val excelFile = File(it.path)
            val excel: HiidoStaticExcel? = readExcel(excelFile)
            val converter = Excel2ClassInterfaceConvert()
            excel?.let { staticExcel ->
                val chooseSheetDialog = ChooseSheetDialog(staticExcel.sheets, object : OnConfirmListener<HiidoStaticSheet?> {
                    override fun onConfirm(result: HiidoStaticSheet?) {
                        result?.let { selectedSheet ->
                            val classStruct = converter.convert2Class(selectedSheet)
                            val template = settings.mCodeTemplates[templateKey]
                            val generator = settings.mVmSourceGenerator
                            val sourceCode = generator.combine(template!!, classStruct!!)
                            val codeConfirmDialog = ConfirmCodeDialog(sourceCode, object : OnConfirmListener<GeneratedSourceCode?> {
                                override fun onConfirm(result: GeneratedSourceCode?) {
                                    result?.let { generatedSourceCode ->
                                        insertCode(generatedSourceCode, e)
                                    }
                                }
                            })
                            codeConfirmDialog.isVisible = true
                        }
                    }
                })
                chooseSheetDialog.isVisible = true
            }
        }
    }
}
