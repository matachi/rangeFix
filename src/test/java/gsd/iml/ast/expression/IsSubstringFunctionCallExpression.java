/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IsSubstringFunctionCallExpression extends BinaryFunctionCallExpression {
    public IsSubstringFunctionCallExpression(Expression arg1, Expression arg2) {
        super("is_substr", arg1, arg2) ;
    }
}
