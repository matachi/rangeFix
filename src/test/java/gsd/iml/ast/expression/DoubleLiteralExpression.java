/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class DoubleLiteralExpression extends AtomicExpression  {

    public DoubleLiteralExpression(Double value) {
        super("double-literal", value) ;
    }

    public DoubleLiteralExpression(double value) {
        super("double-literal", new Double(value)) ;
    }

    public Double get() {
        return (Double) getValue() ;
    }
}
