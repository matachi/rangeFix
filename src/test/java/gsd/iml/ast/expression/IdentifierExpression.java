/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IdentifierExpression extends AtomicExpression {

    public IdentifierExpression(String identifier) {
        super("identifier", identifier) ;
    }

    public String getId() {
        return (String) getValue();
    }
}
