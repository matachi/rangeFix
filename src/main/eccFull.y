%define package "ca.uwaterloo.gsd.rangeFix"
%define parser_class_name "EccFullParser"
%define public
%file-prefix "EccFullParser"
%locations
%error-verbose

 
%code imports{
  import java.io.*;
  import java.util.*;
  import scala.collection.JavaConversions;
  import scala.Option;
  import scala.Some;
  import scala.None$;
}

%code
{
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
  
}

      
%define stype "Object"
%token <Token> VERSION COMMAND CONFIGURATION CDLPACKAGE COMPONENT INTERFACE OPTION DESCRIPTION HARDWARE TEMPLATE PACKAGE USERVALUE INFERREDVALUE FLAVOR DEFAULT REQUIRES ACTIVEIF LEGALVALUES CALCULATED IMPLEMENTEDBY NONEFLAVOR DATA BOOL BOOLDATA ACTIVE INACTIVE ENABLED DISABLED NEQ GEQ LEQ TO IMPLIES GETDATA ISACTIVE ISENABLED ISLOADED ISSUBSTR ISXSUBSTR VERSIONCMP CALCULATIONINDICATOR DEFAULTTEXT FOLLOWINDICATOR LINETEXT LEVELUP LEVELDOWN DERIVEDUSERVALUE
%token <Token> ID STRING INT HEXINT REAL

%type <OptionValue> user_value
%type <OptionValue> inferred_value
%type <OptionValue> derived_user_value
%type <SingleOptionValue> single_value
%type <OptionValue> one_or_two_values

%type <CdlType$.Value> header
%type <List> implemented_bys
%type <String> implemented_by
%type <List> active_ifs
%type <Expression> active_if
%type <Option> calculated
%type <Flavor$.Value> flavor
%type <Flavor$.Value> flavor_type
%type <Option> default_value
%type <List> requireses
%type <Expression> requires
%type <Expression> expr
%type <java.lang.Boolean> noneflavor
//%type <List> arg_list
//%type <List> extra_args
//%type <Expression> arg
%type <Option> legal_values
%type <List> legal_value_list
%type <Range> legal_value
%type <Expression> exprs
%type <java.lang.Long> int
//%type <Token> fun_name

%right '?' ':'
%left IMPLIES
%left '|'
%left '&'
%left '=' NEQ GEQ LEQ '>' '<'
%right '!'
%left '+' '-' '.'
%left '*' '/' '%'

%%

eccFile :  version  /* commands */ toplevel contents level_changes
{
	NodeHelper$.MODULE$.fillImplements(JavaConversions.asScalaBuffer(_nodes));
}

version : VERSION INT ';'

/*
commands : | commands command
command : COMMAND type '{' carg_list '}' ';' 
type : VERSION | COMMAND | ID | CONFIGURATION | CDLPACKAGE | COMPONENT | INTERFACE | OPTION 
carg_list : | carg_list carg
carg : ID | DESCRIPTION | HARDWARE | TEMPLATE | PACKAGE | USERVALUE | INFERREDVALUE
*/

toplevel : CONFIGURATION ID '{'  description  hardware template packages '}' ';'
description : DESCRIPTION STRING ';' 
hardware : ID name ';' 
template : ID name ';' 
name : ID | '-' | name ID | name '-'
packages : | packages package
package : PACKAGE optional_source_specifier ID ID ';' 
	{
		PackageRef pr = new PackageRef($3.getText(), $4.getText());
		_packageRefs.add(pr);
	}
	| PACKAGE optional_source_specifier ID ';' 
	{
		PackageRef pr = new PackageRef($3.getText(), "");
		_packageRefs.add(pr);
	}
optional_source_specifier : | hardware_or_template
hardware_or_template : HARDWARE | TEMPLATE

contents : | contents level_changes content
level_changes : | level_changes level_change
level_change : LEVELDOWN
	{
		if (_nodes.isEmpty()) {
			_virtualLevel++;
		}
		else if(_goDownNode == null) {
			yyerror(@1, "no node to go down");
		}
		else {
			_parentNode = _goDownNode;
		}
	}
	| LEVELUP
	{
		if (_parentNode == null) {
			if (_virtualLevel == 0) yyerror(@1, "go up from the top level");
			else _virtualLevel--;
		}
		else {
			if (_parentNode.getParent().equals(None$.MODULE$))
				_parentNode = null;
			else
				_parentNode = _parentNode.getParent().get();
		}
		_goDownNode = null;
	}
