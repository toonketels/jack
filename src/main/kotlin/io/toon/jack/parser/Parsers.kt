package io.toon.jack.parser

import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.ClassVarStaticModifier.STATIC
import io.toon.jack.parser.SubroutineDeclarationType.*
import io.toon.jack.tokenizer.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

typealias Tokens = MutableList<Token>

private fun <E> MutableList<E>.peak(): E = first()


private fun <E> MutableList<E>.eat(): E {
    val taken = first()
    removeAt(0)
    return taken
}

fun parseClass(tokens: Tokens): Result<Node?> {

    if (parseClassKeyword(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( _) = parseClassKeyword(tokens.eat())
        val ( name) = parseClassName(tokens.eat())
        val ( _ ) = parseSymbol("{")(tokens.eat())
        val ( varDeclarations ) = zeroOrMore(tokens, ::parseClassVarDeclaration)
        val ( subroutineDeclarations ) = zeroOrMore(tokens, ::parseSubroutineDeclaration)
        val ( _) = parseSymbol("}")(tokens.eat())

        ClassNode(
                name = name,
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
        val ( _ ) = parseSymbol(";")(tokens.eat())

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
        val ( _ ) = parseSymbol("(")(tokens.eat())
        val ( parameters ) = parseParameters(tokens)
        val ( _ ) = parseSymbol(")")(tokens.eat())
        val ( body ) = parseSubroutineBody(tokens)

        SubroutineDeclarationNode(subroutineType, returnType, subroutineName, parameters, body)
    }
}

fun parseSubroutineBody(tokens: Tokens): Result<SubroutineBodyNode> {

    return requireAll {

        val ( _ ) = parseSymbol("{")(tokens.eat())
        val ( varDeclarations) = zeroOrMore(tokens, ::parseSubroutineVarDeclaration)
        val ( statements ) = zeroOrMore(tokens, ::parseStatement)
        val ( _ ) = parseSymbol("}")(tokens.eat())

        SubroutineBodyNode(varDeclarations, listOf())
    }

}

fun parseStatements(tokens: Tokens): Result<List<Statement>> {
    return zeroOrMore(tokens, ::parseStatement)
}

fun parseStatement(tokens: Tokens): Result<Statement?> {
    return orMaybe(tokens,
            ::parseLetStatement,
            ::parseIfStatement)
}

fun parseIfStatement(tokens: Tokens): Result<IfStatement?> {

    if (parseKeyword("if")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _ ) = parseKeyword("if")(tokens.eat())
        val ( _ ) = parseSymbol("(")(tokens.eat())
        val ( predicate ) = parseExpression(tokens)
        val ( _ ) = parseSymbol(")")(tokens.eat())
        val ( _ ) = parseSymbol("{")(tokens.eat())
        val ( statements ) = parseStatements(tokens)
        val ( _ ) = parseSymbol("}")(tokens.eat())

        val altStatements= if (parseKeyword("else")(tokens.peak()).isFailure) {
            listOf()
        } else {
            val ( _ ) = parseKeyword("else")(tokens.eat())
            val ( _ ) = parseSymbol("{")(tokens.eat())
            val ( alternative ) = parseStatements(tokens)
            val ( _ ) = parseSymbol("}")(tokens.eat())
            alternative
        }

        IfStatement(predicate, statements, altStatements)
    }
}

fun parseLetStatement(tokens: Tokens): Result<LetStatement?> {

    if (parseKeyword("let")(tokens.peak()).isFailure) return success(null)

    return requireAll {

        val ( _ ) = parseKeyword("let")(tokens.eat())
        val ( varName ) = parseVarName(tokens.eat())
        val ( _ ) = parseSymbol("=")(tokens.eat())
        val ( rhs ) = parseExpression(tokens)
        val ( _ ) = parseSymbol(";")(tokens.eat())


        LetStatement(varName, rhs)
    }
}

fun parseExpression(tokens: Tokens): Result<Expression> {

    return requireAll {

        val ( varName ) = parseVarName(tokens.eat())

        Expression(varName)
    }
}

fun parseSubroutineVarDeclaration(tokens: Tokens): Result<SubroutineVarDeclarationNode?> {

    if (parseKeyword("var")(tokens.peak()).isFailure) return success(null)

    return requireAll {
        val ( _ ) = parseKeyword("var")(tokens.eat())
        val ( type ) = parseType(tokens.eat())
        val ( varNames ) = atLeastOne(tokens, eat(::parseVarName), tryThanEat(parseSymbol(",")))
        val ( _ ) = parseSymbol(";")(tokens.eat())

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

fun parseClassName(token: Token): Result<String> = if (token is IdentifierToken)
    success(token.value) else
    failExceptionally("expected a class name ${token.value} to be an identifier")

fun parseVarName(token: Token): Result<String> = if (token is IdentifierToken)
    success(token.value) else
    failExceptionally("var name ${token.value} is not an identifier")

fun parseSubroutineName(token: Token): Result<String> = if (token is IdentifierToken)
    success(token.value) else
    failExceptionally("subroutine name ${token.value} is not an identifier" )

fun parsePrimitiveType(token: Token): Result<String> = if (token is KeywordToken && token.value in listOf("int", "char", "boolean"))
    success(token.value) else
    failExceptionally("keyword passed in none of int char or boolean")

fun parseSubroutineReturnType(token: Token): Result<String> {
    return or(token, ::parseType, keywordParser { it.value == "void" })
}

fun parseType(token: Token): Result<String> {
    return or(token, ::parsePrimitiveType, ::parseClassName)
}

fun parseStaticModifier(token: Token): Result<ClassVarStaticModifier> {

    if(!(token is KeywordToken && token.value in listOf("static", "field")))
        return failure(Exception("{$token.value} is not the keyword static or field"))

    return success(if (token.value == "static") STATIC else FIELD)
}