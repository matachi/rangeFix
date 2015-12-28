/*
 * Copyright (c) 2010, Xiong Yingfei, University of Waterloo
 * All rights reserved.
 */
package ca.uwaterloo.gsd.rangeFix;


public class Token {

	private int _type;
	private String _text;
	private Location _loc;
	
	public Token(int type, String text, Location loc) {
		super();
		_type = type;
		_text = text;
		_loc = loc;
	}
	public Location getLoc() {
		return _loc;
	}
	public int getType() {
		return _type;
	}
	public String getText() {
		return _text;
	}
	
	public String toString() {
		return _loc.toString() + "-" + _type + "-\"" + _text + "\"";
	}
}
