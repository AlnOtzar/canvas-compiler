package unam.aragon.mx;

import java_cup.runtime.Symbol;
import java.util.LinkedList;

%%

%public
%class Analizador_Lexico
%cupsym sym
%cup
%line
%char
%column
%full
%type java_cup.runtime.Symbol

%{
    public static LinkedList<TError> TablaEL = new LinkedList<TError>();

    private Symbol symbol(int type, Object value) {
        // Guardamos línea en 'left' y columna en 'right'
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
"{"                 { return new Symbol(sym.LLAVE_ABRE); }
"}"                 { return new Symbol(sym.LLAVE_CIERRA); }
"red"               { return symbol(sym.C, yytext()); }
"blue"              { return symbol(sym.C, yytext()); }
"green"             { return symbol(sym.C, yytext()); }
"yellow"             { return symbol(sym.C, yytext()); }
"white"             { return symbol(sym.C, yytext()); }
"orange"             { return symbol(sym.C, yytext()); }

// Números
[0-9]+              { return symbol(sym.NUM, Integer.parseInt(yytext())); }

// Fin de línea
[\n]+               { /* Ignorar líneas vacías */ }

// Espacios y tabuladores
[ \t\r]+            { /* Ignorar espacios y tabs */ }

// Caracter no reconocido
.                   {
                        System.err.println("Caracter no reconocido: " + yytext() + " en línea " + (yyline+1) + " columna " + (yycolumn+1));
                        // Puedes agregar error a la tabla si quieres:
                        TablaEL.add(new TError(yytext(), yyline+1, yycolumn+1, "Error Léxico", "Caracter no reconocido"));
                        return null;
                    }
