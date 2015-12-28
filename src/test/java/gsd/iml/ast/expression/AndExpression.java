/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class AndExpression extends BinaryExpression {

    public AndExpression(Expression left, Expression right) {
        super("&&", left, right) ;
    }

}
