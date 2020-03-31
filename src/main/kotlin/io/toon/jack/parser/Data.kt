package io.toon.jack.parser

interface Node: XMLBuilder {}

data class ClassNode(
        val name: String,
        val classVarDeclarations: List<ClassVarDeclarationNode> = listOf(),
        val subroutineDeclarations: List<SubroutineDeclarationNode> = listOf()): Node {

    override fun buildXML(): XML = xml("class") {
        keyword { "class" }
        identifier { name }
        symbol { "{" }
        classVarDeclarations.forEach { child { it } }
        subroutineDeclarations.forEach { child { it } }
        symbol { "}" }
    }
}

data class ClassVarDeclarationNode(
        val staticModifier: ClassVarStaticModifier,
        val typeName: TypeName,
        val varNames: List<VarName>): Node {

    override fun buildXML(): XML = xml("classVarDec") {
        keyword { staticModifier.value }
        // @TODO maybe not enough info retained to determine its a keyword
        keyword { typeName }

        for ((index, name) in varNames.withIndex()) {
            child { name }
            if (index+1 != varNames.size) {
                symbol { "," }
            }
        }
        symbol { ";" }
    }
}

enum class ClassVarStaticModifier(val value: String) {
    STATIC("static"),
    FIELD("field")
}

typealias TypeName = String

data class SubroutineDeclarationNode(
        val declarationType: SubroutineDeclarationType,
        val returnType: TypeName,
        val subroutineName: String,
        val parameterList: List<Parameter>,
        val body: SubroutineBodyNode
): Node {
    override fun buildXML(): XML = xml("subroutineDec") {
        keyword { declarationType.value }
        keyword { returnType }
        identifier { subroutineName }
        symbol { "(" }
        xml("parameterList") {
            for ((index, parameter) in parameterList.withIndex()) {

                // @TODO also add this one as a children { parameter }
                val (type, name) = parameter
                // @TODO prob not always a keyword
                keyword { type }
                child { name }

                if (index+1 != parameterList.size) {
                    symbol { "," }
                }
            }
        }
        symbol { ")" }
        child { body }

    }
}

enum class SubroutineDeclarationType(val value: String) {
    CONSTRUCTOR("constructor"),
    FUNCTION("function"),
    METHOD("method")
}

data class Parameter(
        val type: TypeName,
        val name: VarName
): Node

data class SubroutineBodyNode(
        val varDeclarations: List<SubroutineVarDeclarationNode>,
        val statements: List<Statement>
): Node {
    override fun buildXML(): XML = xml("subroutineBody") {
        symbol { "{" }

        varDeclarations.forEach { child { it } }

        xml("statements") {
            statements.forEach { child { it } }
        }

        symbol { "}" }
    }
}

data class SubroutineVarDeclarationNode(
        val typeName: TypeName,
        val varNames: List<VarName>
): Node {
    override fun buildXML(): XML = xml("varDec") {
        keyword { "var" }
        // @TODO could be an identifier too
        keyword { typeName }
        for ((index, varName) in varNames.withIndex()) {
            child { varName }
            if (index+1 != varNames.size) {
                symbol { "," }
            }
        }
        symbol { ";" }

    }
}

interface Statement: Node

// @TODO better handling arrayAccess
data class LetStatement(
        val varName: VarName?,
        val arrayAccess: ArrayAccess?,
        val rightExpression: Expression
): Statement {

    constructor(varName: VarName, rightExpression: Expression): this(varName, null, rightExpression)
    constructor(arrayAccess: ArrayAccess, rightExpression: Expression): this(null, arrayAccess, rightExpression)

    override fun buildXML(): XML {
        return xml("letStatement") {
            keyword { "let" }
            // @TODO arrayAccess print it
            child { varName!! }
            symbol { "=" }
            child { rightExpression }
            symbol { ";" }
        }
    }
}

data class IfStatement(
        val predicate: Expression,
        val statements: List<Statement>,
        val altStatements: List<Statement> = listOf()
): Statement {

    override fun buildXML(): XML = xml("ifStatement") {
        keyword { "if" }
        symbol { "(" }
        child { predicate }
        symbol { ")" }
        symbol { "{" }
        statements.forEach { child { it } }
        symbol { "}" }

        if (altStatements.isNotEmpty()) {
            keyword { "else" }
            symbol { "{" }
            altStatements.forEach { child { it } }
            symbol { "}" }
        }
    }
}

data class DoStatement(
        val subroutineCall: SubroutineCall
): Statement

data class ReturnStatement(
        val expression: Expression? = null
): Statement {
    override fun buildXML(): XML = xml("returnStatement") {
        keyword { "return" }
        if (expression != null) child { expression }
        symbol { ";" }
    }
}

data class WhileStatement(
        val predicate: Expression,
        val statements: List<Statement>
): Statement {
    override fun buildXML(): XML = xml("whileStatement") {
        keyword { "while" }
        symbol { "(" }
        child { predicate }
        symbol { ")" }
        symbol { "{" }
        statements.forEach { child { it } }
        symbol { "}" }
    }
}

data class Expression(
        // @TODO better expressions
        val term: TermNode,
        val opTerm: OpTermNode? = null
): Node {

    override fun buildXML(): XML {
        return xml("expression") {
            xml("term") {
                child { term }
            }
        }
    }
}

data class OpTermNode(
    val operator: Operator,
    val term: TermNode
): Node

public enum class Operator(val value: String) {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDED("/"),
    AND("$"),
    OR("|"),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    EQUALS("=")

}

interface TermNode: Node

data class IntegerConstant(val value: Int): TermNode {
    override fun buildXML(): XML = xml("integerConstant") {
        just { value.toString() }
    }
}

data class StringConstant(val value: String): TermNode {
    override fun buildXML(): XML = xml("stringConstant") {
        just { value }
    }
}

data class KeywordConstant(val value: String): TermNode {
    override fun buildXML(): XML = xml("keyword") {
        just { value }
    }
}

data class VarName(val name: String): TermNode {
    override fun buildXML(): XML = xml("identifier") { just { name } }
}

// We might want to return a list
data class ArrayAccess(val varName: VarName, val expression: Expression): TermNode

data class TermExpression(val expression: Expression): TermNode

data class UnaryOp(val op: String, val term: TermNode): TermNode

interface SubroutineCall: TermNode

data class SimpleSubroutineCall(val subroutineName: String, val expressions: List<Expression> = listOf()): SubroutineCall
data class ComplexSubroutineCall(val identifier: String, val subroutineName: String, val expressions: List<Expression> = listOf()): SubroutineCall