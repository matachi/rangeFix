/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class GreaterThanEqualExpression extends BinaryExpression {

    public GreaterThanEqualExpression(Expression left, Expression right) {
        super(">=", left, right) ;
    }
}
