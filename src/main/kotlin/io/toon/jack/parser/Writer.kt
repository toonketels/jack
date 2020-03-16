package io.toon.jack.parser

class Writer {

    var result: String = ""

    fun tag(name: String): Writer {

        result

        return  this
    }

    fun keyword(what: String): Writer {return this}
    fun identifier(what: String): Writer {return this}
    fun symbol(what: String): Writer {return this}
    fun int(what: String): Writer {return this}
    fun string(what: String): Writer {return this}

    fun write(node: Node): Writer {return this}

    fun then(): Writer {return this}
    fun and(): Writer {return this}
}