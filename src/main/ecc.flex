package ca.uwaterloo.gsd.rangeFix;
import java.util.*;

%%

%class EccLexer
%public
%implements EccParser.Lexer
%byaccj
%line
%column
%unicode

%{
  
  private StringBuffer _string = new StringBuffer();
  
  private Token _lastToken; 
  
  private int createToken(int type) {
    return createToken(type, yytext());
  }
    
  private int createToken(int type, String text) {
    Token t = new Token(type, text, new Location(getStartPos(), getEndPos()));
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
    public void yyerror(EccParser.Location loc, String s) {
        yyerror(new Location(loc), s);
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

LINETERMINATOR = \r|\n|\r\n
IDENTIFIER = [^ {};\-\"\#\t\n\r]+
COMMENT = (#[^\n\r]*) 

%state STRING

%%


<YYINITIAL> {

    /* keywords */
    "cdl_savefile_version"   { return createToken(EccParser.VERSION); } 
    "cdl_savefile_command"    { return createToken(EccParser.COMMAND);}
    "cdl_configuration"    { return createToken(EccParser.CONFIGURATION);}  
    "cdl_component"    { return createToken(EccParser.COMPONENT);}  
    "cdl_option"    { return createToken(EccParser.OPTION);}  
    "cdl_interface"    { return createToken(EccParser.INTERFACE);}  
    "cdl_package"    { return createToken(EccParser.CDLPACKAGE);}  
    "description"    { return createToken(EccParser.DESCRIPTION);}  
    "hardware"     { return createToken(EccParser.HARDWARE);}
    "template" { return createToken(EccParser.TEMPLATE);}
    "package" { return createToken(EccParser.PACKAGE);}
	"user_value" { return createToken(EccParser.USERVALUE);}
	"inferred_value" { return createToken(EccParser.INFERREDVALUE);}
    
    "{" | 
    "}" |   
    ";" |
	"-" { return createToken((int) yycharat(0)); }

    "\""                             { yybegin(STRING); _string.setLength(0); }
	
	{COMMENT}	{}
    
    {IDENTIFIER} {  return createToken(EccParser.ID); }

    /* whitespace */
    [ \t\n\r]+ { }
    
}
   
<STRING> {
  "\""                             { yybegin(YYINITIAL); return createToken(EccParser.STRING, _string.toString());}
  
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