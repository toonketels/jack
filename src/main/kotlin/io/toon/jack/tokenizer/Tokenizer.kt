package io.toon.jack.tokenizer

import io.toon.jack.tokenizer.TokenType.*

// @TODO get rid of enum as no longer useful
enum class TokenType(private val representation: String) {
    KEYWORD("keyword"),
    SYMBOL("symbol"),
    IDENTIFIER("identifier"),
    INT_CONST("integerConstant"),
    STRING_CONST("stringConstant");

    override fun toString(): String {
        return representation
    }
}

interface Token {
    val value: String
    val type: TokenType
}

data class KeywordToken(override val value: String): Token {
    override val type: TokenType = KEYWORD
}

data class SymbolToken(override val value: String): Token {
    override val type: TokenType = SYMBOL
}

data class IdentifierToken(override val value: String): Token {
    override val type: TokenType = IDENTIFIER
}

data class IntToken(override val value: String) : Token {
    override val type: TokenType = INT_CONST
}

data class StringToken(override val value: String) : Token {
    override val type: TokenType = STRING_CONST
}

data class TokenResult(val remainder: String, val token: Token? = null)

typealias Tokenizer = (String) -> TokenResult?

private val SYMBOLS = listOf('{', '}', '(', ')', '[', ']', '.', ',', ';', '+', '-', '*', '/', '&', '|', '<', '>', '=', '~')
private val KEYWORDS = listOf("class", "method", "function", "constructor", "int", "boolean", "char", "void", "var", "static", "field",
        "let", "do", "if", "else", "while", "return", "true", "false", "null", "this")

val tokenize: Tokenizer = any(::parseWhiteSpace, ::parseComment, ::parseString, ::parseSymbol, ::parseKeyword, ::parseInt, ::parseIdentifier)

fun parseString(input: String): TokenResult? {

    if (input.startsWith('"')) {
        val match = input.drop(1).takeWhile { it != '"' }
        return TokenResult(input.substring(match.length + 2), StringToken(match))
    }

    return null
}

fun parseKeyword(input: String): TokenResult? {
    val boundary = input.takeWhile { !isBoundary(it) }
    val match = KEYWORDS.find { it == boundary }

    if (match != null) {
        return TokenResult(input.substring(match.length), KeywordToken(match))
    }

    return null

}

private fun isBoundary(it: Char) = it.isWhitespace() || it in SYMBOLS

fun parseSymbol(input: String): TokenResult? {

    if (input.isEmpty()) return null

    var char = input.first()

    if (char in SYMBOLS) {
        val value = when(char.toString()) {
            "<" -> "&lt;"
            ">" -> "&gt;"
            "&" -> "&amp;"
            else  -> char.toString()
        }
        return TokenResult(input.substring(1), SymbolToken(value))
    }

    return null

}

fun parseComment(input: String): TokenResult? {
    if (input.startsWith("//")) {

        val match = input.takeWhile { it != '\n' }
        return TokenResult(input.substring(match.length + 1))

    }
    if (input.startsWith("/*")) {

        val match = input.substringBefore("*/")
        return TokenResult(input.substring(match.length + 2))

    }
    return null;
}

fun parseWhiteSpace(input: String): TokenResult? {
    var match = input.takeWhile { it.isWhitespace() }

    if (match.isNotEmpty()) return TokenResult(input.drop(match.length))

    return null
}

fun parseInt(input: String): TokenResult? {
    val boundary = input.takeWhile { !isBoundary(it) }
    val isNumberic = boundary.isNotBlank() && boundary.all { it.isDigit() }

    if (isNumberic) {
        return TokenResult(input.substring(boundary.length), IntToken(boundary))
    }

    return null
}

fun parseIdentifier(input: String): TokenResult? {
    val boundary = input.takeWhile { !isBoundary(it) }

    val isIdentifier = boundary.isNotEmpty() && boundary.all { it.isLetterOrDigit() || it == '_' } && !boundary.first().isDigit()

    if (isIdentifier) {
        return TokenResult(input.substring(boundary.length), IdentifierToken(boundary))
    }

    return null
}


fun any(vararg tokenizers: Tokenizer): Tokenizer = tokenizers
        .toList()
        .reduceRight {a, b -> either(a, b) }

fun either(a: Tokenizer, b: Tokenizer): Tokenizer = { input -> a(input) ?: b(input) }
