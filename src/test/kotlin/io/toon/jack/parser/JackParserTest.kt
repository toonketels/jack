package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.toon.jack.tokenizer.JackTokenizer
import org.junit.Test


class JackParserTest {
    @Test
    fun testClass() {

        val source = "class Main { }"
        val expected = """
            <class>
            <keyword> class </keyword>
            <identifier> Main </identifier>
            <symbol> { </symbol>
            <symbol> } </symbol>
            </class>
        """.trimIndent()

        val result = parseClass(JackTokenizer(source).toList())

        assertThat(result.isSuccess).isTrue()

        val node = result.getOrThrow()!! as ClassNode
        assertThat(node.name).isEqualTo("Main")


    }
}