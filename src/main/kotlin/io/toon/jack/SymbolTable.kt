package io.toon.jack

import io.toon.jack.Kind.ARGUMENT
import io.toon.jack.Kind.VAR
import io.toon.jack.parser.*
import io.toon.jack.parser.ClassVarStaticModifier.*
import io.toon.jack.parser.SubroutineDeclarationType.METHOD

typealias PropertiesMap = MutableMap<String, Properties>

class SymbolTable {
    val classTable: PropertiesMap = mutableMapOf()
    val subroutineTables: MutableMap<String, PropertiesMap> = mutableMapOf()
    var className: String? = null
    var currentSubroutine: String? = null
    var uniqueLabelId = 1

    fun addTableSymbol(properties: Properties) {
        classTable.put(properties.name, properties)
    }

    fun addSubroutineSymbol(subroutineName: String, properties: Properties) {
        getSubroutineTable(subroutineName).put(properties.name, properties)
    }

    private fun getSubroutineTable(subroutineName: String): PropertiesMap {
        if (subroutineTables[subroutineName] == null) subroutineTables[subroutineName] = mutableMapOf()

        return subroutineTables[subroutineName]!!
    }

    fun get(varName: String, subroutineName: String): Properties? {
        return subroutineTables[subroutineName]?.get(varName) ?: classTable[varName]
    }

    fun get(varName: String): Properties? {
        return if (currentSubroutine == null) classTable[varName]
        else get(varName, currentSubroutine!!)
    }

    // @TODO extract to state object
    fun defineClass(name: String) { className = name }
    fun enterSubroutine(subroutineName: String) {
        currentSubroutine = subroutineName
    }

    fun genLabel(name: String): String {
        val label = "$className.$currentSubroutine.$name.$uniqueLabelId"
        uniqueLabelId++
        return label
    }

    fun leaveSubroutine() {
        currentSubroutine == null
        uniqueLabelId = 1
    }
}



fun createSymbolTable(node: ClassNode): SymbolTable {
    val table = SymbolTable()

    node.classVarDeclarations
            .groupBy { it.staticModifier }
            .flatMap { (kind, declarations) ->
                createClassVarPropertiesByKind(kind, declarations)
            }
            .forEach { table.addTableSymbol(it) }

    node.subroutineDeclarations.forEach { subroutine ->
        val subroutineName = subroutine.subroutineName
        createSubroutineArgumentProperties(subroutine, node.name)
                .forEach { table.addSubroutineSymbol(subroutine.subroutineName, it) }
       createSubroutineVarProperties(subroutine)
                .forEach { table.addSubroutineSymbol(subroutineName, it) }
    }
    return table
}

private fun createSubroutineVarProperties(subroutine: SubroutineDeclarationNode): List<Properties> {
    return subroutine
            .body
            .varDeclarations
            .mapIndexed { declarationIndex, (typeName, varNames) ->

                varNames.mapIndexed { varIndex, (name) ->
                    Properties(name, typeName, VAR, declarationIndex + varIndex)
                }

            }
            .flatten()
}

private fun createSubroutineArgumentProperties(subroutine: SubroutineDeclarationNode, className: String): List<Properties> {

    val parameters: List<Parameter> = if (subroutine.declarationType == METHOD) {
        listOf(Parameter(TypeName(className), VarName("this"))) + subroutine.parameterList
    } else subroutine.parameterList

    return parameters
            .mapIndexed { index, (type, name) ->
                Properties(name.name, type, ARGUMENT, index)
            }
}

private fun createClassVarPropertiesByKind(_kind: ClassVarStaticModifier, declarations: List<ClassVarDeclarationNode>): List<Properties> {

        return declarations.mapIndexed { declarationIndex, (_, type, names) ->

            names.mapIndexed { varIndex, (name) ->
                val kind = when (_kind) {
                    STATIC -> Kind.STATIC
                    FIELD -> Kind.FIELD
                }

                val index = declarationIndex + varIndex

                Properties(name, type, kind, index)
            }
        }.flatten()
}

data class Properties(val name: String, val type: TypeName, val kind: Kind, val index: Int)

enum class Kind {
    STATIC,
    FIELD,
    ARGUMENT,
    VAR;
}