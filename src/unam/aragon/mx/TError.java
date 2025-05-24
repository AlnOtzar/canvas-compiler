package unam.aragon.mx;

public class TError {
    public String lexema;
    public int fila;
    public int columna;
    public String tipo;
    public String descripcion;

    public TError(String lexema, int fila, int columna, String tipo, String descripcion) {
        this.lexema = lexema;
        this.fila = fila;
        this.columna = columna;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        String posFila = (fila >= 0) ? Integer.toString(fila) : "desconocida";
        String posColumna = (columna >= 0) ? Integer.toString(columna) : "desconocida";
        return "[" + tipo + "] en (" + posFila + ", " + posColumna + "): " + descripcion;
    }
}


