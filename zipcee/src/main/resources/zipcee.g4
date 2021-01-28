grammar zipcee;


@parser::header {

}

@parser::members{

}


program : variableDeclaration* functionDeclaration* functionDefinition*  ;

variableDeclaration : type IDENT SEMICOLON ;
functionDeclaration : type IDENT PAREN functionArguments THESIS SEMICOLON ;
functionDefinition : type IDENT PAREN functionArguments THESIS functionBody ;
functionArguments : (type IDENT COMMA)* ;
type : INT | CHARP ;

functionBody : statement ;
statement : LEFTBRACE statement* RIGHTBRACE               /* block statement */
                | (type)* IDENT ASSIGN expression SEMICOLON  /* assignment */
                | RETURN expression SEMICOLON
                | IF PAREN expression THESIS statement (ELSE statement)?
                | WHILE PAREN expression THESIS statement
                | expression SEMICOLON
    ;

expression
 : expression op=(INCR|DECR) // ++. --
// | expression POW<assoc=right> expression           //powExpr
 | MINUS expression                           // unaryMinusExpr
 | NOT expression                             // notExpr
 | expression op=(MULT | DIV | MODULO) expression      // multiplicationExpr
 | expression op=(PLUS | MINUS) expression          //additiveExpr
 | expression op=(LSHIFT | RSHIFT) expression //bitwise
 | expression op=(LE | GE | LT | GT) expression //relationalExpr
 | expression op=(EQUAL | NOTEQUAL) expression              //equalityExpr
 | expression AND expression                        //andExpr
 | expression OR expression                        //andExpr
 | expression XOR expression                        //andExpr
 | expression ANDAND expression                        //andExpr
 | expression OROR expression                         //orExpr
 | primaryExpression                                 //atomExpr
 ;

primaryExpression : NUMBER | IDENT | STRING | PAREN expression THESIS ;


RETURN: 'return';
IF: 'if';
ELSE: 'else';
WHILE: 'while';
INT: 'int';
SEMICOLON: ';';
PAREN: '(';
THESIS: ')';
LEFTBRACE: '{';
RIGHTBRACE: '}';
COMMA: ',';
DQUOTE: '"';

// Operators
MULT: '*';
DIV:'/';
MODULO: '%';

PLUS: '+';
INCR: '++';
MINUS: '-';
DECR:'--';
NOT: '!';

ASSIGN: '=';

EQUAL: '==';
NOTEQUAL: '!=';

AND: '&';
OR: '|';
XOR: '^';

ANDAND: '&&';
OROR: '||';

LSHIFT: '>>';
RSHIFT: '<<';

LT: '<';
GT: '>';
LE: '<=';
GE: '>=';

// Integers
STRING: '"' CHAR*? '"';
CHAR: [ a-zA-Z0-9];

NUMBER: [0-9][0-9]*;
CHARP: 'char' [ ]+ '*' ;

// Variable names
IDENT: [a-zA-Z0-9_]+;

LineComment
    :   '//' ~[\r\n]* 
        -> skip
    ;
// Ignore all white spaces
WS: [ \t\r\n]+ -> skip ;

