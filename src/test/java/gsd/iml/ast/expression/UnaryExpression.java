/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public class UnaryExpression extends Expression {

    private Expression expression ;

    public UnaryExpression(String name, Expression expression) {
        super(name) ;
        this.expression = expression ;
    }

    public Expression getExpression() {
        return expression ;
    }

    @Override
    public int hashCode() {
        return  getName().hashCode() + expression.hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true ;

        if (! (obj instanceof UnaryExpression))
            return false ;

        UnaryExpression exp = (UnaryExpression) obj ;
        return exp.getName().equals(getName()) &&
               exp.getExpression().equals(getExpression()) ;
    }

    @Override
    public String toString() {
        return getName() + getExpression() ;
    }
}
