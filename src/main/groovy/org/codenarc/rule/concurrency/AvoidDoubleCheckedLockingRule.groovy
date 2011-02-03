/*
 * Copyright 2011 the original author or authors.
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
package org.codenarc.rule.concurrency

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.expr.BooleanExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.NotExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.SynchronizedStatement
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.util.AstUtil

/**
 * This rule detects double checked locking, where a 'lock hint' is tested for null before initializing an object within a synchronized block. Double checked locking does not guarantee correctness and is an anti-pattern. 
 *
 * @author Hamlet D'Arcy
 * @version $Revision: 24 $ - $Date: 2009-01-31 13:47:09 +0100 (Sat, 31 Jan 2009) $
 */
class AvoidDoubleCheckedLockingRule extends AbstractAstVisitorRule {
    String name = 'AvoidDoubleCheckedLocking'
    int priority = 2
    Class astVisitorClass = AvoidDoubleCheckedLockingAstVisitor
}

class AvoidDoubleCheckedLockingAstVisitor extends AbstractAstVisitor {
    @Override
    void visitIfElse(IfStatement node) {

        addViolationOnDoubleLocking(node)
        super.visitIfElse(node)
    }

    private def addViolationOnDoubleLocking(IfStatement node) {
        if (!expressionIsNullCheck(node)) {
            return
        }
        SynchronizedStatement syncStatement = getSynchronizedStatement(node.ifBlock)
        if (!syncStatement) {
            return
        }
        if (!isOneLiner(syncStatement.code)) {
            return
        }
        def synchContents = syncStatement.code.statements[0]
        if (expressionIsNullCheck(synchContents)) {
            def varName1 = getNullCheckVariableName(node.booleanExpression)
            def varName2 = getNullCheckVariableName(synchContents.booleanExpression)
            if (varName1 == varName2) {
                if (isOneLiner(synchContents.ifBlock) && expressionIsAssignment(synchContents.ifBlock.statements[0], varName2)) {
                    addViolation(synchContents.ifBlock.statements[0], "Double checked locking detected for variable ${varName1}. replace with more robust lazy initialization")
                }
            }
        }
    }

    private static boolean isOneLiner(statement) {
        if (statement instanceof BlockStatement) {
            if (statement.statements?.size() == 1) {
                return true
            }
        }
        false
    }

    private static SynchronizedStatement getSynchronizedStatement(ASTNode statement) {
        if (statement instanceof BlockStatement && statement.statements?.size() == 1) {
            if (statement.statements[0] instanceof SynchronizedStatement) {
                return statement.statements[0]
            }
        }
        null
    }

    private static boolean expressionIsNullCheck(ASTNode node) {
        if (!(node instanceof IfStatement)) {
            return false
        }
        if (!(node.booleanExpression instanceof BooleanExpression)) {
            return false
        }
        def booleanExp = node.booleanExpression
        if (AstUtil.isBinaryExpressionType(booleanExp.expression, '==')) {
            if (AstUtil.isNull(booleanExp.expression.leftExpression) && booleanExp.expression.rightExpression instanceof VariableExpression) {
                return true
            } else if (AstUtil.isNull(booleanExp.expression.rightExpression) && booleanExp.expression.leftExpression instanceof VariableExpression) {
                return true
            }
        } else if (booleanExp.expression instanceof NotExpression && booleanExp.expression.expression instanceof VariableExpression) {
            return true
        }
        false
    }

    private static String getNullCheckVariableName(ASTNode node) {
        if (!(node instanceof BooleanExpression)) {
            return null
        }
        if (AstUtil.isBinaryExpressionType(node.expression, '==')) {
            if (AstUtil.isNull(node.expression.leftExpression) && node.expression.rightExpression instanceof VariableExpression) {
                return node.expression.rightExpression.variable
            } else if (AstUtil.isNull(node.expression.rightExpression) && node.expression.leftExpression instanceof VariableExpression) {
                return node.expression.leftExpression.variable
            }
        } else if (node.expression instanceof NotExpression && node.expression.expression instanceof VariableExpression) {
            return node.expression.expression.variable
        }
        null
    }

    private static boolean expressionIsAssignment(ASTNode node, String variableName) {
        if (node instanceof Expression && AstUtil.isBinaryExpressionType(node, '=')) {
            if (AstUtil.isVariable(node.leftExpression, variableName)) {
                return true
            }
        } else if (node instanceof ExpressionStatement && AstUtil.isBinaryExpressionType(node.expression, '=')) {
            if (AstUtil.isVariable(node.expression.leftExpression, variableName)) {
                return true
            }
        }
        false
    }
}
