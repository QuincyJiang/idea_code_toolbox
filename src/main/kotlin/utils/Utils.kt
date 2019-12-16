package utils

import com.google.common.collect.Lists
import com.intellij.ide.util.TreeClassChooserFactory
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.javadoc.PsiDocComment
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiUtilBase
import model.Field
import model.Method
import model.Param

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
fun getImportList(javaFile: PsiJavaFile): List<String> {
    val importList = javaFile.importList ?: return ArrayList()
    return importList.importStatements.map { it.qualifiedName?: "" }
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
        else psiMethod.parameterList.parameters.map {
            psiParameter ->
            Param(psiParameter.name?: "", psiParameter.modifierList?.text ?: "", null)
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
            null
        )
    }
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
        content.add(element.text)
    }
    return content
}

fun getDocCommentText(psiClass: PsiClass): ArrayList<String>? {
    if (psiClass.docComment == null) {
        return null
    }
    val content = ArrayList<String>()
    for (element in psiClass.docComment!!.descriptionElements) {
        content.add(element.text)
    }
    return content
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
