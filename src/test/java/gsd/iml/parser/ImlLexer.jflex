package gsd.iml.parser;

import java_cup.runtime.*;

%%

%class ImlLexer
%public
%unicode
%line
%column
%cup
//%debug

%{
  StringBuffer buffer = new StringBuffer();

  private Symbol symbol(int type) {
    return new Symbol(type, yyline+1, yycolumn+1);
  }

  private Symbol symbol(int type, Object value) {
    return new Symbol(type, yyline+1, yycolumn+1, value);
  }

  /**
   * Assumes correct representation of a long value for
   * specified radix in scanner buffer from <code>start</code>
   * to <code>end</code>.
   */
  private Long parseLong(String value, int radix) {
    long result = 0;
    long digit;

    int start = 0 ;
    int end = value.length() ;

    for (int i = start ; i < end; i++) {
      digit  = Character.digit(value.charAt(i),radix);
      result*= radix;
      result+= digit;
    }

    return new Long(result);
  }
%}

LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]

/* Comments */
Comment = {TraditionalComment} | {EndOfLineComment} 

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/* Identifiers */
Identifier = [:jletter:][:jletterdigit:]*

/* Integer literals */
DecLongLiteral  =  0 | [1-9][0-9]*
DecLongLiteralE =  0 | [1-9][0-9]* ([eE] [+]? [0-9]+)

HexLongLiteral    =  0 [xX] 0* [0-9a-fA-F]+

OctLongLiteral    = 0+ {OctDigit}+
OctDigit          = [0-7]

    
/* Double floating point literals */

