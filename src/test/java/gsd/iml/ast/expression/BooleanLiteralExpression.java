/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class BooleanLiteralExpression extends AtomicExpression {

    public BooleanLiteralExpression(Boolean value) {
        super("boolean-literal", value) ;
    }
    
    public BooleanLiteralExpression(boolean value) {
        super("boolean-literal", new Boolean(value)) ;
    }

    public Boolean get() {
        return (Boolean) getValue() ;
    }
}
