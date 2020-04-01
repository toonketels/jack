package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.isTrue
import io.toon.jack.parse
import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.Operator.PLUS
import io.toon.jack.parser.SubroutineDeclarationType.CONSTRUCTOR
import io.toon.jack.tokenizer.JackTokenizer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JackParserProgramTest {

    @Test fun debug() {

        val source = """
           class Main {
               function void main() {
                   var Array a;
                   var int length;
                   var int i, sum;
           	
           	let length = Keyboard.readInt("HOW MANY NUMBERS? ");
           	let a = Array.new(length);
           	let i = 0;
           	
           	while (i < length) {
           	    let a[i] = Keyboard.readInt("ENTER THE NEXT NUMBER: ");
           	    let i = i + 1;
           	}
           	
           	let i = 0;
           	let sum = 0;
           	
           	while (i < length) {
           	    let sum = sum + a[i];
           	    let i = i + 1;
           	}
           	
           	do Output.printString("THE AVERAGE IS: ");
           	do Output.printInt(sum / length);
           	do Output.println();
           	
           	return;
               }
           }
        """.trimIndent()

        val result = parse(source).getOrThrow()

        val expected = this.javaClass.getResource(
                "/ArrayTest/Main.xml").readText()

        assertEquals(expected, result.toXML())
    }

    @Test fun parseArrayTest() {

        val source = javaClass.getResource("/ArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/ArrayTest/Main.xml").readText()

        val result = parse(source).getOrThrow()

        assertEquals(expected, result.toXML())
    }

    @Test fun parseSimpleArrayTest() {
        val source = javaClass.getResource("/SimpleArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/SimpleArrayTest/Main.xml").readText()

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