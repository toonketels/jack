package io.toon.jack

import io.toon.jack.Kind.*
import io.toon.jack.parser.TypeName
import kotlin.test.*

class SymbolTableTest {

    @Test fun createSymbolTableTest() {

        val source = """
            class Square { 
                field int x, y; 
                field int z;
                static String greeting;
                static String prefix, suffix;
                 
                 constructor Square new(int anX, int aY) {
                    var boolean ok, nok;
                    var int i;
                 }
                 
                 method void distance(Square other) {
                 
                 }
            }
        """.trimIndent()

        val node = parse(source).getOrThrow()

        val table  = createSymbolTable(node)

        assertEquals(table.classTable, mapOf<String, Properties>(
                "x" to Properties("x", TypeName("int"), FIELD, 0),
                "y" to Properties("y", TypeName("int"), FIELD, 1),
                "z" to Properties("z", TypeName("int"), FIELD, 2),
                "greeting" to Properties("greeting", TypeName("String"), STATIC, 0),
                "prefix" to Properties("prefix", TypeName("String"), STATIC, 1),
                "suffix" to Properties("suffix", TypeName("String"), STATIC, 2)
        ))

        assertEquals(table.subroutineTables["new"]!!, mapOf<String, Properties>(
                "anX" to Properties("anX", TypeName("int"), ARGUMENT, 0),
                "aY" to Properties("aY", TypeName("int"), ARGUMENT, 1),
                "ok" to Properties("ok", TypeName("boolean"), VAR, 0),
                "nok" to Properties("nok", TypeName("boolean"), VAR, 1),
                "i" to Properties("i", TypeName("int"), VAR, 2)
        ))

        assertEquals(table.subroutineTables["distance"]!!, mapOf<String, Properties>(
                "this" to Properties("this", TypeName("Square"), ARGUMENT, 0),
                "other" to Properties("other", TypeName("Square"), ARGUMENT, 1)
        ))
    }

    @Test fun getRecordTest() {
        val source = """
            class Square { 
                field int x, y; 
                static String greeting;
                static String prefix, suffix;
                 
                 constructor Square new(int anX, int aY) {
                    var boolean ok;
                 }
                 
                 method void distance(Square other) {
                 
                 }
            }
        """.trimIndent()

        val node = parse(source).getOrThrow()

        val table  = createSymbolTable(node)

        assertEquals(table.get("ok", "new"), Properties("ok", TypeName("boolean"), VAR, 0))
        assertEquals(table.get("other", "distance"), Properties("other", TypeName("Square"), ARGUMENT, 1))
        assertEquals(table.get("x"), Properties("x", TypeName("int"), FIELD, 0))
        assertNull(table.get("ok", "distance"))
        assertNull(table.get("ok"))
        assertNull(table.get("something"))

    }

}