
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
/* Line 9 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */

  import java.io.*;
  import java.util.*;
  import scala.collection.JavaConversions;
  import scala.Option;
  import scala.Some;
  import scala.None$;



/* Line 33 of lalr1.java  */
/* Line 53 of "E:\\PriorityFixing\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccFullParser.java"  */

/**
 * A Bison parser, automatically generated from <tt>E:\\PriorityFixing\\src\\main\\eccFull.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class EccFullParser
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
  public static final int VERSION = 258;
  /** Token number, to be returned by the scanner.  */
  public static final int COMMAND = 259;
  /** Token number, to be returned by the scanner.  */
  public static final int CONFIGURATION = 260;
  /** Token number, to be returned by the scanner.  */
  public static final int CDLPACKAGE = 261;
  /** Token number, to be returned by the scanner.  */
  public static final int COMPONENT = 262;
  /** Token number, to be returned by the scanner.  */
  public static final int INTERFACE = 263;
  /** Token number, to be returned by the scanner.  */
  public static final int OPTION = 264;
  /** Token number, to be returned by the scanner.  */
  public static final int DESCRIPTION = 265;
  /** Token number, to be returned by the scanner.  */
  public static final int HARDWARE = 266;
  /** Token number, to be returned by the scanner.  */
  public static final int TEMPLATE = 267;
  /** Token number, to be returned by the scanner.  */
  public static final int PACKAGE = 268;
  /** Token number, to be returned by the scanner.  */
  public static final int USERVALUE = 269;
  /** Token number, to be returned by the scanner.  */
  public static final int INFERREDVALUE = 270;
  /** Token number, to be returned by the scanner.  */
  public static final int FLAVOR = 271;
  /** Token number, to be returned by the scanner.  */
  public static final int DEFAULT = 272;
  /** Token number, to be returned by the scanner.  */
  public static final int REQUIRES = 273;
  /** Token number, to be returned by the scanner.  */
  public static final int ACTIVEIF = 274;
  /** Token number, to be returned by the scanner.  */
  public static final int LEGALVALUES = 275;
  /** Token number, to be returned by the scanner.  */
  public static final int CALCULATED = 276;
  /** Token number, to be returned by the scanner.  */
  public static final int IMPLEMENTEDBY = 277;
  /** Token number, to be returned by the scanner.  */
  public static final int NONEFLAVOR = 278;
  /** Token number, to be returned by the scanner.  */
  public static final int DATA = 279;
  /** Token number, to be returned by the scanner.  */
  public static final int BOOL = 280;
  /** Token number, to be returned by the scanner.  */
  public static final int BOOLDATA = 281;
  /** Token number, to be returned by the scanner.  */
  public static final int ACTIVE = 282;
  /** Token number, to be returned by the scanner.  */
  public static final int INACTIVE = 283;
  /** Token number, to be returned by the scanner.  */
  public static final int ENABLED = 284;
  /** Token number, to be returned by the scanner.  */
  public static final int DISABLED = 285;
  /** Token number, to be returned by the scanner.  */
  public static final int NEQ = 286;
  /** Token number, to be returned by the scanner.  */
  public static final int GEQ = 287;
  /** Token number, to be returned by the scanner.  */
  public static final int LEQ = 288;
  /** Token number, to be returned by the scanner.  */
  public static final int TO = 289;
  /** Token number, to be returned by the scanner.  */
  public static final int IMPLIES = 290;
  /** Token number, to be returned by the scanner.  */
  public static final int GETDATA = 291;
  /** Token number, to be returned by the scanner.  */
  public static final int ISACTIVE = 292;
  /** Token number, to be returned by the scanner.  */
  public static final int ISENABLED = 293;
  /** Token number, to be returned by the scanner.  */
  public static final int ISLOADED = 294;
  /** Token number, to be returned by the scanner.  */
  public static final int ISSUBSTR = 295;
  /** Token number, to be returned by the scanner.  */
  public static final int ISXSUBSTR = 296;
  /** Token number, to be returned by the scanner.  */
  public static final int VERSIONCMP = 297;
  /** Token number, to be returned by the scanner.  */
  public static final int CALCULATIONINDICATOR = 298;
  /** Token number, to be returned by the scanner.  */
  public static final int DEFAULTTEXT = 299;
  /** Token number, to be returned by the scanner.  */
  public static final int FOLLOWINDICATOR = 300;
  /** Token number, to be returned by the scanner.  */
  public static final int LINETEXT = 301;
  /** Token number, to be returned by the scanner.  */
  public static final int LEVELUP = 302;
  /** Token number, to be returned by the scanner.  */
  public static final int LEVELDOWN = 303;
  /** Token number, to be returned by the scanner.  */
  public static final int DERIVEDUSERVALUE = 304;
  /** Token number, to be returned by the scanner.  */
  public static final int ID = 305;
  /** Token number, to be returned by the scanner.  */
  public static final int STRING = 306;
  /** Token number, to be returned by the scanner.  */
  public static final int INT = 307;
  /** Token number, to be returned by the scanner.  */
  public static final int HEXINT = 308;
  /** Token number, to be returned by the scanner.  */
  public static final int REAL = 309;



  
  private Location yylloc (YYStack rhs, int n)
  {
    if (n > 0)
      return new Location (rhs.locationAt (1).begin, rhs.locationAt (n).end);
    else
      return new Location (rhs.locationAt (0).end);
  }

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>EccFullParser</tt>.
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
  public EccFullParser (Lexer yylexer) {
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
	  case 2:
  if (yyn == 2)
    
/* Line 353 of lalr1.java  */
/* Line 100 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
	NodeHelper$.MODULE$.fillImplements(JavaConversions.asScalaBuffer(_nodes));
};
  break;
    

  case 14:
  if (yyn == 14)
    
/* Line 353 of lalr1.java  */
/* Line 121 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
		PackageRef pr = new PackageRef(((Token)(yystack.valueAt (5-(3)))).getText(), ((Token)(yystack.valueAt (5-(4)))).getText());
		_packageRefs.add(pr);
	};
  break;
    

  case 15:
  if (yyn == 15)
    
/* Line 353 of lalr1.java  */
/* Line 126 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
		PackageRef pr = new PackageRef(((Token)(yystack.valueAt (4-(3)))).getText(), "");
		_packageRefs.add(pr);
	};
  break;
    

  case 24:
  if (yyn == 24)
    
/* Line 353 of lalr1.java  */
/* Line 136 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
		if (_nodes.isEmpty()) {
			_virtualLevel++;
		}
		else if(_goDownNode == null) {
			yyerror(yystack.locationAt (1-(1)), "no node to go down");
		}
		else {
			_parentNode = _goDownNode;
		}
	};
  break;
    

  case 25:
  if (yyn == 25)
    
/* Line 353 of lalr1.java  */
/* Line 148 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
		if (_parentNode == null) {
			if (_virtualLevel == 0) yyerror(yystack.locationAt (1-(1)), "go up from the top level");
			else _virtualLevel--;
		}
		else {
			if (_parentNode.getParent().equals(None$.MODULE$))
				_parentNode = null;
			else
				_parentNode = _parentNode.getParent().get();
		}
		_goDownNode = null;
	};
  break;
    

  case 26:
  if (yyn == 26)
    
/* Line 353 of lalr1.java  */
/* Line 175 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    {
	if (_values.containsKey(((Token)(yystack.valueAt (16-(2)))).getText()) || _derivedValues.containsKey(((Token)(yystack.valueAt (16-(2)))).getText()))
		yyerror(yystack.locationAt (16-(2)), "duplicated declaration.");
		
	if (((java.lang.Boolean)(yystack.valueAt (16-(13)))) && !(((Flavor$.Value)(yystack.valueAt (16-(7)))).equals(Flavor.None())))
		yyerror(yystack.locationAt (16-(13)), "Feature with flavor has \"no associated value\".");

	if (((OptionValue)(yystack.valueAt (16-(8)))) != NoneOptionValue$.MODULE$ && ((OptionValue)(yystack.valueAt (16-(9)))) != NoneOptionValue$.MODULE$)
	
		yyerror(yystack.locationAt (16-(8)), "Two user values are provided.");

	if (((OptionValue)(yystack.valueAt (16-(8)))) != NoneOptionValue$.MODULE$ && ((OptionValue)(yystack.valueAt (16-(10)))) != NoneOptionValue$.MODULE$ && !((OptionValue)(yystack.valueAt (16-(8)))).equals(((OptionValue)(yystack.valueAt (16-(10))))))
_values.put(((Token)(yystack.valueAt (16-(2)))).getText(), ((OptionValue)(yystack.valueAt (16-(8)))));		
//yyerror(@8, "the two values of the configuration are not the same.");

	if (((OptionValue)(yystack.valueAt (16-(8)))) != NoneOptionValue$.MODULE$)
		_values.put(((Token)(yystack.valueAt (16-(2)))).getText(), ((OptionValue)(yystack.valueAt (16-(8)))));
	else
		_values.put(((Token)(yystack.valueAt (16-(2)))).getText(), ((OptionValue)(yystack.valueAt (16-(10)))));
	if (((OptionValue)(yystack.valueAt (16-(10)))) != NoneOptionValue$.MODULE$)
		_derivedValues.put(((Token)(yystack.valueAt (16-(2)))).getText(), NoneOptionValue$.MODULE$);
	else
		_derivedValues.put(((Token)(yystack.valueAt (16-(2)))).getText(), ((OptionValue)(yystack.valueAt (16-(9)))));

	EccNode n = new EccNode(((Token)(yystack.valueAt (16-(2)))).getText(), //id
					((CdlType$.Value)(yystack.valueAt (16-(1)))), // cdlType
					((Flavor$.Value)(yystack.valueAt (16-(7)))), // flavor
					((Option)(yystack.valueAt (16-(11)))), // default_value
					((Option)(yystack.valueAt (16-(6)))), // calcualted
					((Option)(yystack.valueAt (16-(12)))), // legal_values
					scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (16-(14))))), //requires
					scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (16-(5))))), //active_if
					scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (16-(4))))) // implemented_bys
					);

	if (_parentNode == null) _nodes.add(n);
	else _parentNode.addChild(n);
	_goDownNode = n;

};
  break;
    

  case 27:
  if (yyn == 27)
    
/* Line 353 of lalr1.java  */
/* Line 217 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = CdlType.Package(); };
  break;
    

  case 28:
  if (yyn == 28)
    
/* Line 353 of lalr1.java  */
/* Line 219 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = CdlType.Component(); };
  break;
    

  case 29:
  if (yyn == 29)
    
/* Line 353 of lalr1.java  */
/* Line 221 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = CdlType.Interface(); };
  break;
    

  case 30:
  if (yyn == 30)
    
/* Line 353 of lalr1.java  */
/* Line 223 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = CdlType.Option(); };
  break;
    

  case 31:
  if (yyn == 31)
    
/* Line 353 of lalr1.java  */
/* Line 226 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new LinkedList(); };
  break;
    

  case 32:
  if (yyn == 32)
    
/* Line 353 of lalr1.java  */
/* Line 228 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { ((List)(yystack.valueAt (2-(1)))).add(((String)(yystack.valueAt (2-(2))))); yyval = ((List)(yystack.valueAt (2-(1)))); };
  break;
    

  case 33:
  if (yyn == 33)
    
/* Line 353 of lalr1.java  */
/* Line 231 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Token)(yystack.valueAt (6-(2)))).getText(); };
  break;
    

  case 38:
  if (yyn == 38)
    
/* Line 353 of lalr1.java  */
/* Line 236 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new LinkedList(); };
  break;
    

  case 39:
  if (yyn == 39)
    
/* Line 353 of lalr1.java  */
/* Line 238 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { ((List)(yystack.valueAt (2-(1)))).add(((Expression)(yystack.valueAt (2-(2))))); yyval = ((List)(yystack.valueAt (2-(1)))); };
  break;
    

  case 40:
  if (yyn == 40)
    
/* Line 353 of lalr1.java  */
/* Line 242 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Expression)(yystack.valueAt (2-(2)))); };
  break;
    

  case 41:
  if (yyn == 41)
    
/* Line 353 of lalr1.java  */
/* Line 245 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = None$.MODULE$; };
  break;
    

  case 42:
  if (yyn == 42)
    
/* Line 353 of lalr1.java  */
/* Line 247 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Some(((Expression)(yystack.valueAt (2-(2))))); };
  break;
    

  case 43:
  if (yyn == 43)
    
/* Line 353 of lalr1.java  */
/* Line 250 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = Flavor.None(); };
  break;
    

  case 44:
  if (yyn == 44)
    
/* Line 353 of lalr1.java  */
/* Line 252 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Flavor$.Value)(yystack.valueAt (2-(2)))); };
  break;
    

  case 45:
  if (yyn == 45)
    
/* Line 353 of lalr1.java  */
/* Line 253 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = Flavor.Bool(); };
  break;
    

  case 46:
  if (yyn == 46)
    
/* Line 353 of lalr1.java  */
/* Line 254 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = Flavor.BoolData(); };
  break;
    

  case 47:
  if (yyn == 47)
    
/* Line 353 of lalr1.java  */
/* Line 255 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = Flavor.Data(); };
  break;
    

  case 48:
  if (yyn == 48)
    
/* Line 353 of lalr1.java  */
/* Line 257 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = NoneOptionValue$.MODULE$; };
  break;
    

  case 49:
  if (yyn == 49)
    
/* Line 353 of lalr1.java  */
/* Line 258 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((OptionValue)(yystack.valueAt (2-(2)))); };
  break;
    

  case 50:
  if (yyn == 50)
    
/* Line 353 of lalr1.java  */
/* Line 259 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = NoneOptionValue$.MODULE$; };
  break;
    

  case 51:
  if (yyn == 51)
    
/* Line 353 of lalr1.java  */
/* Line 260 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((OptionValue)(yystack.valueAt (2-(2)))); };
  break;
    

  case 52:
  if (yyn == 52)
    
/* Line 353 of lalr1.java  */
/* Line 261 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = NoneOptionValue$.MODULE$; };
  break;
    

  case 53:
  if (yyn == 53)
    
/* Line 353 of lalr1.java  */
/* Line 262 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((OptionValue)(yystack.valueAt (2-(2)))); };
  break;
    

  case 54:
  if (yyn == 54)
    
/* Line 353 of lalr1.java  */
/* Line 264 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((SingleOptionValue)(yystack.valueAt (1-(1)))); };
  break;
    

  case 55:
  if (yyn == 55)
    
/* Line 353 of lalr1.java  */
/* Line 266 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { 
	if (!(((SingleOptionValue)(yystack.valueAt (2-(1)))) instanceof IntOptionValue)) {
		yyerror(yystack.locationAt (2-(1)), "the first value of booldata is not integer");
	}
	yyval = new DoubleOptionValue((IntOptionValue)((SingleOptionValue)(yystack.valueAt (2-(1)))), ((SingleOptionValue)(yystack.valueAt (2-(2))))); 
};
  break;
    

  case 56:
  if (yyn == 56)
    
/* Line 353 of lalr1.java  */
/* Line 272 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new StringOptionValue(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 57:
  if (yyn == 57)
    
/* Line 353 of lalr1.java  */
/* Line 273 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new StringOptionValue(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 58:
  if (yyn == 58)
    
/* Line 353 of lalr1.java  */
/* Line 274 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IntOptionValue(((java.lang.Long)(yystack.valueAt (1-(1))))); };
  break;
    

  case 59:
  if (yyn == 59)
    
/* Line 353 of lalr1.java  */
/* Line 275 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new RealOptionValue(new java.lang.Double(((Token)(yystack.valueAt (1-(1)))).getText())); };
  break;
    

  case 60:
  if (yyn == 60)
    
/* Line 353 of lalr1.java  */
/* Line 280 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = None$.MODULE$; };
  break;
    

  case 61:
  if (yyn == 61)
    
/* Line 353 of lalr1.java  */
/* Line 282 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Some(((Expression)(yystack.valueAt (2-(2))))); };
  break;
    

  case 62:
  if (yyn == 62)
    
/* Line 353 of lalr1.java  */
/* Line 284 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Some(((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 63:
  if (yyn == 63)
    
/* Line 353 of lalr1.java  */
/* Line 289 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = None$.MODULE$; };
  break;
    

  case 64:
  if (yyn == 64)
    
/* Line 353 of lalr1.java  */
/* Line 291 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Some(new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer(((List)(yystack.valueAt (2-(2))))))); };
  break;
    

  case 65:
  if (yyn == 65)
    
/* Line 353 of lalr1.java  */
/* Line 293 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { List result = new LinkedList(); result.add(((Range)(yystack.valueAt (1-(1))))); yyval = result; };
  break;
    

  case 66:
  if (yyn == 66)
    
/* Line 353 of lalr1.java  */
/* Line 295 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { ((List)(yystack.valueAt (2-(1)))).add(((Range)(yystack.valueAt (2-(2))))); yyval = ((List)(yystack.valueAt (2-(1)))); };
  break;
    

  case 67:
  if (yyn == 67)
    
/* Line 353 of lalr1.java  */
/* Line 296 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new SingleValueRange(((Expression)(yystack.valueAt (1-(1)))));};
  break;
    

  case 68:
  if (yyn == 68)
    
/* Line 353 of lalr1.java  */
/* Line 298 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new MinMaxRange(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3)))));};
  break;
    

  case 69:
  if (yyn == 69)
    
/* Line 353 of lalr1.java  */
/* Line 302 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new LinkedList(); };
  break;
    

  case 70:
  if (yyn == 70)
    
/* Line 353 of lalr1.java  */
/* Line 304 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { ((List)(yystack.valueAt (2-(1)))).add(((Expression)(yystack.valueAt (2-(2))))); yyval = ((List)(yystack.valueAt (2-(1)))); };
  break;
    

  case 71:
  if (yyn == 71)
    
/* Line 353 of lalr1.java  */
/* Line 306 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Expression)(yystack.valueAt (2-(2)))); };
  break;
    

  case 72:
  if (yyn == 72)
    
/* Line 353 of lalr1.java  */
/* Line 309 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = false; };
  break;
    

  case 73:
  if (yyn == 73)
    
/* Line 353 of lalr1.java  */
/* Line 311 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = true; };
  break;
    

  case 74:
  if (yyn == 74)
    
/* Line 353 of lalr1.java  */
/* Line 315 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Expression)(yystack.valueAt (1-(1)))); };
  break;
    

  case 75:
  if (yyn == 75)
    
/* Line 353 of lalr1.java  */
/* Line 317 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new And(((Expression)(yystack.valueAt (2-(1)))), ((Expression)(yystack.valueAt (2-(2))))); };
  break;
    

  case 76:
  if (yyn == 76)
    
/* Line 353 of lalr1.java  */
/* Line 321 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Conditional(((Expression)(yystack.valueAt (5-(1)))), ((Expression)(yystack.valueAt (5-(3)))), ((Expression)(yystack.valueAt (5-(5))))); };
  break;
    

  case 77:
  if (yyn == 77)
    
/* Line 353 of lalr1.java  */
/* Line 323 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Or(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 78:
  if (yyn == 78)
    
/* Line 353 of lalr1.java  */
/* Line 325 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new And(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 79:
  if (yyn == 79)
    
/* Line 353 of lalr1.java  */
/* Line 327 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Eq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 80:
  if (yyn == 80)
    
/* Line 353 of lalr1.java  */
/* Line 329 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new NEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 81:
  if (yyn == 81)
    
/* Line 353 of lalr1.java  */
/* Line 331 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new GreaterThanOrEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 82:
  if (yyn == 82)
    
/* Line 353 of lalr1.java  */
/* Line 333 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new LessThanOrEq(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 83:
  if (yyn == 83)
    
/* Line 353 of lalr1.java  */
/* Line 335 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new GreaterThan(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 84:
  if (yyn == 84)
    
/* Line 353 of lalr1.java  */
/* Line 337 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new LessThan(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 85:
  if (yyn == 85)
    
/* Line 353 of lalr1.java  */
/* Line 339 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Plus(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 86:
  if (yyn == 86)
    
/* Line 353 of lalr1.java  */
/* Line 341 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Minus(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 87:
  if (yyn == 87)
    
/* Line 353 of lalr1.java  */
/* Line 343 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Times(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 88:
  if (yyn == 88)
    
/* Line 353 of lalr1.java  */
/* Line 345 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Div(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 89:
  if (yyn == 89)
    
/* Line 353 of lalr1.java  */
/* Line 347 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Dot(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 90:
  if (yyn == 90)
    
/* Line 353 of lalr1.java  */
/* Line 349 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Mod(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 91:
  if (yyn == 91)
    
/* Line 353 of lalr1.java  */
/* Line 351 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Implies(((Expression)(yystack.valueAt (3-(1)))), ((Expression)(yystack.valueAt (3-(3))))); };
  break;
    

  case 92:
  if (yyn == 92)
    
/* Line 353 of lalr1.java  */
/* Line 353 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new Not(((Expression)(yystack.valueAt (2-(2))))); };
  break;
    

  case 93:
  if (yyn == 93)
    
/* Line 353 of lalr1.java  */
/* Line 355 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new GetData(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 94:
  if (yyn == 94)
    
/* Line 353 of lalr1.java  */
/* Line 357 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IsActive(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 95:
  if (yyn == 95)
    
/* Line 353 of lalr1.java  */
/* Line 359 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IsEnabled(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 96:
  if (yyn == 96)
    
/* Line 353 of lalr1.java  */
/* Line 361 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IsLoaded(((Token)(yystack.valueAt (4-(3)))).getText()); };
  break;
    

  case 97:
  if (yyn == 97)
    
/* Line 353 of lalr1.java  */
/* Line 363 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IsSubstr(((Expression)(yystack.valueAt (6-(3)))), ((Expression)(yystack.valueAt (6-(5))))); };
  break;
    

  case 98:
  if (yyn == 98)
    
/* Line 353 of lalr1.java  */
/* Line 365 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = ((Expression)(yystack.valueAt (3-(2)))); };
  break;
    

  case 99:
  if (yyn == 99)
    
/* Line 353 of lalr1.java  */
/* Line 367 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IdentifierRef(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 100:
  if (yyn == 100)
    
/* Line 353 of lalr1.java  */
/* Line 369 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new IntLiteral(((java.lang.Long)(yystack.valueAt (1-(1))))); };
  break;
    

  case 101:
  if (yyn == 101)
    
/* Line 353 of lalr1.java  */
/* Line 371 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new RealLiteral(new java.lang.Double(((Token)(yystack.valueAt (1-(1)))).getText())); };
  break;
    

  case 102:
  if (yyn == 102)
    
/* Line 353 of lalr1.java  */
/* Line 373 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new StringLiteral(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 103:
  if (yyn == 103)
    
/* Line 353 of lalr1.java  */
/* Line 391 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = new java.lang.Long(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 104:
  if (yyn == 104)
    
/* Line 353 of lalr1.java  */
/* Line 392 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = - (new java.lang.Long(((Token)(yystack.valueAt (2-(2)))).getText())); };
  break;
    

  case 105:
  if (yyn == 105)
    
/* Line 353 of lalr1.java  */
/* Line 393 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */
    { yyval = java.lang.Long.parseLong(((Token)(yystack.valueAt (1-(1)))).getText().substring(2), 16); };
  break;
    



/* Line 353 of lalr1.java  */
/* Line 1272 of "E:\\PriorityFixing\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccFullParser.java"  */
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
  private static final short yypact_ninf_ = -136;
  private static final short yypact_[] =
  {
         1,   -38,    21,    27,   -11,  -136,    13,  -136,  -136,    -5,
    -136,    58,    19,    24,    20,  -136,  -136,  -136,  -136,  -136,
    -136,  -136,  -136,    33,    16,   -30,    41,    28,  -136,  -136,
    -136,   -34,   -30,  -136,  -136,  -136,  -136,  -136,     7,   -12,
      82,  -136,    25,    43,  -136,    60,  -136,    10,  -136,  -136,
      67,  -136,  -136,    46,   227,   227,  -136,   103,   -47,    87,
      47,    48,    53,    57,    63,  -136,  -136,  -136,  -136,  -136,
     227,    91,   227,   227,   182,  -136,   182,   -13,   131,    75,
    -136,  -136,  -136,    94,    96,   104,   117,   118,   227,    14,
    -136,    29,   182,   227,   227,   227,   227,   227,   227,   227,
     227,   227,   227,   227,   227,   227,   227,   227,   227,  -136,
    -136,  -136,  -136,   -45,   120,  -136,    72,    97,    98,    99,
     100,    92,  -136,    14,    14,    14,   194,   130,   238,   282,
      14,    14,    14,     6,     6,     6,  -136,  -136,  -136,  -136,
    -136,  -136,  -136,   -45,  -136,   -45,   155,  -136,  -136,  -136,
    -136,  -136,  -136,  -136,   227,   227,  -136,  -136,   -45,   163,
      74,   182,  -136,   271,   161,  -136,   227,   182,   227,   159,
     182,   227,  -136,   144,  -136,  -136,  -136,   227,   -16,   182,
     227,   114,  -136,   227,  -136
  };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
  {
         0,     0,     0,     0,     0,     1,     0,    20,     3,     0,
      22,     0,     2,     0,     0,    27,    28,    29,    30,    25,
      24,    23,    21,     0,     0,     0,     0,     0,     5,     8,
       9,     0,     0,    12,    31,    10,    11,     6,     0,     0,
      38,     7,    16,     0,    13,     0,    32,    41,    18,    19,
       0,    17,     4,     0,     0,     0,    39,    43,     0,     0,
       0,     0,     0,     0,     0,    99,   102,   103,   105,   101,
       0,     0,     0,    40,    74,   100,    42,     0,    48,     0,
      15,    34,    35,     0,     0,     0,     0,     0,     0,    92,
     104,     0,    75,     0,     0,     0,     0,     0,     0,     0,
       0,     0,     0,     0,     0,     0,     0,     0,     0,    47,
      45,    46,    44,     0,    52,    14,     0,     0,     0,     0,
       0,     0,    98,    80,    81,    82,    91,     0,    77,    78,
      79,    83,    84,    85,    86,    89,    87,    88,    90,    57,
      56,    59,    49,    54,    58,     0,    50,    36,    37,    33,
      93,    94,    95,    96,     0,     0,    55,    53,     0,    60,
       0,    76,    51,     0,    63,    97,   103,    61,     0,    72,
      62,    64,    65,    67,    73,    69,    66,     0,     0,    68,
       0,     0,    70,    71,    26
  };

  /* YYPGOTO[NTERM-NUM].  */
  private static final short yypgoto_[] =
  {
      -136,  -136,  -136,  -136,  -136,  -136,  -136,   152,  -136,  -136,
    -136,  -136,  -136,  -136,  -136,  -136,  -136,  -136,  -136,  -136,
    -136,  -136,  -136,  -136,  -136,  -136,  -136,  -136,  -136,  -135,
      49,  -136,  -136,  -136,    35,  -136,  -136,  -136,    36,   -55,
     -89
  };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final short
  yydefgoto_[] =
  {
        -1,     2,     3,     7,    14,    26,    33,    31,    39,    44,
      50,    51,    10,    12,    21,    22,    23,    40,    46,    83,
     149,    47,    56,    57,    78,   112,   114,   159,   146,   142,
     143,   164,   169,   171,   172,   178,   182,   175,    73,    74,
      75
  };

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final short yytable_ninf_ = -1;
  private static final short
  yytable_[] =
  {
        76,    42,   180,    79,     1,   139,   140,    67,    68,   141,
     157,   109,   110,   111,     4,    89,    35,    91,    92,    71,
      29,     5,    80,   162,   144,    15,    16,    17,    18,    54,
      36,    55,     6,   121,    30,    37,    48,    49,   123,   124,
     125,   126,   127,   128,   129,   130,   131,   132,   133,   134,
     135,   136,   137,   138,   144,   181,   144,    35,     8,    43,
      93,    94,    95,     9,    96,    11,    19,    20,    13,   144,
      25,    36,   106,   107,   108,    24,    41,   103,   104,   105,
     106,   107,   108,    27,    97,    28,    98,    99,   100,   101,
     102,    32,   103,   104,   105,   106,   107,   108,    34,   160,
     161,   147,   148,   122,    45,    93,    94,    95,   167,    96,
      53,   170,    52,   173,    81,    82,   173,    58,    59,    77,
      84,    85,   179,    93,    94,    95,    86,    96,    92,    97,
      87,    98,    99,   100,   101,   102,    88,   103,   104,   105,
     106,   107,   108,    90,   115,   113,   117,    97,   165,    98,
      99,   100,   101,   102,   118,   103,   104,   105,   106,   107,
     108,    93,    94,    95,   154,    96,   116,   119,   120,   145,
     158,   150,   151,   152,   153,    93,    94,    95,   177,    96,
     163,   168,   174,   184,    38,    97,   155,    98,    99,   100,
     101,   102,   156,   103,   104,   105,   106,   107,   108,    97,
       0,    98,    99,   100,   101,   102,   176,   103,   104,   105,
     106,   107,   108,    93,    94,    95,   183,    96,     0,     0,
       0,     0,     0,     0,     0,    93,    94,    95,     0,     0,
       0,     0,     0,     0,     0,     0,     0,    97,     0,    98,
      99,   100,   101,   102,     0,   103,   104,   105,   106,   107,
     108,    98,    99,   100,   101,   102,     0,   103,   104,   105,
     106,   107,   108,    60,    61,    62,    63,    64,     0,    93,
      94,    95,     0,     0,     0,     0,     0,    65,    66,    67,
      68,    69,     0,     0,     0,     0,     0,     0,     0,    70,
       0,    71,     0,     0,     0,     0,    99,   100,   101,   102,
      72,   103,   104,   105,   106,   107,   108,    60,    61,    62,
      63,    64,     0,    93,    94,    95,     0,     0,     0,     0,
       0,    65,    66,   166,    68,    69,     0,     0,     0,     0,
       0,     0,     0,    70,     0,    71,     0,     0,     0,     0,
       0,   100,   101,   102,    72,   103,   104,   105,   106,   107,
     108
  };

  /* YYCHECK.  */
  private static final short
  yycheck_[] =
  {
        55,    13,    18,    50,     3,    50,    51,    52,    53,    54,
     145,    24,    25,    26,    52,    70,    50,    72,    73,    64,
      50,     0,    69,   158,   113,     6,     7,     8,     9,    19,
      64,    21,     5,    88,    64,    69,    11,    12,    93,    94,
      95,    96,    97,    98,    99,   100,   101,   102,   103,   104,
     105,   106,   107,   108,   143,    71,   145,    50,    69,    71,
      31,    32,    33,    50,    35,    70,    47,    48,    10,   158,
      50,    64,    66,    67,    68,    51,    69,    63,    64,    65,
      66,    67,    68,    50,    55,    69,    57,    58,    59,    60,
      61,    50,    63,    64,    65,    66,    67,    68,    70,   154,
     155,    29,    30,    74,    22,    31,    32,    33,   163,    35,
      50,   166,    69,   168,    27,    28,   171,    50,    72,    16,
      73,    73,   177,    31,    32,    33,    73,    35,   183,    55,
      73,    57,    58,    59,    60,    61,    73,    63,    64,    65,
      66,    67,    68,    52,    69,    14,    50,    55,    74,    57,
      58,    59,    60,    61,    50,    63,    64,    65,    66,    67,
      68,    31,    32,    33,    72,    35,    72,    50,    50,    49,
      15,    74,    74,    74,    74,    31,    32,    33,    34,    35,
      17,    20,    23,    69,    32,    55,    56,    57,    58,    59,
      60,    61,   143,    63,    64,    65,    66,    67,    68,    55,
      -1,    57,    58,    59,    60,    61,   171,    63,    64,    65,
      66,    67,    68,    31,    32,    33,   180,    35,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    31,    32,    33,    -1,    -1,
      -1,    -1,    -1,    -1,    -1,    -1,    -1,    55,    -1,    57,
      58,    59,    60,    61,    -1,    63,    64,    65,    66,    67,
      68,    57,    58,    59,    60,    61,    -1,    63,    64,    65,
      66,    67,    68,    36,    37,    38,    39,    40,    -1,    31,
      32,    33,    -1,    -1,    -1,    -1,    -1,    50,    51,    52,
      53,    54,    -1,    -1,    -1,    -1,    -1,    -1,    -1,    62,
      -1,    64,    -1,    -1,    -1,    -1,    58,    59,    60,    61,
      73,    63,    64,    65,    66,    67,    68,    36,    37,    38,
      39,    40,    -1,    31,    32,    33,    -1,    -1,    -1,    -1,
      -1,    50,    51,    52,    53,    54,    -1,    -1,    -1,    -1,
      -1,    -1,    -1,    62,    -1,    64,    -1,    -1,    -1,    -1,
      -1,    59,    60,    61,    73,    63,    64,    65,    66,    67,
      68
  };

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
  yystos_[] =
  {
         0,     3,    76,    77,    52,     0,     5,    78,    69,    50,
      87,    70,    88,    10,    79,     6,     7,     8,     9,    47,
      48,    89,    90,    91,    51,    50,    80,    50,    69,    50,
      64,    82,    50,    81,    70,    50,    64,    69,    82,    83,
      92,    69,    13,    71,    84,    22,    93,    96,    11,    12,
      85,    86,    69,    50,    19,    21,    97,    98,    50,    72,
      36,    37,    38,    39,    40,    50,    51,    52,    53,    54,
      62,    64,    73,   113,   114,   115,   114,    16,    99,    50,
      69,    27,    28,    94,    73,    73,    73,    73,    73,   114,
      52,   114,   114,    31,    32,    33,    35,    55,    57,    58,
      59,    60,    61,    63,    64,    65,    66,    67,    68,    24,
      25,    26,   100,    14,   101,    69,    72,    50,    50,    50,
      50,   114,    74,   114,   114,   114,   114,   114,   114,   114,
     114,   114,   114,   114,   114,   114,   114,   114,   114,    50,
      51,    54,   104,   105,   115,    49,   103,    29,    30,    95,
      74,    74,    74,    74,    72,    56,   105,   104,    15,   102,
     114,   114,   104,    17,   106,    74,    52,   114,    20,   107,
     114,   108,   109,   114,    23,   112,   109,    34,   110,   114,
      18,    71,   111,   113,    69
  };

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
  yytoken_number_[] =
  {
         0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,   273,   274,
     275,   276,   277,   278,   279,   280,   281,   282,   283,   284,
     285,   286,   287,   288,   289,   290,   291,   292,   293,   294,
     295,   296,   297,   298,   299,   300,   301,   302,   303,   304,
     305,   306,   307,   308,   309,    63,    58,   124,    38,    61,
      62,    60,    33,    43,    45,    46,    42,    47,    37,    59,
     123,   125,    44,    40,    41
  };

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
  yyr1_[] =
  {
         0,    75,    76,    77,    78,    79,    80,    81,    82,    82,
      82,    82,    83,    83,    84,    84,    85,    85,    86,    86,
      87,    87,    88,    88,    89,    89,    90,    91,    91,    91,
      91,    92,    92,    93,    94,    94,    95,    95,    96,    96,
      97,    98,    98,    99,    99,   100,   100,   100,   101,   101,
     102,   102,   103,   103,   104,   104,   105,   105,   105,   105,
     106,   106,   106,   107,   107,   108,   108,   109,   109,   110,
     110,   111,   112,   112,   113,   113,   114,   114,   114,   114,
     114,   114,   114,   114,   114,   114,   114,   114,   114,   114,
     114,   114,   114,   114,   114,   114,   114,   114,   114,   114,
     114,   114,   114,   115,   115,   115
  };

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
  yyr2_[] =
  {
         0,     2,     4,     3,     9,     3,     3,     3,     1,     1,
       2,     2,     0,     2,     5,     4,     0,     1,     1,     1,
       0,     3,     0,     2,     1,     1,    16,     1,     1,     1,
       1,     0,     2,     6,     1,     1,     1,     1,     0,     2,
       2,     0,     2,     0,     2,     1,     1,     1,     0,     2,
       0,     2,     0,     2,     1,     2,     1,     1,     1,     1,
       0,     2,     3,     0,     2,     1,     2,     1,     3,     0,
       2,     2,     0,     1,     1,     2,     5,     3,     3,     3,
       3,     3,     3,     3,     3,     3,     3,     3,     3,     3,
       3,     3,     2,     4,     4,     4,     4,     6,     3,     1,
       1,     1,     1,     1,     2,     1
  };

  /* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname_[] =
  {
    "$end", "error", "$undefined", "VERSION", "COMMAND", "CONFIGURATION",
  "CDLPACKAGE", "COMPONENT", "INTERFACE", "OPTION", "DESCRIPTION",
  "HARDWARE", "TEMPLATE", "PACKAGE", "USERVALUE", "INFERREDVALUE",
  "FLAVOR", "DEFAULT", "REQUIRES", "ACTIVEIF", "LEGALVALUES", "CALCULATED",
  "IMPLEMENTEDBY", "NONEFLAVOR", "DATA", "BOOL", "BOOLDATA", "ACTIVE",
  "INACTIVE", "ENABLED", "DISABLED", "NEQ", "GEQ", "LEQ", "TO", "IMPLIES",
  "GETDATA", "ISACTIVE", "ISENABLED", "ISLOADED", "ISSUBSTR", "ISXSUBSTR",
  "VERSIONCMP", "CALCULATIONINDICATOR", "DEFAULTTEXT", "FOLLOWINDICATOR",
  "LINETEXT", "LEVELUP", "LEVELDOWN", "DERIVEDUSERVALUE", "ID", "STRING",
  "INT", "HEXINT", "REAL", "'?'", "':'", "'|'", "'&'", "'='", "'>'", "'<'",
  "'!'", "'+'", "'-'", "'.'", "'*'", "'/'", "'%'", "';'", "'{'", "'}'",
  "','", "'('", "')'", "$accept", "eccFile", "version", "toplevel",
  "description", "hardware", "template", "name", "packages", "package",
  "optional_source_specifier", "hardware_or_template", "contents",
  "level_changes", "level_change", "content", "header", "implemented_bys",
  "implemented_by", "active_state", "enabled_state", "active_ifs",
  "active_if", "calculated", "flavor", "flavor_type", "user_value",
  "inferred_value", "derived_user_value", "one_or_two_values",
  "single_value", "default_value", "legal_values", "legal_value_list",
  "legal_value", "requireses", "requires", "noneflavor", "exprs", "expr",
  "int", null
  };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
  {
        76,     0,    -1,    77,    78,    87,    88,    -1,     3,    52,
      69,    -1,     5,    50,    70,    79,    80,    81,    83,    71,
      69,    -1,    10,    51,    69,    -1,    50,    82,    69,    -1,
      50,    82,    69,    -1,    50,    -1,    64,    -1,    82,    50,
      -1,    82,    64,    -1,    -1,    83,    84,    -1,    13,    85,
      50,    50,    69,    -1,    13,    85,    50,    69,    -1,    -1,
      86,    -1,    11,    -1,    12,    -1,    -1,    87,    88,    90,
      -1,    -1,    88,    89,    -1,    48,    -1,    47,    -1,    91,
      50,    70,    92,    96,    98,    99,   101,   103,   102,   106,
     107,   112,   110,    71,    69,    -1,     6,    -1,     7,    -1,
       8,    -1,     9,    -1,    -1,    92,    93,    -1,    22,    50,
      72,    94,    72,    95,    -1,    27,    -1,    28,    -1,    29,
      -1,    30,    -1,    -1,    96,    97,    -1,    19,   113,    -1,
      -1,    21,   114,    -1,    -1,    16,   100,    -1,    25,    -1,
      26,    -1,    24,    -1,    -1,    14,   104,    -1,    -1,    15,
     104,    -1,    -1,    49,   104,    -1,   105,    -1,   105,   105,
      -1,    51,    -1,    50,    -1,   115,    -1,    54,    -1,    -1,
      17,   114,    -1,    17,    52,   114,    -1,    -1,    20,   108,
      -1,   109,    -1,   108,   109,    -1,   114,    -1,   114,    34,
     114,    -1,    -1,   110,   111,    -1,    18,   113,    -1,    -1,
      23,    -1,   114,    -1,   113,   114,    -1,   114,    55,   114,
      56,   114,    -1,   114,    57,   114,    -1,   114,    58,   114,
      -1,   114,    59,   114,    -1,   114,    31,   114,    -1,   114,
      32,   114,    -1,   114,    33,   114,    -1,   114,    60,   114,
      -1,   114,    61,   114,    -1,   114,    63,   114,    -1,   114,
      64,   114,    -1,   114,    66,   114,    -1,   114,    67,   114,
      -1,   114,    65,   114,    -1,   114,    68,   114,    -1,   114,
      35,   114,    -1,    62,   114,    -1,    36,    73,    50,    74,
      -1,    37,    73,    50,    74,    -1,    38,    73,    50,    74,
      -1,    39,    73,    50,    74,    -1,    40,    73,   114,    72,
     114,    74,    -1,    73,   114,    74,    -1,    50,    -1,   115,
      -1,    54,    -1,    51,    -1,    52,    -1,    64,    52,    -1,
      53,    -1
  };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final short yyprhs_[] =
  {
         0,     0,     3,     8,    12,    22,    26,    30,    34,    36,
      38,    41,    44,    45,    48,    54,    59,    60,    62,    64,
      66,    67,    71,    72,    75,    77,    79,    96,    98,   100,
     102,   104,   105,   108,   115,   117,   119,   121,   123,   124,
     127,   130,   131,   134,   135,   138,   140,   142,   144,   145,
     148,   149,   152,   153,   156,   158,   161,   163,   165,   167,
     169,   170,   173,   177,   178,   181,   183,   186,   188,   192,
     193,   196,   199,   200,   202,   204,   207,   213,   217,   221,
     225,   229,   233,   237,   241,   245,   249,   253,   257,   261,
     265,   269,   273,   276,   281,   286,   291,   296,   303,   307,
     309,   311,   313,   315,   317,   320
  };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final short yyrline_[] =
  {
         0,    99,    99,   104,   114,   115,   116,   117,   118,   118,
     118,   118,   119,   119,   120,   125,   130,   130,   131,   131,
     133,   133,   134,   134,   135,   147,   161,   216,   218,   220,
     222,   226,   227,   230,   232,   232,   233,   233,   236,   237,
     241,   245,   246,   250,   251,   253,   254,   255,   257,   258,
     259,   260,   261,   262,   264,   265,   272,   273,   274,   275,
     280,   281,   283,   289,   290,   293,   294,   296,   297,   302,
     303,   305,   309,   310,   314,   316,   320,   322,   324,   326,
     328,   330,   332,   334,   336,   338,   340,   342,   344,   346,
     348,   350,   352,   354,   356,   358,   360,   362,   364,   366,
     368,   370,   372,   391,   392,   393
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
       2,     2,     2,    62,     2,     2,     2,    68,    58,     2,
      73,    74,    66,    63,    72,    64,    65,    67,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,    56,    69,
      61,    59,    60,    55,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,    70,    57,    71,     2,     2,     2,     2,
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
      25,    26,    27,    28,    29,    30,    31,    32,    33,    34,
      35,    36,    37,    38,    39,    40,    41,    42,    43,    44,
      45,    46,    47,    48,    49,    50,    51,    52,    53,    54
  };

  private static final byte yytranslate_ (int t)
  {
    if (t >= 0 && t <= yyuser_token_number_max_)
      return yytranslate_table_[t];
    else
      return yyundef_token_;
  }

  private static final int yylast_ = 350;
  private static final int yynnts_ = 41;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 5;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 75;

  private static final int yyuser_token_number_max_ = 309;
  private static final int yyundef_token_ = 2;

/* User implementation code.  */
/* Unqualified %code blocks.  */

/* Line 875 of lalr1.java  */
/* Line 19 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */

  private List<PackageRef> _packageRefs = new LinkedList<PackageRef>();
  private LinkedList<EccNode> _nodes = new LinkedList<EccNode>();
  private Map<String, OptionValue> _values = new HashMap<String, OptionValue>();
  private Map<String, OptionValue> _derivedValues = new HashMap<String, OptionValue>();
  private EccNode _goDownNode = null;
  private EccNode _parentNode = null;
  private int _virtualLevel = 0;
  
  public EccFile getEccFile() {
    return new EccFile(_packageRefs, _values);
  }  

  public Map<String, OptionValue> getDerivedValues() {
    return _derivedValues;
  }  
  
  public Collection<EccNode> allNodes() { 
	return _nodes;
  }
  
  public Collection<CompileError> errors() {
	return ((EccFullLexer)yylexer).getAllErrors();
  }
  
  private boolean isInteger(String t) {
	for (int i = 0; i < t.length(); i++) {
		char c = t.charAt(i);
		if (c > '9' || c < '0') return false;
	}
	return true;
  }
  



/* Line 875 of lalr1.java  */
/* Line 2088 of "E:\\PriorityFixing\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccFullParser.java"  */

}


/* Line 879 of lalr1.java  */
/* Line 395 of "E:\\PriorityFixing\\src\\main\\eccFull.y"  */


