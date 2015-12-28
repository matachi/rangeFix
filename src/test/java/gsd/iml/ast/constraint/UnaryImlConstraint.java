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
public class UnaryImlConstraint extends ImlConstraint {
    private Expression expression ;

    public UnaryImlConstraint(String name, Expression expression) {
        super(name, Arrays.asList(expression)) ;
        this.expression = expression ;
    }

    public Expression getExpression() {
        return expression ;
    }
}
