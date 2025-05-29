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

// reglas
// Palabras clave
"limpiar"                { return symbol(sym.Lpr, yytext()); }
"fondo"                  { return symbol(sym.F, yytext()); }
"posicion"               { return symbol(sym.Ps, yytext()); }
"rectangulo"             { return symbol(sym.Rec, yytext()); }
"linea"                  { return symbol(sym.Ln, yytext()); }
"circulo"                { return symbol(sym.Cir, yytext()); }

// mover
"mover"                  { return symbol(sym.Mv, yytext()); }
"arriba"                 { return symbol(sym.Arriba, yytext()); }
"abajo"                  { return symbol(sym.Abajo, yytext()); }
"derecha"                { return symbol(sym.Derecha, yytext()); }
"izquierda"              { return symbol(sym.Izquierda, yytext()); }

// while
"mientras"               { return symbol(sym.While, yytext()); }
"++"                     { return symbol(sym.INCREMENTO, yytext()); }


// Símbolos
","                 { return symbol(sym.COMA, yytext()); }
";"                 { return symbol(sym.PUNTOCOMA, yytext()); }
"("                 { return symbol(sym.PAR_ABRE, yytext()); }
")"                 { return symbol(sym.PAR_CIERRA, yytext()); }
"="                 { return symbol(sym.IGUAL, yytext()); }
"{"                 { return symbol(sym.LLAVE_ABRE, yytext()); }
"}"                 { return symbol(sym.LLAVE_CIERRA, yytext()); }

// colores
"red"               { return symbol(sym.C, yytext()); }
"blue"              { return symbol(sym.C, yytext()); }
"green"             { return symbol(sym.C, yytext()); }
"yellow"             { return symbol(sym.C, yytext()); }
"white"             { return symbol(sym.C, yytext()); }
"orange"             { return symbol(sym.C, yytext()); }

// id
[a-zA-Z][a-zA-Z0-9_]*    { return symbol(sym.ID, yytext()); } // para q identifique los nombres d figuras


// Números
[0-9]+              { return symbol(sym.NUM, Integer.parseInt(yytext())); }

// Operadores de comparación PARA WHILE
"<"                      { return symbol(sym.MENOR_QUE, yytext()); }

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
