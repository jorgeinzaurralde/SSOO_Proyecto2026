package tiempo;

import java.util.concurrent.Semaphore;

public class RelojSimulacion extends Thread {
    private int tiempoActual;
    private boolean activo;
    private final Semaphore mutex;

    public RelojSimulacion() {
        this.tiempoActual = 0;
        this.activo = true;
        this.mutex = new Semaphore(1);
    }

    public void run() {
        while (estaActivo()) {
            try {
                Thread.sleep(1000);
                incrementarTiempo();
            } catch (InterruptedException e) {
                detener();
            }
        }
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
