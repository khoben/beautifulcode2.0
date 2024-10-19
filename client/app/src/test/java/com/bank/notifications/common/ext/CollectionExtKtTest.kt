package com.bank.notifications.common.ext

import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionReplaceIfTest {

    @Test
    fun `test replaceIf with true predicate`() {
        val collection = listOf("a", "b", "c")
        val result = collection.replaceFirstIf({ it == "b" }) { "replaced" }

        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals("replaced", result[1])
        assertEquals("c", result[2])
    }

    @Test
    fun `test replaceIf with false predicate`() {
        val collection = listOf("a", "b", "c")
        val result = collection.replaceFirstIf({ it == "d" }) { "replaced" }

        assertEquals(3, result.size)
        assertEquals("a", result[0])
        assertEquals("b", result[1])
        assertEquals("c", result[2])
    }

    @Test
    fun `test replaceIf with empty collection`() {
        val collection = emptyList<String>()
        val result = collection.replaceFirstIf({ true }) { "replaced" }

        assertEquals(0, result.size)
    }
}

