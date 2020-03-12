package io.toon.jack.parser

import io.toon.jack.tokenizer.Token
import io.toon.jack.tokenizer.TokenType
import io.toon.jack.tokenizer.TokenType.*
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

fun parseClass(tokens: List<Token>): Result<Node?> {

    val keyword = tokens.first()

    // @TODO revert back?
    if (keyword.type != KEYWORD && keyword.value != "class") return success(null)

    val ( _keyword, className, symbol ) = tokens.take(3)

    if (className.type != IDENTIFIER) return failure(Exception("class keyword should be followed by an identifier"))
    if (symbol.type != SYMBOL && symbol.value != "{") return failure(Exception("missing { in class declaration"))

    val (closing) = tokens.drop(3).take(1)

    if (closing.type != SYMBOL && symbol.value != "}") return failure(Exception("missing } in class declaration"))

    return success(ClassNode(
            name = className.value,
            classVarDeclarations = listOf(),
            subroutineDeclaration = listOf()
    ))
}