package utils

import actions.CreateFileRunnable
import com.google.common.collect.Lists
import com.intellij.ide.util.DirectoryChooser
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.javadoc.PsiDocTokenImpl
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilBase
import model.*
import java.awt.Toolkit
import java.awt.event.ActionEvent
import java.awt.event.KeyEvent
import javax.swing.Action
import javax.swing.JDialog


val TAG = "Utils"

/**
 * 弹窗选择文件
 * */
fun chooseFile(project: Project, title: String, desc: String, callback: OnFileSelected) {
    val fileChooseDescriptor = FileChooserDescriptor(true, false,
        false, false, false, false)
    fileChooseDescriptor.description = desc
    fileChooseDescriptor.title = title
    FileChooser.chooseFile(fileChooseDescriptor, project, null) {
        callback.onFileSeleceted(it.path)
    }
}

interface OnFileSelected {
    fun onFileSeleceted(path: String)
}


/**
 * 选择目标class文件
 * */
fun chooseClass(project: Project, defaultClass: PsiClass): PsiClass {
    val choose = TreeClassChooserFactory.getInstance(project)
        .createProjectScopeChooser("选择一个class文件" , defaultClass)
    choose.showDialog()
    return choose.selected
}

fun getSourcePath(clazz: PsiClass): String {
    val containingFile = clazz.containingFile
    return getSourcePath(containingFile)
}

/**
 * 获取源代码路径
 * */
fun getSourcePath(psiFile: PsiFile): String {
    val classPath = psiFile.virtualFile.path
    return classPath.substring(0, classPath.lastIndexOf('/'))
}


/**
 * 生成class路径
 * */
fun generateClassPath(sourcePath: String, className: String, extension: String = "java"): String {
    return "$sourcePath/$className.$extension"
}

/**
 * 获取目标类的importList
 * */
fun getImportList(javaFile: PsiJavaFile): List<String>? {
    return javaFile.importList?.importStatements?.map { it.qualifiedName?:""}
}

fun getExtendList(javaFile: PsiClass): List<String>? {
    return javaFile.extendsList?.referenceElements?.map {
        it.qualifiedName
    }
}

fun getImplementsList(javaFile: PsiClass): List<String>? {
    return javaFile.implementsList?.referenceElements?.map {
        it.qualifiedName
    }
}

/**
 * 获取PsiClass的所有成员变量
 * */
fun getFields(psiClass: PsiClass): List<Field> {
    return psiClass.fields.map {
            psiField ->
        Field(
            psiField.type.presentableText,
            psiField.name,
            if (psiField.modifierList == null) "" else psiField.modifierList!!.text,
            getDocCommentText(psiField)
        )
    }
}

/**
 * 获取PsiClass的所有方法
 * */
fun getMethods(psiClass: PsiClass): List<Method> {
    return psiClass.methods.map { psiMethod ->
        // 获取PSI 方法参数列表
        val parameters :List<Param> = if (psiMethod.parameterList.isEmpty) ArrayList()
        else psiMethod.parameterList.parameters.filter { it.name != null && !it.name!!.isEmpty() }.map {
            psiParameter ->
            Param(psiParameter.name!!, psiParameter.type.presentableText, getParamsComment(psiMethod, psiParameter))
        }
        // 获取PSI 方法返回值类型
        val returnType = if (psiMethod.returnType == null)
            ""
        else
            psiMethod.returnType!!.presentableText
        // 构造Method model对象
        Method(
            psiMethod.name, psiMethod.modifierList.text,
            returnType, parameters,
            parameters.toParmsStr(),
            psiMethod.body?.text?: "",
            getMethodComments(psiMethod)
        )
    }
}

private fun getMethodComments(method: PsiMethod?): ArrayList<String> {
    val comments = ArrayList<String>()
    method?.docComment?.descriptionElements?.forEach {
        it.text?.let { text ->
            if (!text.trim().isEmpty()) {
                comments.add(text)
            }
        }
    }
    return comments
}

/**
 * 查找param的注释内容
 * */
