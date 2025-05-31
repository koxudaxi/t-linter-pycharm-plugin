package com.koxudaxi.tlinter

import com.intellij.lang.Language
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.xml.XMLLanguage
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLanguageInjectionHost
import com.jetbrains.python.PythonLanguage
import com.jetbrains.python.ast.PyAstStringElement
import com.jetbrains.python.codeInsight.PyInjectionUtil
import com.jetbrains.python.codeInsight.PyInjectorBase
import com.jetbrains.python.psi.*
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext

// Supported languages mapping
// Note: Languages other than HTML/XML require corresponding plugins to be installed
// (e.g., Database Tools for SQL, Markdown plugin for Markdown, etc.)
private val LANGUAGES = mapOf(
    "html" to HTMLLanguage.INSTANCE,
    "xml" to XMLLanguage.INSTANCE,
    "sql" to Language.findLanguageByID("SQL"),
    "json" to Language.findLanguageByID("JSON"),
    "yaml" to Language.findLanguageByID("yaml"),
    "javascript" to Language.findLanguageByID("JavaScript"),
    "js" to Language.findLanguageByID("JavaScript"),
    "typescript" to Language.findLanguageByID("TypeScript"),
    "ts" to Language.findLanguageByID("TypeScript"),
    "css" to Language.findLanguageByID("CSS"),
    "markdown" to Language.findLanguageByID("Markdown"),
    "md" to Language.findLanguageByID("Markdown"),
    "python" to PythonLanguage.INSTANCE,
    "py" to PythonLanguage.INSTANCE,
    "java" to Language.findLanguageByID("JAVA"),
    "kotlin" to Language.findLanguageByID("kotlin"),
    "kt" to Language.findLanguageByID("kotlin"),
).filterValues { it != null }.mapValues { it.value!! }

@Suppress("UnstableApiUsage")
class TemplateStringsInjector : PyInjectorBase() {

    override fun registerInjection(
        registrar: MultiHostRegistrar,
        context: PsiElement,
    ): PyInjectionUtil.InjectionResult {
        val result = super.registerInjection(registrar, context)
        return if (result === PyInjectionUtil.InjectionResult.EMPTY &&
            context is PsiLanguageInjectionHost &&
            context.getContainingFile() is PyFile &&
            context is StringLiteralExpression &&
            (context.firstChild as? PyAstStringElement)?.isTemplate == true
        ) {
            return registerPyElementInjection(registrar, context)
        }
        else result
    }

    override fun getInjectedLanguage(context: PsiElement): Language? {
        return null
    }

    private fun injectLanguage(
        registrar: MultiHostRegistrar,
        host: PyStringLiteralExpression,
        language: Language
    ): PyInjectionUtil.InjectionResult {
        registrar.startInjecting(language)
        
        // Process each fragment of the template string
        // decodedFragments splits the string at template expression boundaries
        // For t"Hello {name}!", it would be: ["Hello ", "{name}", "!"]
        host.decodedFragments.forEach { fragment ->
            val textRange = fragment.first
            val content = fragment.second
            
            // Skip template expressions - they start with '{'
            // This preserves Python syntax highlighting for expressions
            if (!content.startsWith("{")) {
                // Inject language highlighting for non-expression content
                registrar.addPlace("", "", host, textRange)
            }
        }
        
        try {
            registrar.doneInjecting()
        }
        catch (e: Exception) {
            return PyInjectionUtil.InjectionResult.EMPTY
        }
        return PyInjectionUtil.InjectionResult(true, true)
    }

