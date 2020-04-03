package io.toon.jack.parser

import assertk.assertThat
import assertk.assertions.*
import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.Operator.PLUS
import io.toon.jack.parser.SubroutineDeclarationType.CONSTRUCTOR
import io.toon.jack.tokenizer.*
import org.junit.Test


class JackParserTest {

    @Test
    fun testClassHeader() {

        val source = "class Main { field int x, y; static String greeting; }"

        val result = parseClass(JackTokenizer(source).toMutableList())

        assertThat(result.isSuccess).isTrue()

        val node = result.getOrThrow()
        assertThat(node.name).isEqualTo("Main")
    }

    @Test
    fun testParseClassVarDeclarations() {
        val source = "field int x, y;"

        val result = parseClassVarDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(ClassVarDeclarationNode(FIELD, TypeName("int"),  listOf(VarName("x"), VarName("y"))))
    }

    @Test
    fun testParseSubroutineDeclarationsOneParam() {
        val source = "constructor Square new(int size ) { } "

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                TypeName("Square"),
                "new",
                listOf(Parameter(TypeName("int"), VarName("size"))),
                emptyBody()))
    }

    @Test
    fun testParseSubroutineDeclarations() {
        val source = "constructor Square new(int size, Color fill) {}"

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                TypeName("Square"),
                "new",
                listOf(Parameter(TypeName("int"), VarName("size")), Parameter(TypeName("Color"), VarName("fill"))),
                emptyBody()))
    }

    @Test
    fun testParseSubroutineDeclarationsWithBody() {
        val source = """
            constructor Square new(int size, Color fill) {
                var int x, y;
                var Color border;
            }
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                TypeName("Square"),
                "new",
                listOf(Parameter(TypeName("int"), VarName("size")), Parameter(TypeName("Color"), VarName("fill"))),
                SubroutineBodyNode(
                    listOf(
                        SubroutineVarDeclarationNode(TypeName("int"), listOf(VarName("x"), VarName("y"))),
                        SubroutineVarDeclarationNode(TypeName("Color"), listOf(VarName("border")))),
                    listOf())))
    }

    @Test
    fun testParseSubroutineDeclarationsWithStatements() {
        val source = """
            constructor Square new(int size, Color fill) {
                var int x, y;
                
                // return Square;
            }
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList()).getOrThrow()!!

        assertThat(result).isEqualTo(SubroutineDeclarationNode(
                CONSTRUCTOR,
                TypeName("Square"),
                "new",
                listOf(Parameter(TypeName("int"), VarName("size")), Parameter(TypeName("Color"), VarName("fill"))),
                SubroutineBodyNode(listOf(
                        SubroutineVarDeclarationNode(TypeName("int"), listOf(VarName("x"), VarName("y")))
                ), listOf())))
    }

    @Test
    fun testOutOfOrderStatementsShouldFail() {
        val source = """
            constructor Square new {
                var int x, y;
            } (int size, Color fill)
        """.trimIndent()

        val result = parseSubroutineDeclaration(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure, "out of order statements should result in errors").isTrue()
        result.onFailure { assertThat(it.message).isEqualTo("expected symbol ( but got { instead") }
    }

    @Test
    fun testMissingSemicolonShouldFail() {
        val source = """
            if (x) {
                let y = x,
            }
        """.trimIndent()

        val result = parseIfStatement(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure, "missing semicolon should result in failure").isTrue()
        result.onFailure { assertThat(it.message).isEqualTo("expected symbol ; but got , instead") }
    }

    @Test
    fun testLetStatement() {
        val source = """
            let x = y;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as LetStatement

        assertThat(result).isEqualTo(LetStatement(VarName("x"), Expression(VarName("y"))))
    }

    @Test
    fun testLetStatementMissingExpression() {
        val source = """
            let x = ;;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure).isTrue()

        result.onFailure { assertThat(it.message).isEqualTo("expected a value for right hand side of VarName(name=x) but it was empty") }
    }

    @Test
    fun testIfStatement() {
        val source = """
            if ( x ) {  
                let y = x;
            } else {
                let y = z;
            }
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as IfStatement

        assertThat(result).isEqualTo(IfStatement(
                Expression(VarName("x")),
                listOf(LetStatement(VarName("y"), Expression(VarName("x")))),
                listOf(LetStatement(VarName("y"), Expression(VarName("z"))))
        ))
    }

    @Test
    fun testWhileStatement() {
        val source = """
            while ( x ) {
                let x = y;
            }
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as WhileStatement

        assertThat(result).isEqualTo(WhileStatement(
                Expression(VarName("x")),
                listOf(LetStatement(VarName("x"), Expression(VarName("y"))))
        ))
    }

    @Test
    fun testReturnStatement() {
        val source = """
            return x;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as ReturnStatement

        assertThat(result).isEqualTo(ReturnStatement(Expression(VarName("x"))))
    }

    @Test
    fun testReturnStatementWithoutExpression() {
        val source = """
            return ;
            ;
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as ReturnStatement

        assertThat(result).isEqualTo(ReturnStatement())
    }

    @Test
    fun testClassFailStart() {
        val source = """
            return ;
        """.trimIndent()

        val result = parseClass(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure, "class should start with class keyword").isTrue()

        result.onFailure { assertThat(it.message).isEqualTo("expected a value for class but it was empty") }
    }

    @Test
    fun testClassFailParse() {
        val source = """
            class Square {
                constructor Square new () { return x; }
                field int size;
            }
        """.trimIndent()

        val result = parseClass(JackTokenizer(source).toMutableList())

        assertThat(result.isFailure, "fields need to go above subroutines").isTrue()

        result.onFailure { assertThat(it.message).isEqualTo("expected symbol } but got field instead") }
    }

    @Test
    fun testClassAll() {
        val source = """
            class Square {
            
                field int x, y;
                static Color fill;
                
                constructor Square new(int x, int y) { 
                  let z = x;
                  return x; 
                 }
                
                method int area() { return x; }
                
                function int random() { 
                     let z = x;
                     return z;
                }
            }
        """.trimIndent()

        val result = parseClass(JackTokenizer(source).toMutableList())

        assertThat(result.isSuccess).isTrue()

        result.onSuccess {
            assertThat(it).isNotNull()
            assertThat(it.subroutineDeclarations.get(2).body.statements).isNotEmpty()
        }
    }

    @Test
    fun testDoStatement() {
        val source = """
            do x.pay(y);
        """.trimIndent()

        val result = parseStatement(JackTokenizer(source).toMutableList()).getOrThrow()!! as DoStatement

        assertThat(result).isEqualTo(DoStatement(ComplexSubroutineCall(
                "x",
                "pay",
                listOf(Expression(VarName("y")))
        )))
    }

    @Test
    fun testExpressions3() {
        val tokens: Tokens = mutableListOf(IdentifierToken("game"), SymbolToken("["), IntToken("4"), SymbolToken("]"), SymbolToken(";"))

        val result = parseExpression(tokens).getOrThrow()!!

        assertThat(result).isEqualTo(Expression(ArrayAccess(VarName("game"), Expression(IntegerConstant(4)))))
    }

    @Test
    fun testExpressions4() {
        val tokens: Tokens = mutableListOf(KeywordToken("true"), SymbolToken(";"))

        val result = parseExpression(tokens).getOrThrow()!!

        assertThat(result).isEqualTo(Expression(KeywordConstant("true")))
    }

    @Test
    fun testExpressions5() {
        val tokens: Tokens = mutableListOf(SymbolToken("("), IntToken("20"), SymbolToken(")"), SymbolToken(";"))

        val result = parseExpression(tokens).getOrThrow()!!

        assertThat(result).isEqualTo(Expression(TermExpression(Expression(IntegerConstant(20)))))
    }

    @Test
    fun testExpressions6() {
        val tokens: Tokens = mutableListOf(SymbolToken("-"), IntToken("20"), SymbolToken(";"))

        val result = parseExpression(tokens).getOrThrow()!!

        assertThat(result).isEqualTo(Expression(UnaryOp(Operator.MINUS, IntegerConstant(20))))
    }

    @Test
    fun testExpression7() {
        val tokens: Tokens = mutableListOf(IntToken("3"), SymbolToken("+"), IntToken("20"), SymbolToken(";"))

        val result = parseExpression(tokens).getOrThrow()!!

        assertThat(result).isEqualTo(Expression(IntegerConstant(3), OpTermNode(PLUS, IntegerConstant(20))))
    }

    @Test
    fun testExpression8() {
        val tokens: Tokens = mutableListOf(IntToken("3"), SymbolToken("+"), SymbolToken("+"), IntToken("20"))

        val result = parseExpression(tokens)

        assertThat(result.isFailure).isTrue()
        result.onFailure { assertThat(it.message).isEqualTo("expected a value for something but it was empty") }
    }

    @Test
    fun subroutine1() {
        val tokens: Tokens = mutableListOf(IdentifierToken("greeting"), SymbolToken("("), IntToken("2"), SymbolToken(","), IntToken("3"), SymbolToken(")"))

        val result = parseSubroutineCall(tokens)

        assertThat(result.isSuccess).isTrue()

        result.onSuccess {
            assertThat(it)
                    .isEqualTo(SimpleSubroutineCall("greeting", listOf(Expression(IntegerConstant(2)), Expression(IntegerConstant(3)))))
        }
    }

    @Test
    fun subroutine2() {
        val tokens: Tokens = mutableListOf(IdentifierToken("Greeter"), SymbolToken("."), IdentifierToken("greeting"), SymbolToken("("), IntToken("2"), SymbolToken(","), IntToken("3"), SymbolToken(")"))

        val result = parseSubroutineCall(tokens)

        assertThat(result.isSuccess).isTrue()

        result.onSuccess {
            assertThat(it)
                    .isEqualTo(ComplexSubroutineCall("Greeter","greeting", listOf(Expression(IntegerConstant(2)), Expression(IntegerConstant(3)))))
        }
    }

    private fun emptyBody(): SubroutineBodyNode = SubroutineBodyNode(listOf(), listOf())

}