package io.toon.jack.parser


import io.toon.jack.parse
import org.junit.Test
import kotlin.test.assertNotNull

class JackParserProgramTest {

    @Test fun parseArrayTest() {

        val source = javaClass.getResource("/ArrayTest/Main.jack").readText()

        val result = parse(source).getOrThrow()

        assertNotNull(result)
    }

    @Test fun parseSimpleArrayTest() {
        val source = javaClass.getResource("/SimpleArrayTest/Main.jack").readText()

        val result = parse(source).getOrThrow()

        assertNotNull(result)
    }

    @Test fun parseSquareMain() {
        val sourceMain = javaClass.getResource("/Square/Main.jack").readText()

        val resultMain = parse(sourceMain).getOrThrow()
        assertNotNull(resultMain)
    }

    @Test fun parseSquareSquare() {
        val sourceMain = javaClass.getResource("/Square/Square.jack").readText()

        val resultMain = parse(sourceMain).getOrThrow()
        assertNotNull(resultMain)
    }

    @Test fun parseSquareGame() {
        val sourceMain = javaClass.getResource("/Square/SquareGame.jack").readText()

        val resultMain = parse(sourceMain).getOrThrow()
        assertNotNull(resultMain)
    }
}