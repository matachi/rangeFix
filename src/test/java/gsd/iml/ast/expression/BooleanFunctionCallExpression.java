/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class BooleanFunctionCallExpression extends UnaryFunctionCallExpression {
    public BooleanFunctionCallExpression(Expression argument) {
        super("bool", argument) ;
    }
}
