package io.toon.jack.parser

import io.toon.jack.parse
import io.toon.jack.tokenizer.JackTokenizer
import io.toon.jack.tokenizer.Tokenizer
import org.junit.Test
import kotlin.test.assertEquals

class JackParserProgramWriteTest {

    @Test fun debug() {

        val source = """
         
        """.trimIndent()

        val result = parse(source).getOrThrow()
    }

    @Test fun letStatementTest() {


        var source = """
        	while (i < length) {
        	    let sum = sum + a[i];
        	    let i = i + 1;
        	}
        """.trimIndent()
        var expected = """
               <whileStatement>
          <keyword> while </keyword>
          <symbol> ( </symbol>
          <expression>
            <term>
              <identifier> i </identifier>
            </term>
            <symbol> &lt; </symbol>
            <term>
              <identifier> length </identifier>
            </term>
          </expression>
          <symbol> ) </symbol>
          <symbol> { </symbol>
          <statements>
            <letStatement>
              <keyword> let </keyword>
              <identifier> a </identifier>
              <symbol> [ </symbol>
              <expression>
                <term>
                  <identifier> i </identifier>
                </term>
              </expression>
              <symbol> ] </symbol>
              <symbol> = </symbol>
              <expression>
                <term>
                  <identifier> Keyboard </identifier>
                  <symbol> . </symbol>
                  <identifier> readInt </identifier>
                  <symbol> ( </symbol>
                  <expressionList>
                    <expression>
                      <term>
                        <stringConstant> ENTER THE NEXT NUMBER:  </stringConstant>
                      </term>
                    </expression>
                  </expressionList>
                  <symbol> ) </symbol>
                </term>
              </expression>
              <symbol> ; </symbol>
            </letStatement>
            <letStatement>
              <keyword> let </keyword>
              <identifier> i </identifier>
              <symbol> = </symbol>
              <expression>
                <term>
                  <identifier> i </identifier>
                </term>
                <symbol> + </symbol>
                <term>
                  <integerConstant> 1 </integerConstant>
                </term>
              </expression>
              <symbol> ; </symbol>
            </letStatement>
          </statements>
          <symbol> } </symbol>
        </whileStatement>
        """.trimIndent()

        val result = parseWhileStatement(JackTokenizer(source).toMutableList()).getOrThrow()!!

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

        assertEquals(expected, result.toXML())
    }

    @Test fun parseSquareMain() {
        val source = javaClass.getResource("/Square/Main.jack").readText()

        val expected = this.javaClass.getResource(
                "/Square/Main.xml").readText()

        val result = parse(source).getOrThrow()
        assertEquals(expected, result.toXML())
    }

    @Test fun parseSquareSquare() {
        val source = javaClass.getResource("/Square/Square.jack").readText()
        val expected = this.javaClass.getResource(
                "/Square/Square.xml").readText()

        val result = parse(source).getOrThrow()
        assertEquals(expected, result.toXML())
    }

    @Test fun parseSquareGame() {
        val source = javaClass.getResource("/Square/SquareGame.jack").readText()
        val expected = this.javaClass.getResource(
                "/Square/SquareGame.xml").readText()

        val result = parse(source).getOrThrow()
        assertEquals(expected, result.toXML())
    }
}