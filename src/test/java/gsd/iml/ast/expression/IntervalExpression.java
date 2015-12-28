/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class IntervalExpression extends Expression {

    private Expression from ;
    private Expression to ;

    public IntervalExpression(Expression from, Expression to) {
        super("interval") ;
        this.from = from;
        this.to = to;
    }

    public Expression getFrom() {
        return from;
    }

    public Expression getTo() {
        return to;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getFrom().hashCode() + getTo().hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true ;
        
        if (! (obj instanceof IntervalExpression))
            return false ;
        
        IntervalExpression range = (IntervalExpression) obj ;
        return range.getFrom().equals(getFrom()) &&
               range.getTo().equals(getTo()) ;
    }

    @Override
    public String toString() {
        return getFrom() + " to " + getTo() ;
    }


    
}
