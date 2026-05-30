package concurrencia;

import io.ConsolaLogger;
import modelo.Misil;
import tiempo.RelojSimulacion;

import java.util.List;
import java.util.concurrent.Semaphore;

public class ControladorImpactos extends Thread {
    private final List<Misil> misiles;
    private final RelojSimulacion reloj;
    private final ConsolaLogger logger;

    private boolean activo;
    private final Semaphore mutexActivo;

    public ControladorImpactos(List<Misil> misiles, RelojSimulacion reloj, ConsolaLogger logger) {
        this.misiles = misiles;
        this.reloj = reloj;
        this.logger = logger;
        this.activo = true;
        this.mutexActivo = new Semaphore(1);
    }

    public void run() {
        while (estaActivo()) {
            for (Misil misil : misiles) {
                try {
                    if (misil.marcarImpactadoSiCorresponde(reloj.getTiempoActual())) {
                        logger.imprimir("Misil " + misil.getId() + " impacto en " + misil.getZonaObjetivo().getNombre());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
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
