package io.toon.jack.parser

interface Node {}

data class ClassNode(
        val name: String,
        val classVarDeclarations: List<ClassVarDeclarationNode> = listOf(),
        val subroutineDeclarations: List<SubroutineDeclarationNode> = listOf()): Node

data class ClassVarDeclarationNode(
        val staticModifier: ClassVarStaticModifier,
        val typeName: TypeName,
        val varNames: List<String>): Node

enum class ClassVarStaticModifier {
    STATIC,
    FIELD
}

typealias TypeName = String

data class SubroutineDeclarationNode(
        val declarationType: SubroutineDeclarationType,
        val returnType: TypeName,
        val subroutineName: String,
        val parameterList: List<Parameter>,
        val body: SubroutineBodyNode
): Node

enum class SubroutineDeclarationType {
    CONSTRUCTOR,
    FUNCTION,
    METHOD
}

data class Parameter(
        val type: TypeName,
        val name: String
): Node

data class SubroutineBodyNode(
        val varDeclarations: List<SubroutineVarDeclarationNode>,
        val statements: List<Statement>
): Node

data class SubroutineVarDeclarationNode(
        val typeName: TypeName,
        val varNames: List<String>
)

interface Statement: Node

data class LetStatement(
        val varName: String,
//        @TODO array accessor
        val rightExpression: Expression
 ): Statement

data class IfStatement(
        val predicate: Expression,
        val statements: List<Statement>,
        val altStatements: List<Statement> = listOf()
): Statement

data class Expression(
        // @TODO better expressions
        val term: String
): Node