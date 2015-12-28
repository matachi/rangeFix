/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.expression;

/**
 *
 * @author leonardo
 */
public abstract class AtomicExpression extends Expression {
    private Object value ;

    public AtomicExpression(String name, Object value) {
        super(name) ;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true ;

        if (! (obj instanceof AtomicExpression))
            return false ;

        AtomicExpression e = (AtomicExpression) obj ;
        return (e.getName().equals(getName()) && e.getValue().equals(getValue())) ;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() + getValue().hashCode() ;
    }

    @Override
    public String toString() {
        return getName() + ":" + value.toString() ;
    }
}