private fun getParamsComment(method: PsiMethod?, param: PsiParameter): String? {
    return  method?.docComment?.findTagsByName("param")
        ?.filter { it.valueElement != null && it.valueElement!!.text.contains(param.name?:"")  }
        ?.flatMap { it.dataElements.asIterable() }
        ?.filter { element -> element is PsiDocTokenImpl && !element.text.trim().isEmpty()}
        ?.map { it.text.trim() }
        ?.toString()?.replace("[", "")?.replace("]", "")
}


/**
 * 根据PSI METHOD 查找父类
 */
fun findClassNameOfSuperMethod(psiMethod: PsiMethod): String? {
    val superMethods = psiMethod.findDeepestSuperMethods()
    if (superMethods.isNotEmpty()) {
        return superMethods[0].containingClass?.qualifiedName
    }
    return null
}

/**
 * 递归查找Element下的所有class
 *
 * @param element the Element
 * @return the Classes
 */
fun getClasses(element: PsiElement): List<PsiClass> {
    val elements = Lists.newArrayList<PsiClass>()
    val classElements = PsiTreeUtil.getChildrenOfTypeAsList(element, PsiClass::class.java)
    elements.addAll(classElements)
    for (classElement in classElements) {
        elements.addAll(getClasses(classElement))
    }
    return elements
}

/**
 * 获取class类型参数
 * */
fun getClassTypeParameters(psiClass: PsiClass): List<String> {
    return psiClass.typeParameters.map{ it.name?: "" }
}

val specialMDChar = listOf(
    '<', '>', '`', '*', '_', '{',
    '}', '[', ']', '(', ')', '#', '+', '-', '.', '!'
)

/**
 * 处理一下特殊字符
 * */
fun escapeMarkdown(str: String): String {
    val result = StringBuilder()
    for (ch in str.toCharArray()) {
        if (specialMDChar.contains(ch)) {
            result.append('\\')
        }
        result.append(ch)
    }
    return result.toString()
}

/**
 * 获取注释文档
 * */
private fun getDocCommentText(psiField: PsiField): ArrayList<String>? {
    if (psiField.docComment == null) {
        return null
    }
    val content = ArrayList<String>()
    for (element in psiField.docComment!!.descriptionElements) {
        content.addAll(element.text.split("\n"))
    }
    return ArrayList(content.filter { !it.trim().isEmpty() })
}

fun getDocCommentText(psiClass: PsiClass): ArrayList<String>? {
    if (psiClass.docComment == null) {
        return null
    }
    val comments = ArrayList<String>()
    psiClass.docComment!!.descriptionElements.forEach {
        comments.addAll(it.text.split("\n"))
    }
    return ArrayList(comments.filter { !it.trim().isEmpty() })
}


/**
 * 获取字符缩进
 * */
private fun findJavaCodeTextOffset(theElement: PsiElement): Int {
    if (theElement.children.size < 2) {
        throw IllegalStateException("Can not find offset of java code")
    }
    return theElement.children[1].textOffset
}

/**
 * 获取文档缩进
 * */
private fun findJavaDocTextOffset(theElement: PsiElement): Int {
    val javadocElement = theElement.firstChild as? PsiDocComment
        ?: throw IllegalStateException("Cannot find element of type PsiDocComment")
    return javadocElement.textOffset
}

fun pushPostponedChanges(element: PsiElement) {
    val editor = PsiUtilBase.findEditor(element.containingFile)
    if (editor != null) {
        PsiDocumentManager.getInstance(element.project)
            .doPostponedOperationsAndUnblockDocument(editor.document)
    }
}

/**
 * 格式化java文档
 * */
fun reformatJavaDoc(theElement: PsiElement) {
    val codeStyleManager = CodeStyleManager.getInstance(theElement.project)
    try {
        val javadocTextOffset = findJavaDocTextOffset(theElement)
        val javaCodeTextOffset = findJavaCodeTextOffset(theElement)
        codeStyleManager.reformatText(
            theElement.containingFile, javadocTextOffset,
            javaCodeTextOffset + 1
        )
    } catch (e: Exception) {
        Logger.getInstance(TAG).error("reformat code failed", e)
    }

}

