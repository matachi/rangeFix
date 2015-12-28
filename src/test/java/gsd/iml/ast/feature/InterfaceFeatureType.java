/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.feature;

/**
 *
 * @author leonardo
 */
public class InterfaceFeatureType extends FeatureType {

    private static InterfaceFeatureType singleton = new InterfaceFeatureType() ;

    private InterfaceFeatureType() {
        super("interface") ;
    }

    public static InterfaceFeatureType getInstance() {
        return singleton ;
    }
}
