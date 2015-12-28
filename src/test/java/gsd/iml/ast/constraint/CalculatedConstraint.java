/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.constraint;

import gsd.iml.ast.expression.Expression;
import java.util.Arrays;

/**
 *
 * @author leonardo
 */
public class CalculatedConstraint extends UnaryImlConstraint {

    public CalculatedConstraint(Expression expression) {
        super("calculated", expression);
    }
}
