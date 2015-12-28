
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
/* Line 8 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */

  import java.io.*;
  import java.util.*;
  import scala.Option;
  import scala.Some;
  import scala.None$;



/* Line 33 of lalr1.java  */
/* Line 52 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccParser.java"  */

/**
 * A Bison parser, automatically generated from <tt>C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y</tt>.
 *
 * @author LALR (1) parser skeleton written by Paolo Bonzini.
 */
public class EccParser
{
    /** Version number for the Bison executable that generated this parser.  */
  public static final String bisonVersion = "2.4.1";

  /** Name of the skeleton that generated this parser.  */
  public static final String bisonSkeleton = "lalr1.java";


  /** True if verbose error messages are enabled.  */
  public boolean errorVerbose = false;


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
  public static final int ID = 271;
  /** Token number, to be returned by the scanner.  */
  public static final int STRING = 272;



  
  private Location yylloc (YYStack rhs, int n)
  {
    if (n > 0)
      return new Location (rhs.locationAt (1).begin, rhs.locationAt (n).end);
    else
      return new Location (rhs.locationAt (0).end);
  }

  /**
   * Communication interface between the scanner and the Bison-generated
   * parser <tt>EccParser</tt>.
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
  public EccParser (Lexer yylexer) {
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
	  case 30:
  if (yyn == 30)
    
/* Line 353 of lalr1.java  */
/* Line 66 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    {
	PackageRef pr = new PackageRef(((Token)(yystack.valueAt (5-(3)))).getText(), ((Token)(yystack.valueAt (5-(4)))).getText());
	_packageRefs.add(pr);
};
  break;
    

  case 37:
  if (yyn == 37)
    
/* Line 353 of lalr1.java  */
/* Line 75 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    {
	if (_values.containsKey(((Token)(yystack.valueAt (6-(2)))).getText()))
		yyerror(yystack.locationAt (6-(2)), "duplicated declaration.");
		
	_values.put(((Token)(yystack.valueAt (6-(2)))).getText(), ((OptionValue)(yystack.valueAt (6-(4)))));
};
  break;
    

  case 42:
  if (yyn == 42)
    
/* Line 353 of lalr1.java  */
/* Line 82 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { yyval = NoneOptionValue$.MODULE$; };
  break;
    

  case 43:
  if (yyn == 43)
    
/* Line 353 of lalr1.java  */
/* Line 83 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { yyval = ((OptionValue)(yystack.valueAt (1-(1)))); };
  break;
    

  case 44:
  if (yyn == 44)
    
/* Line 353 of lalr1.java  */
/* Line 85 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    {
	if (!((OptionValue)(yystack.valueAt (2-(1)))).equals(((OptionValue)(yystack.valueAt (2-(2))))))
		yyerror(yystack.locationAt (2-(2)), "the two values of the configuration are not the same.");
		
	yyval = ((OptionValue)(yystack.valueAt (2-(1))));

};
  break;
    

  case 45:
  if (yyn == 45)
    
/* Line 353 of lalr1.java  */
/* Line 92 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { yyval = ((SingleOptionValue)(yystack.valueAt (2-(2)))); };
  break;
    

  case 46:
  if (yyn == 46)
    
/* Line 353 of lalr1.java  */
/* Line 94 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { 
	if (!(((SingleOptionValue)(yystack.valueAt (3-(2)))) instanceof IntOptionValue)) {
		yyerror(yystack.locationAt (3-(2)), "the first value of booldata is not integer");
	}
	yyval = new DoubleOptionValue((IntOptionValue)((SingleOptionValue)(yystack.valueAt (3-(2)))), ((SingleOptionValue)(yystack.valueAt (3-(3))))); 
};
  break;
    

  case 49:
  if (yyn == 49)
    
/* Line 353 of lalr1.java  */
/* Line 101 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { yyval = new StringOptionValue(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    

  case 50:
  if (yyn == 50)
    
/* Line 353 of lalr1.java  */
/* Line 102 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */
    { if (isInteger(((Token)(yystack.valueAt (1-(1)))).getText())) yyval = new IntOptionValue(new Integer(((Token)(yystack.valueAt (1-(1)))).getText()));
		else yyval = new StringOptionValue(((Token)(yystack.valueAt (1-(1)))).getText()); };
  break;
    



/* Line 353 of lalr1.java  */
/* Line 495 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccParser.java"  */
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
  private static final byte yypact_ninf_ = -22;
  private static final byte yypact_[] =
  {
        24,    15,    32,   -22,    16,   -22,     7,   -22,    -3,    17,
     -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,
      19,    20,     1,   -22,    25,   -22,   -22,   -22,   -22,   -22,
      21,     4,    23,    30,    26,   -22,   -22,   -22,   -22,   -22,
     -22,   -22,    28,   -22,    29,    27,    36,    11,   -22,   -22,
      31,    34,   -22,   -22,   -22,    22,    11,     6,   -22,    33,
       8,    35,   -22,   -22,   -22,     6,   -22,    37,    38,   -22,
     -22,   -22,    18,    39,   -22,   -22,   -22,   -22,    41,    42,
     -22
  };

