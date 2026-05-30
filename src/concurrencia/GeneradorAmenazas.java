package concurrencia;

import io.ConsolaLogger;
import modelo.EstadoMisil;
import modelo.Misil;
import tiempo.RelojSimulacion;

import java.util.List;

public class GeneradorAmenazas extends Thread {
    private final List<Misil> misiles;
    private final ColaAmenazas colaAmenazas;
    private final RelojSimulacion reloj;
    private final ConsolaLogger logger;

    public GeneradorAmenazas(List<Misil> misiles, ColaAmenazas colaAmenazas, RelojSimulacion reloj, ConsolaLogger logger) {
        this.misiles = misiles;
        this.colaAmenazas = colaAmenazas;
        this.reloj = reloj;
        this.logger = logger;
    }

    public void run() {
        int generados = 0;

        while (generados < misiles.size()) {
            for (Misil misil : misiles) {
                try {
                    if (misil.getEstado() == EstadoMisil.NO_DETECTADO
                            && reloj.getTiempoActual() >= misil.getTiempoAparicion()) {
                        misil.setEstado(EstadoMisil.PENDIENTE);
                        logger.imprimir("Misil " + misil.getId() + " detectado hacia " + misil.getZonaObjetivo().getNombre());
                        colaAmenazas.agregar(misil);
                        generados++;
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
}
