package io.toon.jack.tokenizer

import kotlin.test.*

class JackTokenizerTest {
    @Test fun basicTest() {
        val tokenizer = JackTokenizer("let x = \"Hello world\";")
        val expected = listOf(
                KeywordToken("let"),
                IdentifierToken("x"),
                SymbolToken("="),
                StringToken("Hello world"),
                SymbolToken(";")
        )
        assertEquals(expected, tokenizer.toList())
    }

    @Test fun parse() {
        val (remainder, token) = tokenize("return; let x = 45;")!!

        assertEquals("return", token!!.value)
        assertEquals("; let x = 45;", remainder)
    }

    @Test fun parse2() {

        val input = """
            // This is a comment
            // This is also a comment
            
            /** And this too */
        """.trimIndent()

        val rest = """
            // This is also a comment
            
            /** And this too */
        """.trimIndent()

        val (remainder, token) = tokenize(input)!!

        assertNull(token)
        assertEquals(rest, remainder)
    }

    @Test fun parseString() {

        val (remainder, token) = parseString("\"Hello world\" is cool")!!

        assertEquals("Hello world", token!!.value)
        assertEquals(" is cool", remainder);
    }

    @Test fun parseComment() {
        val (remainder, token) = parseComment("// this is the best thing \n let x = y;")!!

        assertNull(token)
        assertEquals(" let x = y;", remainder)
    }

    @Test fun parseSymbol() {
        val (remainder, token) = parseSymbol("[0]")!!

        assertEquals("[", token!!.value)
        assertEquals("0]", remainder)
    }

    @Test fun parseKeyword() {

        val (remainder, token) = parseKeyword("return; let x = 45;")!!

        assertEquals("return", token!!.value)
        assertEquals("; let x = 45;", remainder)
    }

    @Test fun parseInt() {

        val (remainder, token) = parseInt("5912; let x = 45;")!!

        assertEquals("5912", token!!.value)
        assertEquals("; let x = 45;", remainder)

    }

    @Test fun parseIdentifier() {

        val (remainder, token) = parseIdentifier("a_6543 = 45;")!!

        assertEquals("a_6543", token!!.value)
        assertEquals(" = 45;", remainder)
    }

    @Test fun parseWhitespace() {
        val (remainder, token) = parseWhiteSpace("   \n is cool")!!

        assertNull(token)
        assertEquals("is cool", remainder)
    }

    @Test fun toXMLTest() {
        val tokenizer = JackTokenizer("let x = \"Hello world\";")
        val expected = """
        <tokens>
        <keyword> let </keyword>
        <identifier> x </identifier>
        <symbol> = </symbol>
        <stringConstant> Hello world </stringConstant>
        <symbol> ; </symbol>
        </tokens>
        
        """.trimIndent()

        println(expected)

        assertEquals(expected, tokenizer.toXml())
    }

    @Test fun tokenizeSimpleArrayTest() {

        val source = javaClass.getResource("/project-10/SimpleArrayTest/Main.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/SimpleArrayTest/MainT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun tokenizeArrayTest() {

        val source = javaClass.getResource("/project-10/ArrayTest/Main.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/ArrayTest/MainT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun expressionLessSquareMainTest() {

        val source = javaClass.getResource("/project-10/ExpressionLessSquare/Main.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/ExpressionLessSquare/MainT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun expressionLessSquareSquareTest() {

        val source = javaClass.getResource("/project-10/ExpressionLessSquare/Square.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/ExpressionLessSquare/SquareT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun expressionLessSquareGameTest() {

        val source = javaClass.getResource("/project-10/ExpressionLessSquare/SquareGame.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/ExpressionLessSquare/SquareGameT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun squareMainTest() {
        val source = javaClass.getResource("/project-10/Square/Main.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/Square/MainT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun squareTest() {
        val source = javaClass.getResource("/project-10/Square/Square.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/Square/SquareT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }

    @Test fun squareGameTest() {
        val source = javaClass.getResource("/project-10/Square/SquareGame.jack").readText()
        val tokenized = this.javaClass.getResource(
                "/project-10/Square/SquareGameT.xml").readText()

        assertEquals(tokenized, JackTokenizer(source).toXml())
    }
}
