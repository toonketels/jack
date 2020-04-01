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
        child { staticModifier }
        child { typeName }
        child { list(varNames) }
        symbol { ";" }
    }
}

enum class ClassVarStaticModifier(private val value: String): Node {
    STATIC("static"),
    FIELD("field");

    override fun buildXML(): XML = xml("keyword") { just { value } }
}

data class TypeName(val name: String): Node {
    private val tagName = if (name in listOf("void", "int", "char", "boolean")) "keyword" else "identifier"
    override fun buildXML(): XML = xml(tagName) { just { name } }
}

data class SubroutineDeclarationNode(
        val declarationType: SubroutineDeclarationType,
        val returnType: TypeName,
        val subroutineName: String,
        val parameterList: List<Parameter>,
        val body: SubroutineBodyNode
): Node {
    override fun buildXML(): XML = xml("subroutineDec") {
        child { declarationType }
        child { returnType }
        identifier { subroutineName }
        symbol { "(" }
        xml("parameterList") {
            if (parameterList.isNotEmpty()) child { list(parameterList) }
        }
        symbol { ")" }
        child { body }

    }
}

enum class SubroutineDeclarationType(val value: String): Node {
    CONSTRUCTOR("constructor"),
    FUNCTION("function"),
    METHOD("method");

    override fun buildXML(): XML = xml("keyword") { just { value } }
}

data class Parameter(
        val type: TypeName,
        val name: VarName
): Node {
    override fun buildXML(): XML = xmlList {
        child { type }
        child { name }
    }
}

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
        child { typeName }
        child { list(varNames) }
        symbol { ";" }

    }
}

interface Statement: Node

data class LetStatement private constructor(
        val varName: VarName?,
        val arrayAccess: ArrayAccess?,
        val rightExpression: Expression
): Statement {

    constructor(varName: VarName, rightExpression: Expression): this(varName, null, rightExpression)
    constructor(arrayAccess: ArrayAccess, rightExpression: Expression): this(null, arrayAccess, rightExpression)

    override fun buildXML(): XML {
        return xml("letStatement") {
            keyword { "let" }
            if (varName != null) {
                child { varName }
            } else {
                child { arrayAccess!! }
            }
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
        xml("statements") {
            statements.forEach { child { it } }
        }
        symbol { "}" }

        if (altStatements.isNotEmpty()) {
            keyword { "else" }
            symbol { "{" }
            xml("statements") {
                altStatements.forEach { child { it } }
            }
            symbol { "}" }
        }
    }
}

data class DoStatement(
        val subroutineCall: SubroutineCall
): Statement {
    override fun buildXML(): XML = xml("doStatement") {
        keyword { "do" }
        child { subroutineCall }
        symbol { ";" }
    }
}

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
        xml("statements") {
            statements.forEach { child { it } }
        }
        symbol { "}" }
    }
}

data class Expression(
        val term: TermNode,
        val opTerm: OpTermNode? = null
): Node {

    override fun buildXML(): XML {
        return xml("expression") {
            xml("term") { child { term } }

            if (opTerm != null) { child { opTerm } }
        }
    }
}

data class OpTermNode(
    val operator: Operator,
    val term: TermNode
): Node {
    override fun buildXML(): XML = xmlList {
        child { operator }
        xml("term") { child { term } }
    }
}

enum class Operator(val value: String): Node {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDED("/"),
    AND("&amp;"),
    OR("|"),
    LESS_THAN("&lt;"),
    GREATER_THAN("&gt;"),
    EQUALS("="),
    NEGATE("~");

    override fun buildXML(): XML = xml("symbol") { just { value } }
}

interface TermNode: Node

data class IntegerConstant(val value: Int): TermNode {
    override fun buildXML(): XML = xml("integerConstant") {
        just { value.toString() }
    }
}

data class StringConstant(val value: String): TermNode {
    override fun buildXML(): XML = xml("stringConstant") {
        just { value  }
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

data class ArrayAccess(val varName: VarName, val expression: Expression): TermNode {
    override fun buildXML(): XML = xmlList {
        child { varName }
        symbol { "[" }
        child { expression }
        symbol { "]" }
    }
}

data class TermExpression(val expression: Expression): TermNode {
    override fun buildXML(): XML = xmlList {
        symbol { "(" }
        child { expression }
        symbol { ")" }
    }
}

data class UnaryOp(val op: Operator, val term: TermNode): TermNode {
    override fun buildXML(): XML = xmlList {
        child { op }
        xml("term") {
            child { term }
        }
    }
}

interface SubroutineCall: TermNode

data class SimpleSubroutineCall(val subroutineName: String, val expressions: List<Expression> = listOf()): SubroutineCall {
    override fun buildXML(): XML = xmlList {
        identifier { subroutineName }
        symbol { "(" }
        xml("expressionList") {
            if (expressions.isNotEmpty()) child { list(expressions) }
        }
        symbol { ")" }
    }
}

data class ComplexSubroutineCall(val identifier_: String, val subroutineName: String, val expressions: List<Expression> = listOf()): SubroutineCall {
    override fun buildXML(): XML = xmlList {
        identifier { identifier_ }
        symbol { "." }
        identifier { subroutineName }
        symbol { "(" }
        xml("expressionList") {
            if (expressions.isNotEmpty()) child { list(expressions) }
        }
        symbol { ")" }
    }
}

private fun list(items: List<Node>, separator: String = ","): XMLBuilder = object: XMLBuilder {
    override fun buildXML(): XML = xmlList {
        for ((index, name) in items.withIndex()) {
            child { name }
            if (index+1 != items.size) {
                symbol { separator }
            }
        }
    }
}