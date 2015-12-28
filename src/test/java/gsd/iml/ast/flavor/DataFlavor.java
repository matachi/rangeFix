/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.flavor;

/**
 *
 * @author leonardo
 */
public class DataFlavor extends Flavor {
    private static DataFlavor singleton = new DataFlavor() ;
    
    private DataFlavor() {
        super("flavor data") ;
    }

    public static DataFlavor getInstance() {
        return singleton ;
    }
}
