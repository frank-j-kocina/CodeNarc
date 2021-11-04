/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule.formatting

import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.AbstractAstVisitor

/**
 * Collections must be formatted according to Gravie conventions
 *
 * @author Frank J Kocina
 */
class CollectionIndentationRule extends AbstractAstVisitorRule {

    String name = 'CollectionIndentation'
    int priority = 2
    Class astVisitorClass = CollectionIndentationAstVisitor
}

class CollectionIndentationAstVisitor extends AbstractAstVisitor {

    @Override
    void visitListExpression(ListExpression listExpression) {
        // collections without any expressions are always ok
        if (!listExpression.expressions) {
            return
        }

        // single-line collections are always ok
        if (listExpression.lineNumber == listExpression.lastLineNumber) {
            return
        }

        // the first collection expression on each line should have the same indentation
        int firstExpressionColumnNumber = listExpression.expressions.first().columnNumber
        int currentExpressionLineNumber = listExpression.expressions.first().lineNumber
        listExpression.expressions.each { Expression expression ->
            if (expression.lineNumber == currentExpressionLineNumber) {
                return
            }
            currentExpressionLineNumber = expression.lineNumber
            if (expression.columnNumber != firstExpressionColumnNumber) {
                addViolation(expression, "The collection entry `${expression.text}` is at the incorrect indentation level: Expected column ${firstExpressionColumnNumber} but was ${expression.columnNumber}")
            }
        }
    }
}
