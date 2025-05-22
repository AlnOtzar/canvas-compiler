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
    boolean trianguloActivo = false;
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
    void actionCompilar() {
        this.guardarArchivo();

        try{
            Reader reader = new BufferedReader(new FileReader("src/codigoPrueba.txt"));

            Analizador_Lexico lexer = new Analizador_Lexico(reader);
            parser parser = new parser(lexer);
            parser.parse();
            System.out.println("Análisis sintáctico completado sin errores.");
        } catch (Exception e) {
            System.err.println("Error durante el análisis: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    void actionEjecutar(ActionEvent event) {
        this.leerArchivo();
        this.lecturaComando();
        this.iniciar();
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

    class Triangulo extends Figura {
        int x, y, alto;

        Triangulo(String id, Color color, int x, int y, int alto) {
            super(id, color);
            this.x = x;
            this.y = y;
            this.alto = alto;
        }

        @Override
        void dibujar(GraphicsContext g) {
            g.setFill(color);
            double[] puntosX = {x, x - alto/2.0, x + alto/2.0};
            double[] puntosY = {y, y + alto, y + alto};
            g.fillPolygon(puntosX, puntosY, 3);
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
        if (trianguloActivo) {
            graficos.setFill(fondoFigura);
            double[] puntosX = { x, x - alto / 2.0, x + alto / 2.0 };
            double[] puntosY = { y, y + alto, y + alto };
            graficos.fillPolygon(puntosX, puntosY, 3);
            trianguloActivo = false;
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

                case "tgl": // Triángulo: "nombre = tgl,x,y,alto,color;"
                    figuras.add(new Triangulo(
                            id,
                            obtenerColor(args[4].trim()),
                            Integer.parseInt(args[1].trim()),
                            Integer.parseInt(args[2].trim()),
                            Integer.parseInt(args[3].trim())
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

    //    simulacion de instruccionws
    private void leerArchivo() {
//        comandos.add("FIGURA, x ,y ,ancho, alto, COLOR");

        comandos.add("circuloEE = cir,20,45,40,40,red");
        comandos.add("rectanguslo3 = rec,100,100,80,50,blue");
//        comandos.add("LineaXD = lin,220,220,green");
        comandos.add("TRULO = tgl,150,200,80,black");
        comandos.add("lpr");
        comandos.add("f,red");
        comandos.add("ps,150,150");

    }

    //    si comandos esta vacio, entonces dejas de leer
    private void lecturaComando() {
        if (comandos.isEmpty()) {
            tiempo.stop();
            return;
        }

        String comandoCompleto = comandos.remove(0); // Solo remover una vez

        if (comandoCompleto.contains("=")) {
            procesarComandoConNombre(comandoCompleto);
            return;
        }

//        creamos un arreglo de string para los momandos y los vamos imprimiendo
        String[] comando = comandoCompleto.split(","); // Usar comandoCompleto en lugar de remover otro
        System.out.println("Comando: " + comando[0]);

        switch (comando[0]) {
//            fondo
            case "f" -> {
                fondoActivo = true;
                color = obtenerColor(comando[1]);
            }
//            limpiar
            case "lpr" -> limpiar = true;
//            posicion
            case "ps" -> {
                x = Integer.parseInt(comando[1]);
                y = Integer.parseInt(comando[2]);
                posicionPunto = true;
            }
//            linea
            case "lin" -> {
                ancho = Integer.parseInt(comando[1]);
                alto = Integer.parseInt(comando[2]);
                colorFigura = obtenerColor(comando[3]);
                lineaActiva = true;
            }
//            rectangulo
            case "rec" -> {
                x = Integer.parseInt(comando[1]);
                y = Integer.parseInt(comando[2]);
                ancho = Integer.parseInt(comando[3]);
                alto = Integer.parseInt(comando[4]);
                fondoFigura = obtenerColor(comando[5]);
                rectanguloActivo = true;
            }
//            triangulo
            case "tgl" -> {
                x = Integer.parseInt(comando[1]);
                y = Integer.parseInt(comando[2]);
                alto = Integer.parseInt(comando[3]);
                fondoFigura = obtenerColor(comando[4]);
                trianguloActivo = true;
            }
//            circulo
            case "cir" -> {
                x = Integer.parseInt(comando[1]);
                y = Integer.parseInt(comando[2]);
                ancho = Integer.parseInt(comando[3]);
                alto = Integer.parseInt(comando[4]);
                fondoFigura = obtenerColor(comando[5]);
                circuloActivo = true;
            }
//            objeto se crea
            case "obj" -> {
                String tipo = comando[1];
                int x = Integer.parseInt(comando[2]);
                int y = Integer.parseInt(comando[3]);
                int ancho = Integer.parseInt(comando[4]);
                int alto = Integer.parseInt(comando[5]);
                Color color = obtenerColor(comando[6]);
                Figura nuevaFigura = null;

                switch (tipo) {
                    case "rec" -> nuevaFigura = new Rectangulo("obj", color, x, y, ancho, alto);
                    case "cir" -> nuevaFigura = new Circulo("obj", color, x, y, ancho, alto);
                    case "ln" -> nuevaFigura = new Circulo("obj", color, x, y, ancho, alto);
                    default -> System.out.println("Tipo de figura no reconocido: " + tipo);
                }

                if (nuevaFigura != null) {
                    figuras.add(nuevaFigura);
                }
            }
        }
    }
    //    ---
    public void iniciar() {
        componentesIniciar();
        ciclo();
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
                    System.out.println(System.getProperty("javafx.runtime.version"));
                }
                pintar();
            }
        };
        tiempo.start();
    }

    //----------------EJECUCION---------------------

    private Color obtenerColor(String colorStr) {
        return switch (colorStr.toLowerCase()) {
            case "red" -> Color.RED;
            case "blue" -> Color.BLUE;
            case "green" -> Color.GREEN;
            default -> Color.BLACK;
        };
    }

    private void cerrarJuego() {
        Stage stage = (Stage) canvas.getScene().getWindow();
        stage.setOnCloseRequest(event -> {
            tiempo.stop();
            stage.close();
        });
    }




}
