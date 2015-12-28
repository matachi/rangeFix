/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gsd.iml.ast.feature;

/**
 *
 * @author leonardo
 */
public class PackageFeatureType extends FeatureType {

    private static PackageFeatureType singleton = new PackageFeatureType() ;

    private PackageFeatureType() {
        super("package") ;
    }

    public static PackageFeatureType getInstance() {
        return singleton ;
    }
}
