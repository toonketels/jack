package io.toon.jack.parser

import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.ClassVarStaticModifier.STATIC
import io.toon.jack.parser.SubroutineDeclarationType.*
import io.toon.jack.tokenizer.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private fun <E> MutableList<E>.peak(): E = first()

private fun <E> MutableList<E>.eat(): E {
    val taken = first()
    removeAt(0)
    return taken
}


fun parseClass(tokens: MutableList<Token>): Result<Node?> {

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

fun parseClassVarDeclaration(tokens: MutableList<Token>): Result<ClassVarDeclarationNode?> {

    // Bail out if we dont start with static or fieldm
    if (parseStaticModifier(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( modifier ) = parseStaticModifier(tokens.eat())
        val ( type ) = parseType(tokens.eat())
        val ( name ) = parseVarName(tokens.eat())
        val ( otherVarNames ) = zeroOrMore(tokens, ::parseOtherVarName)
        val ( _ ) = parseSymbol(";")(tokens.eat())

        ClassVarDeclarationNode(
                staticModifier = modifier,
                typeName = type,
                varName = name,
                otherVarNames = otherVarNames)
    }
}

operator fun <T> Result<T>.component1() = getOrThrow()

fun parseOtherVarName(tokens: MutableList<Token>): Result<String?> {

    if (parseSymbol(",")(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {
        val ( _ ) = parseSymbol(",")(tokens.eat())
        val ( name ) = parseIdentifier()(tokens.eat())

        name
    }
}

fun parseSubroutineDeclaration(tokens: MutableList<Token>): Result<SubroutineDeclarationNode?> {

    if (parseSubroutineType(tokens.peak()).isFailure) {
        return success(null)
    }

    return requireAll {

        val ( subroutineType) = parseSubroutineType(tokens.eat())
        val ( returnType ) = parseSubroutineReturnType(tokens.eat())
        val ( subroutineName ) = parseSubroutineName(tokens.eat())
        val ( _ ) = parseSymbol("(")(tokens.eat())
        val ( _ ) = parseSymbol(")")(tokens.eat())

        SubroutineDeclarationNode(subroutineType, returnType, subroutineName, listOf())
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