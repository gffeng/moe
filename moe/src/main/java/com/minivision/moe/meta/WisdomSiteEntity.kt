package com.minivision.moe.meta


/**
 *广联达解析后消息体
 *
 * @author gf
 * @date 2021/4/25
 */
abstract class WisdomSiteEntity(var cmd: String, var data: Any?) : ISendEntity {

    /**
     * 将JSON格式转换成广联达格式
     */
    override fun parse(): String {
        var body = ""
        data?.let {
            val oClass = it::class.java
            val fields = oClass.declaredFields
            fields.forEachIndexed { index, item ->
                item.isAccessible = true
                val name = item.name
                val value = item.get(data)
                body += "$name=\"$value\""
                if (index != fields.size - 1) {
                    body += SPACE.toString()
                }
            }
        }
        return LEFT_BRACKET.toString() + cmd + SPLIT_HEAD + body + SPLIT_END + RIGHT_BRACKET.toString()
    }


    override fun body(): Any? {
        return data
    }
}