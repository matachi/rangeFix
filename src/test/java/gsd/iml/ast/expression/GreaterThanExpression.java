/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class GreaterThanExpression extends BinaryExpression {

    public GreaterThanExpression(Expression left, Expression right) {
        super(">", left, right) ;
    }
}

