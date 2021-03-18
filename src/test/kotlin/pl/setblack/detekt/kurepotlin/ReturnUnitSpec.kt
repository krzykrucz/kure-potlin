package pl.setblack.detekt.kurepotlin

import io.gitlab.arturbosch.detekt.api.Finding
import io.gitlab.arturbosch.detekt.rules.setupKotlinEnvironment
import io.gitlab.arturbosch.detekt.test.lintWithContext
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import pl.setblack.detekt.kurepotlin.rules.ReturnUnit

class ReturnUnitSpec : Spek({
    setupKotlinEnvironment()
    val env: KotlinCoreEnvironment by memoized()

    describe("a rule") {

        val subject by memoized { ReturnUnit() }

        it("find returns of Unit") {
            val messages = subject.lintWithContext(env, impureCode).map(Finding::message)
            assertThat(messages).containsExactly(
                "Function impureUnitExplicit in the file Test.kt returns Unit.",
                "Function impureUnitImplicit in the file Test.kt returns Unit.",
                "Function impureUnitExpression in the file Test.kt returns Unit.",
            )
        }
    }
})

private const val impureCode: String =
    """
        fun impureUnitExplicit(): Unit { }
        
        fun impureUnitImplicit() { }
        
        fun impureUnitExpression() = Unit

        fun pureString() = "pure"
    """