  /* YYDEFACT[S] -- default rule to reduce with in state S when YYTABLE
     doesn't specify something else to do.  Zero means the default is an
     error.  */
  private static final byte yydefact_[] =
  {
         0,     0,     0,     4,     0,     1,     0,     3,     0,     0,
       5,    35,     7,     8,    10,    11,    12,    13,    14,     9,
       0,     0,     2,    15,     0,    38,    39,    40,    41,    36,
       0,     0,     0,     0,     0,    18,    19,    20,    21,    22,
      23,    17,     0,    16,     0,     0,     0,    42,     6,    25,
       0,     0,    28,    47,    48,     0,    43,     0,    26,     0,
       0,     0,    44,    50,    49,    45,    27,    31,     0,    29,
      37,    46,     0,     0,    24,    33,    34,    32,     0,     0,
      30
  };

  /* YYPGOTO[NTERM-NUM].  */
  private static final byte yypgoto_[] =
  {
       -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,
     -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,   -22,
     -20,   -22,   -21
  };

  /* YYDEFGOTO[NTERM-NUM].  */
  private static final byte
  yydefgoto_[] =
  {
        -1,     2,     3,     6,    10,    20,    31,    43,    11,    33,
      46,    52,    60,    69,    73,    77,    22,    29,    30,    55,
      56,    57,    65
  };

  /* YYTABLE[YYPACT[STATE-NUM]].  What to do in state STATE-NUM.  If
     positive, shift that token.  If negative, reduce the rule which
     number is the opposite.  If zero, do what YYDEFACT says.  */
  private static final byte yytable_ninf_ = -1;
  private static final byte
  yytable_[] =
  {
        12,    13,    14,    15,    16,    17,    18,    25,    26,    27,
      28,     8,     9,    19,    35,    36,    37,    38,    39,    40,
      41,    67,    63,    64,    42,    53,    54,     1,    68,    75,
      76,     4,     5,    21,     7,    32,    62,    34,    23,    24,
      44,    45,    61,    50,    71,    47,    48,    49,    51,    58,
      59,    66,     0,    70,     0,    78,    74,    79,    72,     0,
      80
  };

  /* YYCHECK.  */
  private static final byte
  yycheck_[] =
  {
         3,     4,     5,     6,     7,     8,     9,     6,     7,     8,
       9,     4,     5,    16,    10,    11,    12,    13,    14,    15,
      16,    13,    16,    17,    20,    14,    15,     3,    20,    11,
      12,    16,     0,    16,    18,    10,    56,    16,    19,    19,
      17,    11,    20,    16,    65,    19,    18,    18,    12,    18,
      16,    18,    -1,    18,    -1,    16,    18,    16,    21,    -1,
      18
  };

  /* STOS_[STATE-NUM] -- The (internal number of the) accessing
     symbol of state STATE-NUM.  */
  private static final byte
  yystos_[] =
  {
         0,     3,    23,    24,    16,     0,    25,    18,     4,     5,
      26,    30,     3,     4,     5,     6,     7,     8,     9,    16,
      27,    16,    38,    19,    19,     6,     7,     8,     9,    39,
      40,    28,    10,    31,    16,    10,    11,    12,    13,    14,
      15,    16,    20,    29,    17,    11,    32,    19,    18,    18,
      16,    12,    33,    14,    15,    41,    42,    43,    18,    16,
      34,    20,    42,    16,    17,    44,    18,    13,    20,    35,
      18,    44,    21,    36,    18,    11,    12,    37,    16,    16,
      18
  };

  /* TOKEN_NUMBER_[YYLEX-NUM] -- Internal symbol number corresponding
     to YYLEX-NUM.  */
  private static final short
  yytoken_number_[] =
  {
         0,   256,   257,   258,   259,   260,   261,   262,   263,   264,
     265,   266,   267,   268,   269,   270,   271,   272,    59,   123,
     125,    45
  };

  /* YYR1[YYN] -- Symbol number of symbol that rule YYN derives.  */
  private static final byte
  yyr1_[] =
  {
         0,    22,    23,    24,    25,    25,    26,    27,    27,    27,
      27,    27,    27,    27,    27,    28,    28,    29,    29,    29,
      29,    29,    29,    29,    30,    31,    32,    33,    34,    34,
      35,    36,    36,    37,    37,    38,    38,    39,    40,    40,
      40,    40,    41,    41,    41,    42,    42,    43,    43,    44,
      44
  };

  /* YYR2[YYN] -- Number of symbols composing right hand side of rule YYN.  */
  private static final byte
  yyr2_[] =
  {
         0,     2,     4,     3,     0,     2,     6,     1,     1,     1,
       1,     1,     1,     1,     1,     0,     2,     1,     1,     1,
       1,     1,     1,     1,     9,     3,     3,     3,     0,     2,
       5,     0,     2,     1,     1,     0,     2,     6,     1,     1,
       1,     1,     0,     1,     2,     2,     3,     1,     1,     1,
       1
  };

