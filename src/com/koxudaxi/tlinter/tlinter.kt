package com.koxudaxi.tlinter

import com.intellij.psi.util.QualifiedName
import com.jetbrains.python.psi.PyQualifiedNameOwner
import com.jetbrains.python.psi.PyReferenceExpression
import kotlin.io.resolve

const val TEMPLATE_QUALIFIED_NAME = "string.templatelib.Template"
const val ANNOTATED_QUALIFIED_NAME = "typing.Annotated"


val PyReferenceExpression.isTemplate: Boolean
    get() = (this.reference.resolve() as? PyQualifiedNameOwner)?.qualifiedName == TEMPLATE_QUALIFIED_NAME

val PyReferenceExpression.isAnnotated: Boolean
    get() = (this.reference.resolve() as? PyQualifiedNameOwner)?.qualifiedName == ANNOTATED_QUALIFIED_NAME