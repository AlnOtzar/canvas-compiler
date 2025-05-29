package unam.aragon.mx;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.Stack;



import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class CompiladorController {

    private GraphicsContext graficos;
    private Scene escena;
    private AnimationTimer tiempo;
    private ArrayList<String> comandos = new ArrayList<>();
    private ArrayList<Figura> figuras = new ArrayList<>();

    // coordenadas y dimensiones
    private int x = 0;
    private int y = 0;
    private int ancho = 0;
    private int alto = 0;

    // banderas
    boolean fondoActivo = false;
    boolean posicionPunto = false;
    boolean lineaActiva = false;
    boolean limpiar = false;
    boolean rectanguloActivo = false;
    boolean circuloActivo = false;

    // colores
    Color color = null;
    Color fondoFigura = null;
    Color colorFigura = null;

    // WHILE Y MOVIMIENTO
    //private Map<String, Integer> variables = new HashMap<>();
    private Stack<Integer> stackWhileStart = new Stack<>(); // Guarda índices de inicio de bucles
    private Stack<String[]> stackWhileConditions = new Stack<>(); // Guarda condiciones

    @FXML
    private Button btnEjecutar;

    @FXML
    private Button btnCompilar;

    @FXML
    private Canvas canvas;

    @FXML
    private TextArea txtCodigo;

    @FXML
    private TextArea txtMensajes;

    private final String archivoDestino = "codigo.txt";


    @FXML
    public void actionCompilar() {
        parser.TablaES.clear();
        ComandoGlobal.comandos.clear();
        ComandoGlobal.compiladoCorrectamente = true;
        txtMensajes.clear();
        this.guardarArchivo();

        try {
            Reader reader = new BufferedReader(new FileReader("src/codigoPrueba.txt"));

            Analizador_Lexico analizadorLexico = new Analizador_Lexico(reader);
            parser sintactico = new parser(analizadorLexico);

            sintactico.parse();

            if (!parser.TablaES.isEmpty()) {
                ComandoGlobal.compiladoCorrectamente = false; // errores encontrados
                txtMensajes.appendText("Errores sintácticos encontrados:\n");
                for (TError e : parser.TablaES) {
                    txtMensajes.appendText(e.toString() + "\n");
                }
                btnEjecutar.setDisable(true);
            } else {
                txtMensajes.appendText("Compilación terminada exitosamente. No se encontraron errores.\n");
                btnEjecutar.setDisable(false);
            }

        } catch (Exception e) {
            ComandoGlobal.compiladoCorrectamente = false;
            e.printStackTrace();
            txtMensajes.appendText("Error durante la compilación: " + e.getMessage() + "\n");
            btnEjecutar.setDisable(true);
        }
    }


    @FXML
    void actionEjecutar(ActionEvent event) {

        if (!ComandoGlobal.compiladoCorrectamente) {
            txtMensajes.appendText("No se puede ejecutar: primero debes compilar sin errores.\n");
            return;
        }

        if (tiempo != null) {
            tiempo.stop();
        }

        this.indiceComandoc = 0;
        this.iniciar();
        this.ciclo();
    }

    private void guardarArchivo() {
        String rutaArchivo = "src/codigoPrueba.txt";
        String contenido = txtCodigo.getText();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo))) {
            writer.write(contenido);
            System.out.println("Archivo guardado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        btnEjecutar.setDisable(true);
        txtCodigo.textProperty().addListener((obs, oldText, newText) -> {
            ComandoGlobal.compiladoCorrectamente = false;
            btnEjecutar.setDisable(true);
        });
    }


    public void setEscena(Scene escena) {
        this.escena = escena;
    }

//    FIGURA????______________________________________________________

    abstract class Figura {
        String id;
        Color color;

        Figura(String id, Color color) {
            this.id = id;
            this.color = color;
        }

        //        verifica si el id es igul
        boolean coincideId(String idBuscado) {
            return this.id.equalsIgnoreCase(idBuscado);
        }

        abstract void dibujar(GraphicsContext g);

        //        MOVER
        abstract void mover(int dx, int dy);

    }

    //    rectangulo (w, h, red) -> "int, int, c"
    class Rectangulo extends Figura {
        int x, y, ancho, alto;

        Rectangulo(String id, Color color, int x, int y, int ancho, int alto) {
            super(id, color);
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
        }

        @Override
        void dibujar(GraphicsContext g) {
            g.setFill(color);
            g.fillRect(x, y, ancho, alto);
        }

//        MOVER
        @Override
        void mover(int dx, int dy) {
            x += dx;
            y += dy;
        }
    }

    class Circulo extends Figura {
        int x, y, ancho, alto;

        Circulo(String id, Color color, int x, int y, int ancho, int alto) {
            super(id, color);
            this.x = x;
            this.y = y;
            this.ancho = ancho;
            this.alto = alto;
        }

        @Override
        void dibujar(GraphicsContext g) {
            g.setFill(color);
            g.fillOval(x - ancho / 2.0, y - alto / 2.0, ancho, alto);
        }

        //        MOVER
        @Override
        void mover(int dx, int dy) {
            x += dx;
            y += dy;
        }
    }

    class Linea extends Figura {
        int x1, y1, x2, y2;

        Linea(String id, Color color, int x1, int y1, int x2, int y2) {
            super(id, color);
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        void dibujar(GraphicsContext g) {
            g.setStroke(color);
            g.strokeLine(x1, y1, x2, y2);
        }

        //        MOVER
        @Override
        void mover(int dx, int dy) {
            x1 += dx;
            y1 += dy;
            x2 += dx;
            y2 += dy;
        }
    }

    private void pintar() {
        if (fondoActivo) {
            graficos.setFill(color);
            graficos.fillRect(0, 0, 400, 400);
            fondoActivo = false;
        }
        if (limpiar) {
            graficos.clearRect(0, 0, 400, 400);
            figuras.clear(); // limpiar también los objetos creados
            limpiar = false;
        }
        if (posicionPunto) {
            graficos.setFill(Color.BLACK);
            graficos.fillOval(x, y, 5, 5);
        }
        if (lineaActiva) {
            graficos.setStroke(colorFigura);
            graficos.strokeLine(x, y, ancho, alto);
            lineaActiva = false;
        }
        if (rectanguloActivo) {
            graficos.setFill(fondoFigura);
            graficos.fillRect(x, y, ancho, alto);
            rectanguloActivo = false;
        }
        if (circuloActivo) {
            graficos.setFill(fondoFigura);
            graficos.fillOval(x - ancho / 2.0, y - alto / 2.0, ancho, alto);
            circuloActivo = false;
        }

        // Dibujar figuras personalizadas
        for (Figura f : figuras) {
            f.dibujar(graficos);
        }
    }

    //----------------INTEGRAR NOMBRE DE FIGURA---------------------


//si tienen nombre entonces se ejecuta uno u otra funcion
    private void procesarComando(String comando) {
        if (comando.contains("=")) {
            procesarComandoConNombre(comando);
        } else {
            procesarComandoSinNombre(comando);
        }
    }

//----------------INTEGRAR NOMBRE DE FIGURA---------------------
//    CON NOMBRE

    private void procesarComandoConNombre(String comando) {
        try {
            String[] partes = comando.split("=");
            String id = partes[0].trim().replace(";", "");
            String parametros = partes[1].trim().replace(";", "");

            // Verificar si ya existe una figura con ese id
            for (Figura f : figuras) {
                if (f.coincideId(id)) {
                    txtMensajes.appendText("Error: '" + id + "' ya existe\n");
                    return;
                }
            }

            String[] args = parametros.split(",");
            String tipo = args[0].trim();

            try {
                Figura nuevaFigura = null;
                switch (tipo) {
                    case "cir": // Círculo: "nombre = cir,x,y,ancho,alto,color;"
                        nuevaFigura = new Circulo(
                                id,
                                obtenerColor(args[5].trim()),
                                Integer.parseInt(args[1].trim()),
                                Integer.parseInt(args[2].trim()),
                                Integer.parseInt(args[3].trim()),
                                Integer.parseInt(args[4].trim())
                        );
                        break;

                    case "rec": // Rectángulo: "nombre = rec,x,y,ancho,alto,color;"
                        nuevaFigura = new Rectangulo(
                                id,
                                obtenerColor(args[5].trim()),
                                Integer.parseInt(args[1].trim()),
                                Integer.parseInt(args[2].trim()),
                                Integer.parseInt(args[3].trim()),
                                Integer.parseInt(args[4].trim())
                        );
                        break;

                    case "lin": // Línea: "nombre = lin,x1,y1,x2,y2,color;"
                        nuevaFigura = new Linea(
                                id,
                                obtenerColor(args[5].trim()),
                                Integer.parseInt(args[1].trim()),
                                Integer.parseInt(args[2].trim()),
                                Integer.parseInt(args[3].trim()),
                                Integer.parseInt(args[4].trim())
                        );
                        break;

                    default:
                        txtMensajes.appendText("Error: Tipo de figura '" + tipo + "' no reconocido\n");
                        return;
                }

                if (nuevaFigura != null) {
                    figuras.add(nuevaFigura);
                    // Dibujar la figura inmediatamente
                    nuevaFigura.dibujar(graficos);
                    txtMensajes.appendText("Figura '" + id + "' creada y dibujada exitosamente\n");
                }
            } catch (Exception e) {
                txtMensajes.appendText("Error en los parámetros para '" + id + "'. Verifica los valores\n");
            }

        } catch (Exception e) {
            txtMensajes.appendText("Error en comando: " + e.getMessage() + "\n");
        }
    }


    //    SIN NOMBRE
private void procesarComandoSinNombre(String comando) {
    if (comando == null || comando.trim().isEmpty()) {
        txtMensajes.appendText("Error: Comando vacío\n");
        return;
    }
    String[] partes = comando.split(",");
    String tipo = partes[0].trim();

    try {
        switch(tipo) {
            case "rec": // Rectángulo: "rec,ancho,alto,color"
                if (partes.length < 4) {
                    txtMensajes.appendText("Error: Faltan parámetros. Formato: rec,ancho,alto,color\n");
                    return;
                }
                if (partes.length >= 4) {
                    graficos.setFill(obtenerColor(partes[3].trim()));
                    graficos.fillRect(
                            x, // Usa la posición actual (x,y)
                            y,
                            Integer.parseInt(partes[1].trim()),
                            Integer.parseInt(partes[2].trim())
                    );
                }
                break;

            case "cir": // Círculo: "cir,radio,color"
                if (partes.length < 3) {
                    txtMensajes.appendText("Error: Faltan parámetros. Formato: cir,radio,color\n");
                    return;
                }
                if (partes.length >= 3) {
                    int radio = Integer.parseInt(partes[1].trim());
                    graficos.setFill(obtenerColor(partes[2].trim()));
                    graficos.fillOval(
                            x - radio, // Centrado en (x,y)
                            y - radio,
                            radio * 2,
                            radio * 2
                    );
                }
                break;

            case "lin": // Línea: "ln,x2,y2,color"
                if (partes.length < 4) {
                    txtMensajes.appendText("Error: Faltan parámetros. Formato: ln,x2,y2,color\n");
                    return;
                }
                if (partes.length >= 4) {
                    graficos.setStroke(obtenerColor(partes[3].trim()));
                    graficos.strokeLine(
                            x,  // Desde (x,y) actual
                            y,
                            Integer.parseInt(partes[1].trim()), // Hasta (x2,y2)
                            Integer.parseInt(partes[2].trim())
                    );
                }
                break;

            default:
                txtMensajes.appendText("Error: Tipo de figura '" + tipo + "' no reconocido\n");
        }
    } catch (Exception e) {
        txtMensajes.appendText("Error en comando: " + e.getMessage() + "\n");
    }
}


    //----------------INTEGRAR NOMBRE DE FIGURA---------------------


    //----------------EJECUCION---------------------

    private int indiceComandoc = 0;

    private void lecturaComando() {
        if (indiceComandoc < ComandoGlobal.comandos.size()) {
            String comando = ComandoGlobal.comandos.get(indiceComandoc);
            System.out.println("Procesando comando: " + comando);

            String[] partes = comando.split(",");

            switch (partes[0]) {
                case "fig_nombrada":
                    procesarFiguraNombrada(partes);
                    break;

                case "lpr":
                    limpiar = true;
                    break;

                case "f":
                    fondoActivo = true;
                    if (partes.length > 1) {
                        color = obtenerColor(partes[1]);
                    } else {
                        System.out.println("Comando 'f' sin color");
                    }
                    break;

                case "ps":
                    if (partes.length >= 3) {
                        try {
                            x = Integer.parseInt(partes[1].trim());
                            y = Integer.parseInt(partes[2].trim());
                            posicionPunto = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Error en comando 'ps': " + e.getMessage());
                        }
                    } else {
                        System.out.println("Comando 'ps' incompleto");
                    }
                    break;

                case "rec":
                    if (partes.length >= 6) {
                        try {
                            x = Integer.parseInt(partes[1].trim());
                            y = Integer.parseInt(partes[2].trim());
                            ancho = Integer.parseInt(partes[3].trim());
                            alto = Integer.parseInt(partes[4].trim());
                            fondoFigura = obtenerColor(partes[5].trim());
                            rectanguloActivo = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Error en comando 'rec': " + e.getMessage());
                        }
                    }
                    break;

                case "ln":
                    if (partes.length >= 6) {
                        try {
                            // Punto de inicio (desde posición actual o comando ps)
                            int x1 = Integer.parseInt(partes[1].trim());
                            int y1 = Integer.parseInt(partes[2].trim());

                            // Punto final
                            int x2 = Integer.parseInt(partes[3].trim());
                            int y2 = Integer.parseInt(partes[4].trim());

                            // Color
                            colorFigura = obtenerColor(partes[5].trim());

                            // Actualizar variables para pintar
                            this.x = x1;
                            this.y = y1;
                            this.ancho = x2;
                            this.alto = y2;
                            lineaActiva = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Error en comando 'ln': " + e.getMessage());
                        }
                    }
                    break;

                case "cir":
                    if (partes.length >= 5) {
                        try {
                            // Posición del centro
                            int centroX = Integer.parseInt(partes[1].trim());
                            int centroY = Integer.parseInt(partes[2].trim());

                            // Radio
                            int radio = Integer.parseInt(partes[3].trim());

                            // Color
                            fondoFigura = obtenerColor(partes[4].trim());

                            // Actualizar variables para pintar
                            this.x = centroX - radio;
                            this.y = centroY - radio;
                            this.ancho = radio * 2;
                            this.alto = radio * 2;
                            circuloActivo = true;
                        } catch (NumberFormatException e) {
                            System.out.println("Error en comando 'cir': " + e.getMessage());
                        }
                    }
                    break;

//                    MOVER Y WHILE
                /*
                case "mv":
                    procesarMovimiento(partes);
                    break;

                case "while_begin":
                    procesarWhileBegin(partes);
                    break;
                case "while_end":
                    procesarWhileEnd();
                    break;
                case "incrementar":
                    procesarIncremento(partes);
                    break;

                 */
            }
            indiceComandoc++;

        } else {
            System.out.println("No hay más comandos para procesar");
            if (tiempo != null) tiempo.stop();
        }
        /*
        if (comando.contains("=") && !comando.contains("mv") && !comando.contains("fig_nombrada")) {
            String[] asignacion = comando.split("=");
            if (asignacion.length == 2) {
                String var = asignacion[0].trim();
                try {
                    int valor = Integer.parseInt(asignacion[1].replace(";", "").trim());
                    variables.put(var, valor);
                } catch (NumberFormatException e) {
                    System.out.println("Error en asignación: " + comando);
                }
            }
        }*/
    }

    private void procesarMovimiento(String[] partes) {
        if (partes.length < 4) {
            System.out.println("Comando 'mv' incompleto");
            return;
        }
        String nombreFigura = partes[1];
        String direccion = partes[2];
        int cantidad = Integer.parseInt(partes[3]);

        for (Figura figura : figuras) {
            if (figura.coincideId(nombreFigura)) {
                int dx = 0, dy = 0;
                switch (direccion) {
                    case "arriba": dy = -cantidad; break;
                    case "abajo": dy = cantidad; break;
                    case "izquierda": dx = -cantidad; break;
                    case "derecha": dx = cantidad; break;
                }
                figura.mover(dx, dy);
                break;
            }
        }
    }

    /*private void procesarWhileBegin(String[] partes) {
        if (partes.length < 4) {
            System.out.println("Comando 'while_begin' incompleto");
            return;
        }
        String variable = partes[1];
        String operador = partes[2];
        int limite = Integer.parseInt(partes[3]);

        int valorActual = variables.getOrDefault(variable, 0);
        boolean cumple = false;

        switch (operador) {
            case "<": cumple = valorActual < limite; break;
        }

        if (cumple) {
            // Guardar posición actual para volver
            stackWhileStart.push(indiceComandoc);
            stackWhileConditions.push(partes);
        } else {
            // Saltar hasta el while_end
            int nivel = 1;
            while (indiceComandoc < ComandoGlobal.comandos.size() && nivel > 0) {
                indiceComandoc++;
                if (indiceComandoc < ComandoGlobal.comandos.size()) {
                    String cmd = ComandoGlobal.comandos.get(indiceComandoc);
                    if (cmd.startsWith("while_begin")) nivel++;
                    else if (cmd.equals("while_end")) nivel--;
                }
            }
        }
    }

    private void procesarWhileEnd() {
        if (!stackWhileStart.isEmpty()) {
            // Volver al inicio del while
            String[] condicion = stackWhileConditions.peek();
            String variable = condicion[1];
            String operador = condicion[2];
            int limite = Integer.parseInt(condicion[3]);

            int valorActual = variables.getOrDefault(variable, 0);
            boolean cumple = false;

            switch (operador) {
                case "<": cumple = valorActual < limite; break;
            }

            if (cumple) {
                indiceComandoc = stackWhileStart.peek() - 1; // -1 porque luego se incrementa
            } else {
                stackWhileStart.pop();
                stackWhileConditions.pop();
            }
        } else {
            System.out.println("Error: while_end sin while_begin correspondiente");
        }
    }

     */
    /*
    private void procesarIncremento(String[] partes) {
        if (partes.length < 2) {
            System.out.println("Comando 'incrementar' incompleto");
            return;
        }
        String variable = partes[1];
        int valorActual = variables.getOrDefault(variable, 0);
        variables.put(variable, valorActual + 1);
    }

     */


    private void procesarFiguraNombrada(String[] partes) {
        if (partes.length < 6) {
            System.out.println("Comando fig_nombrada incompleto");
            return;
        }

        String nombre = partes[1];
        String tipo = partes[2];

        // Verificar ID duplicado
        for (Figura f : figuras) {
            if (f.coincideId(nombre)) {
                System.out.println("Error: Figura '" + nombre + "' ya existe");
                return;
            }
        }

        try {
            switch(tipo) {
                case "rec":
                    if (partes.length >= 8) {
                        int x = Integer.parseInt(partes[3].trim());
                        int y = Integer.parseInt(partes[4].trim());
                        int ancho = Integer.parseInt(partes[5].trim());
                        int alto = Integer.parseInt(partes[6].trim());
                        Color color = obtenerColor(partes[7].trim());
                        figuras.add(new Rectangulo(nombre, color, x, y, ancho, alto));
                    }
                    break;

                case "cir":
                    if (partes.length >= 7) {
                        int centroX = Integer.parseInt(partes[3].trim());
                        int centroY = Integer.parseInt(partes[4].trim());
                        int radio = Integer.parseInt(partes[5].trim());
                        Color color = obtenerColor(partes[6].trim());
                        figuras.add(new Circulo(nombre, color, centroX, centroY, radio*2, radio*2));
                    }
                    break;

                case "ln":
                    if (partes.length >= 8) {
                        int x1 = Integer.parseInt(partes[3].trim());
                        int y1 = Integer.parseInt(partes[4].trim());
                        int x2 = Integer.parseInt(partes[5].trim());
                        int y2 = Integer.parseInt(partes[6].trim());
                        Color color = obtenerColor(partes[7].trim());
                        figuras.add(new Linea(nombre, color, x1, y1, x2, y2));
                    }
                    break;

                default:
                    System.out.println("Tipo de figura no reconocido: " + tipo);
            }
        } catch (Exception e) {
            System.out.println("Error procesando figura nombrada '" + nombre + "': " + e.getMessage());
        }
    }

    public void iniciar() {
        componentesIniciar();
    }

    private void componentesIniciar() {

        graficos = canvas.getGraphicsContext2D();
    }

    private void ciclo() {
        final long[] tiempoInicio = {System.nanoTime()};
        tiempo = new AnimationTimer() {
            @Override
            public void handle(long tiempoActual) {
                double t = (tiempoActual - tiempoInicio[0]) / 1_000_000_000.0;
                if ((int) t % 5 == 1) {
                    tiempoInicio[0] = System.nanoTime();
                    lecturaComando();
                }
                pintar();
            }
        };

        tiempo.start();
    }

    //----------------EJECUCION---------------------
    private static final Map<String, Color> colores = Map.of(
            "red", Color.RED,
            "blue", Color.BLUE,
            "green", Color.GREEN,
            "yellow", Color.YELLOW,
            "black", Color.BLACK,
            "orange", Color.ORANGE,
            "white", Color.WHITE
    );

    private Color obtenerColor(String colorStr) {
        if (colorStr == null) return Color.BLACK;

        String colorLimpio = colorStr.trim().toLowerCase();
        System.out.println("Color recibido (limpio): '" + colorLimpio + "'");

        return colores.getOrDefault(colorLimpio, Color.BLACK);
    }


    private void cerrarJuego() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.setOnCloseRequest(event -> {
            tiempo.stop();
            stage.close();
        });
    }

}
