package actions

import ToolboxSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiDocumentManager
import generator.Excel2ClassInterfaceConvert
import model.GeneratedSourceCode
import model.HiidoStaticExcel
import model.HiidoStaticSheet
import ui.ChooseSheetDialog
import ui.ConfirmCodeDialog
import ui.onConfirmListener
import ui.onSelectListener
import utils.readExcel
import utils.reformatJavaFile
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
                val chooseSheetDialog = ChooseSheetDialog(staticExcel.sheets, object : onSelectListener {
                    override fun onSelected(sheet: HiidoStaticSheet?) {
                        sheet?.let { selectedSheet ->
                            val classStruct = converter.convert2Class(selectedSheet)
                            val template = settings.mCodeTemplates[templateKey]
                            val generator = settings.mVmSourceGenerator
                            val result = generator.combine(template!!, classStruct!!, null)
                            val codeConfirmDialog = ConfirmCodeDialog(result, object : onConfirmListener {
                                override fun onSelected(code: GeneratedSourceCode?) {
                                    code?.let { generatedSourceCode ->
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

    fun insertCode(code: GeneratedSourceCode, e: AnActionEvent) {
        val editor = e.getData(PlatformDataKeys.EDITOR)
        val project = e.getData(PlatformDataKeys.PROJECT)
        if (editor == null || project == null) {
            return
        }
        //获取SelectionModel和Document对象
        val selectionModel = editor.selectionModel
        val document = editor.document
        //得到选中字符串的结束位置
        val endOffset = selectionModel.selectionEnd
        // 插入截止index为document -1
        val maxOffset = document.textLength - 1
        // 获取光标选中行
        val curLineNumber = document.getLineNumber(endOffset)
        // 插入到选中行的下一行
        val nextLineStartOffset = document.getLineStartOffset(curLineNumber + 1)
        //计算字符串的插入位置
        val insertOffset = if (maxOffset > nextLineStartOffset) nextLineStartOffset else maxOffset
        val runnable = Runnable {
            // post 一个runnable 去执行插入代码操作
            document.insertString(insertOffset, code.sourceCode)
            // document操作之后必须commit 再执行代码format 不然会报错
            PsiDocumentManager.getInstance(project).commitDocument(document)
            val psiFile = e.getData(CommonDataKeys.PSI_FILE)
            psiFile?.let {
                reformatJavaFile(it)
            }
        }
        //加入任务，由IDEA调度执行这个任务
        WriteCommandAction.runWriteCommandAction(project, runnable)

    }
}