content : header //1
	ID 	//2
	'{'  //3
	implemented_bys //4
	active_ifs 	//5
	calculated  //6
	flavor 	//7
	user_value //8
	derived_user_value //9
	inferred_value //10
	default_value //11
	legal_values //12
	noneflavor //13
	requireses '}' ';' 
{
	if (_values.containsKey($2.getText()) || _derivedValues.containsKey($2.getText()))
		yyerror(@2, "duplicated declaration.");
		
	if ($13 && !($7.equals(Flavor.None())))
		yyerror(@13, "Feature with flavor has \"no associated value\".");

	if ($8 != NoneOptionValue$.MODULE$ && $9 != NoneOptionValue$.MODULE$)
	
		yyerror(@8, "Two user values are provided.");

	if ($8 != NoneOptionValue$.MODULE$ && $10 != NoneOptionValue$.MODULE$ && !$8.equals($10))
_values.put($2.getText(), $8);		
//yyerror(@8, "the two values of the configuration are not the same.");

	if ($8 != NoneOptionValue$.MODULE$)
		_values.put($2.getText(), $8);
	else
		_values.put($2.getText(), $10);
	if ($10 != NoneOptionValue$.MODULE$)
		_derivedValues.put($2.getText(), NoneOptionValue$.MODULE$);
	else
		_derivedValues.put($2.getText(), $9);

	EccNode n = new EccNode($2.getText(), //id
					$1, // cdlType
					$7, // flavor
					$11, // default_value
					$6, // calcualted
					$12, // legal_values
					scala.collection.JavaConversions.asScalaBuffer($14), //requires
					scala.collection.JavaConversions.asScalaBuffer($5), //active_if
					scala.collection.JavaConversions.asScalaBuffer($4) // implemented_bys
					);

	if (_parentNode == null) _nodes.add(n);
	else _parentNode.addChild(n);
	_goDownNode = n;

}

header : CDLPACKAGE 
	{ $$ = CdlType.Package(); }
	| COMPONENT 
	{ $$ = CdlType.Component(); }
	| INTERFACE 
	{ $$ = CdlType.Interface(); }
	| OPTION
	{ $$ = CdlType.Option(); }

implemented_bys : 
	{ $$ = new LinkedList(); }
	| implemented_bys implemented_by
	{ $1.add($2); $$ = $1; }
implemented_by : 
	IMPLEMENTEDBY ID ',' active_state ',' enabled_state
	{ $$ = $2.getText(); }
active_state : ACTIVE | INACTIVE
enabled_state : ENABLED | DISABLED

active_ifs : 
	{ $$ = new LinkedList(); }
	| active_ifs active_if
	{ $1.add($2); $$ = $1; }

active_if : 
	ACTIVEIF exprs
	{ $$ = $2; }

calculated : 
	{ $$ = None$.MODULE$; }
	| CALCULATED expr
	{ $$ = new Some($2); }

flavor : 
	{ $$ = Flavor.None(); }
	| FLAVOR flavor_type
	{ $$ = $2; }
flavor_type : BOOL { $$ = Flavor.Bool(); }
	| BOOLDATA { $$ = Flavor.BoolData(); }
	| DATA { $$ = Flavor.Data(); }	

user_value : { $$ = NoneOptionValue$.MODULE$; }
| USERVALUE one_or_two_values { $$ = $2; }
inferred_value : { $$ = NoneOptionValue$.MODULE$; }
| INFERREDVALUE one_or_two_values { $$ = $2; }
derived_user_value : { $$ = NoneOptionValue$.MODULE$; }
| DERIVEDUSERVALUE one_or_two_values { $$ = $2; }

one_or_two_values : single_value  { $$ = $1; }
| single_value single_value  
{ 
	if (!($1 instanceof IntOptionValue)) {
		yyerror(@1, "the first value of booldata is not integer");
	}
	$$ = new DoubleOptionValue((IntOptionValue)$1, $2); 
}	
single_value : STRING { $$ = new StringOptionValue($1.getText()); }
| ID  { $$ = new StringOptionValue($1.getText()); }
| int { $$ = new IntOptionValue($1); }
| REAL	{ $$ = new RealOptionValue(new java.lang.Double($1.getText())); }



