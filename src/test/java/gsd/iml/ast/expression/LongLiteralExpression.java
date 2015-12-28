/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class LongLiteralExpression extends AtomicExpression  {

    public LongLiteralExpression(Long value) {
        super("long-literal", value) ;
    }

    public LongLiteralExpression(long value) {
        super("long-literal", new Long(value)) ;
    }

    public Long get() {
        return (Long) getValue() ;
    }
}
