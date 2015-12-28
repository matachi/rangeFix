
/* A Bison parser, made by GNU Bison 2.4.1.  */

/* Skeleton implementation for Bison LALR(1) parsers in Java
   
      Copyright (C) 2007, 2008 Free Software Foundation, Inc.
   
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.
   
   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

package ca.uwaterloo.gsd.rangeFix;
/* First part of user declarations.  */

/* "%code imports" blocks.  */

/* Line 33 of lalr1.java  */
/* Line 9 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */

  import java.io.*;
  import java.util.*;
  import scala.collection.JavaConversions;
  import scala.Option;
  import scala.Some;
  import scala.None$;



/* Line 33 of lalr1.java  */
/* Line 53 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\AnnotationParser.java"  */

/**
 * A Bison parser, automatically generated from <tt>C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class AnnotationParser
{
    /** Version number for the Bison executable that generated this parser.  */
  public static final String bisonVersion = "2.4.1";

  /** Name of the skeleton that generated this parser.  */
  public static final String bisonSkeleton = "lalr1.java";


  /** True if verbose error messages are enabled.  */
  public boolean errorVerbose = true;


  /**
   * A class defining a pair of positions.  Positions, defined by the
   * <code>Position</code> class, denote a point in the input.
   * Locations represent a part of the input through the beginning
   * and ending positions.  */
  public class Location {
    /** The first, inclusive, position in the range.  */
    public Position begin;

    /** The first position beyond the range.  */
    public Position end;

    /**
     * Create a <code>Location</code> denoting an empty range located at
     * a given point.
     * @param loc The position at which the range is anchored.  */
    public Location (Position loc) {
      this.begin = this.end = loc;
    }

    /**
     * Create a <code>Location</code> from the endpoints of the range.
     * @param begin The first position included in the range.
     * @param end   The first position beyond the range.  */
    public Location (Position begin, Position end) {
      this.begin = begin;
      this.end = end;
    }

    /**
     * Print a representation of the location.  For this to be correct,
     * <code>Position</code> should override the <code>equals</code>
     * method.  */
    public String toString () {
      if (begin.equals (end))
        return begin.toString ();
      else
        return begin.toString () + "-" + end.toString ();
    }
  }



  /** Token returned by the scanner to signal the end of its input.  */
  public static final int EOF = 0;

