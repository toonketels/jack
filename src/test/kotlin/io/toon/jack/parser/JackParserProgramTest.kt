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
               static boolean test;    // Added for testing -- there is no static keyword
                                       // in the Square files.
               function void main() {
                 var SquareGame game;
                 let game = SquareGame.new();
                 do game.run();
                 do game.dispose();
                 return;
               }

               function void test() {  // Added to test Jack syntax that is not use in
                   var int i, j;       // the Square files.
                   var String s;
                   var Array a;
                   if (false) {
                       let s = "string constant";
                       let s = null;
                       let a[1] = a[2];
                   }
                   else {              // There is no else keyword in the Square files.
                       let i = i * (-j);
                       let j = j / (-2);   // note: unary negate constant 2
                       let i = i | j;
                   }
                   return;
               }
           }
        """.trimIndent()

        val result = parse(source).getOrThrow()
    }

    @Test fun parseArrayTest() {

        val source = javaClass.getResource("/ArrayTest/Main.jack").readText()
        val expected = this.javaClass.getResource(
                "/ArrayTest/Main.xml").readText()

        val result = parse(source).getOrThrow()

        assertNotNull(result)
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