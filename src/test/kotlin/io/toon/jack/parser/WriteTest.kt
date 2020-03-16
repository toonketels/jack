package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.toon.jack.tokenizer.JackTokenizer
import kotlin.test.Test


class WriterTest {

    @Test
    fun test4() {

        var source = """
            class Main {
                static boolean test;    // Added for testing -- there is no static keyword
                                        // in the Square files.
                function void main() {
                  var SquareGame game;
                  
                  let game = y;

                  if (game) {
                    let z = y;
                  }
                  
                  return;
                }
            }
        """.trimIndent()

        val result = parseClass(JackTokenizer(source).toMutableList()).getOrThrow()!!

        print(result.buildXML().toXML())
    }

    @Test
    fun test3() {
        val source = """
            let x = y;
        """.trimIndent()

        val expectation = """
            <letStatement>
              <keyword> let </keyword>
              <identifier> x </identifier>
              <symbol> = </symbol>
              <expression>
                <term>
                  <identifier> y </identifier>
                </term>
              </expression>
              <symbol> ; </symbol>
            </letStatement>
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as LetStatement

        assertThat(result.buildXML().toXML()).isEqualTo(expectation)

    }

    @Test fun testmore() {

        val root =  xml("expression") {
            xml("term") {
                identifier { "some identifier" }
            }
        }

        val expectation = """
            <expression>
              <term>
                <identifier> some identifier </identifier>
              </term>
            </expression>
        """.trimIndent()

        assertThat(root.toXML()).isEqualTo(expectation)

    }


    @Test
    fun test2() {
        val x = "hello"

        val root = xml("letStatement") {
            keyword { "let" }
            identifier { "x" }
            symbol { "=" }
            identifier { "y" }
            symbol { ";" }
        }

        val expectation = """
            <letStatement>
              <keyword> let </keyword>
              <identifier> x </identifier>
              <symbol> = </symbol>
              <identifier> y </identifier>
              <symbol> ; </symbol>
            </letStatement>
        """.trimIndent()

        assertThat(root.toXML()).isEqualTo(expectation)
    }
    @Test
    fun test1() {

        val root = xml("letStatement") {
            just {
                "what is this?"
            }
        }

        assertThat(root.toXML()).isEqualTo("<letStatement> what is this? </letStatement>")
    }

    @org.junit.Test
    fun testWrite() {

        """
                    <letStatement>
                      <keyword> let </keyword>
                      <identifier> x </identifier>
                      <symbol> = </symbol>
                      <expression> y </expression>
                      <symbol> ; </symbol>
                    </letStatement>
        """.trimIndent()

        """
            letStatement
                keyword(let)
                identifier(x)
                symbol(=)
                expression.write(y)
                symbol(;)
                
        """.trimIndent()

        val source = """
                     let z = x;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as LetStatement

//        result.write(Writer())
    }
}
