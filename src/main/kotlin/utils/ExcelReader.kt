package utils

import model.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream


/**
 * 读取excel 并解析出HiidoStaticModel
 * */
fun readExcel(file: File): HiidoStaticExcel?  {
    val fileInputStream = FileInputStream(file)
    val sheetsMap = HashMap<String, HiidoStaticSheet>()
    val sheetsIndexMap = HashMap<String, HiidoStaticSheetsIndex>()
    val workbook = XSSFWorkbook(fileInputStream)

    // 第一次遍历 获取每个sheet的索引表
    workbook.iterator().forEach { xssfSheet ->
        // 获取第一行表头
        xssfSheet.forEachIndexed { index, xssfRow ->
            // 遍历每个sheet的row 第一次遇到包含'Label'标签的单元格时，开始计算index索引
              xssfRow.forEach loop@{
                if ( it.cellType == Cell.CELL_TYPE_STRING && it.stringCellValue == "Label") {
                    val sheetIndex = HiidoStaticSheetsIndex::class.java.newInstance()
                    sheetIndex.keysIndexMap = HashMap()
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
                            hiidoStaticKey.key = key
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
            Cell.CELL_TYPE_STRING -> return this.stringCellValue
            Cell.CELL_TYPE_BLANK -> return ""
            Cell.CELL_TYPE_BOOLEAN -> return this.booleanCellValue.toString()
            Cell.CELL_TYPE_ERROR -> return ""
            Cell.CELL_TYPE_NUMERIC -> return this.numericCellValue.toString()
            Cell.CELL_TYPE_FORMULA -> return ""
        }
    }
    return ""
}


//@Test
//@Throws(IOException::class)
//fun main(args: Array<String>?) {
//    val file = File("src/main/resources/temp/test.xlsx")
//    val excel = readExcel(file)
//    print(excel.toString())
//}