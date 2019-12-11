package model

/**
 * 将一个Class对象抽象为Model实体 一个Class包括多个成员变量、方法，方法同时包含多枚入参
 * */

/**
 * Method
 * 方法的抽象实体
 * @param name 方法名 eg. fun() 的 fun
 * @param modifier 方法修饰符  eg . public fun() 的 public
 * @param returnType 返回值类型 eg. public fun(): Int 里的 Int
 * @param params 方法入参 eg . public fun(arg1: String, arg2: String): Int 里的 arg1, arg2
 * */
data class Method(var name: String, var modifier: String, var returnType: String, var params: List<Param>)
/**
 * MethodParam
 * 方法入参的抽象实体
 * @param name 入参名 eg . public fun(arg1: String): Int 里的 args1
 * @param type 入参类型 eg . public fun(arg1: String): Int 里的 String
 * */
data class Param(var name: String, var type: String)

/**
 * ClassField
 * 类的成员变量抽象实体
 * @param type 类型
 * @param name 成员变量名
 * @param modifier 成员变量访问修饰符
 * @param comment 变量注释
 * */

data class Field(var type: String, var name: String, var modifier: String, var comment: String)

/**
 * Class
 * 一个class文件的抽象实体
 * @param name 类名
 * @param types 类类型 接口 类 枚举
 * @param comment 类注释
 * @param extends 继承信息
 * @param implements 接口信息
 * @param packageName 包名
 * @param importList 导包列表
 * @param fields 类成员变量
 * @param methods 类函数
 * @param
 * */
data class Class(var name: String, var types: List<String>, var comment: String?, var extends: String?,
                 var implements: List<String>?, var packageName: String, var importList: List<String>?,
                 var fields: List<Field>?,
                 var methods : List<Method>?)

enum class ClassType {
    Interface, Class, Enum
}