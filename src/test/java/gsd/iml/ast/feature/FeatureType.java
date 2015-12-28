/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.feature;

import gsd.iml.ast.ImlAstObject;

/**
 *
 * @author leonardo
 */
public abstract class FeatureType extends ImlAstObject {

    public FeatureType(String name) {
        super(name) ;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true ;

        if (! (obj instanceof FeatureType))
            return false ;

        FeatureType type = (FeatureType) obj ;
        return type.getName().equals(getName()) ;
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
