package io.toon.jack.parser

import io.toon.jack.Kind
import io.toon.jack.SymbolTable
import io.toon.jack.parser.ClassVarStaticModifier.FIELD
import io.toon.jack.parser.Segment.*
import io.toon.jack.parser.SubroutineDeclarationType.CONSTRUCTOR
import io.toon.jack.parser.SubroutineDeclarationType.METHOD


// @TODO have Node implement CodeGen
interface Node: XMLBuilder {}

data class ClassNode(
        val name: String,
        val classVarDeclarations: List<ClassVarDeclarationNode> = listOf(),
        val subroutineDeclarations: List<SubroutineDeclarationNode> = listOf()): Node, CodeGen {

    val fieldCount = classVarDeclarations.fold(0) { count, decl -> count + decl.fieldCount }

    override fun buildXML(): XML = xml("class") {
        keyword { "class" }
        identifier { name }
        symbol { "{" }
        classVarDeclarations.forEach { child { it } }
        subroutineDeclarations.forEach { child { it } }
        symbol { "}" }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, this) {
        symbols.defineClass(name)
        subroutineDeclarations.forEach { addStatements(it) }
    }
}

data class ClassVarDeclarationNode(
        val staticModifier: ClassVarStaticModifier,
        val typeName: TypeName,
        val varNames: List<VarName>): Node {

    val fieldCount = if (staticModifier == FIELD) varNames.size else 0

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
): Node, CodeGen {

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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {

        symbols.enterSubroutine(subroutineName)

        assert(classNode != null) { "Expected the classNode pased to generate the function name" }
        function("${classNode!!.name}.$subroutineName", body.varCount)
        if (declarationType == CONSTRUCTOR) {
            push(CONSTANT, classNode!!.fieldCount)
            call("Memory.alloc", 1)
            pop(POINTER, 0)
        }
        if (declarationType == METHOD) {
            push(ARGUMENT, 0)
            pop(POINTER, 0)
        }

        addStatements(body)

        symbols.leaveSubroutine()
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
): Node, CodeGen {

    val varCount = varDeclarations.fold(0) { total, declaration -> total + declaration.varCount }

    override fun buildXML(): XML = xml("subroutineBody") {
        symbol { "{" }

        varDeclarations.forEach { child { it } }

        xml("statements") {
            statements.forEach { child { it } }
        }

        symbol { "}" }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        statements.forEach { addStatements(it) }
    }
}

data class SubroutineVarDeclarationNode(
        val typeName: TypeName,
        val varNames: List<VarName>
): Node {

    val varCount: Int = varNames.size

    override fun buildXML(): XML = xml("varDec") {
        keyword { "var" }
        child { typeName }
        child { list(varNames) }
        symbol { ";" }

    }
}

interface Statement: Node, CodeGen

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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {

        addStatements(rightExpression)

        if (varName != null) {
            // @TODO delegate to varName
            // @TODO better assertions
            val (_, type, kind, index) = symbols.get(varName!!.name)!!
            pop(toSegment(kind), index)
        }
        if (arrayAccess != null) {
            addStatements(arrayAccess)

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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {

        val labelPrefix = symbols.genLabel("if")
        val withoutElse = altStatements.isEmpty()

        addStatements(predicate)
        not()
        if (withoutElse) {
            ifGoto("$labelPrefix.end")
            statements.forEach { addStatements(it) }
        } else {
            ifGoto("$labelPrefix.else")
            statements.forEach { addStatements(it) }
            goto("$labelPrefix.end")
            label("$labelPrefix.else")
            altStatements.forEach { addStatements(it) }
        }
        label("$labelPrefix.end")
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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        addStatements(subroutineCall)
        pop(TEMP, 0)
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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        if (expression == null) push(CONSTANT, 0)
        else addStatements(expression)
        returnIt()
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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {

        val labelPrefix = symbols.genLabel("while")

        label("$labelPrefix.start")
        addStatements(predicate)
        not()
        ifGoto("$labelPrefix.end")
        statements.forEach { addStatements(it) }
        goto("$labelPrefix.start")
        label("$labelPrefix.end")
    }
}

data class Expression(
        val term: TermNode,
        val opTerm: OpTermNode? = null
): Node, CodeGen {

    override fun buildXML(): XML {
        return xml("expression") {
            xml("term") { child { term } }

            if (opTerm != null) { child { opTerm } }
        }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        addStatements(term)
        if (opTerm != null) addStatements(opTerm)
    }
}

data class OpTermNode(
    val operator: Operator,
    val term: TermNode
): Node, CodeGen {
    override fun buildXML(): XML = xmlList {
        child { operator }
        xml("term") { child { term } }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        addStatements(term)
        addStatements(operator)
    }
}

enum class Operator(val value: String): Node, CodeGen {
    PLUS("+") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            add()
        }
    },
    MINUS("-") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            sub()
        }
    },
    MULTIPLY("*") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            call("Math.multiply", 2)
        }
    },
    DIVIDED("/") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            call("Math.divide", 2)
        }
    },
    AND("&amp;") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            and()
        }
    },
    OR("|") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            or()
        }
    },
    LESS_THAN("&lt;") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
           lt()
        }
    },
    GREATER_THAN("&gt;") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            gt()
        }
    },
    EQUALS("=") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            eq()
        }
    },
    NEGATE("~") {
        override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
            neg()
        }
    };

    override fun buildXML(): XML = xml("symbol") { just { value } }
}

