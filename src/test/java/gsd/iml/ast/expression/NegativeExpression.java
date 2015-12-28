/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class NegativeExpression extends UnaryExpression {

    public NegativeExpression(Expression exp) {
        super("negative", exp) ;
    }

}
