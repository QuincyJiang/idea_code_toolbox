package decoder

import com.intellij.openapi.vfs.VirtualFile
import model.Class

/**
 * 转换器 将VFS文件 （可以是一个目标类， 一个excel文件，或者一个proto文件） 转为抽象的Class实体
 * */

interface IConvert {

    fun convert2Class(input: VirtualFile): Class?
}

class Excel2ClassConvert: IConvert {

    override fun convert2Class(input: VirtualFile): Class? {
        return null
    }
}

class Pb2ClassConvert: IConvert {
    override fun convert2Class(input: VirtualFile): Class? {
        return null
    }
}