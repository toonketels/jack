package io.toon.jack


import io.toon.jack.Kind.*
import io.toon.jack.parser.TypeName
import kotlin.test.*

class CodeGenTest {

    @Test fun test1() {
        val source = """
            class Square {
                
                function int print(int x, int y) {
                    var int result;
                    let result = x + y;
                    do Output.printInt(result);
                    return result;
                }
            }
        """.trimIndent()

        var expected =  """
            function Square.print 1
            push argument 0
            push argument 1
            add
            pop local 0
            push local 0
            call Output.printInt 1
            pop temp 0
            push local 0
            return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun test2() {
        val source = """
            class Square {
            
                field int x, y;
                
                constructor Square new(int xx, int yy) {
                    let x = xx;
                    let y = yy;
                    return this;
                }
            }
        """.trimIndent()

        var expected =  """
            function Square.new 0
            push constant 2
            call Memory.alloc 1
            pop pointer 0
            push argument 0
            pop this 0
            push argument 1
            pop this 1
            push pointer 0
            return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun seven() {
        val expected = """
            function Main.main 0
            push constant 1
            push constant 2
            push constant 3
            call Math.multiply 2
            add
            call Output.printInt 1
            pop temp 0
            push constant 0
            return
        """.trimIndent()
        val source = javaClass.getResource("/project-11/Seven/Main.jack").readText()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)

    }
}