/* Tokens.  */
  /** Token number, to be returned by the scanner.  */
  public static final int DEFAULT = 258;
  /** Token number, to be returned by the scanner.  */
  public static final int REQUIRES = 259;
  /** Token number, to be returned by the scanner.  */
  public static final int ACTIVEIF = 260;
  /** Token number, to be returned by the scanner.  */
  public static final int LEGALVALUES = 261;
  /** Token number, to be returned by the scanner.  */
  public static final int CALCULATED = 262;
  /** Token number, to be returned by the scanner.  */
  public static final int NEQ = 263;
  /** Token number, to be returned by the scanner.  */
  public static final int GEQ = 264;
  /** Token number, to be returned by the scanner.  */
  public static final int LEQ = 265;
  /** Token number, to be returned by the scanner.  */
  public static final int TO = 266;
  /** Token number, to be returned by the scanner.  */
  public static final int IMPLIES = 267;
  /** Token number, to be returned by the scanner.  */
  public static final int GETDATA = 268;
  /** Token number, to be returned by the scanner.  */
  public static final int ISACTIVE = 269;
  /** Token number, to be returned by the scanner.  */
  public static final int ISENABLED = 270;
  /** Token number, to be returned by the scanner.  */
  public static final int ISLOADED = 271;
  /** Token number, to be returned by the scanner.  */
  public static final int ISSUBSTR = 272;
  /** Token number, to be returned by the scanner.  */
  public static final int ISXSUBSTR = 273;
  /** Token number, to be returned by the scanner.  */
  public static final int VERSIONCMP = 274;
  /** Token number, to be returned by the scanner.  */
  public static final int TOINT = 275;
  /** Token number, to be returned by the scanner.  */
  public static final int TOSTRING = 276;
  /** Token number, to be returned by the scanner.  */
  public static final int TOBOOL = 277;
  /** Token number, to be returned by the scanner.  */
  public static final int TYPE = 278;
  /** Token number, to be returned by the scanner.  */
  public static final int INTTYPE = 279;
  /** Token number, to be returned by the scanner.  */
  public static final int STRINGTYPE = 280;
  /** Token number, to be returned by the scanner.  */
  public static final int ENUMTYPE = 281;
  /** Token number, to be returned by the scanner.  */
  public static final int BOOLTYPE = 282;
  /** Token number, to be returned by the scanner.  */
  public static final int IN = 283;
  /** Token number, to be returned by the scanner.  */
  public static final int REPLACING = 284;
  /** Token number, to be returned by the scanner.  */
  public static final int ID = 285;
  /** Token number, to be returned by the scanner.  */
  public static final int STRING = 286;
  /** Token number, to be returned by the scanner.  */
  public static final int INT = 287;
  /** Token number, to be returned by the scanner.  */
  public static final int HEXINT = 288;
  /** Token number, to be returned by the scanner.  */
  public static final int REAL = 289;



  
  private Location yylloc (YYStack rhs, int n)
  {
    if (n > 0)
      return new Location (rhs.locationAt (1).begin, rhs.locationAt (n).end);
    else
      return new Location (rhs.locationAt (0).end);
  }

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>AnnotationParser</tt>.
   */
  public interface Lexer {
    /**
     * Method to retrieve the beginning position of the last scanned token.
     * @return the position at which the last scanned token starts.  */
    Position getStartPos ();

    /**
     * Method to retrieve the ending position of the last scanned token.
     * @return the first position beyond the last scanned token.  */
    Position getEndPos ();

    /**
     * Method to retrieve the semantic value of the last scanned token.
     * @return the semantic value of the last scanned token.  */
    Object getLVal ();

    /**
     * Entry point for the scanner.  Returns the token identifier corresponding
     * to the next token and prepares to return the semantic value
     * and beginning/ending positions of the token. 
     * @return the token identifier corresponding to the next token. */
    int yylex () throws java.io.IOException;

    /**
     * Entry point for error reporting.  Emits an error
     * referring to the given location in a user-defined way.
     *
     * @param loc The location of the element to which the
     *                error message is related
     * @param s The string for the error message.  */
     void yyerror (Location loc, String s);
  }

  /** The object doing lexical analysis for us.  */
  private Lexer yylexer;
  
  



  /**
   * Instantiates the Bison-generated parser.
   * @param yylexer The scanner that will supply tokens to the parser.
   */
  public AnnotationParser (Lexer yylexer) {
    this.yylexer = yylexer;
    
  }

  private java.io.PrintStream yyDebugStream = System.err;

  /**
   * Return the <tt>PrintStream</tt> on which the debugging output is
   * printed.
   */
  public final java.io.PrintStream getDebugStream () { return yyDebugStream; }

  /**
   * Set the <tt>PrintStream</tt> on which the debug output is printed.
   * @param s The stream that is used for debugging output.
   */
  public final void setDebugStream(java.io.PrintStream s) { yyDebugStream = s; }

  private int yydebug = 0;

  /**
   * Answer the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   */
  public final int getDebugLevel() { return yydebug; }

  /**
   * Set the verbosity of the debugging output; 0 means that all kinds of
   * output from the parser are suppressed.
   * @param level The verbosity level for debugging output.
   */
  public final void setDebugLevel(int level) { yydebug = level; }

  private final int yylex () throws java.io.IOException {
    return yylexer.yylex ();
  }
  protected final void yyerror (Location loc, String s) {
    yylexer.yyerror (loc, s);
  }

  
  protected final void yyerror (String s) {
    yylexer.yyerror ((Location)null, s);
  }
  protected final void yyerror (Position loc, String s) {
    yylexer.yyerror (new Location (loc), s);
  }

  protected final void yycdebug (String s) {
    if (yydebug > 0)
      yyDebugStream.println (s);
  }

  private final class YYStack {
    private int[] stateStack = new int[16];
    private Location[] locStack = new Location[16];
    private Object[] valueStack = new Object[16];

    public int size = 16;
    public int height = -1;
    
    public final void push (int state, Object value    	   	      	    , Location loc) {
      height++;
      if (size == height) 
        {
	  int[] newStateStack = new int[size * 2];
	  System.arraycopy (stateStack, 0, newStateStack, 0, height);
	  stateStack = newStateStack;
	  
	  Location[] newLocStack = new Location[size * 2];
	  System.arraycopy (locStack, 0, newLocStack, 0, height);
	  locStack = newLocStack;
	  
	  Object[] newValueStack = new Object[size * 2];
	  System.arraycopy (valueStack, 0, newValueStack, 0, height);
	  valueStack = newValueStack;

	  size *= 2;
	}

      stateStack[height] = state;
      locStack[height] = loc;
      valueStack[height] = value;
    }

    public final void pop () {
      height--;
    }

    public final void pop (int num) {
      // Avoid memory leaks... garbage collection is a white lie!
      if (num > 0) {
	java.util.Arrays.fill (valueStack, height - num + 1, height, null);
        java.util.Arrays.fill (locStack, height - num + 1, height, null);
      }
      height -= num;
    }

    public final int stateAt (int i) {
      return stateStack[height - i];
    }

    public final Location locationAt (int i) {
      return locStack[height - i];
    }

    public final Object valueAt (int i) {
      return valueStack[height - i];
    }

    // Print the state stack on the debug stream.
    public void print (java.io.PrintStream out)
    {
      out.print ("Stack now");
      
      for (int i = 0; i < height; i++)
        {
	  out.print (' ');
	  out.print (stateStack[i]);
        }
      out.println ();
    }
  }

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return success (<tt>true</tt>).  */
  public static final int YYACCEPT = 0;

  /**
   * Returned by a Bison action in order to stop the parsing process and
   * return failure (<tt>false</tt>).  */
  public static final int YYABORT = 1;

  /**
   * Returned by a Bison action in order to start error recovery without
   * printing an error message.  */
  public static final int YYERROR = 2;

  /**
   * Returned by a Bison action in order to print an error message and start
   * error recovery.  */
  public static final int YYFAIL = 3;

  private static final int YYNEWSTATE = 4;
  private static final int YYDEFAULT = 5;
  private static final int YYREDUCE = 6;
  private static final int YYERRLAB1 = 7;
  private static final int YYRETURN = 8;

  private int yyerrstatus_ = 0;

  /**
   * Return whether error recovery is being done.  In this state, the parser
   * reads token until it reaches a known state, and then restarts normal
   * operation.  */
  public final boolean recovering ()
  {
    return yyerrstatus_ == 0;
  }

  private int yyaction (int yyn, YYStack yystack, int yylen) 
  {
    Object yyval;
    Location yyloc = yylloc (yystack, yylen);

    /* If YYLEN is nonzero, implement the default value of the action:
       `$$ = $1'.  Otherwise, use the top of the stack.
    
       Otherwise, the following line sets YYVAL to garbage.
       This behavior is undocumented and Bison
       users should not rely upon it.  */
    if (yylen > 0)
      yyval = yystack.valueAt (yylen - 1);
    else
      yyval = yystack.valueAt (0);
    
    yy_reduce_print (yyn, yystack);

    switch (yyn)
      {
	  case 4:
  if (yyn == 4)
    
/* Line 353 of lalr1.java  */
/* Line 79 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    {_curID = ((Token)(yystack.valueAt (1-(1)))).getText(); _activeIfCounter = 0; _reqIfCounter = 0;};
  break;
    

  case 5:
  if (yyn == 5)
    
/* Line 353 of lalr1.java  */
/* Line 81 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    {
		for(Object ann : ((List)(yystack.valueAt (5-(4))))) {
			_nodeAnnotations.add(((Token)(yystack.valueAt (5-(1)))).getText(), (NodeAnnotation)ann);
		}
	};
  break;
    

  case 6:
  if (yyn == 6)
    
/* Line 353 of lalr1.java  */
/* Line 89 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { List result = new LinkedList(); if (((NodeAnnotation)(yystack.valueAt (1-(1)))) != null) result.add(((NodeAnnotation)(yystack.valueAt (1-(1))))); yyval = result; };
  break;
    

  case 7:
  if (yyn == 7)
    
/* Line 353 of lalr1.java  */
/* Line 91 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { if (((NodeAnnotation)(yystack.valueAt (2-(2)))) != null) ((List)(yystack.valueAt (2-(1)))).add(((NodeAnnotation)(yystack.valueAt (2-(2))))); };
  break;
    

  case 8:
  if (yyn == 8)
    
/* Line 353 of lalr1.java  */
/* Line 95 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { _typeAnnotations.add(((TypeAnnotation)(yystack.valueAt (1-(1))))); yyval = null; };
  break;
    

  case 9:
  if (yyn == 9)
    
/* Line 353 of lalr1.java  */
/* Line 97 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((NodeAnnotation)(yystack.valueAt (1-(1)))); };
  break;
    

  case 10:
  if (yyn == 10)
    
/* Line 353 of lalr1.java  */
/* Line 99 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((NodeAnnotation)(yystack.valueAt (1-(1)))); };
  break;
    

  case 11:
  if (yyn == 11)
    
/* Line 353 of lalr1.java  */
/* Line 101 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((NodeAnnotation)(yystack.valueAt (1-(1)))); };
  break;
    

  case 12:
  if (yyn == 12)
    
/* Line 353 of lalr1.java  */
/* Line 103 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((NodeAnnotation)(yystack.valueAt (1-(1)))); };
  break;
    

  case 13:
  if (yyn == 13)
    
/* Line 353 of lalr1.java  */
/* Line 105 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((NodeAnnotation)(yystack.valueAt (1-(1)))); };
  break;
    

  case 14:
  if (yyn == 14)
    
/* Line 353 of lalr1.java  */
/* Line 109 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    {yyval = new TypeAnnotation(_curID, ((Type)(yystack.valueAt (3-(3)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (3-(2)))))); };
  break;
    

  case 15:
  if (yyn == 15)
    
/* Line 353 of lalr1.java  */
/* Line 111 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new HashSet<String>();};
  break;
    

  case 16:
  if (yyn == 16)
    
/* Line 353 of lalr1.java  */
/* Line 112 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((Set)(yystack.valueAt (4-(3))));};
  break;
    

  case 17:
  if (yyn == 17)
    
/* Line 353 of lalr1.java  */
/* Line 113 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { Set<String> result = new HashSet<String>(); result.add(((Token)(yystack.valueAt (1-(1)))).getText()); yyval = result; };
  break;
    

  case 18:
  if (yyn == 18)
    
/* Line 353 of lalr1.java  */
/* Line 114 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { ((Set)(yystack.valueAt (3-(1)))).add(((Token)(yystack.valueAt (3-(3)))).getText()); yyval = ((Set)(yystack.valueAt (3-(1)))); };
  break;
    

  case 19:
  if (yyn == 19)
    
/* Line 353 of lalr1.java  */
/* Line 117 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = NumberType$.MODULE$; };
  break;
    

  case 20:
  if (yyn == 20)
    
/* Line 353 of lalr1.java  */
/* Line 119 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = StringType$.MODULE$; };
  break;
    

  case 21:
  if (yyn == 21)
    
/* Line 353 of lalr1.java  */
/* Line 121 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = BoolType$.MODULE$; };
  break;
    

  case 22:
  if (yyn == 22)
    
/* Line 353 of lalr1.java  */
/* Line 125 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new EnumType(JavaConversions.asScalaBuffer(((List)(yystack.valueAt (4-(3))))).toSet()); };
  break;
    

  case 23:
  if (yyn == 23)
    
/* Line 353 of lalr1.java  */
/* Line 129 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { List result = new LinkedList(); result.add(((Literal)(yystack.valueAt (1-(1))))); yyval = result; };
  break;
    

  case 24:
  if (yyn == 24)
    
/* Line 353 of lalr1.java  */
/* Line 132 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { ((List)(yystack.valueAt (3-(1)))).add(((Literal)(yystack.valueAt (3-(3))))); yyval = ((List)(yystack.valueAt (3-(1)))); };
  break;
    

  case 25:
  if (yyn == 25)
    
/* Line 353 of lalr1.java  */
/* Line 136 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ActiveIfAnnotation(((Expression)(yystack.valueAt (6-(3)))), _activeIfCounter, ((Option)(yystack.valueAt (6-(5)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (6-(6)))))); _activeIfCounter++; };
  break;
    

  case 26:
  if (yyn == 26)
    
/* Line 353 of lalr1.java  */
/* Line 138 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ActiveIfAnnotation(((Expression)(yystack.valueAt (9-(6)))), ((java.lang.Long)(yystack.valueAt (9-(3)))).intValue(), ((Option)(yystack.valueAt (9-(8)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (9-(9)))))); _activeIfCounter = ((java.lang.Long)(yystack.valueAt (9-(3)))).intValue(); };
  break;
    

  case 27:
  if (yyn == 27)
    
/* Line 353 of lalr1.java  */
/* Line 140 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = None$.MODULE$; };
  break;
    

  case 28:
  if (yyn == 28)
    
/* Line 353 of lalr1.java  */
/* Line 141 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Some(((Expression)(yystack.valueAt (4-(3))))); };
  break;
    

  case 29:
  if (yyn == 29)
    
/* Line 353 of lalr1.java  */
/* Line 145 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new CalculatedAnnotation(((Expression)(yystack.valueAt (6-(3)))), ((Option)(yystack.valueAt (6-(5)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (6-(6)))))); };
  break;
    

  case 30:
  if (yyn == 30)
    
/* Line 353 of lalr1.java  */
/* Line 149 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new DefaultAnnotation(((Expression)(yystack.valueAt (6-(3)))), ((Option)(yystack.valueAt (6-(5)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (6-(6)))))); };
  break;
    

  case 31:
  if (yyn == 31)
    
/* Line 353 of lalr1.java  */
/* Line 153 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { 
		yyval = new LegalValuesAnnotation(
			new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (6-(3)))))),
			((Option)(yystack.valueAt (6-(5)))),
			JavaConversions.asScalaSet(((Set)(yystack.valueAt (6-(6)))))); 
	};
  break;
    

  case 32:
  if (yyn == 32)
    
/* Line 353 of lalr1.java  */
/* Line 159 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = None$.MODULE$; };
  break;
    

  case 33:
  if (yyn == 33)
    
/* Line 353 of lalr1.java  */
/* Line 160 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Some(new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (4-(3))))))); };
  break;
    

  case 34:
  if (yyn == 34)
    
/* Line 353 of lalr1.java  */
/* Line 162 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { List result = new LinkedList(); result.add(((Range)(yystack.valueAt (1-(1))))); yyval = result; };
  break;
    

  case 35:
  if (yyn == 35)
    
/* Line 353 of lalr1.java  */
/* Line 164 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { ((List)(yystack.valueAt (3-(1)))).add(((Range)(yystack.valueAt (3-(3))))); yyval = ((List)(yystack.valueAt (3-(1)))); };
  break;
    

  case 36:
  if (yyn == 36)
    
/* Line 353 of lalr1.java  */
/* Line 165 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new SingleValueRange(((Expression)(yystack.valueAt (1-(1)))));};
  break;
    

  case 37:
  if (yyn == 37)
    
/* Line 353 of lalr1.java  */
/* Line 167 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new MinMaxRange(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3)))));};
  break;
    

  case 38:
  if (yyn == 38)
    
/* Line 353 of lalr1.java  */
/* Line 170 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ReqAnnotation(((Expression)(yystack.valueAt (6-(3)))), _reqIfCounter, ((Option)(yystack.valueAt (6-(5)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (6-(6)))))); _reqIfCounter++; };
  break;
    

  case 39:
  if (yyn == 39)
    
/* Line 353 of lalr1.java  */
/* Line 172 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ReqAnnotation(((Expression)(yystack.valueAt (9-(6)))), ((java.lang.Long)(yystack.valueAt (9-(3)))).intValue(), ((Option)(yystack.valueAt (9-(8)))), JavaConversions.asScalaSet(((Set)(yystack.valueAt (9-(9)))))); _reqIfCounter = ((java.lang.Long)(yystack.valueAt (9-(3)))).intValue(); };
  break;
    

  case 40:
  if (yyn == 40)
    
/* Line 353 of lalr1.java  */
/* Line 175 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Conditional(((Expression)(yystack.valueAt (5-(1)))), ((Expression)(yystack.valueAt (5-(3)))), ((Expression)(yystack.valueAt (5-(5))))); };
  break;
    

  case 41:
  if (yyn == 41)
    
/* Line 353 of lalr1.java  */
/* Line 177 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Or(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 42:
  if (yyn == 42)
    
/* Line 353 of lalr1.java  */
/* Line 179 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new And(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 43:
  if (yyn == 43)
    
/* Line 353 of lalr1.java  */
/* Line 181 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Eq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 44:
  if (yyn == 44)
    
/* Line 353 of lalr1.java  */
/* Line 183 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new NEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 45:
  if (yyn == 45)
    
/* Line 353 of lalr1.java  */
/* Line 185 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new GreaterThanOrEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 46:
  if (yyn == 46)
    
/* Line 353 of lalr1.java  */
/* Line 187 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new LessThanOrEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 47:
  if (yyn == 47)
    
/* Line 353 of lalr1.java  */
/* Line 189 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new GreaterThan(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 48:
  if (yyn == 48)
    
/* Line 353 of lalr1.java  */
/* Line 191 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new LessThan(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 49:
  if (yyn == 49)
    
/* Line 353 of lalr1.java  */
/* Line 193 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Plus(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 50:
  if (yyn == 50)
    
/* Line 353 of lalr1.java  */
/* Line 195 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Minus(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 51:
  if (yyn == 51)
    
/* Line 353 of lalr1.java  */
/* Line 197 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Times(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 52:
  if (yyn == 52)
    
/* Line 353 of lalr1.java  */
/* Line 199 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Div(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 53:
  if (yyn == 53)
    
/* Line 353 of lalr1.java  */
/* Line 201 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Dot(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 54:
  if (yyn == 54)
    
/* Line 353 of lalr1.java  */
/* Line 203 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Mod(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 55:
  if (yyn == 55)
    
/* Line 353 of lalr1.java  */
/* Line 205 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Implies(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 56:
  if (yyn == 56)
    
/* Line 353 of lalr1.java  */
/* Line 207 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new Not(((Expression)(yystack.valueAt (2-(2))))); };
  break;
    

  case 57:
  if (yyn == 57)
    
/* Line 353 of lalr1.java  */
/* Line 209 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new GetData(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 58:
  if (yyn == 58)
    
/* Line 353 of lalr1.java  */
/* Line 211 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IsActive(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 59:
  if (yyn == 59)
    
/* Line 353 of lalr1.java  */
/* Line 213 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IsEnabled(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 60:
  if (yyn == 60)
    
/* Line 353 of lalr1.java  */
/* Line 215 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IsLoaded(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 61:
  if (yyn == 61)
    
/* Line 353 of lalr1.java  */
/* Line 217 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IsSubstr(((Expression)(yystack.valueAt (6-(3)))), ((Expression)(yystack.valueAt (6-(5))))); };
  break;
    

  case 62:
  if (yyn == 62)
    
/* Line 353 of lalr1.java  */
/* Line 219 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ToInt(((Expression)(yystack.valueAt (4-(3))))); };
  break;
    

  case 63:
  if (yyn == 63)
    
/* Line 353 of lalr1.java  */
/* Line 221 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ToString(((Expression)(yystack.valueAt (4-(3))))); };
  break;
    

  case 64:
  if (yyn == 64)
    
/* Line 353 of lalr1.java  */
/* Line 223 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new ToBool(((Expression)(yystack.valueAt (4-(3))))); };
  break;
    

  case 65:
  if (yyn == 65)
    
/* Line 353 of lalr1.java  */
/* Line 225 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((Expression)(yystack.valueAt (3-(2)))); };
  break;
    

  case 66:
  if (yyn == 66)
    
/* Line 353 of lalr1.java  */
/* Line 227 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IdentifierRef(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 67:
  if (yyn == 67)
    
/* Line 353 of lalr1.java  */
/* Line 229 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = ((Literal)(yystack.valueAt (1-(1)))); };
  break;
    

  case 68:
  if (yyn == 68)
    
/* Line 353 of lalr1.java  */
/* Line 232 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new IntLiteral(((java.lang.Long)(yystack.valueAt (1-(1))))); };
  break;
    

  case 69:
  if (yyn == 69)
    
/* Line 353 of lalr1.java  */
/* Line 234 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new RealLiteral(new java.lang.Double(((Token)(yystack.valueAt (1-(1)))).getText())); };
  break;
    

  case 70:
  if (yyn == 70)
    
/* Line 353 of lalr1.java  */
/* Line 236 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new StringLiteral(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 71:
  if (yyn == 71)
    
/* Line 353 of lalr1.java  */
/* Line 237 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = new java.lang.Long(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 72:
  if (yyn == 72)
    
/* Line 353 of lalr1.java  */
/* Line 238 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = - (new java.lang.Long(((Token)(yystack.valueAt (2-(2)))).getText())); };
  break;
    

  case 73:
  if (yyn == 73)
    
/* Line 353 of lalr1.java  */
/* Line 239 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */
    { yyval = java.lang.Long.parseLong(((Token)(yystack.valueAt (1-(1)))).getText().substring(2), 16); };
  break;
    



/* Line 353 of lalr1.java  */
/* Line 1068 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\AnnotationParser.java"  */
	default: break;
      }

    yy_symbol_print ("-> $$ =", yyr1_[yyn], yyval, yyloc);

    yystack.pop (yylen);
    yylen = 0;

    /* Shift the result of the reduction.  */
    yyn = yyr1_[yyn];
    int yystate = yypgoto_[yyn - yyntokens_] + yystack.stateAt (0);
    if (0 <= yystate && yystate <= yylast_
	&& yycheck_[yystate] == yystack.stateAt (0))
      yystate = yytable_[yystate];
    else
      yystate = yydefgoto_[yyn - yyntokens_];

    yystack.push (yystate, yyval, yyloc);
    return YYNEWSTATE;
  }

  /* Return YYSTR after stripping away unnecessary quotes and
     backslashes, so that it's suitable for yyerror.  The heuristic is
     that double-quoting is unnecessary unless the string contains an
     apostrophe, a comma, or backslash (other than backslash-backslash).
     YYSTR is taken from yytname.  */
  private final String yytnamerr_ (String yystr)
  {
    if (yystr.charAt (0) == '"')
      {
        StringBuffer yyr = new StringBuffer ();
        strip_quotes: for (int i = 1; i < yystr.length (); i++)
          switch (yystr.charAt (i))
            {
            case '\'':
            case ',':
              break strip_quotes;

            case '\\':
	      if (yystr.charAt(++i) != '\\')
                break strip_quotes;
              /* Fall through.  */
            default:
              yyr.append (yystr.charAt (i));
              break;

            case '"':
              return yyr.toString ();
            }
      }
    else if (yystr.equals ("$end"))
      return "end of input";

    return yystr;
  }

  /*--------------------------------.
  | Print this symbol on YYOUTPUT.  |
  `--------------------------------*/

  private void yy_symbol_print (String s, int yytype,
			         Object yyvaluep				 , Object yylocationp)
  {
    if (yydebug > 0)
    yycdebug (s + (yytype < yyntokens_ ? " token " : " nterm ")
	      + yytname_[yytype] + " ("
	      + yylocationp + ": "
	      + (yyvaluep == null ? "(null)" : yyvaluep.toString ()) + ")");
  }

  /**
   * Parse input from the scanner that was specified at object construction
   * time.  Return whether the end of the input was reached successfully.
   *
   * @return <tt>true</tt> if the parsing succeeds.  Note that this does not
   *          imply that there were no syntax errors.
   */
  public boolean parse () throws java.io.IOException
  {
    /// Lookahead and lookahead in internal form.
    int yychar = yyempty_;
    int yytoken = 0;

    /* State.  */
    int yyn = 0;
    int yylen = 0;
    int yystate = 0;

    YYStack yystack = new YYStack ();

    /* Error handling.  */
    int yynerrs_ = 0;
    /// The location where the error started.
    Location yyerrloc = null;

    /// Location of the lookahead.
    Location yylloc = new Location (null, null);

    /// @$.
    Location yyloc;

    /// Semantic value of the lookahead.
    Object yylval = null;

    int yyresult;

    yycdebug ("Starting parse\n");
    yyerrstatus_ = 0;


    /* Initialize the stack.  */
    yystack.push (yystate, yylval, yylloc);

    int label = YYNEWSTATE;
    for (;;)
      switch (label)
      {
        /* New state.  Unlike in the C/C++ skeletons, the state is already
	   pushed when we come here.  */
      case YYNEWSTATE:
        yycdebug ("Entering state " + yystate + "\n");
        if (yydebug > 0)
          yystack.print (yyDebugStream);
    
        /* Accept?  */
        if (yystate == yyfinal_)
          return true;
    
        /* Take a decision.  First try without lookahead.  */
        yyn = yypact_[yystate];
        if (yyn == yypact_ninf_)
          {
            label = YYDEFAULT;
	    break;
          }
    
        /* Read a lookahead token.  */
        if (yychar == yyempty_)
          {
	    yycdebug ("Reading a token: ");
	    yychar = yylex ();
            
	    yylloc = new Location(yylexer.getStartPos (),
	    		   	            yylexer.getEndPos ());
            yylval = yylexer.getLVal ();
          }
    
        /* Convert token to internal form.  */
        if (yychar <= EOF)
          {
	    yychar = yytoken = EOF;
	    yycdebug ("Now at end of input.\n");
          }
        else
          {
	    yytoken = yytranslate_ (yychar);
	    yy_symbol_print ("Next token is", yytoken,
	    		     yylval, yylloc);
          }
    
        /* If the proper action on seeing token YYTOKEN is to reduce or to
           detect an error, take that action.  */
        yyn += yytoken;
        if (yyn < 0 || yylast_ < yyn || yycheck_[yyn] != yytoken)
          label = YYDEFAULT;
    
        /* <= 0 means reduce or error.  */
        else if ((yyn = yytable_[yyn]) <= 0)
          {
	    if (yyn == 0 || yyn == yytable_ninf_)
	      label = YYFAIL;
	    else
	      {
	        yyn = -yyn;
	        label = YYREDUCE;
	      }
          }
    
        else
          {
            /* Shift the lookahead token.  */
	    yy_symbol_print ("Shifting", yytoken,
	    		     yylval, yylloc);
    
            /* Discard the token being shifted.  */
            yychar = yyempty_;
    
            /* Count tokens shifted since error; after three, turn off error
               status.  */
            if (yyerrstatus_ > 0)
              --yyerrstatus_;
    
            yystate = yyn;
            yystack.push (yystate, yylval, yylloc);
            label = YYNEWSTATE;
          }
        break;
    
      /*-----------------------------------------------------------.
      | yydefault -- do the default action for the current state.  |
      `-----------------------------------------------------------*/
      case YYDEFAULT:
        yyn = yydefact_[yystate];
        if (yyn == 0)
          label = YYFAIL;
        else
          label = YYREDUCE;
        break;
    
      /*-----------------------------.
      | yyreduce -- Do a reduction.  |
      `-----------------------------*/
      case YYREDUCE:
        yylen = yyr2_[yyn];
        label = yyaction (yyn, yystack, yylen);
	yystate = yystack.stateAt (0);
        break;
    
      /*------------------------------------.
      | yyerrlab -- here on detecting error |
      `------------------------------------*/
      case YYFAIL:
        /* If not already recovering from an error, report this error.  */
        if (yyerrstatus_ == 0)
          {
	    ++yynerrs_;
	    yyerror (yylloc, yysyntax_error (yystate, yytoken));
          }
    
        yyerrloc = yylloc;
        if (yyerrstatus_ == 3)
          {
	    /* If just tried and failed to reuse lookahead token after an
	     error, discard it.  */
    
	    if (yychar <= EOF)
	      {
	      /* Return failure if at end of input.  */
	      if (yychar == EOF)
	        return false;
	      }
	    else
	      yychar = yyempty_;
          }
    
        /* Else will try to reuse lookahead token after shifting the error
           token.  */
        label = YYERRLAB1;
        break;
    
      /*---------------------------------------------------.
      | errorlab -- error raised explicitly by YYERROR.  |
      `---------------------------------------------------*/
      case YYERROR:
    
        yyerrloc = yystack.locationAt (yylen - 1);
        /* Do not reclaim the symbols of the rule which action triggered
           this YYERROR.  */
        yystack.pop (yylen);
        yylen = 0;
        yystate = yystack.stateAt (0);
        label = YYERRLAB1;
        break;
    
      /*-------------------------------------------------------------.
      | yyerrlab1 -- common code for both syntax error and YYERROR.  |
      `-------------------------------------------------------------*/
      case YYERRLAB1:
        yyerrstatus_ = 3;	/* Each real token shifted decrements this.  */
    
        for (;;)
          {
	    yyn = yypact_[yystate];
	    if (yyn != yypact_ninf_)
	      {
	        yyn += yyterror_;
	        if (0 <= yyn && yyn <= yylast_ && yycheck_[yyn] == yyterror_)
	          {
	            yyn = yytable_[yyn];
	            if (0 < yyn)
		      break;
	          }
	      }
    
	    /* Pop the current state because it cannot handle the error token.  */
	    if (yystack.height == 1)
	      return false;
    
	    yyerrloc = yystack.locationAt (0);
	    yystack.pop ();
	    yystate = yystack.stateAt (0);
	    if (yydebug > 0)
	      yystack.print (yyDebugStream);
          }
    
	
	/* Muck with the stack to setup for yylloc.  */
	yystack.push (0, null, yylloc);
	yystack.push (0, null, yyerrloc);
        yyloc = yylloc (yystack, 2);
	yystack.pop (2);

        /* Shift the error token.  */
        yy_symbol_print ("Shifting", yystos_[yyn],
			 yylval, yyloc);
    
        yystate = yyn;
	yystack.push (yyn, yylval, yyloc);
        label = YYNEWSTATE;
        break;
    
        /* Accept.  */
      case YYACCEPT:
        return true;
    
        /* Abort.  */
      case YYABORT:
        return false;
      }
  }

  // Generate an error message.
  private String yysyntax_error (int yystate, int tok)
  {
    if (errorVerbose)
      {
        int yyn = yypact_[yystate];
        if (yypact_ninf_ < yyn && yyn <= yylast_)
          {
	    StringBuffer res;

	    /* Start YYX at -YYN if negative to avoid negative indexes in
	       YYCHECK.  */
	    int yyxbegin = yyn < 0 ? -yyn : 0;

	    /* Stay within bounds of both yycheck and yytname.  */
	    int yychecklim = yylast_ - yyn + 1;
	    int yyxend = yychecklim < yyntokens_ ? yychecklim : yyntokens_;
	    int count = 0;
	    for (int x = yyxbegin; x < yyxend; ++x)
	      if (yycheck_[x + yyn] == x && x != yyterror_)
	        ++count;

	    // FIXME: This method of building the message is not compatible
	    // with internationalization.
	    res = new StringBuffer ("syntax error, unexpected ");
	    res.append (yytnamerr_ (yytname_[tok]));
	    if (count < 5)
	      {
	        count = 0;
	        for (int x = yyxbegin; x < yyxend; ++x)
	          if (yycheck_[x + yyn] == x && x != yyterror_)
		    {
		      res.append (count++ == 0 ? ", expecting " : " or ");
		      res.append (yytnamerr_ (yytname_[x]));
		    }
	      }
	    return res.toString ();
          }
      }

    return "syntax error";
  }


  /* YYPACT[STATE-NUM] -- Index in YYTABLE of the portion describing
     STATE-NUM.  */
  private static final short yypact_ninf_ = -124;
  private static final short yypact_[] =
  {
       -21,  -124,     5,  -124,   -33,  -124,  -124,    15,   -31,   -18,
     -17,    -9,    -8,     4,    24,  -124,  -124,  -124,  -124,  -124,
    -124,  -124,   508,   -19,   508,   -19,   508,   508,   508,    21,
      67,  -124,  -124,    22,    26,    34,    38,    55,    63,    69,
      77,  -124,  -124,  -124,  -124,  -124,   508,    97,   508,    78,
    -124,  -124,    83,    99,    89,   140,    45,  -124,   390,   159,
     120,  -124,  -124,    76,  -124,  -124,   123,   125,   126,   127,
     508,   508,   508,   508,    36,  -124,   263,   508,   508,   508,
     508,   508,   508,   508,   508,   508,   508,   508,   508,   508,
     508,   508,   508,   129,   105,   129,   106,   129,   508,   132,
     508,   129,  -124,   -13,    10,   110,   111,   112,   113,   241,
     283,   304,   324,  -124,    36,    36,    36,   431,   369,   451,
     472,    36,    36,    36,    49,    49,    49,  -124,  -124,  -124,
     116,     4,   508,     4,   508,     4,  -124,   118,     4,   410,
       4,  -124,   135,    46,  -124,  -124,  -124,  -124,  -124,   508,
    -124,  -124,  -124,   508,   508,  -124,   181,  -124,   200,  -124,
     508,  -124,  -124,  -124,    10,  -124,   345,   410,   222,   129,
     129,    50,  -124,  -124,  -124,     4,     4,  -124,  -124,  -124
  };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
  {
         0,     4,     0,     2,     0,     1,     3,     0,     0,     0,
       0,     0,     0,    15,     0,     6,     8,    11,    12,     9,
      13,    10,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     5,     7,     0,     0,     0,     0,     0,     0,     0,
       0,    66,    70,    71,    73,    69,     0,     0,     0,     0,
      67,    68,     0,     0,     0,     0,     0,    34,    36,     0,
       0,    19,    20,     0,    21,    14,     0,     0,     0,     0,
       0,     0,     0,     0,    56,    72,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    27,     0,    27,     0,    27,     0,    32,
       0,    27,    17,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,    65,    44,    45,    46,    55,     0,    41,
      42,    43,    47,    48,    49,    50,    53,    51,    52,    54,
       0,    15,     0,    15,     0,    15,    35,     0,    15,    37,
      15,    16,     0,     0,    23,    57,    58,    59,    60,     0,
      62,    63,    64,     0,     0,    30,     0,    38,     0,    25,
       0,    31,    29,    18,     0,    22,     0,    40,     0,    27,
      27,     0,    24,    61,    28,    15,    15,    33,    39,    26
  };

  /* YYPGOTO[NTERM-NUM].  */
  private static final short yypgoto_[] =
  {
      -124,  -124,   171,  -124,  -124,   160,  -124,  -123,  -124,  -124,
    -124,  -124,   -94,  -124,  -124,  -124,  -124,    16,    84,  -124,
     -22,   -93,    79
  };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final short
  yydefgoto_[] =
  {
        -1,     2,     3,     4,    14,    15,    16,    30,   103,    65,
     143,    17,   131,    18,    19,    20,   138,    56,    57,    21,
      58,    50,    51
  };

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final short yytable_ninf_ = -1;
  private static final short
  yytable_[] =
  {
        49,   133,    53,   135,    55,     5,    59,   140,   155,     1,
     157,   144,   159,    43,    44,   161,     7,   162,     8,     9,
      10,    11,    12,    22,    74,    47,    76,     8,     9,    10,
      11,    12,    29,    23,    25,     1,    24,    26,    13,   141,
     142,    42,    43,    44,    45,    27,    28,    13,   109,   110,
     111,   112,   178,   179,    47,   114,   115,   116,   117,   118,
     119,   120,   121,   122,   123,   124,   125,   126,   127,   128,
     129,   172,    60,    66,    31,   175,   176,    67,   139,    87,
      88,    89,    90,    91,    92,    68,    77,    78,    79,    69,
      80,    61,    62,    63,    64,    90,    91,    92,    98,   164,
      99,   165,    52,    98,    54,   177,    70,    77,    78,    79,
     156,    80,   158,    81,    71,    82,    83,    84,    85,    86,
      72,    87,    88,    89,    90,    91,    92,   166,    73,    75,
     104,   167,   168,    93,    81,    94,    82,    83,    84,    85,
      86,    96,    87,    88,    89,    90,    91,    92,    77,    78,
      79,   102,    80,   105,    95,   106,   107,   108,   130,   132,
     134,   137,   145,   146,   147,   148,   163,    77,    78,    79,
     154,    80,   160,     6,    32,    81,   171,    82,    83,    84,
      85,    86,   136,    87,    88,    89,    90,    91,    92,    77,
      78,    79,     0,    80,    81,    97,    82,    83,    84,    85,
      86,     0,    87,    88,    89,    90,    91,    92,    77,    78,
      79,     0,    80,     0,   101,     0,    81,     0,    82,    83,
      84,    85,    86,     0,    87,    88,    89,    90,    91,    92,
      77,    78,    79,     0,    80,    81,   169,    82,    83,    84,
      85,    86,     0,    87,    88,    89,    90,    91,    92,    77,
      78,    79,     0,    80,     0,   170,     0,    81,     0,    82,
      83,    84,    85,    86,     0,    87,    88,    89,    90,    91,
      92,    77,    78,    79,     0,    80,    81,   174,    82,    83,
      84,    85,    86,     0,    87,    88,    89,    90,    91,    92,
       0,    77,    78,    79,   149,    80,     0,     0,    81,     0,
      82,    83,    84,    85,    86,     0,    87,    88,    89,    90,
      91,    92,    77,    78,    79,   113,    80,     0,    81,     0,
      82,    83,    84,    85,    86,     0,    87,    88,    89,    90,
      91,    92,    77,    78,    79,   150,    80,     0,     0,    81,
       0,    82,    83,    84,    85,    86,     0,    87,    88,    89,
      90,    91,    92,    77,    78,    79,   151,    80,     0,    81,
       0,    82,    83,    84,    85,    86,     0,    87,    88,    89,
      90,    91,    92,     0,     0,     0,   152,    77,    78,    79,
      81,    80,    82,    83,    84,    85,    86,     0,    87,    88,
      89,    90,    91,    92,     0,     0,     0,   173,    77,    78,
      79,   100,    80,     0,    81,   153,    82,    83,    84,    85,
      86,     0,    87,    88,    89,    90,    91,    92,    77,    78,
      79,     0,    80,     0,     0,    81,     0,    82,    83,    84,
      85,    86,     0,    87,    88,    89,    90,    91,    92,    77,
      78,    79,     0,     0,     0,    81,     0,    82,    83,    84,
      85,    86,     0,    87,    88,    89,    90,    91,    92,    77,
      78,    79,     0,     0,     0,     0,     0,     0,    82,    83,
      84,    85,    86,     0,    87,    88,    89,    90,    91,    92,
      77,    78,    79,     0,     0,     0,     0,     0,     0,    83,
      84,    85,    86,     0,    87,    88,    89,    90,    91,    92,
       0,     0,     0,     0,     0,     0,     0,     0,     0,     0,
       0,    84,    85,    86,     0,    87,    88,    89,    90,    91,
      92,    33,    34,    35,    36,    37,     0,     0,    38,    39,
      40,     0,     0,     0,     0,     0,     0,     0,    41,    42,
      43,    44,    45,     0,     0,     0,     0,     0,     0,     0,
      46,     0,    47,     0,     0,     0,     0,     0,     0,    48
  };

  /* YYCHECK.  */
  private static final short
  yycheck_[] =
  {
        22,    95,    24,    97,    26,     0,    28,   101,   131,    30,
     133,   104,   135,    32,    33,   138,    49,   140,     3,     4,
       5,     6,     7,    54,    46,    44,    48,     3,     4,     5,
       6,     7,    28,    51,    51,    30,    54,    54,    23,    52,
      53,    31,    32,    33,    34,    54,    54,    23,    70,    71,
      72,    73,   175,   176,    44,    77,    78,    79,    80,    81,
      82,    83,    84,    85,    86,    87,    88,    89,    90,    91,
      92,   164,    51,    51,    50,   169,   170,    51,   100,    43,
      44,    45,    46,    47,    48,    51,     8,     9,    10,    51,
      12,    24,    25,    26,    27,    46,    47,    48,    53,    53,
      55,    55,    23,    53,    25,    55,    51,     8,     9,    10,
     132,    12,   134,    35,    51,    37,    38,    39,    40,    41,
      51,    43,    44,    45,    46,    47,    48,   149,    51,    32,
      54,   153,   154,    55,    35,    52,    37,    38,    39,    40,
      41,    52,    43,    44,    45,    46,    47,    48,     8,     9,
      10,    31,    12,    30,    55,    30,    30,    30,    29,    54,
      54,    29,    52,    52,    52,    52,    31,     8,     9,    10,
      54,    12,    54,     2,    14,    35,   160,    37,    38,    39,
      40,    41,    98,    43,    44,    45,    46,    47,    48,     8,
       9,    10,    -1,    12,    35,    55,    37,    38,    39,    40,
      41,    -1,    43,    44,    45,    46,    47,    48,     8,     9,
      10,    -1,    12,    -1,    55,    -1,    35,    -1,    37,    38,
      39,    40,    41,    -1,    43,    44,    45,    46,    47,    48,
       8,     9,    10,    -1,    12,    35,    55,    37,    38,    39,
      40,    41,    -1,    43,    44,    45,    46,    47,    48,     8,
       9,    10,    -1,    12,    -1,    55,    -1,    35,    -1,    37,
      38,    39,    40,    41,    -1,    43,    44,    45,    46,    47,
      48,     8,     9,    10,    -1,    12,    35,    55,    37,    38,
      39,    40,    41,    -1,    43,    44,    45,    46,    47,    48,
      -1,     8,     9,    10,    53,    12,    -1,    -1,    35,    -1,
      37,    38,    39,    40,    41,    -1,    43,    44,    45,    46,
      47,    48,     8,     9,    10,    52,    12,    -1,    35,    -1,
      37,    38,    39,    40,    41,    -1,    43,    44,    45,    46,
      47,    48,     8,     9,    10,    52,    12,    -1,    -1,    35,
      -1,    37,    38,    39,    40,    41,    -1,    43,    44,    45,
      46,    47,    48,     8,     9,    10,    52,    12,    -1,    35,
      -1,    37,    38,    39,    40,    41,    -1,    43,    44,    45,
      46,    47,    48,    -1,    -1,    -1,    52,     8,     9,    10,
      35,    12,    37,    38,    39,    40,    41,    -1,    43,    44,
      45,    46,    47,    48,    -1,    -1,    -1,    52,     8,     9,
      10,    11,    12,    -1,    35,    36,    37,    38,    39,    40,
      41,    -1,    43,    44,    45,    46,    47,    48,     8,     9,
      10,    -1,    12,    -1,    -1,    35,    -1,    37,    38,    39,
      40,    41,    -1,    43,    44,    45,    46,    47,    48,     8,
       9,    10,    -1,    -1,    -1,    35,    -1,    37,    38,    39,
      40,    41,    -1,    43,    44,    45,    46,    47,    48,     8,
       9,    10,    -1,    -1,    -1,    -1,    -1,    -1,    37,    38,
      39,    40,    41,    -1,    43,    44,    45,    46,    47,    48,
       8,     9,    10,    -1,    -1,    -1,    -1,    -1,    -1,    38,
      39,    40,    41,    -1,    43,    44,    45,    46,    47,    48,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      -1,    39,    40,    41,    -1,    43,    44,    45,    46,    47,
      48,    13,    14,    15,    16,    17,    -1,    -1,    20,    21,
      22,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    30,    31,
      32,    33,    34,    -1,    -1,    -1,    -1,    -1,    -1,    -1,
      42,    -1,    44,    -1,    -1,    -1,    -1,    -1,    -1,    51
  };

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
  yystos_[] =
  {
         0,    30,    57,    58,    59,     0,    58,    49,     3,     4,
       5,     6,     7,    23,    60,    61,    62,    67,    69,    70,
      71,    75,    54,    51,    54,    51,    54,    54,    54,    28,
      63,    50,    61,    13,    14,    15,    16,    17,    20,    21,
      22,    30,    31,    32,    33,    34,    42,    44,    51,    76,
      77,    78,    78,    76,    78,    76,    73,    74,    76,    76,
      51,    24,    25,    26,    27,    65,    51,    51,    51,    51,
      51,    51,    51,    51,    76,    32,    76,     8,     9,    10,
      12,    35,    37,    38,    39,    40,    41,    43,    44,    45,
      46,    47,    48,    55,    52,    55,    52,    55,    53,    55,
      11,    55,    31,    64,    54,    30,    30,    30,    30,    76,
      76,    76,    76,    52,    76,    76,    76,    76,    76,    76,
      76,    76,    76,    76,    76,    76,    76,    76,    76,    76,
      29,    68,    54,    68,    54,    68,    74,    29,    72,    76,
      68,    52,    53,    66,    77,    52,    52,    52,    52,    53,
      52,    52,    52,    36,    54,    63,    76,    63,    76,    63,
      54,    63,    63,    31,    53,    55,    76,    76,    76,    55,
      55,    73,    77,    52,    55,    68,    68,    55,    63,    63
  };

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
  yytoken_number_[] =
  {
         0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,    63,    58,   124,    38,    61,
      62,    60,    33,    43,    45,    46,    42,    47,    37,   123,
     125,    40,    41,    44,    91,    93
  };

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
  yyr1_[] =
  {
         0,    56,    57,    57,    59,    58,    60,    60,    61,    61,
      61,    61,    61,    61,    62,    63,    63,    64,    64,    65,
      65,    65,    65,    66,    66,    67,    67,    68,    68,    69,
      70,    71,    72,    72,    73,    73,    74,    74,    75,    75,
      76,    76,    76,    76,    76,    76,    76,    76,    76,    76,
      76,    76,    76,    76,    76,    76,    76,    76,    76,    76,
      76,    76,    76,    76,    76,    76,    76,    76,    77,    77,
      77,    78,    78,    78
  };

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
  yyr2_[] =
  {
         0,     2,     1,     2,     0,     5,     1,     2,     1,     1,
       1,     1,     1,     1,     3,     0,     4,     1,     3,     1,
       1,     1,     4,     1,     3,     6,     9,     0,     4,     6,
       6,     6,     0,     4,     1,     3,     1,     3,     6,     9,
       5,     3,     3,     3,     3,     3,     3,     3,     3,     3,
       3,     3,     3,     3,     3,     3,     2,     4,     4,     4,
       4,     6,     4,     4,     4,     3,     1,     1,     1,     1,
       1,     1,     2,     1
  };

  /* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname_[] =
  {
    "$end", "error", "$undefined", "DEFAULT", "REQUIRES", "ACTIVEIF",
  "LEGALVALUES", "CALCULATED", "NEQ", "GEQ", "LEQ", "TO", "IMPLIES",
  "GETDATA", "ISACTIVE", "ISENABLED", "ISLOADED", "ISSUBSTR", "ISXSUBSTR",
  "VERSIONCMP", "TOINT", "TOSTRING", "TOBOOL", "TYPE", "INTTYPE",
  "STRINGTYPE", "ENUMTYPE", "BOOLTYPE", "IN", "REPLACING", "ID", "STRING",
  "INT", "HEXINT", "REAL", "'?'", "':'", "'|'", "'&'", "'='", "'>'", "'<'",
  "'!'", "'+'", "'-'", "'.'", "'*'", "'/'", "'%'", "'{'", "'}'", "'('",
  "')'", "','", "'['", "']'", "$accept", "node_annotations",
  "node_annotation", "$@1", "inner_anntations", "inner_anntation",
  "type_annotation", "applicable_files", "files", "type", "literal_list",
  "active_if", "optional_old_expr", "calculated", "default_value",
  "legal_values", "optional_legal_value_list", "legal_value_list",
  "legal_value", "requires", "expr", "literal", "int", null
  };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
  {
        57,     0,    -1,    58,    -1,    57,    58,    -1,    -1,    30,
      59,    49,    60,    50,    -1,    61,    -1,    60,    61,    -1,
      62,    -1,    70,    -1,    75,    -1,    67,    -1,    69,    -1,
      71,    -1,    23,    63,    65,    -1,    -1,    28,    51,    64,
      52,    -1,    31,    -1,    64,    53,    31,    -1,    24,    -1,
      25,    -1,    27,    -1,    26,    54,    66,    55,    -1,    77,
      -1,    66,    53,    77,    -1,     5,    54,    76,    55,    68,
      63,    -1,     5,    51,    78,    52,    54,    76,    55,    68,
      63,    -1,    -1,    29,    54,    76,    55,    -1,     7,    54,
      76,    55,    68,    63,    -1,     3,    54,    76,    55,    68,
      63,    -1,     6,    54,    73,    55,    72,    63,    -1,    -1,
      29,    54,    73,    55,    -1,    74,    -1,    73,    53,    74,
      -1,    76,    -1,    76,    11,    76,    -1,     4,    54,    76,
      55,    68,    63,    -1,     4,    51,    78,    52,    54,    76,
      55,    68,    63,    -1,    76,    35,    76,    36,    76,    -1,
      76,    37,    76,    -1,    76,    38,    76,    -1,    76,    39,
      76,    -1,    76,     8,    76,    -1,    76,     9,    76,    -1,
      76,    10,    76,    -1,    76,    40,    76,    -1,    76,    41,
      76,    -1,    76,    43,    76,    -1,    76,    44,    76,    -1,
      76,    46,    76,    -1,    76,    47,    76,    -1,    76,    45,
      76,    -1,    76,    48,    76,    -1,    76,    12,    76,    -1,
      42,    76,    -1,    13,    51,    30,    52,    -1,    14,    51,
      30,    52,    -1,    15,    51,    30,    52,    -1,    16,    51,
      30,    52,    -1,    17,    51,    76,    53,    76,    52,    -1,
      20,    51,    76,    52,    -1,    21,    51,    76,    52,    -1,
      22,    51,    76,    52,    -1,    51,    76,    52,    -1,    30,
      -1,    77,    -1,    78,    -1,    34,    -1,    31,    -1,    32,
      -1,    44,    32,    -1,    33,    -1
  };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final short yyprhs_[] =
  {
         0,     0,     3,     5,     8,     9,    15,    17,    20,    22,
      24,    26,    28,    30,    32,    36,    37,    42,    44,    48,
      50,    52,    54,    59,    61,    65,    72,    82,    83,    88,
      95,   102,   109,   110,   115,   117,   121,   123,   127,   134,
     144,   150,   154,   158,   162,   166,   170,   174,   178,   182,
     186,   190,   194,   198,   202,   206,   210,   213,   218,   223,
     228,   233,   240,   245,   250,   255,   259,   261,   263,   265,
     267,   269,   271,   274
  };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short yyrline_[] =
  {
         0,    77,    77,    77,    79,    79,    88,    90,    94,    96,
      98,   100,   102,   104,   108,   111,   112,   113,   114,   116,
     118,   120,   124,   128,   131,   135,   137,   140,   141,   144,
     148,   152,   159,   160,   162,   163,   165,   166,   169,   171,
     174,   176,   178,   180,   182,   184,   186,   188,   190,   192,
     194,   196,   198,   200,   202,   204,   206,   208,   210,   212,
     214,   216,   218,   220,   222,   224,   226,   228,   231,   233,
     235,   237,   238,   239
  };

  // Report on the debug stream that the rule yyrule is going to be reduced.
  private void yy_reduce_print (int yyrule, YYStack yystack)
  {
    if (yydebug == 0)
      return;

    int yylno = yyrline_[yyrule];
    int yynrhs = yyr2_[yyrule];
    /* Print the symbols being reduced, and their result.  */
    yycdebug ("Reducing stack by rule " + (yyrule - 1)
	      + " (line " + yylno + "), ");

    /* The symbols being reduced.  */
    for (int yyi = 0; yyi < yynrhs; yyi++)
      yy_symbol_print ("   $" + (yyi + 1) + " =",
		       yyrhs_[yyprhs_[yyrule] + yyi],
		       ((yystack.valueAt (yynrhs-(yyi + 1)))), 
		       yystack.locationAt (yynrhs-(yyi + 1)));
  }

  /* YYTRANSLATE(YYLEX) -- Bison symbol number corresponding to YYLEX.  */
  private static final byte yytranslate_table_[] =
  {
         0,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,    42,     2,     2,     2,    48,    38,     2,
      51,    52,    46,    43,    53,    44,    45,    47,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,    36,     2,
      41,    39,    40,    35,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,    54,     2,    55,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,    49,    37,    50,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     1,     2,     3,     4,
       5,     6,     7,     8,     9,    10,    11,    12,    13,    14,
      15,    16,    17,    18,    19,    20,    21,    22,    23,    24,
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34
  };

  private static final byte yytranslate_ (int t)
  {
    if (t >= 0 && t <= yyuser_token_number_max_)
      return yytranslate_table_[t];
    else
      return yyundef_token_;
  }

  private static final int yylast_ = 559;
  private static final int yynnts_ = 23;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 5;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 56;

  private static final int yyuser_token_number_max_ = 289;
  private static final int yyundef_token_ = 2;

/* User implementation code.  */
/* Unqualified %code blocks.  */

/* Line 875 of lalr1.java  */
/* Line 19 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */

  private NodeAnnotations _nodeAnnotations = new NodeAnnotations();
  private LinkedList<TypeAnnotation> _typeAnnotations = new LinkedList<TypeAnnotation>();
  private String _curID;
  private int _activeIfCounter = 0;
  private int _reqIfCounter = 0;
  
  public NodeAnnotations getNodeAnnotations() {
    return _nodeAnnotations;
  }  
  
  public TypeAnnotations getTypeAnnotations() { 
	return new TypeAnnotations(JavaConversions.asScalaBuffer(_typeAnnotations));
  }
  
  public Collection<CompileError> errors() {
	return ((AnnotationLexer)yylexer)._allErrors;
  }



/* Line 875 of lalr1.java  */
/* Line 1873 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\AnnotationParser.java"  */

}


/* Line 879 of lalr1.java  */
/* Line 241 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\annotation.y"  */


