/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.feature;

/**
 *
 * @author leonardo
 */
public class OptionFeatureType extends FeatureType {

    private static OptionFeatureType singleton = new OptionFeatureType() ;

    private OptionFeatureType() {
        super("option") ;
    }

    public static OptionFeatureType getInstance() {
        return singleton ;
    }
}
