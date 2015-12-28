%define package "ca.uwaterloo.gsd.rangeFix"
%define parser_class_name "AnnotationParser"
%define public
%file-prefix "AnnotationParser"
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
}

      
%define stype "Object"
%token <Token> DEFAULT REQUIRES ACTIVEIF LEGALVALUES CALCULATED NEQ GEQ LEQ TO IMPLIES GETDATA ISACTIVE ISENABLED ISLOADED ISSUBSTR ISXSUBSTR VERSIONCMP TOINT TOSTRING TOBOOL TYPE INTTYPE STRINGTYPE ENUMTYPE BOOLTYPE IN REPLACING
%token <Token> ID STRING INT HEXINT REAL

%type <List> inner_anntations
%type <NodeAnnotation> inner_anntation
%type <TypeAnnotation> type_annotation
%type <List> literal_list
%type <Literal> literal
%type <Type> type
%type <Expression> expr
%type <NodeAnnotation> default_value
%type <NodeAnnotation> calculated
%type <NodeAnnotation> active_if
%type <NodeAnnotation> requires
%type <NodeAnnotation> legal_values
%type <List> legal_value_list
%type <Range> legal_value
%type <java.lang.Long> int
%type <Option> optional_legal_value_list
%type <Option> optional_old_expr
%type <Set> applicable_files
%type <Set> files



%right '?' ':'
%left IMPLIES
%left '|'
%left '&'
%left '=' NEQ GEQ LEQ '>' '<'
%right '!'
%left '+' '-' '.'
%left '*' '/' '%'

%%

node_annotations : node_annotation | node_annotations node_annotation

node_annotation : ID {_curID = $1.getText(); _activeIfCounter = 0; _reqIfCounter = 0;} 
	'{' inner_anntations '}'
	{
		for(Object ann : $4) {
			_nodeAnnotations.add($1.getText(), (NodeAnnotation)ann);
		}
	}

inner_anntations : 
	inner_anntation 
	{ List result = new LinkedList(); if ($1 != null) result.add($1); $$ = result; }
	| inner_anntations inner_anntation
	{ if ($2 != null) $1.add($2); }

inner_anntation :
	type_annotation
	{ _typeAnnotations.add($1); $$ = null; }
	| default_value
	{ $$ = $1; }
	| requires
	{ $$ = $1; }
	| active_if
	{ $$ = $1; }
	| calculated
	{ $$ = $1; }
	| legal_values
	{ $$ = $1; }
	
type_annotation :
	TYPE applicable_files type
	{$$ = new TypeAnnotation(_curID, $3, JavaConversions.asScalaSet($2)); }
	
applicable_files : { $$ = new HashSet<String>();}
	| IN '(' files ')' { $$ = $3;}
files : STRING { Set<String> result = new HashSet<String>(); result.add($1.getText()); $$ = result; }
	| files ',' STRING { $1.add($3.getText()); $$ = $1; }

type : INTTYPE
	{ $$ = NumberType$.MODULE$; }
	| STRINGTYPE
	{ $$ = StringType$.MODULE$; }
	| BOOLTYPE
	{ $$ = BoolType$.MODULE$; }
//	| REALTYPE
//	{ $$ = RealType$.MODULE$; }
	| ENUMTYPE '[' literal_list ']'
	{ $$ = new EnumType(JavaConversions.asScalaBuffer($3).toSet()); }
	
literal_list : 
	literal 
	{ List result = new LinkedList(); result.add($1); $$ = result; }
	| 
	literal_list ',' literal
	{ $1.add($3); $$ = $1; }
	
active_if : 
	ACTIVEIF '[' expr ']' optional_old_expr applicable_files
	{ $$ = new ActiveIfAnnotation($3, _activeIfCounter, $5, JavaConversions.asScalaSet($6)); _activeIfCounter++; }
	| ACTIVEIF '(' int ')' '[' expr ']' optional_old_expr applicable_files
	{ $$ = new ActiveIfAnnotation($6, $3.intValue(), $8, JavaConversions.asScalaSet($9)); _activeIfCounter = $3.intValue(); }

optional_old_expr : { $$ = None$.MODULE$; }
	| REPLACING '[' expr ']' { $$ = new Some($3); }

calculated : 
	CALCULATED '[' expr ']' optional_old_expr applicable_files
	{ $$ = new CalculatedAnnotation($3, $5, JavaConversions.asScalaSet($6)); }

default_value : 
	DEFAULT '[' expr ']' optional_old_expr applicable_files 
	{ $$ = new DefaultAnnotation($3, $5, JavaConversions.asScalaSet($6)); }

legal_values :
	LEGALVALUES '[' legal_value_list ']' optional_legal_value_list applicable_files
	{ 
		$$ = new LegalValuesAnnotation(
			new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer($3)),
			$5,
			JavaConversions.asScalaSet($6)); 
	}
optional_legal_value_list : { $$ = None$.MODULE$; }
	| REPLACING '[' legal_value_list ']' { $$ = new Some(new LegalValuesOption(scala.collection.JavaConversions.asScalaBuffer($3))); }
legal_value_list :
	legal_value { List result = new LinkedList(); result.add($1); $$ = result; }
	| legal_value_list ',' legal_value 
	{ $1.add($3); $$ = $1; }
legal_value : expr { $$ = new SingleValueRange($1);}
	| expr TO expr
	{ $$ = new MinMaxRange($1, $3);}
	
requires : 	REQUIRES '[' expr ']' optional_old_expr applicable_files
	{ $$ = new ReqAnnotation($3, _reqIfCounter, $5, JavaConversions.asScalaSet($6)); _reqIfCounter++; }
	| REQUIRES '(' int ')' '[' expr ']' optional_old_expr applicable_files
	{ $$ = new ReqAnnotation($6, $3.intValue(), $8, JavaConversions.asScalaSet($9)); _reqIfCounter = $3.intValue(); }

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
	| TOINT '(' expr ')'
	{ $$ = new ToInt($3); }
	| TOSTRING '(' expr ')'
	{ $$ = new ToString($3); }
	| TOBOOL '(' expr ')'
	{ $$ = new ToBool($3); }
	| '(' expr ')'
	{ $$ = $2; }
	| ID
	{ $$ = new IdentifierRef($1.getText()); }
	| literal
	{ $$ = $1; }
literal :
	int
	{ $$ = new IntLiteral($1); }
	| REAL
	{ $$ = new RealLiteral(new java.lang.Double($1.getText())); }
	| STRING
	{ $$ = new StringLiteral($1.getText()); }
int : INT { $$ = new java.lang.Long($1.getText()); }
	| '-' INT { $$ = - (new java.lang.Long($2.getText())); }
	| HEXINT { $$ = java.lang.Long.parseLong($1.getText().substring(2), 16); }
		
%%
