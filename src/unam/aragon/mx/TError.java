package unam.aragon.mx;

public class TError {
    String lexema;
    int line;
    int column;
    String tipo;
    String descripcion;

    public TError(String lexema, int line, int column, String tipo, String descripcion) {
        this.lexema = lexema;
        this.line = line;
        this.column = column;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

}
