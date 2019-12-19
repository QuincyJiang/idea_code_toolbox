package model

/**
 * 埋点业务代码
 * */

@NoArg data class HiidoStaticExcel(var sheets: LinkedHashMap<String, HiidoStaticSheet>)
@NoArg data class HiidoStaticSheet(var modelList: ArrayList<HiidoStaticModel>)
@NoArg data class HiidoStaticModel(var lable: String, var property: String, var type: String, var keyList: ArrayList<HiidoStaticKey>, var remark: String)
@NoArg data class HiidoStaticKey(var key: String, var value: String)
// 表格表头索引
@NoArg data class HiidoStaticSheetsIndex(var titleRowIndex: Int, var keysIndexMap: LinkedHashMap<String, Int>, var labelIndex: Int,
                                         var propertyIndex: Int, var typeIndex: Int, var remarkIndex: Int)