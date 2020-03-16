package io.toon.jack.parser

interface XMLBuilder {
    fun buildXML(): XML = XMLValue("// @TODO")
}

interface XML {
    fun toXML(indent: String = ""): String
}

class XMLValue(val value: String): XML {
    override fun toXML(indent: String): String = "${value}".prependIndent(indent)
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

    fun child(callback: () -> XMLBuilder): XML {
        val node = callback().buildXML()
        children.add(node)
        return node
    }

    override fun toXML(indent: String):String {

        var ch = children.map { it.toXML(indent + "  ") }

        val ls = mutableListOf<String>("${indent}<${tagName}>")
        ls.addAll(ch)
        ls.add("${indent}</${tagName}>")

        return ls.joinToString("\n")
    }
}

fun xml(tagName: String, init: XMLNode.() -> Unit): XMLNode {
    val node = XMLNode(tagName)
    node.init()
    return node;
}