    private fun getLanguageFromAnnotation(annotation: PyExpression?): String? {
        if (annotation == null) return null
        
        // Handle Annotated[Template, "html"] pattern
        if (annotation is PySubscriptionExpression) {
            val operand = annotation.operand as? PyReferenceExpression ?: return null
            if (!operand.isAnnotated) return null

            val indexExpression = annotation.indexExpression
            
            // Handle tuple arguments
            if (indexExpression is PyTupleExpression) {
                val args = indexExpression.elements
                if (args.size >= 2) {
                    val firstArg = args[0] as? PyReferenceExpression ?: return null
                    val pyQualifiedNameOwner = firstArg.reference.resolve() as? PyQualifiedNameOwner ?: return null
                    if (pyQualifiedNameOwner.qualifiedName != TEMPLATE_QUALIFIED_NAME) return null
                    val secondArg = args[1] as? PyStringLiteralExpression ?: return null
                    return secondArg.stringValue
                }
            }
            
            // Handle single argument (might be a type alias)
            if (indexExpression is PyReferenceExpression) {
                val resolved = indexExpression.reference.resolve()
                if (resolved is PyTargetExpression) {
                    return getLanguageFromTypeAlias(resolved, null)
                }
            }
        }
        
        // Handle direct type alias reference
        if (annotation is PyReferenceExpression) {
            val resolved = annotation.reference.resolve()
            if (resolved is PyTargetExpression) {
                return getLanguageFromTypeAlias(resolved, null)
            }
        }
        
        return null
    }

    private fun getLanguageFromTypeAlias(targetExpression: PyTargetExpression, context: TypeEvalContext?): String? {
        // Check if it's a type alias (type html = Annotated[Template, "html"])
        val assignedValue = targetExpression.findAssignedValue() as? PySubscriptionExpression ?: return null
        
        // Check if it's Annotated[...]
        val operand = assignedValue.operand as? PyReferenceExpression ?: return null
        if (!operand.isAnnotated) return null

        // Get the arguments
        val indexExpression = assignedValue.indexExpression as? PyTupleExpression ?: return null
        val args = indexExpression.elements
        if (args.size < 2) return null
        
        // Check if first argument is Template
        val firstArg = args[0] as? PyReferenceExpression ?: return null
        if (!firstArg.isTemplate) return null

        // Get the language from second argument
        val secondArg = args[1] as? PyStringLiteralExpression ?: return null
        return secondArg.stringValue
    }

    private fun registerPyElementInjection(
        registrar: MultiHostRegistrar,
        host: PsiLanguageInjectionHost,
    ): PyInjectionUtil.InjectionResult {
        if (host !is PyStringLiteralExpression) return PyInjectionUtil.InjectionResult.EMPTY
        val context = TypeEvalContext.codeAnalysis(host.project, host.containingFile)
        
        // Check assignment type (e.g., content: Annotated[Template, "html"] = t"...")
        val assignment = host.parent as? PyAssignmentStatement
        if (assignment != null) {
            val target = assignment.targets.firstOrNull() as? PyTargetExpression
            if (target != null) {
                // Check annotation directly
                val annotation = target.annotation?.value
                val language = getLanguageFromAnnotation(annotation)
                if (language != null) {
                    LANGUAGES[language]?.let { lang ->
                        return injectLanguage(registrar, host, lang)
                    }
                }
            }
        }
        
        // Check function parameter type inference
        val pyCallExpression = (host.parent as? PyArgumentList)?.callExpression
        if (pyCallExpression != null) {
            val mappingResults = pyCallExpression.multiMapArguments(PyResolveContext.defaultContext(context))
            
            for (mappingResult in mappingResults) {
                val mappedParams = mappingResult.mappedParameters
                for ((arg, param) in mappedParams) {
                    if (arg == host && param != null) {
                        // Check parameter annotation
                        val paramAnnotation = (param as? PyNamedParameter)?.annotation?.value
                        val language = getLanguageFromAnnotation(paramAnnotation)
                        if (language != null) {
                            LANGUAGES[language]?.let { lang ->
                                return injectLanguage(registrar, host, lang)
                            }
                        }
                    }
                }
            }
        }
        
        return PyInjectionUtil.InjectionResult.EMPTY
    }
}