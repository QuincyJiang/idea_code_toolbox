package actions

import ToolboxSettings
import com.intellij.designer.clipboard.SimpleTransferable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.roots.JavaProjectRootsUtil
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.refactoring.PackageWrapper
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil
import generator.Java2ClassConvert
import model.*
import ui.ConfirmCodeDialog
import utils.*
import java.awt.datatransfer.DataFlavor
import java.util.*


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
        codeTemplate?.let {
            // 根据模板预设类型判断操作
            when (it.templateType) {
                // 生成代码段 默认插在当前光标的下一行
                TemplateType.CodeBlock -> {
                    generateCodeBlock(e, it)
                }
                // 生成新的文件 弹路径选择对话框 默认文件路径与当前光标所选文件在同一包下
                TemplateType.File -> {
                    generateNewFile(e, it)
                }
                // 生成的代码拷贝在clipboard中
                TemplateType.Clipboard -> {
                    copyToClipboard(e, it)
                }
            }
        }
    }

    private fun generateCodeBlock(e: AnActionEvent, codeTemplate:CodeTemplate) {
        val generatedSourceCode = generateSourceCodeFromEditor(e, codeTemplate)
        generatedSourceCode?.generatedSourceCode?.let {
            insertCode(it, e)
        }
    }

    private fun generateNewFile(e: AnActionEvent, codeTemplate:CodeTemplate) {
        val sourceCodeBundle = generateSourceCodeFromEditor(e, codeTemplate)
        sourceCodeBundle?.generatedSourceCode?.let {
            val codeConfirmDialog = ConfirmCodeDialog(it, object : OnConfirmListener<GeneratedSourceCode?> {
                override fun onConfirm(result: GeneratedSourceCode?) {
                    result?.let { generatedSourceCode ->
                        generatedSourceCode.className.let { className ->
                            val project = e.project
                            val psiFile = e.getData(CommonDataKeys.PSI_FILE)?.containingDirectory // origin file directory
                            project?.let { javaProject ->
                                val targetDirectories = LinkedHashSet<PsiDirectory>()
                                val relativePathsToCreate = HashMap<PsiDirectory, String>()
                                MoveClassesOrPackagesUtil.buildDirectoryList(
                                    PackageWrapper(PsiManager.getInstance(javaProject), className),
                                    JavaProjectRootsUtil.getSuitableDestinationSourceRoots(javaProject),
                                    targetDirectories,
                                    relativePathsToCreate
                                )
                                chooseDirectory(targetDirectories.toArray(PsiDirectory.EMPTY_ARRAY), psiFile, project, relativePathsToCreate, object : OnConfirmListener<VirtualFile?> {
                                    override fun onConfirm(result: VirtualFile?) {
                                        saveToFile(
                                            e,
                                            codeTemplate.codeLanguage,
                                            sourceCodeBundle.generatedSourceCode.className,
                                            sourceCodeBundle.generatedSourceCode.sourceCode,
                                            sourceCodeBundle.classStruct,
                                            result
                                        )
                                    }
                                })
                            }
                        }
                    }
                }
            })
            codeConfirmDialog.isVisible = true
        }
    }

    private fun copyToClipboard(e: AnActionEvent, codeTemplate:CodeTemplate) {
        val codeBundle = generateSourceCodeFromEditor(e, codeTemplate)
        codeBundle?.generatedSourceCode?.let {
            val codeConfirmDialog = ConfirmCodeDialog(it, object : OnConfirmListener<GeneratedSourceCode?> {
                override fun onConfirm(result: GeneratedSourceCode?) {
                    result?.let { generatedSourceCode ->
                        CopyPasteManager.getInstance()
                            .setContents(SimpleTransferable(generatedSourceCode.sourceCode, DataFlavor.stringFlavor))
                        Messages.showMessageDialog(e.project,"","代码已成功拷贝到剪贴板",Messages.getInformationIcon())
                    }
                }
            })
            codeConfirmDialog.isVisible = true
        }
    }

    // 从光标选中文件获取classStruct并合并为目标代码
    private fun generateSourceCodeFromEditor(e: AnActionEvent, codeTemplate:CodeTemplate): SourceCodeBundle? {
        e.getData(CommonDataKeys.PSI_FILE)?.let {
            e.getData(PlatformDataKeys.EDITOR)?.let { editor ->
                // 拿到光标最外层的psiClass 目的是要用这个psiClass构造classStruct
                val psiClass = getElement(editor, PsiClass::class.java)
                psiClass?.let {
                    val converter = Java2ClassConvert()
                    // 获取classStruct结构化数据
                    val classStruct = converter.convert2Class(it)
                    classStruct?.let { struct ->
                        // 获取codeGenerator
                        val generator = if (codeTemplate.templateLanguage == TemplateLanguage.Vm) {
                            settings.mVmSourceGenerator
                        }  else {
                            settings.mGroovySourceGenerator
                        }
                        return SourceCodeBundle(struct, codeTemplate, generator.combine(codeTemplate, struct))
                    }
                }
            }
        }?: return null
    }
}
