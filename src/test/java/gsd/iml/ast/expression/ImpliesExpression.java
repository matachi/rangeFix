/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class ImpliesExpression extends BinaryExpression {

    public ImpliesExpression(Expression left, Expression right) {
        super("implies", left, right) ;
    }

}
