/*
 * Copyright (c) 2010, Xiong Yingfei, University of Waterloo
 * All rights reserved.
 */
package ca.uwaterloo.gsd.rangeFix;

public class Position {
	int _line;
	int _column;
	public Position(int line, int column) {
		super();
		_line = line;
		_column = column;
	}
	public int getLine() {
		return _line;
	}
	public int getColumn() {
		return _column;
	}
	@Override
	public String toString() {
		return new Integer(_line) + ":" + new Integer(_column);
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _column;
		result = prime * result + _line;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Position other = (Position) obj;
		if (_column != other._column)
			return false;
		if (_line != other._line)
			return false;
		return true;
	}
	
	
	
}
