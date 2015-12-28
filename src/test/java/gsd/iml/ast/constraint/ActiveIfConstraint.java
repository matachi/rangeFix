/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.constraint;

import gsd.iml.ast.expression.Expression;

/**
 *
 * @author leonardo
 */
public class ActiveIfConstraint extends UnaryImlConstraint {

    public ActiveIfConstraint(Expression expression) {
        super("active_if", expression) ;
    }
}
