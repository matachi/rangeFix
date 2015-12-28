/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public abstract class BinaryExpression extends Expression {

    private Expression left ;
    private Expression right ;

    public BinaryExpression(String name, Expression left, Expression right) {
        super(name) ;
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getLeft().hashCode() + getRight().hashCode() ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true ;

        if (! (obj instanceof BinaryExpression))
            return false ;

        BinaryExpression e = (BinaryExpression) obj ;
        return (e.getName().equals(getName()) &&
                e.getLeft().equals(getLeft()) &&
                e.getRight().equals(getRight())) ;
    }
    
    @Override
    public String toString() {
        return getLeft() + " " + getName() + " " + getRight() ;
    }
}
