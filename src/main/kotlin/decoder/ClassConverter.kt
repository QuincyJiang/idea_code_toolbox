package decoder

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiJavaFile
import model.ClassStruct
import utils.getFields
import utils.getImportList
import utils.getMethods

/**
 * 转换器 将VFS文件 （可以是一个目标类， 一个excel文件，或者一个proto文件） 转为抽象的Class实体
 * */

interface IConvert {

    fun convert2Class(input: Any): ClassStruct?
}

class Excel2ClassConvert: IConvert {

    override fun convert2Class(input: Any): ClassStruct? {
        // todo Excel 转 ClassStruct
        return null
    }
}

class Pb2ClassConvert: IConvert {
    override fun convert2Class(input: Any): ClassStruct? {
        // todo Pb文件 转 ClassStruct
        return null
    }
}

/**
 * Java对象转ClassStruct转换器
 * */
class Java2ClassConvert: IConvert {
    override fun convert2Class(input: Any): ClassStruct? {
        if (input is PsiClass) {
            val psiFile = input.containingFile
            val classType = input.classKind.name
            val className = input.name
            val comment = input.docComment?.text
            val extends = input.extendsList?.referenceElements?.map {
                it.qualifiedName
            }
            val implements = input.implementsList?.referenceElements?.map {
                it.qualifiedName
            }
            val packageName = ((psiFile as PsiClassOwner).packageName)
            val fields = (getFields(input))
            if (psiFile !is PsiJavaFile) throw Exception("Not a java file!")
            val importList = getImportList(psiFile)
            val methods = (getMethods(input))
            return ClassStruct(className?: "", classType, comment,extends?.get(0)?: "", implements, packageName,
             importList, fields, methods)
        } else throw IllegalArgumentException("Use Java2ClassCovert need an PsiJavaFile type input")
    }
}