package gsd.iml.ast.flavor;

import gsd.iml.ast.ImlAstObject;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author leonardo
 */
abstract public class Flavor extends ImlAstObject {

    public Flavor(String name) {
        super(name) ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true ;

        if (! (obj instanceof Flavor))
            return false ;

        Flavor f = (Flavor) obj ;
        return f.getName().equals(getName()) ;
    }

    @Override
    public int hashCode() {
        return getName().hashCode() ;
    }
    
    @Override
    public String toString() {
        return getName() ;
    }
}