/**
 * 格式化java代码
 * */
fun reformatJavaFile(theElement: PsiElement) {
    val codeStyleManager = CodeStyleManager.getInstance(theElement.project)
    try {
        codeStyleManager.reformat(theElement)
    } catch (e: Exception) {
        Logger.getInstance(TAG).error("reformat Java file failed", e)
    }
}

/**
 * 设置为居中dialog
 * */
fun JDialog.centerDialog(width: Int, height: Int) {
    this.setSize(width, height)
    val screenSize = Toolkit.getDefaultToolkit().screenSize //获取屏幕的尺寸
    val screenWidth = screenSize.width //获取屏幕的宽
    val screenHeight = screenSize.height //获取屏幕的高
    this.setLocation(screenWidth / 2 - width / 2, screenHeight / 2 - height / 2)//设置窗口居中显示
}

/**
 * 返回光标所在的最内层psiElement
 * */
fun getElement(editor: Editor, psiFile: PsiFile): PsiElement? {
    return psiFile.findElementAt(editor.caretModel.offset)
}

/**
 * 根据光标位置查找符合要求的psiElement
 * 比如 当前光标在一个PsiMethod
 * 但是要拿到它实际所在的class对象 就要用该方法往上递归查找
 * */

fun <T : PsiElement> getElement(editor: Editor, target: Class<T>): T? {
    val element = PsiUtilBase.getElementAtCaret(editor) ?: return null
    return PsiTreeUtil.getParentOfType(element, target)
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

fun chooseDirectory(
    targetDirectories: Array<PsiDirectory>,
    initialDirectory: PsiDirectory?,
    project: Project,
    relativePathsToCreate: Map<PsiDirectory, String>,
    listener: OnConfirmListener<VirtualFile?>
) {
    val SHOW_SOURCE_CODE = 555
    val chooser = object : DirectoryChooser(project) {
        override fun createLeftSideActions(): Array<Action> {
            val action = object : DialogWrapper.DialogWrapperAction("Show Source") {
                override fun doAction(e: ActionEvent) {
                    close(SHOW_SOURCE_CODE)
                }
            }
            action.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S)
            action.putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY, 0)
            action.putValue(Action.LONG_DESCRIPTION, "Show Generate Source")
            return arrayOf(action)
        }
    }
    chooser.title = "选择目标代码生成路径"
    chooser.fillList(
        targetDirectories,
        initialDirectory,
        project,
        relativePathsToCreate
    )
    chooser.show()
    if (chooser.isOK && chooser.selectedDirectory != null)
        chooser.selectedDirectory?.virtualFile?.let {
            listener.onConfirm(it)
        }
}

fun saveToFile(
    anActionEvent: AnActionEvent,
    language: CodeLanguage,
    className: String,
    content: String,
    currentClass: ClassStruct,
    destination: VirtualFile?
) {
    val sourcePath = destination?.path + "/" + currentClass.packageName.replace(".", "/")
    val targetPath = generateClassPath(sourcePath, className, language(language) )
    val manager = VirtualFileManager.getInstance()
    val virtualFile = manager
        .refreshAndFindFileByUrl(VfsUtil.pathToUrl(targetPath))
    if (virtualFile == null || !virtualFile.exists() || userConfirmedOverride()) {
        // runnable 去写file
        ApplicationManager.getApplication().runWriteAction(
            CreateFileRunnable(
                targetPath, content, "UTF-8", anActionEvent
                    .dataContext
            )
        )
    }
}

private fun language(language: CodeLanguage): String {
    return when(language) {
        CodeLanguage.Java -> "java"
        CodeLanguage.Kotlin -> "kt"
    }
}

private fun userConfirmedOverride(): Boolean {
    return Messages.showYesNoDialog("Overwrite?", "File Exists", null) == Messages.YES
}