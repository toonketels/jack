package io.toon.jack.parser

import io.toon.jack.SymbolTable

interface CodeGen {
    fun genCode(symbols: SymbolTable, classNode: ClassNode? = null): List<String>
    fun genProgram(symbols: SymbolTable): String = genCode(symbols).joinToString("\n")
}

fun genVMCode(symbols: SymbolTable, classNode: ClassNode?, init: VMCodeBuilder.() -> Unit): List<String> {
    val builder = VMCodeBuilder(symbols, classNode)
    builder.init()
    return builder._statements
}

class VMCodeBuilder(private val symbols: SymbolTable, private val classNode: ClassNode?) {
    val _statements: MutableList<String> = mutableListOf()

    fun push(segment: Segment, number: Int) = statement("push ${segment.value} $number")
    fun pop(segment: Segment, number: Int) = statement("pop ${segment.value} $number")

    fun label(label: String) = statement("label $label")
    fun goto(label: String) = statement("goto $label")
    fun ifGoto(label: String) = statement("if-goto $label")

    fun function(functionName: String, numberOfLocals: Int) = statement("function $functionName $numberOfLocals")
    fun returnIt() = statement("return")
    fun call(functionName: String, numberOfArguments: Int) = statement("call $functionName $numberOfArguments")

    fun add() = statement("add")
    fun sub() = statement("sub")
    fun neg() = statement("neg")
    fun not() = statement("not")
    fun or() = statement("or")
    fun and() = statement("and")
    fun eq() = statement("eq")
    fun gt() = statement("gt")
    fun lt() = statement("lt")

    fun addStatements(generator: CodeGen) = _statements.addAll(generator.genCode(symbols, classNode))

    private fun statement(statement: String) {
        _statements.add(statement)
    }
}

enum class Segment(val value: String) {
    LOCAL("local"),
    ARGUMENT("argument"),
    POINTER("pointer"),
    THIS("this"),
    THAT("that"),
    TEMP("temp"),
    STATIC("static"),
    CONSTANT("constant");
}

