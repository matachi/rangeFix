/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class OrExpression extends BinaryExpression {

    public OrExpression(Expression left, Expression right) {
        super("||", left, right) ;
    }

}