interface TermNode: Node, CodeGen

data class IntegerConstant(val value: Int): TermNode {
    override fun buildXML(): XML = xml("integerConstant") {
        just { value.toString() }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        push(CONSTANT, value)
    }
}

data class StringConstant(val value: String): TermNode {
    override fun buildXML(): XML = xml("stringConstant") {
        just { value  }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        push(CONSTANT, value.length)
        call("String.new", 1)
        value.chars().forEach {
            push(CONSTANT, it)
            call("String.appendChar", 2)
        }
    }
}

data class KeywordConstant(val value: String): TermNode {
    override fun buildXML(): XML = xml("keyword") {
        just { value }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        // @TODO how do we know its only used as a termnode here?
        when (value) {
            "this" -> push(POINTER, 0)
            "null", "false" -> push(CONSTANT, 0)
            "true" -> {
                push(CONSTANT, 1)
                neg()
            }
            else -> throw NotImplementedError()
        }
    }
}

data class VarName(val name: String): TermNode {
    override fun buildXML(): XML = xml("identifier") { just { name } }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        // @TODO we dont know if its for accessing or assignment
        // @TODO better assertions
        val (_, type, kind, index) = symbols.get(name)!!

        when(kind) {
            Kind.STATIC -> push(STATIC, index)
            Kind.FIELD -> push(THIS, index)
            Kind.ARGUMENT -> push(ARGUMENT, index)
            Kind.VAR -> push(LOCAL, index)
        }

    }
}

data class ArrayAccess(val varName: VarName, val expression: Expression, val assignment: Boolean = false): TermNode {
    override fun buildXML(): XML = xmlList {
        child { varName }
        symbol { "[" }
        child { expression }
        symbol { "]" }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        val (_, type, kind, index) = symbols.get(varName!!.name)!!
        push(toSegment(kind), index)
        addStatements(expression)
        add()
        pop(POINTER, 1)
        if (assignment) pop(THAT, 0) else push(THAT, 0)
    }
}

data class TermExpression(val expression: Expression): TermNode {
    override fun buildXML(): XML = xmlList {
        symbol { "(" }
        child { expression }
        symbol { ")" }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        // @TODO should change order of operations
        addStatements(expression)
    }
}

data class UnaryOp(val op: Operator, val term: TermNode): TermNode {
    override fun buildXML(): XML = xmlList {
        child { op }
        xml("term") {
            child { term }
        }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        addStatements(term)
        addStatements(op)
    }
}

interface SubroutineCall: TermNode, CodeGen

data class SimpleSubroutineCall(val subroutineName: String, val expressions: List<Expression> = listOf()): SubroutineCall {
    override fun buildXML(): XML = xmlList {
        identifier { subroutineName }
        symbol { "(" }
        xml("expressionList") {
            if (expressions.isNotEmpty()) child { list(expressions) }
        }
        symbol { ")" }
    }

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        expressions.forEach { addStatements(it) }
        call("subroutineName", expressions.size)
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

    override fun genCode(symbols: SymbolTable, classNode: ClassNode?): List<String> = genVMCode(symbols, classNode) {
        val properties = symbols.get(identifier_)
        if (properties != null) {
            val (name, type, kind, index) = properties
            push(toSegment(kind), index)
            expressions.forEach { addStatements(it) }
            call("${type.name}.$subroutineName", expressions.size + 1)
        } else {
            expressions.forEach { addStatements(it) }
            call("$identifier_.$subroutineName", expressions.size)
        }
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
private fun toSegment(kind: Kind): Segment = when(kind) {
    Kind.STATIC -> STATIC
    Kind.FIELD -> THIS
    Kind.ARGUMENT -> ARGUMENT
    Kind.VAR -> LOCAL
}
