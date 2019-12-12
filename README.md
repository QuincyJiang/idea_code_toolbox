#Code Generator Plugin for IDEA
## 一、概述
这是一个基于`Velocity（VM 模板）`语法 和 `Groovy` 脚本语法，通过自定义脚本文件 和一个抽象的 `contextClass`概念 来完成模板代码自动生成的 `IDEA/AndroidStudio` 插件。

它通过内置的`Converter`， 将 `VirtualFile`（IDEA的api提供的对文件系统的抽象）转化为统一的`Class`（非`Java`概念的`Class`对象，其实是一种对`Java`文件结构的抽象， 下文的`Class`均指代这个意思）对象， 这个`VirtualFile`对象可以是文本文件、excel表格、通过文件选择器选中的一个`.java`文件，也可能是鼠标当前所在的`java`对象。

插件内置了三个 `Converter`，分别是 `Java2ClassConverter`, `Pb2ClassConverter`, `Excel2ClassConverter`,他们各自通过一定的规则，对`java`文件，`proto`文件，`excel`文件进行了解析并转化为统一的`Class`实体。

解析后的`Class`实体包含了模板需要的全部信息。 此时 将该`Class`实体 和 模板文件 交给 `TemplateDecoder` 去匹配解析，替换模板中的占位符，输出生成的目标代码。

`TemplateDecoder` 支持解析 `Groovy` 和 `Velocity` 两种格式文件，都是非常常见的常用模板语言。


##  二、特点：
插件的特点是高度的灵活化和丰富的拓展性：

#### 1. 你可以在模板文件中通过`contextClass`来引用上下文对象的所有属性

包括

* **类属性：**
类名、类类型、类修饰符、继承和实现类、导包列表、成员变量列表、方法列表
* **成员变量属性：**
变量名、变量类型、变量修饰符
* **方法属性：**
方法名、方法修饰符、方法入参、返回值
* **参数属性：**
参数名、参数类型

以此定制自己的代码生成规则。
#### 2. 通过自定义模板，可以生成Java、Kotlin、或者更多IDEA支持的语言

语言语法通过模板定义就好了 XD

#### 2. 模板文件支持  `Velocity` 语法 和 `Groovy` 语法。

#### 3. 可能会支持动态加载自定义转换器

这个当做一个feature去做，通过动态加载jar包应该是可以做到的，目前的设计还未支持。如果可以自定义转换器，插件的灵活性可以大大提升。




## 三、模板通配符

### contextClass

用来生成模板类的父类，是一个`Class`实体

此处的父类不是`java`意义上的父类，可以把它理解为一个上下文对象，
这个父类 可以是从当前鼠标所在的类右键点击`Generate`菜单获取的，
也可以是通过导入特定格式文件解析出来的抽象，比如解析特定格式的excel文件，然后按照特定规则序列化获取到的抽象）

可以用该通配符来匹配目标类中的 类名、包名、继承、接口、成员变量、函数
用以生成模板类指定格式的代码。

具体说明如下：

```groovy

    $contextClass.name - 类名
    $contextClass.types - 类类型 // class interface enum
    $contextClass.packageName - 包名
    $contextClass.implements - 继承的接口列表
    $contextClass.importList - 类导包列表
    $contextClass.extends - 继承的父类
    $contextClass.comments - 类说明
    $contextClass.fields - 类成员变量 可使用 for field in fields 迭代
                    |_ $field.type: 变量类型
                    |_ $field.name: 变量名
                    |_ $field.modifier: 变量访问修饰符 public static
                    |_ $field.comment: 变量注释
    $contextClass.methods - 类的方法 可使用 for methods in methods 迭代
                    |_ $methods.name: 方法名
                    |_ $methods.modifier: 方法访问修饰符
                    |_ $methods.returnType: 方法返回类型
                    |_ $methods.params: 方法入参
                                    |_ $param.name: 方法参数名
                                    |_ $param.type: 方法参数类型
```

### 其他通用通配符

```
  $TIME - 返回当前时间 格式为 yyyy-MM-dd
  $USER - 返回当前登录用户名 user.name
  $ClassName - 当前模板生成的目标java文件名
```

### Demo Template File

```groovy

package $contextClass.packageName;

#foreach($import in $contextClass.importList)
import $import;

/**
 * @Date Created: $TIME
 * @Author $USER
 * @Description: $contextClass.comments
 */
public ${ClassName} extends AbstractBaseCore implements ${contextClass}.name {

#foreach($method in contextClass.methods)
    /**
    * $method.comments
    **/
    public $method.modifier $method.returnType $method.name (method.params) {

        }
}
#end
```





