/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast;

/**
 *
 * @author leonardo
 */
public abstract class ImlAstObject {
    private String name ;

    public ImlAstObject(String name) {
        this.name = name ;
    }

    public String getName() {
        return name ;
    }

    @Override
    public abstract boolean equals(Object obj) ;

    @Override
    public abstract int hashCode() ;

    @Override
    public abstract String toString() ;
 }
