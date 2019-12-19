package utils

import generator.Excel2ClassInterfaceConvert
import generator.VelocityTemplateGenerator
import getDefaultTemplates
import model.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.IOException


/**
 * 读取excel 并解析出HiidoStaticModel
 * */
fun readExcel(file: File): HiidoStaticExcel?  {
    val fileInputStream = FileInputStream(file)
    val sheetsMap = LinkedHashMap<String, HiidoStaticSheet>()
    val sheetsIndexMap = LinkedHashMap<String, HiidoStaticSheetsIndex>()
    val workbook = XSSFWorkbook(fileInputStream)

    // 第一次遍历 获取每个sheet的索引表
    workbook.iterator().forEach { xssfSheet ->
        // 获取第一行表头
        xssfSheet.forEachIndexed { index, xssfRow ->
            // 遍历每个sheet的row 第一次遇到包含'Label'标签的单元格时，开始计算index索引
              xssfRow.forEach loop@{
                if ( it.cellType == CellType.STRING && it.stringCellValue == "Label") {
                    val sheetIndex = HiidoStaticSheetsIndex::class.java.newInstance()
                    sheetIndex.keysIndexMap = LinkedHashMap()
                    sheetIndex.titleRowIndex = index
                    xssfRow.forEachIndexed{index, cell ->
                        when(cell.stringCellValue) {
                            // 将所有标签index保存起来
                            "Label" -> sheetIndex.labelIndex = index
                            "Property" -> sheetIndex.propertyIndex = index
                            "Type"-> sheetIndex.typeIndex = index
                            "Remark"-> sheetIndex.remarkIndex = index
                        }
                        // 将所有key的index保存起来
                        if (cell.stringCellValue.contains("Key")) {
                            sheetIndex.keysIndexMap[cell.stringCellValue] = index
                        }
                    }
                    // 保存xlsx文件的所有sheet 以及每个sheet对应的title index索引
                    sheetsIndexMap[xssfSheet.sheetName] = sheetIndex

                    return@loop
                }
            }
        }
    }
    // 第二次遍历 构造HiidoStaticExcel对象
    try {
        workbook.iterator().forEach { xssfSheet ->
            val hiidoStaticSheet = HiidoStaticSheet::class.java.newInstance()
            hiidoStaticSheet.modelList = ArrayList()
            //拿到当前sheet索引表
            val sheetIndex = sheetsIndexMap[xssfSheet.sheetName]
            xssfSheet.forEachIndexed outerLoop@ {index, xssfSeetRow ->
                // 出去表头行 解析其余行 生成HiidoSaticSheet
                if (index > sheetIndex!!.titleRowIndex) {
                    val hiidoStaticModel = HiidoStaticModel::class.java.newInstance()
                    hiidoStaticModel.lable = xssfSeetRow.getCell(sheetIndex.labelIndex).getCellContent()
                    hiidoStaticModel.property = xssfSeetRow.getCell(sheetIndex.propertyIndex).getCellContent()
                    hiidoStaticModel.type = xssfSeetRow.getCell(sheetIndex.typeIndex).getCellContent()
                    hiidoStaticModel.remark = xssfSeetRow.getCell(sheetIndex.remarkIndex).getCellContent()
                    val keys = ArrayList<HiidoStaticKey>()
                    // 遍历key列表
                    sheetIndex.keysIndexMap.forEach{ key, keysIndex ->
                        // 当前key所在单元格内容非空 保存在keys列表中
                        val keyContent = xssfSeetRow.getCell(keysIndex).getCellContent()
                        if (!keyContent.isEmpty()) {
                            val hiidoStaticKey = HiidoStaticKey::class.java.newInstance()
                            hiidoStaticKey.key = key.toLowerCase()
                            hiidoStaticKey.value = keyContent
                            keys.add(hiidoStaticKey)
                        }
                    }
                    hiidoStaticModel.keyList = keys
                    hiidoStaticSheet.modelList.add(hiidoStaticModel)
                }
            }
            sheetsMap[xssfSheet.sheetName] = hiidoStaticSheet
        }
        return HiidoStaticExcel(sheetsMap)
    } catch (e: Exception) {
       e.printStackTrace()
    } finally {
        fileInputStream.close()
    }
    return null
}

fun Cell?.getCellContent(): String {
    if (this != null) {
        when (this.cellType) {
            CellType.STRING -> return this.stringCellValue
            CellType.BLANK -> return ""
            CellType.BOOLEAN -> return this.booleanCellValue.toString()
            CellType.ERROR -> return ""
            CellType.NUMERIC -> return this.numericCellValue.toString()
            CellType.FORMULA -> return ""
        }
    }
    return ""
}


@Throws(IOException::class)
fun main(args: Array<String>?) {
    val file = File("src/main/resources/temp/test.xlsx")
    val excel = readExcel(file)
    val converter = Excel2ClassInterfaceConvert()
    val classStruce = converter.convert2Class(excel!!.sheets["6.6"]!!)
    val codeGenerator = VelocityTemplateGenerator()
    val template = getDefaultTemplates("HiidoStaticImp",
        CodeLanguage.Java, "Default", "HiidoStaticImp.vm")
    val code = codeGenerator.combine(template, classStruce!!, null)

}