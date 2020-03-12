package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.SubroutineDeclarationType.CONSTRUCTOR
import io.toon.jack.tokenizer.JackTokenizer
import org.junit.Test


class JackParserTest {
    @Test
    fun testClassHeader() {

        val source = "class Main { field int x, y; static String greeting; }"

        val result = parseClass(JackTokenizer(source).toMutableList())

        assertThat(result.isSuccess).isTrue()

        val node = result.getOrThrow()!! as ClassNode
        assertThat(node.name).isEqualTo("Main")
    }

    @Test
    fun testParseClassVarDeclarations() {
        val source = "field int x, y;"

        val result = parseClassVarDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(ClassVarDeclarationNode(FIELD, "int", "x", listOf("y")))
    }

    @Test
    fun testParseSubroutineDeclarations() {
        val source = "constructor Square new()"

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(CONSTRUCTOR, "Square", "new", listOf()))
    }
}