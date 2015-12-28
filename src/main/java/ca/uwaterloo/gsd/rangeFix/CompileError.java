/*
 * Copyright (c) 2010, Xiong Yingfei, University of Waterloo
 * All rights reserved.
 */
package ca.uwaterloo.gsd.rangeFix;

public class CompileError {
    public Location loc;
    public String msg;
    public CompileError(Location l, String m) {
        loc = l; msg = m;
    }
	public String toString() {
		return loc.toString() + ": " + msg;
	}
}
