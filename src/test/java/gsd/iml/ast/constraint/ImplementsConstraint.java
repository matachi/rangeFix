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
public class ImplementsConstraint extends UnaryImlConstraint {

    public ImplementsConstraint(Expression expression) {
        super("implements", expression) ;
    }
}
