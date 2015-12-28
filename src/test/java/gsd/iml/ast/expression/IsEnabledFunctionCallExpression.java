/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IsEnabledFunctionCallExpression extends UnaryFunctionCallExpression {

    public IsEnabledFunctionCallExpression(Expression expression) {
        super("is_enabled", expression) ;
    }

}
