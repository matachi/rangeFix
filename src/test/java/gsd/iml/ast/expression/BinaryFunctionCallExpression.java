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
public class BinaryFunctionCallExpression extends FunctionCallExpression {
    private Expression argument1 ;
    private Expression argument2 ;


    public BinaryFunctionCallExpression(String name, Expression argument1, Expression argument2) {
        super(name, Arrays.asList(argument1, argument2)) ;
        this.argument1 = argument1 ;
        this.argument2 = argument2 ;
    }

    public Expression getFirstArgument() {
        return argument1 ;
    }

    public Expression getSecondArgument() {
        return argument2 ;
    }
}
