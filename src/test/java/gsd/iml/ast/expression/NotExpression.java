/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class NotExpression extends UnaryExpression {

    public NotExpression(Expression expression) {
        super("!", expression) ;
    }

}
