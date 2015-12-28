/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.constraint;

import gsd.iml.ast.expression.Expression;
import java.util.List;

/**
 *
 * @author leonardo
 */
public class LegalValuesConstraint extends ImlConstraint {

    public LegalValuesConstraint(List<Expression> expressions) {
        super("legal_values", expressions) ;
    }
}
