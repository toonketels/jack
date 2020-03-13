package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.SubroutineDeclarationType.CONSTRUCTOR
import io.toon.jack.tokenizer.JackTokenizer
import org.junit.Test
import kotlin.test.Ignore


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

        assertThat(result).isEqualTo(ClassVarDeclarationNode(FIELD, "int",  listOf("x", "y")))
    }

    @Test
    fun testParseSubroutineDeclarationsOneParam() {
        val source = "constructor Square new(int size ) { } "

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                "Square",
                "new",
                listOf(Parameter("int", "size")),
                emptyBody()))
    }

    @Test
    fun testParseSubroutineDeclarations() {
        val source = "constructor Square new(int size, Color fill) {}"

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                "Square",
                "new",
                listOf(Parameter("int", "size"), Parameter("Color", "fill")),
                emptyBody()))
    }

    @Test
    fun testParseSubroutineDeclarationsWithBody() {
        val source = """
            constructor Square new(int size, Color fill) {
                var int x, y;
                var Color border;
            }
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                "Square",
                "new",
                listOf(Parameter("int", "size"), Parameter("Color", "fill")),
                SubroutineBodyNode(
                    listOf(
                        SubroutineVarDeclarationNode("int", listOf("x", "y")),
                        SubroutineVarDeclarationNode("Color", listOf("border"))),
                    listOf())))
    }

    @Test
    fun testParseSubroutineDeclarationsWithStatements() {
        val source = """
            constructor Square new(int size, Color fill) {
                var int x, y;
                
                return Square;
            }
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                "Square",
                "new",
                listOf(Parameter("int", "size"), Parameter("Color", "fill")),
                SubroutineBodyNode(listOf(
                        SubroutineVarDeclarationNode("int", listOf("x", "y"))
                ), listOf())))
    }

    @Ignore("out of order statements dont fail atm")
    @Test
    fun testOutOfOrderStatementsShouldFail() {
        val source = """
            constructor Square new {
                var int x, y;
            } (int size, Color fill)
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure, "out of order statements should result in errors").isTrue()
    }

    @Ignore("missing semicolons dont fail atm")
    @Test
    fun testMissingSemicolonShouldFail() {
        val source = """
            if ( x ) {  
                let y = x
            } else {
                let y = z;
            }
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList())


        assertThat(result.isFailure, "missing semicolon should result in failure").isTrue()
    }

    @Test
    fun testLetStatement() {
        val source = """
            let x = y;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as LetStatement

        assertThat(result).isEqualTo(LetStatement("x", Expression("y")))
    }

    @Test
    fun testIfStatement() {
        val source = """
            if ( x ) {  
                let y = x;
            } else {
                let y = z;
            }
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as IfStatement

        assertThat(result).isEqualTo(IfStatement(
                Expression("x"),
                listOf(LetStatement("y", Expression("x"))),
                listOf(LetStatement("y", Expression("z")))
        ))
    }

    private fun emptyBody(): SubroutineBodyNode = SubroutineBodyNode(listOf(), listOf())
}