/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class ConditionalExpression extends Expression {

    private Expression condition ;
    private Expression pass ;
    private Expression fail ;

    public ConditionalExpression(Expression condition, Expression pass, Expression fail) {
        super("conditional-expression") ;
        this.condition = condition ;
        this.pass = pass ;
        this.fail = fail ;
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getPass() {
        return pass;
    }  

    public Expression getFail() {
        return fail;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true ;

        if (! (obj instanceof ConditionalExpression))
            return false ;

        ConditionalExpression c = (ConditionalExpression) obj ;
        return c.getCondition().equals(getCondition()) &&
               c.getPass().equals(getPass()) &&
               c.getFail().equals(getFail()) ;
    }

    @Override
    public int hashCode() {
        return getCondition().hashCode() +
               getPass().hashCode() +
               getFail().hashCode() ;
    }

    @Override
    public String toString() {
       return getCondition() + " ? " + getPass() + " : " + getFail() ;
    }
}
