package io.toon.jack.parser

import io.toon.jack.parse
import io.toon.jack.parseAndWrite
import io.toon.jack.tokenizer.JackTokenizer
import io.toon.jack.tokenizer.Tokenizer
import org.junit.Test
import kotlin.test.assertEquals

class JackParserProgramWriteTest {

    @Test fun parseArrayTest() {

        val source = javaClass.getResource("/ArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/ArrayTest/Main.xml").readText()

        val result = parseAndWrite(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun parseSimpleArrayTest() {
        val source = javaClass.getResource("/SimpleArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/SimpleArrayTest/Main.xml").readText()

        val result = parseAndWrite(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun parseSquareMain() {
        val source = javaClass.getResource("/Square/Main.jack").readText()

        val expected = this.javaClass.getResource(
                "/Square/Main.xml").readText()

        val result = parseAndWrite(source).getOrThrow()
        assertEquals(expected, result)
    }

    @Test fun parseSquareSquare() {
        val source = javaClass.getResource("/Square/Square.jack").readText()
        val expected = this.javaClass.getResource(
                "/Square/Square.xml").readText()

        val result = parseAndWrite(source).getOrThrow()
        assertEquals(expected, result)
    }

    @Test fun parseSquareGame() {
        val source = javaClass.getResource("/Square/SquareGame.jack").readText()
        val expected = this.javaClass.getResource(
                "/Square/SquareGame.xml").readText()

        val result = parseAndWrite(source).getOrThrow()
        assertEquals(expected, result)
    }
}