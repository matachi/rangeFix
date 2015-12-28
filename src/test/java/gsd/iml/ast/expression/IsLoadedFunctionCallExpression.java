/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IsLoadedFunctionCallExpression extends UnaryFunctionCallExpression {

    public IsLoadedFunctionCallExpression(Expression  argument) {
        super("is_loaded", argument) ;
    }

}
