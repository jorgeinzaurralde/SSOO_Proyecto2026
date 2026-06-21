package tiempo;

import concurrencia.ColaAmenazas;

import java.util.concurrent.Semaphore;

public class RelojSimulacion extends Thread {
    private int tiempoActual;
    private boolean activo;
    private final Semaphore mutex;
    private ColaAmenazas colaAmenazas;

    public RelojSimulacion() {
        this.tiempoActual = 0;
        this.activo = true;
        this.mutex = new Semaphore(1);
        this.colaAmenazas = null;
    }

    public void run() {
        while (estaActivo()) {
            try {
                Thread.sleep(1000);
                incrementarTiempo();
                reordenarCola();
            } catch (InterruptedException e) {
                detener();
            }
        }
    }

    public void configurarColaAmenazas(ColaAmenazas colaAmenazas) {
        this.colaAmenazas = colaAmenazas;
    }

    public int getTiempoActual() {
        try {
            mutex.acquire();
            try {
                return tiempoActual;
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return tiempoActual;
        }
    }

    public void detener() {
        try {
            mutex.acquire();
            try {
                activo = false;
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void incrementarTiempo() throws InterruptedException {
        mutex.acquire();
        try {
            tiempoActual++;
        } finally {
            mutex.release();
        }
    }

    private void reordenarCola() throws InterruptedException {
        if (colaAmenazas != null) {
            colaAmenazas.reordenarPorPrioridad();
        }
    }

    private boolean estaActivo() {
        try {
            mutex.acquire();
            try {
                return activo;
            } finally {
                mutex.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
