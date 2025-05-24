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

    @FXML
    private Button bntEjecutar;

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
        parser.TablaES.clear();
        txtMensajes.clear();
        this.guardarArchivo();

        try {
            Reader reader = new BufferedReader(new FileReader("src/codigoPrueba.txt"));

            Analizador_Lexico analizadorLexico = new Analizador_Lexico(reader);
            parser sintactico = new parser(analizadorLexico);

            sintactico.parse();

            if (!parser.TablaES.isEmpty()) {
                txtMensajes.appendText("Errores sintácticos encontrados:\n");
                for (TError e : parser.TablaES) {
                    txtMensajes.appendText(e.toString() + "\n");
                }
            } else {
                txtMensajes.appendText("Compilación terminada exitosamente. No se encontraron errores.\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            txtMensajes.appendText("Error durante la compilación: " + e.getMessage() + "\n");
        }
    }


    @FXML
    void actionEjecutar(ActionEvent event) {
        if (tiempo != null) {
            tiempo.stop();
        }

        this.indiceComandoc = 0;
        this.iniciar();
        this.ciclo();   // solo aquí debe arrancar
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


    public void setEscena(Scene escena) {
        this.escena = escena;
    }


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
//        abstract void mover(int dx, int dy);

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

//    DE ACUERDO A LA GAMATICA
//        primero debemos definir el nombe de la figura
//        despues es ir colocando los parametros
//            circulo1 = cir,20,45,red;
//        a ese figura con NOMBRE sera la manera de representar la figura
//
//        Gracias a ese identificador, después podemos aplicar comandos sobre esa figura:
//            comandos.add("mv, circulo1, abajo, 10;");
//            comandos.add("mv, circulo1, arriba, 5;");

    private void procesarComandoConNombre(String comando) {
        // Formato: "nombre = tipo,param1,param2,...,color;"
        String[] partes = comando.split("=");
        String id = partes[0].trim().replace(";", "");
        String parametros = partes[1].trim().replace(";", "");

        // Verificar ID duplicado
        for (Figura f : figuras) {
            if (f.coincideId(id)) {
                txtMensajes.appendText("Error: '" + id + "' ya existe\n");
                return;
            }
        }

        if (!comando.contains("=")) {
            txtMensajes.appendText("Error: Falta '=' en el comando\n");
            return;
        }

        String[] args = parametros.split(",");
        String tipo = args[0].trim();

        try {
            switch(tipo) {
                case "cir": // Círculo: "nombre = cir,x,y,ancho,alto,color;"
                    figuras.add(new Circulo(
                            id,
                            obtenerColor(args[5].trim()),
                            Integer.parseInt(args[1].trim()),
                            Integer.parseInt(args[2].trim()),
                            Integer.parseInt(args[3].trim()),
                            Integer.parseInt(args[4].trim())
                    ));
                    break;

                case "rec": // Rectángulo: "nombre = rec,x,y,ancho,alto,color;"
                    figuras.add(new Rectangulo(
                            id,
                            obtenerColor(args[5].trim()),
                            Integer.parseInt(args[1].trim()),
                            Integer.parseInt(args[2].trim()),
                            Integer.parseInt(args[3].trim()),
                            Integer.parseInt(args[4].trim())
                    ));
                    break;

                case "lin": // Línea: "nombre = lin,x1,y1,x2,y2,color;"
                    // (Requiere implementar clase Linea)
                    figuras.add(new Linea(
                            id,
                            obtenerColor(args[5].trim()),
                            Integer.parseInt(args[1].trim()),
                            Integer.parseInt(args[2].trim()),
                            Integer.parseInt(args[3].trim()),
                            Integer.parseInt(args[4].trim())
                    ));
                    break;


                default:
                    txtMensajes.appendText("Error: Tipo de figura '" + tipo + "' no reconocido\n");
                    return;
            }
            txtMensajes.appendText("Figura '" + id + "' creada exitosamente\n");
        } catch (Exception e) {
            txtMensajes.appendText("Error en los parámetros para '" + id + "'. Verifica los valores\n");
        }
    }

    //----------------INTEGRAR NOMBRE DE FIGURA---------------------


    //----------------EJECUCION---------------------

    private int indiceComandoc = 0;

    private void lecturaComando() {
        if (indiceComandoc < ComandoGlobal.comandos.size()) {
            String comando = ComandoGlobal.comandos.get(indiceComandoc);
            String[] partes = comando.split(",");
            switch (partes[0]) {
                case "lpr" -> limpiar = true;
                case "f" -> {
                    fondoActivo = true;
                    if (partes.length > 1) {
                        color = obtenerColor(partes[1]);
                    } else {
                        System.out.println("Comando 'f' sin color");
                    }
                }
                // otros casos (ps, rec, etc) aquí...
            }
            indiceComandoc++;  // Avanza para la próxima llamada
        } else {
            System.out.println("No hay más comandos para procesar");
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
