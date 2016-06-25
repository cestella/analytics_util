// parser rules
grammar Date;

date : day_of_week? WS? day separator month separator year .*? EOF
     | day_of_week? WS? year separator month separator day .*? EOF
     | day_of_week? WS? month_digit separator day separator year .*? EOF
     | day_of_week? WS? month_text separator? day separator? separator year .*? EOF
     | day_of_week? WS? day separator month_text separator? separator year .*? EOF
     ;

month : month_text | month_digit;
month_text : JAN WS? PUNCT?| FEB WS? PUNCT? | MAR WS? PUNCT?| APR WS? PUNCT? 
           | MAY WS? PUNCT?| JUN WS? PUNCT? | JUL WS? PUNCT? | AUG WS? PUNCT?
           | SEP WS? PUNCT? | OCT WS? PUNCT? | NOV WS? PUNCT? | DEC WS? PUNCT?;
month_digit : DIGIT DIGIT | DIGIT;



year : DIGIT DIGIT DIGIT DIGIT | DIGIT DIGIT;
day : DIGIT DIGIT | DIGIT;

separator : CALSEP | PERIOD | WS | PUNCT;
day_of_week : SUN WS? PUNCT? | MON WS? PUNCT? | TUE WS? PUNCT? | WED WS? PUNCT?
            | THU WS? PUNCT? | FRI WS? PUNCT?
            ;

CALSEP : [/\\\-] ;

SUN : [Ss][Uu][Nn]PERIOD?
    | [Ss][Uu][Nn][Dd][Aa][Yy]
    ;
MON : [Mm][Oo][Nn]PERIOD?
    | [Mm][Oo][Nn][Dd][Aa][Yy]
    ;
TUE : [Tt][Uu][Ee]PERIOD?
    | [Tt][Uu][Ee][Ss][Dd][Aa][Yy]
    ;
WED : [Ww][Ee][Dd]PERIOD?
    | [Ww][Ee][Dd][Nn][Ee][Ss][Dd][Aa][Yy]
    ;
THU : [Tt][Hh][Uu]PERIOD? | [Tt][Hh][Uu][Rr][Ss][Dd][Aa][Yy]
    ;
FRI : [Ff][Rr][Ii]PERIOD? | [Ff][Rr][Ii][Dd][Aa][Yy];

JAN : [Jj][Aa][Nn]PERIOD?
    | [Jj][Aa][Nn][Uu][Aa][Rr][Yy];
FEB : [Ff][Ee][Bb]PERIOD?
    | [Ff][Ee][Bb][Rr][Uu][Aa][Rr][Yy] 
    | [Ff][Ee][Bb][Uu][Aa][Rr][Yy] ;
MAR : [Mm][Aa][Rr]PERIOD?
    | [Mm][Aa][Rr][Cc][Hh]
    ;
APR : [Aa][Pp][Rr]PERIOD?
    | [Aa][Pp][Rr][Ii][Ll]
    ;
MAY : [Mm][Aa][Yy] ; 
JUN : [Jj][Uu][Nn]PERIOD?
    | [Jj][Uu][Nn][Ee] 
    ;
JUL : [Jj][Uu][Ll]PERIOD?
    |[Jj][Uu][Ll][Yy]
    ;
AUG : [Aa][Uu][Gg]PERIOD?
    | [Aa][Uu][Gg][Uu][Ss][Tt] 
    ;
SEP : [Ss][Ee][Pp]PERIOD?
    | [Ss][Ee][Pp][Tt][Ee][Mm][Bb][Ee][Rr] 
    ; 
OCT : [Oo][Cc][Tt]PERIOD?
    | [Oo][Cc][Tt][Oo][Bb][Ee][Rr]
    ; 
NOV : [Nn][Oo][Vv]PERIOD?
    | [Nn][Oo][Vv][Ee][Mm][Bb][Ee][Rr]
    ;
DEC : [Dd][Ee][Cc]PERIOD?
    | [Dd][Ee][Cc][Ee][Mm][Bb][Ee][Rr]
    ;
PUNCT : ',';
PERIOD : '.';
WS : [ \t]*;

DIGIT : [0-9];
REST : .;
