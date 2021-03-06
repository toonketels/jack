package io.toon.jack.parser

import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.ClassVarStaticModifier.STATIC
import io.toon.jack.parser.SubroutineDeclarationType.*
import io.toon.jack.tokenizer.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

typealias Tokens = MutableList<Token>

private fun <E> MutableList<E>.peak(n: Int): Result<List<E>> = try { success(take(n)) } catch (e: Exception) { failure(e) }

private fun <E> MutableList<E>.peak(): E = first()


private fun <E> MutableList<E>.eat(): E {
    val taken = first()
    removeAt(0)
    return taken
}

fun parseClass(tokens: Tokens): Result<ClassNode> = require(maybeParseClass(tokens), "class")

fun maybeParseClass(tokens: Tokens): Result<ClassNode?> {

    if (parseClassKeyword(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( _a) = parseClassKeyword(tokens.eat())
        val ( className) = parseClassName(tokens.eat())
        val ( _b ) = parseSymbol("{")(tokens.eat())
        val ( varDeclarations ) = zeroOrMore(tokens, ::parseClassVarDeclaration)
        val ( subroutineDeclarations ) = zeroOrMore(tokens, ::parseSubroutineDeclaration)
        val ( _c) = parseSymbol("}")(tokens.eat())

        ClassNode(
                name = className.name,
                classVarDeclarations = varDeclarations,
                subroutineDeclarations = subroutineDeclarations
        )
    }
}

fun parseClassVarDeclaration(tokens: Tokens): Result<ClassVarDeclarationNode?> {

    if (parseStaticModifier(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( modifier ) = parseStaticModifier(tokens.eat())
        val ( type ) = parseType(tokens.eat())
        val ( varNames ) = atLeastOne(tokens, tryThanEat(::parseVarName), tryThanEat(parseSymbol(",")))
        val ( _a ) = parseSymbol(";")(tokens.eat())

        ClassVarDeclarationNode(
                staticModifier = modifier,
                typeName = type,
                varNames = varNames)
    }
}

operator fun <T> Result<T>.component1() = getOrThrow()

fun parseSubroutineDeclaration(tokens: Tokens): Result<SubroutineDeclarationNode?> {

    if (parseSubroutineType(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( subroutineType) = parseSubroutineType(tokens.eat())
        val ( returnType ) = parseSubroutineReturnType(tokens.eat())
        val ( subroutineName ) = parseSubroutineName(tokens.eat())
        val ( _a ) = parseSymbol("(")(tokens.eat())
        val ( parameters ) = parseParameters(tokens)
        val ( _b ) = parseSymbol(")")(tokens.eat())
        val ( body ) = parseSubroutineBody(tokens)

        SubroutineDeclarationNode(subroutineType, returnType, subroutineName, parameters, body)
    }
}

fun parseSubroutineBody(tokens: Tokens): Result<SubroutineBodyNode> {

    return requireAll {

        val ( _a ) = parseSymbol("{")(tokens.eat())
        val ( varDeclarations) = zeroOrMore(tokens, ::parseSubroutineVarDeclaration)
        val ( statements ) = parseStatements(tokens)
        val ( _b ) = parseSymbol("}")(tokens.eat())

        SubroutineBodyNode(varDeclarations, statements)
    }

}

fun parseStatements(tokens: Tokens): Result<List<Statement>> {
    return zeroOrMore(tokens, ::parseStatement)
}

fun parseStatement(tokens: Tokens): Result<Statement?> {
    val orMaybe = orMaybe(tokens,
            ::parseLetStatement,
            ::parseIfStatement,
            ::parseWhileStatement,
            ::parseDoStatement,
            ::parseReturnStatement)
    return orMaybe
}

fun parseDoStatement(tokens: Tokens): Result<DoStatement?> {

    if (parseKeyword("do")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseKeyword("do")(tokens.eat())
        val ( subroutine ) = require(parseSubroutineCall(tokens))
        val ( _b ) = parseSymbol(";")(tokens.eat())

        DoStatement(subroutine)
    }
}

fun parseReturnStatement(tokens: Tokens): Result<ReturnStatement?> {

    if (parseKeyword("return")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseKeyword("return")(tokens.eat())
        val ( expression ) = parseExpression(tokens)
        val ( _b ) = parseSymbol(";")(tokens.eat())

        ReturnStatement(expression)
    }
}


fun parseWhileStatement(tokens: Tokens): Result<WhileStatement?> {

    if (parseKeyword("while")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseKeyword("while")(tokens.eat())
        val ( _b ) = parseSymbol("(")(tokens.eat())
        val ( predicate ) = require(parseExpression(tokens), "predicate in while statement")
        val ( _c ) = parseSymbol(")")(tokens.eat())
        val ( _d ) = parseSymbol("{")(tokens.eat())
        val ( statements ) = parseStatements(tokens)
        val ( _e ) = parseSymbol("}")(tokens.eat())

        WhileStatement(predicate, statements)
    }
}

fun parseIfStatement(tokens: Tokens): Result<IfStatement?> {

    if (parseKeyword("if")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseKeyword("if")(tokens.eat())
        val ( _b ) = parseSymbol("(")(tokens.eat())
        val ( predicate ) = require(parseExpression(tokens), "predicate in if statement")
        val ( _c ) = parseSymbol(")")(tokens.eat())
        val ( _d ) = parseSymbol("{")(tokens.eat())
        val ( statements ) = parseStatements(tokens)
        val ( _e ) = parseSymbol("}")(tokens.eat())

        val altStatements= if (parseKeyword("else")(tokens.peak()).isFailure) {
            listOf()
        } else {
            val ( _f ) = parseKeyword("else")(tokens.eat())
            val ( _g ) = parseSymbol("{")(tokens.eat())
            val ( alternative ) = parseStatements(tokens)
            val ( _h ) = parseSymbol("}")(tokens.eat())
            alternative
        }

        IfStatement(predicate, statements, altStatements)
    }
}

fun parseLetStatement(tokens: Tokens): Result<LetStatement?> {

    if (parseKeyword("let")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseKeyword("let")(tokens.eat())
        val ( arrayAccess ) = parseArrayAccess(tokens, true)
        // @TOOD refactor
        if (arrayAccess == null) {
            val ( varName ) = parseVarName(tokens.eat())
            val ( _b ) = parseSymbol("=")(tokens.eat())
            val ( rhs ) = require(parseExpression(tokens), "right hand side of ${varName}")
            val ( _c ) = parseSymbol(";")(tokens.eat())

            LetStatement(varName, rhs)
        } else {
            val (_b) = parseSymbol("=")(tokens.eat())
            val (rhs) = require(parseExpression(tokens), "right hand side of ${arrayAccess}")
            val (_c) = parseSymbol(";")(tokens.eat())


            LetStatement(arrayAccess, rhs)
        }
    }
}


fun <T> require(result: Result<T?>, name: String = "something"): Result<T> =
        if (result.isSuccess && result.getOrNull() == null)
        failExceptionally("expected a value for ${name} but it was empty")
        else result as Result<T>

fun parseTerm(tokens: Tokens): Result<TermNode?> {
    return orMaybe(tokens,
            ::parseSubroutineCall,
            ::parseIntegerConstant,
            ::parseStringConstant,
            ::parseKeywordConstant,
            ::parseArrayAccess,
            ::parseVarName,
            ::parseTermExpression,
            ::parseUnaryOp);
}

fun parseExpression(tokens: Tokens): Result<Expression?> {

    parseTerm(tokens)
            .onFailure { return failure(it) }
            .onSuccess { term ->
                if (term == null) return success(null)

                parseOpTerm(tokens)
                        .onFailure { return failure(it) }
                        .onSuccess { opTerm -> return success(Expression(term, opTerm)) }
            }

    return success(null)
}

fun parseSubroutineCall(tokens: Tokens): Result<SubroutineCall?> = orMaybe(tokens,
        ::parseSimpleSubroutineCall,
        ::parseComplexSubroutineCall)

fun parseSimpleSubroutineCall(tokens: Tokens): Result<SimpleSubroutineCall?> {

    tokens.peak(2)
            .onFailure {
                return success(null) }
            .onSuccess {
                val (first, second) = it

                if (parseSubroutineName(first).isFailure || parseSymbol("(")(second).isFailure) return success(null)

                return requireAll {

                    val ( name ) = parseSubroutineName(tokens.eat())
                    val ( _a ) = parseSymbol("(")(tokens.eat())
                    val ( expressions ) = zeroOrMore(tokens, ::parseExpression, tryThanEat(parseSymbol(",")))

                    val ( _b ) = parseSymbol(")")(tokens.eat())

                    SimpleSubroutineCall(name, expressions)
                }
    }

    return success(null)
}

fun parseComplexSubroutineCall(tokens: Tokens): Result<ComplexSubroutineCall?> {

    tokens.peak(2)
            .onFailure {
                return success(null) }
            .onSuccess {
                val (first, second) = it

                if (parseIdentifier()(first).isFailure || parseSymbol(".")(second).isFailure) return success(null)

                return requireAll {

                    val ( identifier ) = parseIdentifier()(tokens.eat())
                    val ( _a ) = parseSymbol(".")(tokens.eat())
                    val ( name ) = parseSubroutineName(tokens.eat())
                    val ( _b ) = parseSymbol("(")(tokens.eat())
                    val ( expressions ) = zeroOrMore(tokens, ::parseExpression, tryThanEat(parseSymbol(",")))

                    val ( _c ) = parseSymbol(")")(tokens.eat())

                    ComplexSubroutineCall(identifier, name, expressions)
                }
            }

    return success(null)
}

fun<R> attempt(tokens: Tokens, parse: (Token) -> Result<R>): R? {
    return if (parse(tokens.peak()).isSuccess) parse(tokens.eat()).getOrNull() else null
}

fun parseOpTerm(tokens: Tokens): Result<OpTermNode?> {

    if (parseOp(tokens.peak()).isFailure) return success(null)

    return requireAll {
        val ( operator ) = parseOp(tokens.eat())

        val ( term ) = require(parseTerm(tokens))


        OpTermNode(operator, term)
    }
}

fun parseOp(token: Token): Result<Operator> {
    if (token !is SymbolToken) return failExceptionally("expected an operator but got ${token.value} as ${token.type} instead")

    return when (token.value) {
        "+" -> success(Operator.PLUS)
        "-" -> success(Operator.MINUS)
        "*" -> success(Operator.MULTIPLY)
        "/" -> success(Operator.DIVIDED)
        "&lt;" -> success(Operator.LESS_THAN) // <
        "&gt;" -> success(Operator.GREATER_THAN) // >
        "&amp;" -> success(Operator.AND) // &
        "|" -> success(Operator.OR)
        "=" -> success(Operator.EQUALS)
        "~" -> success(Operator.NOT)
        else -> failExceptionally("not a valid operator ${token.value}")
    }
}

fun parseSubroutineVarDeclaration(tokens: Tokens): Result<SubroutineVarDeclarationNode?> {

    if (parseKeyword("var")(tokens.peak()).isFailure) return success(null)

    return requireAll {
        val ( _a ) = parseKeyword("var")(tokens.eat())
        val ( type ) = parseType(tokens.eat())
        val ( varNames ) = atLeastOne(tokens, eat(::parseVarName), tryThanEat(parseSymbol(",")))
        val ( _b ) = parseSymbol(";")(tokens.eat())

        SubroutineVarDeclarationNode(type, varNames)
    }
}

fun parseParameters(tokens: Tokens): Result<List<Parameter>> {
    return zeroOrMore(tokens, ::parseParameter, tryThanEat(parseSymbol(",")))
}

fun parseParameter(tokens: Tokens): Result<Parameter?> {
    if (parseType(tokens.peak()).isFailure) return success(null)

    return requireAll {
        val ( type ) = parseType(tokens.eat())
        val ( name ) = parseVarName(tokens.eat())

        Parameter(type, name)
    }
}

fun <T> mappedKeywordParser(error: String? = null, validator: (KeywordToken) -> Boolean, mapper: (String) -> T): (Token) -> Result<T> {
    return {token -> if (token is KeywordToken && validator(token))
        success(token.value).map(mapper)
    else failExceptionally(error ?: "expected specific keyword but got ${token.value} as ${token.type}")
    }
}

fun keywordParser(error: String? = null, validator: (KeywordToken) -> Boolean): (Token) -> Result<String> {
    return {token -> if (token is KeywordToken && validator(token))
        success(token.value)
        else failExceptionally(error ?: "expected specific keyword but got ${token.value} as ${token.type}")
    }
}

fun parseIdentifier(error: String? = null): (Token) -> Result<String> {
    return {token ->
        val message = error ?: "expected an identifier but got ${token.value} as ${token.type}"
        if (token is IdentifierToken) success(token.value) else failExceptionally(message)
    }
}

val parseClassKeyword = keywordParser("expected the class keyword but wasn't there") { it.value ==  "class" }

fun parseSubroutineType(token: Token): Result<SubroutineDeclarationType> {
    return if (token is KeywordToken && token.value in listOf("constructor", "function", "method")) {
        val type = when(token.value) {
            "constructor" -> CONSTRUCTOR
            "function" -> FUNCTION
            "method" -> METHOD
            else -> throw IllegalStateException()
        }
        success(type)
    } else {
        failExceptionally("expected a subroutine type but got ${token.value} as ${token.type}")
    }
}

fun <R> eat(parse: (Token) -> Result<R>): (Tokens) -> Result<R> = fun(tokens): Result<R> {
    return parse(tokens.eat())
}

fun <R> tryThanEat(parse: (Token) -> Result<R>): (Tokens) -> Result<R?> = fun(tokens): Result<R?> {

        if (parse(tokens.peak()).isFailure) return success(null)

        return parse(tokens.eat())
}


fun parseKeyword(keyword: String): (Token) -> Result<Unit> = { token ->
    if (token is KeywordToken && token.value == keyword) success(Unit) else failExceptionally("expected keyword $keyword but got ${token.value} instead")
}

fun parseSymbol(symbol: String): (Token) -> Result<Unit> = { token ->
    if (token is SymbolToken && token.value == symbol) success(Unit) else failExceptionally("expected symbol $symbol but got ${token.value} instead")
}

fun parseClassName(token: Token): Result<TypeName> = if (token is IdentifierToken)
    success(TypeName(token.value)) else
    failExceptionally("expected a class name ${token.value} to be an identifier")

fun parseVarName(token: Token): Result<VarName> = if (token is IdentifierToken)
    success(VarName(token.value)) else
    failExceptionally("var name ${token.value} is not an identifier")

fun parseVarName(tokens: Tokens): Result<VarName?> = if (tokens.peak() is IdentifierToken)
    success(VarName(tokens.eat().value)) else
    success(null)

fun parseSubroutineName(token: Token): Result<String> = if (token is IdentifierToken)
    success(token.value) else
    failExceptionally("subroutine name ${token.value} is not an identifier" )

fun parsePrimitiveType(token: Token): Result<TypeName> = if (token is KeywordToken && token.value in listOf("int", "char", "boolean"))
    success(TypeName(token.value)) else
    failExceptionally("keyword passed in none of int char or boolean")

fun parseSubroutineReturnType(token: Token): Result<TypeName> {
    return or(token, ::parseType, mappedKeywordParser(null, { it.value == "void" }, { TypeName(it) }) )
}

fun parseType(token: Token): Result<TypeName> {
    return or(token, ::parsePrimitiveType, ::parseClassName)
}

fun parseStaticModifier(token: Token): Result<ClassVarStaticModifier> {

    if(!(token is KeywordToken && token.value in listOf("static", "field")))
        return failure(Exception("{$token.value} is not the keyword static or field"))

    return success(if (token.value == "static") STATIC else FIELD)
}

fun parseIntegerConstant(tokens: Tokens): Result<IntegerConstant?> = if (tokens.peak() is IntToken)
    success(IntegerConstant(tokens.eat().value.toInt(10)))
    else success(null)

fun parseStringConstant(tokens: Tokens): Result<StringConstant?> = if (tokens.peak() is StringToken)
    success(StringConstant(tokens.eat().value))
    else success(null)

fun parseKeywordConstant(tokens: Tokens): Result<KeywordConstant?> = tokens.peak().let {
    if (it is KeywordToken && it.value in listOf("true", "false", "this", "null"))
        success(KeywordConstant(tokens.eat().value))
        else success(null)
}

fun parseTermExpression(tokens: Tokens): Result<TermExpression?> {

    if (parseSymbol("(")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _a ) = parseSymbol("(")(tokens.eat())
        val ( expression ) = require(parseExpression(tokens), "expression")
        val ( _b ) = parseSymbol(")")(tokens.eat())

        TermExpression(expression)
    }
}

fun parseUnaryOp(tokens: Tokens): Result<UnaryOpNode?> {

    if (parseSymbol("-")(tokens.peak()).isFailure && parseSymbol("~")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( operator )  = parseOp(tokens.eat())
        val ( term ) = require(parseTerm(tokens), "term")

        UnaryOpNode(operator, term)
    }
}

fun parseArrayAccess(tokens: Tokens): Result<ArrayAccess?> = parseArrayAccess(tokens, false)

fun parseArrayAccess(tokens: Tokens, assignment: Boolean = false): Result<ArrayAccess?> {

     tokens.peak(2)
             .onFailure {
                 return success(null)
             }
             .onSuccess {
                 val (varName, symbol) = it

                 if (parseVarName(varName).isFailure || parseSymbol("[")(symbol).isFailure) return success(null)

                 return requireAll {

                     val ( varName ) = parseVarName(tokens.eat())
                     val ( _a ) = parseSymbol("[")(tokens.eat())
                     val ( expression ) = require(parseExpression(tokens), "expression")
                     val ( _b ) = parseSymbol("]")(tokens.eat())

                     ArrayAccess(varName, expression, assignment)
                 }
             }

    return success(null)
}