package ca.uwaterloo.gsd.rangeFix;
import java.util.*;

%%

%class AnnotationLexer
%public
%implements AnnotationParser.Lexer
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
    public void yyerror(AnnotationParser.Location loc, String s) {
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
IDENTIFIER = [a-zA-Z_][a-zA-Z0-9_]*
INT = [0-9]+
REAL = [0-9]+(\.[0-9]+)?([eE][\+\-]?{INT})?
HEXINT = 0[xX][0-9a-fA-F]+

%state STRING COMMENT LINERETURN

%%


<YYINITIAL> {

    "default_value" { return createToken(AnnotationParser.DEFAULT);}
    "legal_values" { return createToken(AnnotationParser.LEGALVALUES);}
    "active_if" { return createToken(AnnotationParser.ACTIVEIF);}
    "requires" { return createToken(AnnotationParser.REQUIRES);}
    "calculated" { return createToken(AnnotationParser.CALCULATED);}
    "is_active" { return createToken(AnnotationParser.ISACTIVE);}
    "is_enabled" { return createToken(AnnotationParser.ISENABLED);}
    "is_loaded" { return createToken(AnnotationParser.ISLOADED);}
    "is_substr" { return createToken(AnnotationParser.ISSUBSTR);}
    "is_xsubstr" { return createToken(AnnotationParser.ISXSUBSTR);}
    "version_cmp" { return createToken(AnnotationParser.VERSIONCMP);}
    "type" { return createToken(AnnotationParser.TYPE);}
    "number" { return createToken(AnnotationParser.INTTYPE);}
//    "real" { return createToken(AnnotationParser.REALTYPE);}
    "string" { return createToken(AnnotationParser.STRINGTYPE);}
    "bool" { return createToken(AnnotationParser.BOOLTYPE);}
    "enum" { return createToken(AnnotationParser.ENUMTYPE);}
	"to" { return createToken(AnnotationParser.TO);}
	"implies" { return createToken(AnnotationParser.IMPLIES); }
	"toInt" { return createToken(AnnotationParser.TOINT); }
	"toBool" { return createToken(AnnotationParser.TOBOOL); }
	"toString" { return createToken(AnnotationParser.TOSTRING); }
	"in" { return createToken(AnnotationParser.IN); }
	"replacing" { return createToken(AnnotationParser.REPLACING); }
	
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
	"<" |
	"[" |
	"]" 
	{ return createToken((int) yycharat(0)); }

	"!=" 	{ return createToken(AnnotationParser.NEQ); }
	">=" 	{ return createToken(AnnotationParser.GEQ); }
	"<=" 	{ return createToken(AnnotationParser.LEQ); }
	
    "\""                             { yybegin(STRING); _string.setLength(0); _stringBegin = getStartPos();}
	
	"#"	{ yybegin(COMMENT); }

	{HEXINT} { return createToken(AnnotationParser.HEXINT); }
    {INT} {  return createToken(AnnotationParser.INT); }
    {IDENTIFIER} {  return createToken(AnnotationParser.ID); }
	{REAL} { return createToken(AnnotationParser.REAL); }

    /* whitespace */
    [ \t\r\n]+ { }
    
}

<COMMENT> {
	[^\n\r]* {}
	{EOL} { yybegin(YYINITIAL); }
}

  
<STRING> {
  "\""                             { yybegin(YYINITIAL); return createToken(AnnotationParser.STRING, _string.toString(), _stringBegin, getEndPos());}
  
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
  
  /* error cases */
  \\.                            { yyerror(new Location(getStartPos(), getEndPos()), "Illegal escape sequence \""+yytext()+"\""); return -1; }
  [\n\r]               { yyerror(new Location(getStartPos(), getEndPos()), "Unterminated string at end of line"); return -1;}
}
 
/* error fallback */
[^]    { yyerror(new Location(getStartPos(), getEndPos()), "Error: unexpected character '"+yytext()+"'"); return -1; } 