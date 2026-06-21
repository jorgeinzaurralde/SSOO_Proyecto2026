package io;

import tiempo.RelojSimulacion;

import java.util.concurrent.Semaphore;

public class ConsolaLogger {
    private final RelojSimulacion reloj;
    private final Semaphore mutexSalida;

    public ConsolaLogger(RelojSimulacion reloj) {
        this.reloj = reloj;
        this.mutexSalida = new Semaphore(1);
    }

    public void imprimir(String mensaje) {
        try {
            mutexSalida.acquire();
            try {
                System.out.println("[t=" + reloj.getTiempoActual() + "s] " + mensaje);
            } finally {
                mutexSalida.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void imprimirSinTiempo(String mensaje) {
        try {
            mutexSalida.acquire();
            try {
                System.out.println(mensaje);
            } finally {
                mutexSalida.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
