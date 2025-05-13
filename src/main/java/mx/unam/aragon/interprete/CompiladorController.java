package mx.unam.aragon.interprete;

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

import java.util.ArrayList;

public class CompiladorController {

    private GraphicsContext graficos;
    private Scene escena;
    private AnimationTimer tiempo;
    private ArrayList<String> comandos = new ArrayList<>();


    // coordenadas y dimensiones
    private int x = 0;
    private int y = 0;
    private int ancho = 0;
    private int alto = 0;
    private int radio = 0;

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

    @FXML
    void actionCompilar(ActionEvent event) {
        // Por ahora vacío
    }

    @FXML
    void actionEjecutar(ActionEvent event) {
        this.leerArchivo();
        this.lecturaComando();
        this.iniciar();
    }

    public void setEscena(Scene escena) {
        this.escena = escena;
    }

    public void iniciar() {
        componentesIniciar();
        ciclo();
    }

    abstract class Figura {
        String id;
        Color color;

        Figura(String id, Color color) {
            this.id = id;
            this.color = color;
        }

        abstract void dibujar(GraphicsContext g);
    }


    private void pintar() {
        if (fondoActivo) {
            graficos.setFill(color);
            graficos.fillRect(0, 0, 400, 400);
            fondoActivo = false;
        }
        if (limpiar) {
            graficos.clearRect(0, 0, 400, 400);
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
            graficos.fillOval(x - radio, y - radio, radio * 2, radio * 2);
            circuloActivo = false;
        }


    }

    private void leerArchivo() {
        comandos.add("lpr");
        comandos.add("f,rojo");
        comandos.add("ps,150,150");
        comandos.add("lin,220,220,verde");
        comandos.add("ps,50,70");
        comandos.add("lin,120,20,red");
        comandos.add("ps,51,71");
        comandos.add("lin,121,21,verde");
        comandos.add("rec,100,100,80,50,azul");
        comandos.add("rec,200,100,200,50,verde");
        comandos.add("rec,50,300,80,100,black");
        comandos.add("tgl,150,200,80,black");
        comandos.add("cir,110,120,40,black");

    }

    private void lecturaComando() {
        if (comandos.isEmpty()) {
            System.out.println("Mato");
            tiempo.stop();
            return;
        }

        String[] comando = comandos.remove(0).split(",");
        System.out.println("Comando: " + comando[0]);

        // comando de color de fondo
        if (comando[0].equals("f")) {
            fondoActivo = true;
            this.color = obtenerColor(comando[1]);
        }

        // comando limpiar
        else if (comando[0].equals("lpr")) {
            limpiar = true;
        }

        // comando punto
        else if (comando[0].equals("ps")) {
            this.x = Integer.parseInt(comando[1]);
            this.y = Integer.parseInt(comando[2]);
            posicionPunto = true;
        }

        // comando línea
        else if (comando[0].equals("lin")) {
            this.ancho = Integer.parseInt(comando[1]);
            this.alto = Integer.parseInt(comando[2]);
            this.colorFigura = obtenerColor(comando[3]);
            lineaActiva = true;
        }

        // comando rectángulo
        else if (comando[0].equals("rec")) {
            this.x = Integer.parseInt(comando[1]);
            this.y = Integer.parseInt(comando[2]);
            this.ancho = Integer.parseInt(comando[3]);
            this.alto = Integer.parseInt(comando[4]);
            this.fondoFigura = obtenerColor(comando[5]);
            rectanguloActivo = true;
        }
        //comando triangulo
        else if (comando[0].equals("tgl")) {
            this.x = Integer.parseInt(comando[1]);
            this.y = Integer.parseInt(comando[2]);
            this.alto = Integer.parseInt(comando[3]);
            this.fondoFigura = obtenerColor(comando[4]);
            trianguloActivo = true;
        }
        //comando circulo
        else if(comando[0].equals("cir")){
            this.x = Integer.parseInt(comando[1]);
            this.y = Integer.parseInt(comando[2]);
            this.radio = Integer.parseInt(comando[3]);
            this.fondoFigura = obtenerColor(comando[4]);
            circuloActivo = true;
        }
    }

    private Color obtenerColor(String colorStr) {
        return switch (colorStr.toLowerCase()) {
            case "rojo", "red" -> Color.RED;
            case "azul", "blue" -> Color.BLUE;
            case "verde", "green" -> Color.GREEN;
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

}
