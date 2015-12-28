/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.flavor;

/**
 *
 * @author leonardo
 */
public class BoolDataFlavor extends Flavor  {
    private static BoolDataFlavor singleton = new BoolDataFlavor() ;

    private BoolDataFlavor() {
        super("flavor booldata") ;
    }

    public static BoolDataFlavor getInstance() {
        return singleton ;
    }
}
