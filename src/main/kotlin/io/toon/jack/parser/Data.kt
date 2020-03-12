package io.toon.jack.parser

interface Node {}

data class ClassNode(
        val name: String,
        val classVarDeclarations: List<ClassVarDeclarationNode> = listOf(),
        val subroutineDeclarations: List<SubroutineDeclarationNode> = listOf()): Node

data class ClassVarDeclarationNode(
        val staticModifier: ClassVarStaticModifier,
        val typeName: TypeName,
        val varName: String,
        val otherVarNames: List<String>): Node

enum class ClassVarStaticModifier {
    STATIC,
    FIELD
}

typealias TypeName = String

data class SubroutineDeclarationNode(
        val declarationType: SubroutineDeclarationType,
        val returnType: TypeName,
        val subroutineName: String,
        val parameterList: List<Node>
): Node

enum class SubroutineDeclarationType {
    CONSTRUCTOR,
    FUNCTION,
    METHOD
}
