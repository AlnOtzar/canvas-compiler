package unam.aragon.mx;
import java_cup.runtime.*;
import java.util.LinkedList;

%%

%public
%class Analizador_Lexico
%cupsym Simbolos
%cup
%implements java_cup.runtime.Scanner
%line
%char
%column
%full
%type java_cup.runtime.Symbol

%{
    public static LinkedList<TError> TablaEL = new LinkedList<TError>();

    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
%}


%eof{
    return new java_cup.runtime.Symbol(sym.EOF);
%eof}

%%

// Palabras clave
"limpiar"           { return symbol(sym.Lpr, yytext()); }
"fondo"             { return symbol(sym.F, yytext()); }
"posicion"          { return symbol(sym.Ps, yytext()); }
"rectangulo"        { return symbol(sym.Rec, yytext()); }
"linea"             { return symbol(sym.Ln, yytext()); }
"circulo"           { return symbol(sym.Cir, yytext()); }

// Símbolos
","                 { return symbol(sym.COMA, yytext()); }
";"                 { return symbol(sym.PUNTOCOMA, yytext()); }
"("                 { return symbol(sym.PAR_ABRE, yytext()); }
")"                 { return symbol(sym.PAR_CIERRA, yytext()); }
"="                 { return symbol(sym.IGUAL, yytext()); }
"red"               { return symbol(sym.C, yytext()); }
"blue"              { return symbol(sym.C, yytext()); }
"green"             { return symbol(sym.C, yytext()); }

// Números
[0-9]+              { return symbol(sym.NUM, Integer.parseInt(yytext())); }

// Fin de línea
[\n]+               { /* Ignorar líneas vacías */ }

// Espacios
[ \t\r]+            { /* Ignorar espacios */ }

// Caracter no reconocido
.                   { System.err.println("Caracter no reconocido: " + yytext()); return null; }
