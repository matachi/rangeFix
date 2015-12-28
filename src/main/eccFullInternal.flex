package ca.uwaterloo.gsd.rangeFix;
import java.util.*;

%%

%class EccFullInternalLexer
%public
%implements IEccFullInternalLexer
%byaccj
%line
%column
%unicode

%{
  
  private StringBuffer _string = new StringBuffer();
  
  private Token _lastToken; 
  
  private int _lastPseudoCommentLine;
  
  private Position _stringBegin;

  private int createToken(int type) {
    return createToken(type, yytext());
  }

  private int createToken(int type, String text) {
    Token t = new Token(type, text, new Location(getStartPos(), getEndPos()));
    _lastToken = t;
    return type;
  }

  private int createToken(int type, String text, Position begin, Position end) {
    Token t = new Token(type, text, new Location(begin, end));
    _lastToken = t;
    return type;
  }

	@Override
    public Object getLVal() {
        return _lastToken;
    }
    
    @Override
    public Position getStartPos() {
        return new Position(yyline + 1, yycolumn + 1);
    }
    
    @Override
    public Position getEndPos() {
        return new Position(yyline + 1, yycolumn + yylength());
    }
    
    @Override
    public void yyerror(EccFullParser.Location loc, String s) {
        yyerror(new Location(loc), s);
    }
	
	public void setLocation(int line, int column){
		yyline = line;
		yycolumn = column;
	}

    
    public void yyerror(Location loc, String s) {
        if (_reportError)
            System.err.println(String.format("%1$d:%2$d-%3$d:%4$d:%5$s", 
                loc.getBegin().getLine(), loc.getBegin().getColumn(),
                loc.getEnd().getLine(), loc.getEnd().getColumn(),
                s));
        _allErrors.add(new CompileError(loc, s));
            
    }
    
    
    private boolean _reportError;
    public void setReportError(boolean report){
        _reportError = report;
    }
    
    Collection<CompileError> _allErrors = new LinkedList<CompileError>();
    
  
  
%}

EOL = \r|\n|\r\n
//IDENTIFIER = [^ {};\-\"\#\t\n\r]+
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
INT = [0-9]+
REAL = [0-9]+(\.[0-9]+)?([eE][\+\-]?{INT})?
HEXINT = 0[xX][0-9a-fA-F]+

%state STRING COMMENT LINERETURN

%%


