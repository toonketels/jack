package io.toon.jack.tokenizer

class JackTokenizer(private val input: String): Iterable<Token> {

    override fun iterator(): Iterator<Token> = TokenizerIterator(input, tokenize)

    fun toXml(): String {
        val self = this
        return buildString {
            appendln("<tokens>")
            for (token: Token in self) { appendln(    "<${token.type}> ${token.value} </${token.type}>") }
            appendln("</tokens>")
        }
    }
}

class TokenizerIterator(var input: String, var tokenize: Tokenizer): Iterator<Token> {

    var currentToken: Token? = nextToken()

    override fun hasNext(): Boolean = currentToken != null

    override fun next(): Token = currentToken!!.also { currentToken = nextToken() }

    fun nextToken(): Token? {
        var token: Token? = null
        var remainder = input

        while (token == null && remainder.isNotEmpty()) {
            val result = tokenize(remainder)?.also {
                remainder = it.remainder
                token = it.token
            }

            requireNotNull(result) { "Error parsing ${remainder.substring(0, 10)}" }
        }

        input = remainder
        return token
    }
}