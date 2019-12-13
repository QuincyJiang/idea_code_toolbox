package decoder

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassOwner
import com.intellij.psi.PsiJavaFile
import model.ClassStruct
import model.HiidoStaticSheet
import model.Method
import model.Param
import utils.adjustSlash
import utils.getFields
import utils.getImportList
import utils.getMethods

/**
 * 转换器 将VFS文件 （可以是一个目标类， 一个excel文件，或者一个proto文件） 转为抽象的Class实体
 * */

interface IConvert {

    fun convert2Class(input: Any): ClassStruct?
}

// 用来解析埋点表格数据
class Excel2ClassInterfaceConvert: IConvert {

    override fun convert2Class(input: Any): ClassStruct? {
        if (input !is HiidoStaticSheet) {
            throw IllegalArgumentException("Input is not an HiidoStaticSheet type ")
        }
        var classStruct = ClassStruct::class.java.newInstance()
        classStruct.name = "AutoGeneratedCode"
        classStruct.comment = "Auto generated code, do not edit"
        classStruct.type = "Class"
        var methods = ArrayList<Method>()
        // todo  构造ClassStruct
        input.modelList.forEach{ hiidoModel ->
            val method = Method::class.java.newInstance()
            method.name = "reportEvent${hiidoModel.lable.adjustSlash()}"
            method.modifier = "public"
            method.returnType = "void"
            if (!hiidoModel.remark.isEmpty()) {
                method.comment = hiidoModel.remark
            }
            val params = ArrayList<Param>()
            hiidoModel.keyList.forEach {
                val param = Param(it.key, "String")
                params.add(param)
            }
            method.params = params
            methods.add(method)
        }
        classStruct.methods = methods
        return classStruct
    }
}
/**
 * Method
 * 方法的抽象实体
 * @param name 方法名 eg. fun() 的 fun
 * @param modifier 方法修饰符  eg . public fun() 的 public
 * @param returnType 返回值类型 eg. public fun(): Int 里的 Int
 * @param params 方法入参 eg . public fun(arg1: String, arg2: String): Int 里的 arg1, arg2
 * @param content 方法体
 * */

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