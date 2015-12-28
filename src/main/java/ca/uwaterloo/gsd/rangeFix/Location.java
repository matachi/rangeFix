/*
 * Copyright (c) 2011, Xiong Yingfei, University of Waterloo
 * All rights reserved.
 */
package ca.uwaterloo.gsd.rangeFix;

public class Location {
	/** The first, inclusive, position in the range. */
	private Position begin;
	public Position getBegin()  { return begin;}

	/** The first position beyond the range. */
	private Position end;
	public Position getEnd() {return end;}
	
	public Location(EccParser.Location l) {
		begin = l.begin;
		end = l.end;
	}

	public Location(EccFullParser.Location l) {
		begin = l.begin;
		end = l.end;
	}

	public Location(AnnotationParser.Location l) {
		begin = l.begin;
		end = l.end;
	}
	
	/**
	 * Create a <code>Location</code> denoting an empty range located at a given
	 * point.
	 * 
	 * @param loc
	 *            The position at which the range is anchored.
	 */
	public Location(Position loc) {
		this.begin = this.end = loc;
	}

	/**
	 * Create a <code>Location</code> from the endpoints of the range.
	 * 
	 * @param begin
	 *            The first position included in the range.
	 * @param end
	 *            The first position beyond the range.
	 */
	public Location(Position begin, Position end) {
		this.begin = begin;
		this.end = end;
	}

	/**
	 * Print a representation of the location. For this to be correct,
	 * <code>Position</code> should override the <code>equals</code> method.
	 */
	public String toString() {
		if (begin.equals(end))
			return begin.toString();
		else
			return begin.toString() + "-" + end.toString();
	}
	
	public EccParser.Location toParserLocation(EccParser p){
		return p.new Location(begin, end);
	}
}
