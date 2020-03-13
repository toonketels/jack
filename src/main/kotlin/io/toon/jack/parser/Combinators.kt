package io.toon.jack.parser

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

// Returns the result of the first parser that succeeds with a value
fun <I, R> orMaybe(input: I, vararg parsers: (I) -> Result<R?>): Result<R?> {
    val failures = mutableListOf<Throwable>()
    for (parse in parsers) {
        parse(input)
                .onFailure { return failure(it) }
                .onSuccess { if (it !=null) return success(it) }
    }
    return success(null)
}

// Builds a list of zero more items obtained by applying the same parser multiple times
fun <I, R> zeroOrMore(tokens: MutableList<I>, parse: (MutableList<I>) -> Result<R?>): Result<List<R>> {

    val acc = mutableListOf<R>()
    while (true) {
        if (tokens.isEmpty()) return success(acc)
        parse(tokens)
                .onFailure { return failure(it) }
                .onSuccess {
                    if (it == null) return success(acc)
                    acc.add(it!!)
                }
    }
}

fun <I, R> zeroOrMore(tokens: MutableList<I>, parseItem: (MutableList<I>) -> Result<R?>, parseSeparator: (MutableList<I>) -> Result<Any?>): Result<List<R>> {

    val acc = mutableListOf<R>()
    while (true) {
        // Stop when we have no more tokens to parse
        if (tokens.isEmpty()) return success(acc)
        // After the first round, only continue when a separator is found
        if (acc.isNotEmpty()) parseSeparator(tokens)
                .onFailure { return failure(it) }
                .onSuccess { if (it == null) return success(acc) }
        // Parse the actual item to accumulate it
        parseItem(tokens)
                .onFailure { return failure(it) }
                .onSuccess {
                    if (it == null) return success(acc)
                    acc.add(it!!)
                }
    }
}

fun <I, R> atLeastOne(tokens: MutableList<I>, parse: (MutableList<I>) -> Result<R?>): Result<List<R>> {
    return zeroOrMore(tokens, parse)
            .onFailure { return failure(it) }
            .onSuccess { return if (it.isEmpty()) failExceptionally("expected at least one item but got zero") else success(it) }


}

fun <I, R> atLeastOne(tokens: MutableList<I>, parseItem: (MutableList<I>) -> Result<R?>, parseSeparator: (MutableList<I>) -> Result<Any?>): Result<List<R>> {
    return zeroOrMore(tokens, parseItem, parseSeparator)
            .onFailure { return failure(it) }
            .onSuccess { return if (it.isEmpty()) failExceptionally("expected at least one item but got zero") else success(it) }


}


fun <T> failExceptionally(message: String): Result<T> = failure(Exception(message))