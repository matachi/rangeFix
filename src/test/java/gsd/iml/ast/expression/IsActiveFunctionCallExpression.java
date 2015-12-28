/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IsActiveFunctionCallExpression extends UnaryFunctionCallExpression {
    public IsActiveFunctionCallExpression(Expression argument) {
        super("is_active", argument) ;
    }
}
