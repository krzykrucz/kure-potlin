package pl.setblack.detekt.kurepotlin.rules

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.DetektVisitor
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtFunctionType
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.resolve.BindingContext

/**
 * @requiresTypeResolution
 */
class FunctionSignatureNotImplemented(config: Config = Config.empty) : Rule(config) {

    override val issue: Issue =
        Issue(
            javaClass.simpleName,
            Severity.CodeSmell,
            "Function type should be implemented by a property, a function or a class",
            Debt.TWENTY_MINS
        )

    override fun visit(root: KtFile) {
        super.visit(root)

        val visitor = UnimplementedFunctionTypeVisitor(bindingContext)
        root.accept(visitor)

        visitor.getUnimplementedFunctionSignatures().forEach {
            report(
                CodeSmell(
                    issue,
                    Entity.from(it),
                    "Function type signature ${it.nameAsSafeName.identifier} is not implemented."
                )
            )
        }
    }

    private class UnimplementedFunctionTypeVisitor(private val bindingContext: BindingContext) : DetektVisitor() {

        private val functionTypeAliases = mutableSetOf<KtTypeAlias>()
        private val topLevelProperties = mutableSetOf<KtProperty>()
        private val topLevelFunctions = mutableSetOf<KtNamedFunction>()
        private val topLevelClasses = mutableSetOf<KtClass>()

        fun getUnimplementedFunctionSignatures(): List<KtNamedDeclaration> =
            functionTypeAliases
                .filter(KtTypeAlias::isTopLevel)
                .filterNot { it.isImplemented() }

        private fun KtTypeAlias.isImplemented(): Boolean =
            topLevelProperties.any { it.doesImplement(this) }
                || topLevelFunctions.any { it.doesImplement(this) }
                || topLevelClasses.any { it.doesImplement(this) }

        override fun visitTypeAlias(typeAlias: KtTypeAlias) {
            if (typeAlias.getTypeReference()?.typeElement is KtFunctionType)
                functionTypeAliases += typeAlias
            super.visitTypeAlias(typeAlias)
        }

        override fun visitClass(klass: KtClass) {
            if (klass.isTopLevel())
                topLevelClasses += klass
            super.visitClass(klass)
        }

        override fun visitProperty(property: KtProperty) {
            if (property.isTopLevel)
                topLevelProperties += property
            super.visitProperty(property)
        }

        override fun visitNamedFunction(function: KtNamedFunction) {
            if (function.isTopLevel)
                topLevelFunctions += function
            super.visitNamedFunction(function)
        }

        private fun KtClass.doesImplement(typeAlias: KtTypeAlias): Boolean =
            superTypeListEntries.any { superType ->
                superType.typeReference?.text == typeAlias.name
            }

        private fun KtNamedFunction.doesImplement(typeAlias: KtTypeAlias): Boolean =
            typeReference?.text == typeAlias.name

        private fun KtProperty.doesImplement(typeAlias: KtTypeAlias): Boolean =
            typeReference?.text == typeAlias.name

    }
}


