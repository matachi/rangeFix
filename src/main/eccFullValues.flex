package ca.uwaterloo.gsd.rangeFix;
import java.util.*;

%%

%class EccFullValuesLexer
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

STRING = [^ \"\#\t\n\r]+
INT = \-?[0-9]+
REAL = [0-9]+(\.[0-9]+)?([eE][\+\-]?[0-9]+)?
HEXINT = 0[xX][0-9a-fA-F]+

%state STRING 

%%


<YYINITIAL> {


	{HEXINT} { return createToken(EccFullParser.HEXINT); }
    {INT} {  return createToken(EccFullParser.INT); }
	{REAL} { return createToken(EccFullParser.REAL); }
    {STRING} {  return createToken(EccFullParser.STRING); }

    "\""                             { yybegin(STRING); _string.setLength(0); _stringBegin = getStartPos();}

    /* whitespace */
    [ \t\r\n]+ { }
    
}

<STRING> {
  "\""                     { yybegin(YYINITIAL); return createToken(EccFullParser.STRING, _string.toString(), _stringBegin, getEndPos());}
  
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