default_value : 
	{ $$ = None$.MODULE$; }
	| DEFAULT expr
	{ $$ = new Some($2); }
	| DEFAULT INT expr
	{ $$ = new Some($3); }
	
	

legal_values :
	{ $$ = None$.MODULE$; }
	| LEGALVALUES legal_value_list
	{ $$ = new Some(new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer($2))); }
legal_value_list :
	legal_value { List result = new LinkedList(); result.add($1); $$ = result; }
	| legal_value_list legal_value 
	{ $1.add($2); $$ = $1; }
legal_value : expr { $$ = new SingleValueRange($1);}
	| expr TO expr
	{ $$ = new MinMaxRange($1, $3);}
	
	
requireses : 
	{ $$ = new LinkedList(); }
	| requireses requires
	{ $1.add($2); $$ = $1; }
requires : 	REQUIRES exprs
	{ $$ = $2; }

noneflavor : 
	{ $$ = false; }
	| NONEFLAVOR
	{ $$ = true; }
	
exprs : 
	expr
	{ $$ = $1; }
	| exprs expr 
	{ $$ = new And($1, $2); }
	

expr : expr '?' expr ':' expr
	{ $$ = new Conditional($1, $3, $5); }
	| expr '|' expr
	{ $$ = new Or($1, $3); }
	| expr '&' expr
	{ $$ = new And($1, $3); }
	| expr '=' expr
	{ $$ = new Eq($1, $3); }
	| expr NEQ expr
	{ $$ = new NEq($1, $3); }
	| expr GEQ expr
	{ $$ = new GreaterThanOrEq($1, $3); }
	| expr LEQ expr
	{ $$ = new LessThanOrEq($1, $3); }
	| expr '>' expr
	{ $$ = new GreaterThan($1, $3); }
	| expr '<' expr
	{ $$ = new LessThan($1, $3); }
	| expr '+' expr
	{ $$ = new Plus($1, $3); }
	| expr '-' expr
	{ $$ = new Minus($1, $3); }
	| expr '*' expr
	{ $$ = new Times($1, $3); }
	| expr '/' expr
	{ $$ = new Div($1, $3); }
	| expr '.' expr
	{ $$ = new Dot($1, $3); }
	| expr '%' expr
	{ $$ = new Mod($1, $3); }
	| expr IMPLIES expr
	{ $$ = new Implies($1, $3); }
	| '!' expr
	{ $$ = new Not($2); }
	| GETDATA '(' ID ')'
	{ $$ = new GetData($3.getText()); }
	| ISACTIVE '(' ID ')'
	{ $$ = new IsActive($3.getText()); }
	| ISENABLED '(' ID ')'
	{ $$ = new IsEnabled($3.getText()); }
	| ISLOADED '(' ID ')'
	{ $$ = new IsLoaded($3.getText()); }
	| ISSUBSTR '(' expr ',' expr ')'
	{ $$ = new IsSubstr($3, $5); }
	| '(' expr ')'
	{ $$ = $2; }
	| ID
	{ $$ = new IdentifierRef($1.getText()); }
	| int
	{ $$ = new IntLiteral($1); }
	| REAL
	{ $$ = new RealLiteral(new java.lang.Double($1.getText())); }
	| STRING
	{ $$ = new StringLiteral($1.getText()); }
//arg_list : 
//	{ $$ = new LinkedList(); }
//	| extra_args arg
//	{ $1.add($2); $$ = $1; }
//extra_args : 
//	{ $$ = new LinkedList(); }
//	| extra_args arg ','
//	{ $1.add($2); $$ = $1; }
//arg : expr
//	{ $$ = $1; }
//fun_name : GETDATA { $$ = $1; } 
//	| ISACTIVE { $$ = $1; } 
//	| ISENABLED { $$ = $1; } 
//	| ISLOADED { $$ = $1; } 
//	| ISSUBSTR { $$ = $1; } 
//	| ISXSUBSTR { $$ = $1; } 
//	| VERSIONCMP { $$ = $1; } 
int : INT { $$ = new java.lang.Long($1.getText()); }
	| '-' INT { $$ = - (new java.lang.Long($2.getText())); }
	| HEXINT { $$ = java.lang.Long.parseLong($1.getText().substring(2), 16); }
		
%%
