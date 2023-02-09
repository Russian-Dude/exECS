package com.rdude.exECS

import com.rdude.exECS.utils.reflection.After
import com.rdude.exECS.utils.reflection.AutoRegistrar
import com.rdude.exECS.utils.reflection.Before
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.reflect.full.isSubclassOf

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AutoRegistrarTest {

    interface TestElement {
        val id: Char
    }

    interface UnusedElement

    class ElementA : TestElement {
        override val id: Char = 'A'
    }

    @Before(ElementA::class)
    private object ElementB : TestElement {
        override val id: Char = 'B'
    }

    class ElementC : TestElement {
        override val id: Char = 'C'
    }

    @Before(ElementA::class)
    @After(ElementE::class, ElementE.ElementG::class)
    class ElementD : TestElement {
        override val id: Char = 'D'
    }

    @Before(ElementB::class)
    private class ElementE : TestElement {

        override val id: Char = 'E'

        @Before(ElementA::class)
        @After(ElementB::class)
        object ElementG : TestElement {
            override val id: Char = 'G'
        }
    }

    class ElementF : TestElement {

        override val id: Char = 'F'

        private inner class ElementH : TestElement {

            override val id: Char = 'H'

            private inner class ElementG : TestElement {
                override val id: Char = 'J'
            }
        }
    }

    class ElementY : TestElement, UnusedElement {
        override val id: Char = 'Y'
    }


    private val result: MutableList<Char> = ArrayList(8)


    @BeforeAll
    fun init() {
        val registrar = AutoRegistrar<TestElement, MutableList<Char>> { element, list ->
            list.add(element.id)
        }
        registrar.filter = { !it.isSubclassOf(UnusedElement::class) }
        registrar.register(result)
    }


    @Test
    fun `required elements registered`() {
        val required = listOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H')
        assert(result.containsAll(required)) {
            val notRegistered = required.filter { !result.contains(it) }
            "Not all of the required elements have been registered. Elements that have not been registered: $notRegistered"
        }
    }

    @Test
    fun `order test 1`() {
        assert(result.indexOf('B') < result.indexOf('A')) {
            "Element B must be registered before Element A, because Element B is annotated with @Before(ElementA)"
        }
    }

    @Test
    fun `order test 2`() {
        assert(result.indexOf('D') < result.indexOf('A')) {
            "Element D must be registered before Element A, because Element D is annotated with @Before(ElementA)"
        }
    }

    @Test
    fun `order test 3`() {
        assert(result.indexOf('D') > result.indexOf('E')) {
            "Element D must be registered after Element E, because Element D is annotated with @After(ElementE)"
        }
    }

    @Test
    fun `order test 4`() {
        assert(result.indexOf('D') > result.indexOf('G')) {
            "Element D must be registered after Element G, because Element D is annotated with @After(ElementG)"
        }
    }

    @Test
    fun `order test 5`() {
        assert(result.indexOf('E') < result.indexOf('B')) {
            "Element E must be registered before Element B, because Element E is annotated with @Before(ElementB)"
        }
    }

    @Test
    fun `order test 6`() {
        assert(result.indexOf('E') < result.indexOf('A')) {
            "Element E must be registered before Element A, because Element E is annotated with @Before(ElementB) " +
                    "and Element B is annotated with @Before(ElementA)"
        }
    }

    @Test
    fun `order test 7`() {
        assert(result.indexOf('G') > result.indexOf('B')) {
            "Element G must be registered after Element B, because Element G is annotated with @After(ElementB)"
        }
    }

    @Test
    fun `order test 8`() {
        assert(result.indexOf('G') < result.indexOf('A')) {
            "Element G must be registered before Element A, because Element G is annotated with @Before(ElementA)"
        }
    }

}