  /* YYTNAME[SYMBOL-NUM] -- String name of the symbol SYMBOL-NUM.
     First, the terminals, then, starting at \a yyntokens_, nonterminals.  */
  private static final String yytname_[] =
  {
    "$end", "error", "$undefined", "VERSION", "COMMAND", "CONFIGURATION",
  "CDLPACKAGE", "COMPONENT", "INTERFACE", "OPTION", "DESCRIPTION",
  "HARDWARE", "TEMPLATE", "PACKAGE", "USERVALUE", "INFERREDVALUE", "ID",
  "STRING", "';'", "'{'", "'}'", "'-'", "$accept", "eccFile", "version",
  "commands", "command", "type", "arg_list", "arg", "toplevel",
  "description", "hardware", "template", "packages", "package",
  "optional_source_specifier", "hardware_or_template", "contents",
  "content", "header", "optional_value", "prefixed_value", "prefix",
  "single_value", null
  };

  /* YYRHS -- A `-1'-separated list of the rules' RHS.  */
  private static final byte yyrhs_[] =
  {
        23,     0,    -1,    24,    25,    30,    38,    -1,     3,    16,
      18,    -1,    -1,    25,    26,    -1,     4,    27,    19,    28,
      20,    18,    -1,     3,    -1,     4,    -1,    16,    -1,     5,
      -1,     6,    -1,     7,    -1,     8,    -1,     9,    -1,    -1,
      28,    29,    -1,    16,    -1,    10,    -1,    11,    -1,    12,
      -1,    13,    -1,    14,    -1,    15,    -1,     5,    16,    19,
      31,    32,    33,    34,    20,    18,    -1,    10,    17,    18,
      -1,    11,    16,    18,    -1,    12,    16,    18,    -1,    -1,
      34,    35,    -1,    13,    36,    16,    16,    18,    -1,    -1,
      21,    37,    -1,    11,    -1,    12,    -1,    -1,    38,    39,
      -1,    40,    16,    19,    41,    20,    18,    -1,     6,    -1,
       7,    -1,     8,    -1,     9,    -1,    -1,    42,    -1,    42,
      42,    -1,    43,    44,    -1,    43,    44,    44,    -1,    14,
      -1,    15,    -1,    17,    -1,    16,    -1
  };

  /* YYPRHS[YYN] -- Index of the first RHS symbol of rule number YYN in
     YYRHS.  */
  private static final short yyprhs_[] =
  {
         0,     0,     3,     8,    12,    13,    16,    23,    25,    27,
      29,    31,    33,    35,    37,    39,    40,    43,    45,    47,
      49,    51,    53,    55,    57,    67,    71,    75,    79,    80,
      83,    89,    90,    93,    95,    97,    98,   101,   108,   110,
     112,   114,   116,   117,   119,   122,   125,   129,   131,   133,
     135
  };

  /* YYRLINE[YYN] -- Source line where rule number YYN was defined.  */
  private static final byte yyrline_[] =
  {
         0,    50,    50,    52,    54,    54,    55,    56,    56,    56,
      56,    56,    56,    56,    56,    57,    57,    58,    58,    58,
      58,    58,    58,    58,    60,    61,    62,    63,    64,    64,
      65,    70,    70,    71,    71,    73,    73,    74,    81,    81,
      81,    81,    82,    83,    84,    92,    93,   100,   100,   101,
     102
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
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,    21,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,    18,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,     2,     2,     2,     2,     2,     2,     2,
       2,     2,     2,    19,     2,    20,     2,     2,     2,     2,
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
      15,    16,    17
  };

  private static final byte yytranslate_ (int t)
  {
    if (t >= 0 && t <= yyuser_token_number_max_)
      return yytranslate_table_[t];
    else
      return yyundef_token_;
  }

  private static final int yylast_ = 60;
  private static final int yynnts_ = 23;
  private static final int yyempty_ = -2;
  private static final int yyfinal_ = 5;
  private static final int yyterror_ = 1;
  private static final int yyerrcode_ = 256;
  private static final int yyntokens_ = 22;

  private static final int yyuser_token_number_max_ = 272;
  private static final int yyundef_token_ = 2;

/* User implementation code.  */
/* Unqualified %code blocks.  */

/* Line 875 of lalr1.java  */
/* Line 17 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */

  private List<PackageRef> _packageRefs = new LinkedList<PackageRef>();
  private Map<String, OptionValue> _values = new HashMap<String, OptionValue>();
  
  public EccFile parsedFile() {
    return new EccFile(_packageRefs, _values);
  }  
  
  public Collection<CompileError> errors() {
	return ((EccLexer)yylexer)._allErrors;
  }
  
  private boolean isInteger(String t) {
	for (int i = 0; i < t.length(); i++) {
		char c = t.charAt(i);
		if (c > '9' || c < '0') return false;
	}
	return true;
  }
  



/* Line 875 of lalr1.java  */
/* Line 1147 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\generated\\ca\\uwaterloo\\gsd\\rangeFix\\EccParser.java"  */

}


/* Line 879 of lalr1.java  */
/* Line 104 of "C:\\Users\\FlyingHero\\Documents\\Projects\\2011\\RangeFix3\\src\\main\\ecc.y"  */