<YYINITIAL> {

    /* keywords */
    "cdl_savefile_version"   { return createToken(EccFullParser.VERSION); } 
    "cdl_savefile_command"    { yybegin(COMMENT);/*return createToken(EccFullParser.COMMAND);*/}
    "cdl_configuration"    { return createToken(EccFullParser.CONFIGURATION);}  
    "cdl_component"    { return createToken(EccFullParser.COMPONENT);}  
    "cdl_option"    { return createToken(EccFullParser.OPTION);}  
    "cdl_interface"    { return createToken(EccFullParser.INTERFACE);}  
    "cdl_package"    { return createToken(EccFullParser.CDLPACKAGE);}  
    "description"    { return createToken(EccFullParser.DESCRIPTION);}  
    "-hardware"     { return createToken(EccFullParser.HARDWARE);}
    "-template" { return createToken(EccFullParser.TEMPLATE);}
    "get_data" { return createToken(EccFullParser.GETDATA);}
    "is_active" { return createToken(EccFullParser.ISACTIVE);}
    "is_enabled" { return createToken(EccFullParser.ISENABLED);}
    "is_loaded" { return createToken(EccFullParser.ISLOADED);}
    "is_substr" { return createToken(EccFullParser.ISSUBSTR);}
    "is_xsubstr" { return createToken(EccFullParser.ISXSUBSTR);}
    "version_cmp" { return createToken(EccFullParser.VERSIONCMP);}
    "package" { return createToken(EccFullParser.PACKAGE);}
	"user_value" { yybegin(LINERETURN);return createToken(EccFullParser.USERVALUE);}
	"inferred_value" { yybegin(LINERETURN);return createToken(EccFullParser.INFERREDVALUE);}
	"data" { return createToken(EccFullParser.DATA);}
	"bool" { return createToken(EccFullParser.BOOL);}
	"booldata" { return createToken(EccFullParser.BOOLDATA);}
	"inactive" { return createToken(EccFullParser.INACTIVE);}
	"active" { return createToken(EccFullParser.ACTIVE);}
	"enabled" { return createToken(EccFullParser.ENABLED);}
	"disabled" { return createToken(EccFullParser.DISABLED);}
	"to" { return createToken(EccFullParser.TO);}
	"implies" { return createToken(EccFullParser.IMPLIES); }
	"# user_value" { yybegin(LINERETURN);return createToken(EccFullParser.DERIVEDUSERVALUE);}
	"# Flavor:" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.FLAVOR);}
	"# Default value:" { _lastPseudoCommentLine = yyline; yybegin(LINERETURN); return createToken(EccFullParser.DEFAULT);}
	"# Requires:" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.REQUIRES);}
	"# ActiveIf constraint:" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.ACTIVEIF);}
	"# Legal values:" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.LEGALVALUES);}
	"# Calculated value:" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.CALCULATED);}
	"# Implemented by" { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.IMPLEMENTEDBY);}
	"# There is no associated value." { _lastPseudoCommentLine = yyline; return createToken(EccFullParser.NONEFLAVOR);}
    \#[ \t]{5}[ \t]+
	{ 
		if (_lastPseudoCommentLine != yyline - 1) yybegin(COMMENT); 
		else {
			_lastPseudoCommentLine = yyline;
			return EccFullParser.FOLLOWINDICATOR;
		}
		
	} 
	"#   -->" {yybegin(COMMENT); return createToken(EccFullParser.CALCULATIONINDICATOR); }
	"# >"{EOL} {return createToken(EccFullParser.LEVELDOWN); }
	"# <"{EOL} { return createToken(EccFullParser.LEVELUP); }
	
    "{" | 
    "}" |   
    ";" |
	"-" | 
	"(" |
	")" |
	"?" |
	":" |
	"||" |
	"&&" |
	"==" |
	"!" |
	"+" |
	"-" |
	"*" |
	"/" |
	"." |
	"," |
	">" |
	"<" 
	{ return createToken((int) yycharat(0)); }

	"!=" 	{ return createToken(EccFullParser.NEQ); }
	">=" 	{ return createToken(EccFullParser.GEQ); }
	"<=" 	{ return createToken(EccFullParser.LEQ); }
	
    "\""                             { yybegin(STRING); _string.setLength(0); _stringBegin = getStartPos();}
	
	"#"	{ yybegin(COMMENT); }

	{HEXINT} { return createToken(EccFullParser.HEXINT); }
    {INT} {  return createToken(EccFullParser.INT); }
    {IDENTIFIER} {  return createToken(EccFullParser.ID); }
	{REAL} { return createToken(EccFullParser.REAL); }

    /* whitespace */
    [ \t\r\n]+ { }
    
}

<COMMENT> {
	[^\n\r]* {}
	{EOL} { yybegin(YYINITIAL); }
}

<LINERETURN> {
	[^\n\r]* { return createToken(EccFullParser.LINETEXT); }
	{EOL} { yybegin(YYINITIAL); }
}
   
<STRING> {
  "\""                             { yybegin(YYINITIAL); return createToken(EccFullParser.STRING, _string.toString(), _stringBegin, getEndPos());}
  
  [^\n\r\"\\]+             { _string.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { _string.append( '\b' ); }
  "\\t"                          { _string.append( '\t' ); }
  "\\n"                          { _string.append( '\n' ); }
  "\\f"                          { _string.append( '\f' ); }
  "\\r"                          { _string.append( '\r' ); }
  "\\\""                         { _string.append( '\"' ); }
  "\\'"                          { _string.append( '\'' ); }
  "\\\\"                         { _string.append( '\\' ); }
  \\.                            { _string.append( yytext().substring(1) ); }
  
  /* error cases */
	[\n\r]               { yyerror(new Location(getStartPos(), getEndPos()), "Unterminated string at end of line"); return -1;}
}
 
/* error fallback */
[^]    { yyerror(new Location(getStartPos(), getEndPos()), "Error: unexpected character '"+yytext()+"'"); return -1; } 