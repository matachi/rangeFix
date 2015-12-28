/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.flavor;

/**
 *
 * @author leonardo
 */
public class NoneFlavor extends Flavor {

    private static NoneFlavor singleton = new NoneFlavor() ;

    private NoneFlavor() {
        super("flavor none") ;
    }

    public static NoneFlavor getInstance() {
        return singleton ;
    }
}
