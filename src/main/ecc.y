%define package "ca.uwaterloo.gsd.rangeFix"
%define parser_class_name "EccParser"
%define public
%file-prefix "EccParser"
%locations

 
%code imports{
  import java.io.*;
  import java.util.*;
  import scala.Option;
  import scala.Some;
  import scala.None$;
}

%code
{
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
  
}

      
%define stype "Object"
%token <Token> VERSION COMMAND CONFIGURATION CDLPACKAGE COMPONENT INTERFACE OPTION DESCRIPTION HARDWARE TEMPLATE PACKAGE USERVALUE INFERREDVALUE
%token <Token> ID STRING 

%type <OptionValue> optional_value
%type <SingleOptionValue> single_value
%type <OptionValue> prefixed_value

%%

eccFile : version commands toplevel contents

version : VERSION ID ';'

commands : | commands command
command : COMMAND type '{' arg_list '}' ';'
type : VERSION | COMMAND | ID | CONFIGURATION | CDLPACKAGE | COMPONENT | INTERFACE | OPTION 
arg_list : | arg_list arg
arg : ID | DESCRIPTION | HARDWARE | TEMPLATE | PACKAGE | USERVALUE | INFERREDVALUE

toplevel : CONFIGURATION ID '{' description hardware template packages '}' ';'
description : DESCRIPTION STRING ';'
hardware : HARDWARE ID ';'
template : TEMPLATE ID ';'
packages : | packages package
package : PACKAGE optional_source_specifier ID ID ';' 
{
	PackageRef pr = new PackageRef($3.getText(), $4.getText());
	_packageRefs.add(pr);
}
optional_source_specifier : | '-' hardware_or_template
hardware_or_template : HARDWARE | TEMPLATE

contents : | contents content
content : header ID '{' optional_value '}' ';'
{
	if (_values.containsKey($2.getText()))
		yyerror(@2, "duplicated declaration.");
		
	_values.put($2.getText(), $4);
}
header : CDLPACKAGE | COMPONENT | INTERFACE | OPTION
optional_value : { $$ = NoneOptionValue$.MODULE$; }
| prefixed_value { $$ = $1; }
| prefixed_value prefixed_value 
{
	if (!$1.equals($2))
		yyerror(@2, "the two values of the configuration are not the same.");
		
	$$ = $1;

}
prefixed_value : prefix single_value  { $$ = $2; }
| prefix single_value single_value  
{ 
	if (!($2 instanceof IntOptionValue)) {
		yyerror(@2, "the first value of booldata is not integer");
	}
	$$ = new DoubleOptionValue((IntOptionValue)$2, $3); 
}	
prefix : USERVALUE | INFERREDVALUE
single_value : STRING { $$ = new StringOptionValue($1.getText()); }
| ID { if (isInteger($1.getText())) $$ = new IntOptionValue(new Integer($1.getText()));
		else $$ = new StringOptionValue($1.getText()); }
%%
