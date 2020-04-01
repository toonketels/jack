package io.toon.jack.parser

interface XMLBuilder {
    fun buildXML(): XML = XMLValue("// @TODO")
    fun toXML(): String = buildXML().toXML()
}

interface XML {
    fun toXML(indent: String = ""): String
}

class XMLValue(val value: String): XML {
    override fun toXML(indent: String): String = "$value".prependIndent(indent)
}

class KeywordNode(val value: String): XML {
    override fun toXML(indent: String): String = "<keyword> ${value} </keyword>".prependIndent(indent)
}

class IdentifierNode(val value: String): XML {
    override fun toXML(indent: String): String = "<identifier> ${value} </identifier>".prependIndent(indent)
}

class SymbolNode(val value: String): XML {
    override fun toXML(indent: String): String = "<symbol> ${value} </symbol>".prependIndent(indent)
}

open class XMLList(): XML {

    val items: MutableList<XML> = mutableListOf()

    fun keyword(callback: () -> String): KeywordNode {
        val node = KeywordNode(callback())
        items.add(node)
        return node
    }

    fun identifier(callback: () -> String): IdentifierNode {
        val node = IdentifierNode(callback())
        items.add(node)
        return node
    }

    fun symbol(callback: () -> String): SymbolNode {
        val node = SymbolNode(callback())
        items.add(node)
        return node
    }

    fun xml(tagName: String, init: XMLNode.() -> Unit): XMLNode {
        val node = XMLNode(tagName)
        node.init()
        items.add(node)
        return node;
    }

    fun child(callback: () -> XMLBuilder): XML {
        val node = callback().buildXML()
        items.add(node)
        return node
    }

    override fun toXML(indent: String): String {
        var ch = items.map { it.toXML(indent) }

        return ch.joinToString("\n")
    }
}

open class XMLNode(val tagName: String): XML {

    var children: MutableList<XML> = mutableListOf()

    fun just(callback: () -> String) {
        children.add(XMLValue(callback()))
    }

    fun keyword(callback: () -> String): KeywordNode {
        val node = KeywordNode(callback())
        children.add(node)
        return node
    }

    fun identifier(callback: () -> String): IdentifierNode {
        val node = IdentifierNode(callback())
        children.add(node)
        return node
    }

    fun symbol(callback: () -> String): SymbolNode {
        val node = SymbolNode(callback())
        children.add(node)
        return node
    }

    fun xml(tagName: String, init: XMLNode.() -> Unit): XMLNode {
        val node = XMLNode(tagName)
        node.init()
        children.add(node)
        return node;
    }

    fun xmlList(init: XMLList.() -> Unit): XMLList {
        val list = XMLList()
        list.init()
        children.add(list)
        return list
    }

    fun child(callback: () -> XMLBuilder): XML {
        val node = callback().buildXML()
        children.add(node)
        return node
    }

    override fun toXML(indent: String): String {
        return if (children.size == 1 && children.first() is XMLValue) {
            withValueToXML(indent, (children.first() as XMLValue))
        } else {
            withChildrenToXML(indent)
        }
    }

    fun withValueToXML(indent: String, value: XMLValue): String = "<${tagName}> ${value.value} </${tagName}>".prependIndent(indent)

    fun withChildrenToXML(indent: String): String {
        var ch = children.map { it.toXML(indent + "  ") }

        val ls = mutableListOf<String>("${indent}<${tagName}>")
        ls.addAll(ch)
        ls.add("${indent}</${tagName}>")

        return ls.joinToString("\n")
    }
}

fun xmlList(init: XMLList.() -> Unit): XMLList {
    val list = XMLList()
    list.init()
    return list
}

fun xml(tagName: String, init: XMLNode.() -> Unit): XMLNode {
    val node = XMLNode(tagName)
    node.init()
    return node;
}