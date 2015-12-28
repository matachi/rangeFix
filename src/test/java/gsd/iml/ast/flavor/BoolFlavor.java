/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.flavor;

/**
 *
 * @author leonardo
 */
public class BoolFlavor extends Flavor {
    private static BoolFlavor singleton = new BoolFlavor() ;

    private BoolFlavor() {
        super("flavor bool") ;
    }

    public static BoolFlavor getInstance() {
        return singleton ;
    }
}