DoubleLiteral = ({FLit1}|{FLit2}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]* 
FLit2    = \. [0-9]+ 
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter = [^\r\n\"\\]

%eofval{
    return new Symbol(Token.EOF);
%eofval}

%state STRING

%%  
<YYINITIAL> {

  "flavor"                       { return symbol(Token.FLAVOR) ; }
  "display"                      { return symbol(Token.DISPLAY); }
  "description"                  { return symbol(Token.DESCRIPTION) ; }
  "option"                       { return symbol(Token.OPTION) ; }
  "component"                    { return symbol(Token.COMPONENT) ; }
  "package"                      { return symbol(Token.PACKAGE) ; }
  "interface"                    { return symbol(Token.INTERFACE) ; }
  "none"                         { return symbol(Token.NONE) ; }
  "data"                         { return symbol(Token.DATA) ; }
  "bool"                         { return symbol(Token.BOOL) ; }
  "booldata"                     { return symbol(Token.BOOLDATA) ; }
  "default_value"                { return symbol(Token.DEFAULT_VALUE) ; }
  "active_if"                    { return symbol(Token.ACTIVE_IF) ; }
  "is_enabled"                   { return symbol(Token.IS_ENABLED) ; }
  "is_loaded"                    { return symbol(Token.IS_LOADED) ; }
  "requires"                     { return symbol(Token.REQUIRES) ; }
  "calculated"                   { return symbol(Token.CALCULATED) ; }
  "implements"                   { return symbol(Token.IMPLEMENTS) ; }
  "legal_values"                 { return symbol(Token.LEGAL_VALUES) ; }
  "to"                           { return symbol(Token.TO) ; }
  
  /* operators */
  ">"                            { return symbol(Token.GT); }
  "<"                            { return symbol(Token.LT); }
  "!"                            { return symbol(Token.NOT); }
  "?"                            { return symbol(Token.QUESTION); }
  ":"                            { return symbol(Token.COLON); }
  "=="                           { return symbol(Token.EQ); }
  "<="                           { return symbol(Token.LTEQ); }
  ">="                           { return symbol(Token.GTEQ); }
  "!="                           { return symbol(Token.NEQ); }
  "&&"                           { return symbol(Token.AND); }
  "||"                           { return symbol(Token.OR); }
  "implies"                      { return symbol(Token.IMPLIES) ; }
  "+"                            { return symbol(Token.PLUS); }
  "-"                            { return symbol(Token.MINUS); }
  "*"                            { return symbol(Token.TIMES); }
  "/"                            { return symbol(Token.DIVIDE); }
  "&"                            { return symbol(Token.BT_AND); }
  "|"                            { return symbol(Token.BT_OR); }
  "^"                            { return symbol(Token.BT_XOR); }
  "%"                            { return symbol(Token.MOD); }
  "<<"                           { return symbol(Token.BT_LEFT); }
  ">>"                           { return symbol(Token.BT_RIGHT); }
  "--"                           { return symbol(Token.MINUS_MINUS) ; }

  /* separators */
  "("                            { return symbol(Token.LPAR); }
  ")"                            { return symbol(Token.RPAR); }
  "{"                            { return symbol(Token.LBRACE); }
  "}"                            { return symbol(Token.RBRACE); }
  "["                            { return symbol(Token.LBRACK); }
  "]"                            { return symbol(Token.RBRACK); }
  ","                            { return symbol(Token.COMMA); }
  "."                            { return symbol(Token.DOT); }
  
  /* pre-defined IML functions */
  "is_substr"                    { return symbol(Token.IS_SUBSTRING) ; }
  "is_active"                    { return symbol(Token.IS_ACTIVE) ; }

  /* string literal */
  \"                             { yybegin(STRING); buffer = new StringBuffer() ; }

  /* numeric literals */
  
  {DecLongLiteral}               { return symbol(Token.LONG_LITERAL, parseLong(yytext(), 10)); }

  {DecLongLiteralE}              { return symbol(Token.LONG_LITERAL, (long) Double.parseDouble(yytext())); }
  
  {HexLongLiteral}               { return symbol(Token.LONG_LITERAL, parseLong(yytext().substring(2, yytext().length()), 16)) ; }
 
  {OctLongLiteral}               { return symbol(Token.LONG_LITERAL, parseLong(yytext().substring(1, yytext().length()), 8)) ; }
  
  {DoubleLiteral}                {
                                   System.err.println(ImlParseMessage.warning
                                        ("Double value found: " + yytext(), yyline+1, yycolumn+1)) ;
                                   return symbol(Token.DOUBLE_FLOATING_POINT_LITERAL, new Double(yytext())) ;
                                 }
  
  /* comments */
  {Comment}                      { /* ignore */ }

  /* whitespace */
  {WhiteSpace}                   { /* ignore */ }

  /* identifiers */ 
  {Identifier}                   { return symbol(Token.ID, yytext()); }
}

<STRING> {
  \"                             { yybegin(YYINITIAL); return symbol(Token.STRING_LITERAL, buffer.toString()); }
  
  {StringCharacter}+             { buffer.append( yytext() ); }
  
  /* escape sequences */
  "\\b"                          { buffer.append( '\b' ); }
  "\\t"                          { buffer.append( '\t' ); }
  "\\n"                          { buffer.append( '\n' ); }
  "\\f"                          { buffer.append( '\f' ); }
  "\\r"                          { buffer.append( '\r' ); }
  "\\\""                         { buffer.append( '\"' ); }
  "\\'"                          { buffer.append( '\'' ); }
  "\\\\"                         { buffer.append( '\\' ); }
  \\[0-3]?{OctDigit}?{OctDigit}  { char val = (char) Integer.parseInt(yytext().substring(1),8);
                        				   buffer.append( val ); }
  
  /* error cases */
  \\.                            { throw new RuntimeException(ImlParseMessage.error("Illegal escape sequence \""+yytext()+"\"", yyline+1, yycolumn+1)); }
  {LineTerminator}               { throw new RuntimeException(ImlParseMessage.error("Unterminated string at end of line", yyline+1, yycolumn+1)); }
}

// Error fallback
.| \n                           { throw new RuntimeException(ImlParseMessage.error("Unrecognized character \"" + yytext() + "\"", yyline+1, yycolumn+1)); }
			  
