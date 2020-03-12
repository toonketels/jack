package io.toon.jack.parser

import io.toon.jack.tokenizer.Token
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

// Converts exceptions into success and failure
fun <T> requireAll(callback: () -> T): Result<T> {
    return try {
        success(callback())
    } catch (e: Exception) {
        failure(e)
    }
}

// Returns the result of the first parser that succeeds
fun <I, R> or(input: I, vararg parsers: (I) -> Result<R>): Result<R> {
    val failures = mutableListOf<Throwable>()
    for (parse in parsers) {
        parse(input)
                .onFailure { failures.add(it) }
                .onSuccess { return success(it) }
    }
    return failExceptionally("none of the parsers successful: ${failures.joinToString { it.message!! }}")
}

// @TODO maybe not pass list
// Builds a list of zero more items obtained by applying the same parser multiple times
fun <I, R> zeroOrMore(tokens: MutableList<I>, parse: (MutableList<I>) -> Result<R?>): Result<List<R>> {

    val acc = mutableListOf<R>()
    while (true) {
        if (tokens.isEmpty()) return success(acc)
        parse(tokens)
                .onFailure { return Result.failure(it) }
                .onSuccess {
                    if (it == null) return success(acc)
                    acc.add(it!!)
                }
    }
}

fun <T> failExceptionally(message: String): Result<T> = failure(Exception(message))