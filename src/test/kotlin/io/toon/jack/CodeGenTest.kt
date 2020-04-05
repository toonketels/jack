package io.toon.jack


import io.toon.jack.Kind.*
import io.toon.jack.parser.TypeName
import kotlin.test.*

class CodeGenTest {

    @Test fun basicFunctionTest() {
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

    @Test fun constructorTest() {
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

    @Test fun methodTest() {
        val source = """
            class Square {
            
                field int x, y;
                
                method int doubleY(Square other) {
                    var int distance;
                    let distance = x * other.getX();
                    return distance;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doubleY 1
             push argument 0
             pop pointer 0
             push this 0
             push argument 1
             call Square.getX 1
             call Math.multiply 2
             pop local 0
             push local 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun ifStatementTest() {
        val source = """
            class Square {
            
                field int x, y;
                
                method int doSomething(int z) {
                    var int a;
                    if (z > x) {
                        let a = x;
                    } else {
                        let z = y;
                    }
                    if (z < a) {
                        return z;
                    }
                    return a;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doSomething 1
             push argument 0
             pop pointer 0
             push argument 1
             push this 0
             gt
             not
             if-goto Square.doSomething.if.1.else
             push this 0
             pop local 0
             goto Square.doSomething.if.1.end
             label Square.doSomething.if.1.else
             push this 1
             pop argument 1
             label Square.doSomething.if.1.end
             push argument 1
             push local 0
             lt
             not
             if-goto Square.doSomething.if.2.end
             push argument 1
             return
             label Square.doSomething.if.2.end
             push local 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun whileStatementTest() {
        val source = """
            class Square {
                
                function void doSomething(int times) {
                    var int i;
                    let i = 0;
                    while (i < times) {
                        do Output.printInt(i);
                        let i = i + 1;
                    }
                    return;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doSomething 1
             push constant 0
             pop local 0
             label Square.doSomething.while.1.start
             push local 0
             push argument 0
             lt
             not
             if-goto Square.doSomething.while.1.end
             push local 0
             call Output.printInt 1
             pop temp 0
             push local 0
             push constant 1
             add
             pop local 0
             goto Square.doSomething.while.1.start
             label Square.doSomething.while.1.end
             push constant 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun booleanTest() {
        val source = """
            class Square {
                function boolean doSomething() {
                    var boolean ok, nok;
                    let ok = true;
                    let nok = false;
                    return ok;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doSomething 2
             push constant 1
             neg
             pop local 0
             push constant 0
             pop local 1
             push local 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun nullTest() {
        val source = """
            class Square {
                function Square doSomething() {
                    var Square it;
                    let it = null;
                    return it;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doSomething 1
             push constant 0
             pop local 0
             push local 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun unaryOpTest() {
        @Test fun booleanTest() {
            val source = """
            class Square {
                function boolean doSomething(boolean ok) {
                    var boolean nok;
                    let nok = ~ok;
                    return nok;
                }
            }
        """.trimIndent()

            var expected =  """
             function Square.doSomething 1
             push argument 0
             neg
             pop local 0
             push local 0
             return
        """.trimIndent()

            val result = parseAndGenCode(source).getOrThrow()

            assertEquals(expected, result)
        }
    }

    @Test fun stringTest() {
        val source = """
            class Square {
                function String doSomething() {
                    var String it;
                    let it = "ok";
                    return it;
                }
            }
        """.trimIndent()

        var expected =  """
             function Square.doSomething 1
             push constant 2
             call String.new 1
             push constant 111
             call String.appendChar 2
             push constant 107
             call String.appendChar 2
             pop local 0
             push local 0
             return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun arrayTest() {
        val source = """
            class Square {
                function Array doSomething() {
                    var Array it;
                    let it = Array.new(5);
                    let it[2] = true;
                    return it;
                }
            }
        """.trimIndent()

        var expected =  """
            function Square.doSomething 1
            push constant 5
            call Array.new 1
            pop local 0
            push constant 1
            neg
            push local 0
            push constant 2
            add
            pop pointer 1
            pop that 0
            push local 0
            return
        """.trimIndent()

        val result = parseAndGenCode(source).getOrThrow()

        assertEquals(expected, result)
    }

    @Test fun arrayTest2() {
        val source = """
            class Square {
                function Array doSomething(Array other) {
                    var Array it;
                    let it = Array.new(5);
                    let it[2] = other[3];
                    return it;
                }
            }
        """.trimIndent()

        var expected =  """
            function Square.doSomething 1
            push constant 5
            call Array.new 1
            pop local 0
            push argument 0
            push constant 3
            add
            pop pointer 1
            push that 0
            push local 0
            push constant 2
            add
            pop pointer 1
            pop that 0
            push local 0
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