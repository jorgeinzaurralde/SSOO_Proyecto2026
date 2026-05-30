package planificacion;

import modelo.Misil;
import tiempo.RelojSimulacion;

import java.util.LinkedList;

public class PlanificadorFCFS implements Planificador {
    public Misil seleccionar(LinkedList<Misil> cola, RelojSimulacion reloj) throws InterruptedException {
        if (cola.isEmpty()) {
            return null;
        }

        Misil misil = cola.getFirst();
        if (misil.puedeSerAtendido(reloj.getTiempoActual())) {
            cola.removeFirst();
            return misil;
        }

        cola.removeFirst();
        return null;
    }
}
