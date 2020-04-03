package io.toon.jack

import io.toon.jack.parser.ClassNode
import io.toon.jack.parser.parseClass
import io.toon.jack.tokenizer.JackTokenizer

fun parse(source: String):  Result<ClassNode> = parseClass(JackTokenizer(source).toMutableList())

fun parseAndWriteAST(source: String): Result<String> = parse(source).map { it.toXML() + "\n" }

fun parseAndGenCode(source: String): Result<String> = parse(source).map { ast ->
        val table = createSymbolTable(ast)
        ast.genProgram(table)
    }