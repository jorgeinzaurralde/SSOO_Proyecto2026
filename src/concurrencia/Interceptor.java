package concurrencia;

import io.ConsolaLogger;
import modelo.Misil;
import planificacion.Planificador;
import tiempo.RelojSimulacion;

import java.util.concurrent.Semaphore;

public class Interceptor extends Thread {
    private final int numero;
    private final ColaAmenazas colaAmenazas;
    private final Planificador planificador;
    private final RelojSimulacion reloj;
    private final ConsolaLogger logger;

    private boolean activo;
    private final Semaphore mutexActivo;

    public Interceptor(int numero, ColaAmenazas colaAmenazas, Planificador planificador, RelojSimulacion reloj, ConsolaLogger logger) {
        this.numero = numero;
        this.colaAmenazas = colaAmenazas;
        this.planificador = planificador;
        this.reloj = reloj;
        this.logger = logger;
        this.activo = true;
        this.mutexActivo = new Semaphore(1);
    }

    public void run() {
        while (estaActivo()) {
            try {
                Misil misil = colaAmenazas.obtenerProximo(planificador, reloj);

                if (!estaActivo()) {
                    return;
                }

                if (misil == null) {
                    continue;
                }

                if (!misil.marcarEnAtencionSiPuede(reloj.getTiempoActual())) {
                    continue;
                }

                logger.imprimir("Interceptor " + numero + " comienza a desactivar misil " + misil.getId());
                Thread.sleep(misil.getTiempoDesactivacion() * 1000L);

                if (misil.marcarDesactivadoSiNoImpacto(reloj.getTiempoActual())) {
                    logger.imprimir("Interceptor " + numero + " desactivo misil " + misil.getId());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                detener();
            }
        }
    }

    public void detener() {
        try {
            mutexActivo.acquire();
            try {
                activo = false;
            } finally {
                mutexActivo.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean estaActivo() {
        try {
            mutexActivo.acquire();
            try {
                return activo;
            } finally {
                mutexActivo.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
