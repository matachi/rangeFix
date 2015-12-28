/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class BitwiseXorExpression extends BinaryExpression {

    public BitwiseXorExpression(Expression left, Expression right) {
        super("^", left, right) ;
    }
}
