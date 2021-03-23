package pl.setblack.detekt.kurepotlin

import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.assertThat
import io.gitlab.arturbosch.detekt.test.lintWithContext
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import pl.setblack.detekt.kurepotlin.rules.FunctionSignatureNotImplemented

class FunctionSignatureNotImplementedSpec : Spek({
    setupKotlinEnvironment()
    val env: KotlinCoreEnvironment by memoized()

    describe("a rule") {

        val subject by memoized { FunctionSignatureNotImplemented() }

        it("find returns of Unit") {
            val messages = subject.lintWithContext(env, impureCode)
            assertThat(messages).hasSize(1)
        }
    }
})

private const val impureCode: String =
    """
        typealias ImplementedWithProperty = () -> String

        val propertyImplementation: ImplementedWithProperty = { "" }
        
        typealias ImplementedWithFunction = () -> String
        
        fun functionImplementation(): ImplementedWithFunction = { "" }        

        typealias ImplementedWithClass = () -> String

        class ClassImplementation : ImplementedWithClass {
            override fun invoke() = ""
        }

        typealias NotImplemented = () -> String

        val notImplementation: () -> String = { "" }
    """
