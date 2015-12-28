package gsd.iml.ast.feature;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author leonardo
 */
public class ComponentFeatureType extends FeatureType {

    private static ComponentFeatureType singleton = new ComponentFeatureType() ;

    private ComponentFeatureType() {
        super("component") ;
    }

    public static ComponentFeatureType getInstance() {
        return singleton ;
    }
}
