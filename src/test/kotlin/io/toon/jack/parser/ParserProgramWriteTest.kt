package io.toon.jack.parser

import io.toon.jack.parseAndWriteAST
import org.junit.Test
import kotlin.test.assertEquals

class JackParserProgramWriteTest {

    @Test fun parseArrayTest() {

        val source = javaClass.getResource("/project-10/ArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/project-10/ArrayTest/Main.xml").readText()

        val result = parseAndWriteAST(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun parseSimpleArrayTest() {
        val source = javaClass.getResource("/project-10/SimpleArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/project-10/SimpleArrayTest/Main.xml").readText()

        val result = parseAndWriteAST(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun parseSquareMain() {
        val source = javaClass.getResource("/project-10/Square/Main.jack").readText()

        val expected = this.javaClass.getResource(
                "/project-10/Square/Main.xml").readText()

        val result = parseAndWriteAST(source).getOrThrow()
        assertEquals(expected, result)
    }

    @Test fun parseSquareSquare() {
        val source = javaClass.getResource("/project-10/Square/Square.jack").readText()
        val expected = this.javaClass.getResource(
                "/project-10/Square/Square.xml").readText()

        val result = parseAndWriteAST(source).getOrThrow()
        assertEquals(expected, result)
    }

    @Test fun parseSquareGame() {
        val source = javaClass.getResource("/project-10/Square/SquareGame.jack").readText()
        val expected = this.javaClass.getResource(
                "/project-10/Square/SquareGame.xml").readText()

        val result = parseAndWriteAST(source).getOrThrow()
        assertEquals(expected, result)
    }
}