/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class StringLiteralExpression extends AtomicExpression {

    public StringLiteralExpression(String value) {
        super("string-literal", value) ;
    }

    public String get() {
        return (String) getValue() ;
    }

    @Override
    public String toString() {
        return getName() + ":\"" + get() + "\"" ;
    }
}
