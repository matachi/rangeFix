/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

import java.util.Arrays;

/**
 *
 * @author leonardo
 */
public class UnaryFunctionCallExpression extends FunctionCallExpression {

    private Expression argument ;

    public UnaryFunctionCallExpression(String name, Expression argument) {
        super(name, Arrays.asList(argument)) ;
        this.argument = argument ;
    }
    
    public Expression getArgument() {
        return argument ;
    }
}
