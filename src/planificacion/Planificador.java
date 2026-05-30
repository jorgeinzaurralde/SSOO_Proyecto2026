package planificacion;

import modelo.Misil;
import tiempo.RelojSimulacion;

import java.util.LinkedList;

public interface Planificador {
    Misil seleccionar(LinkedList<Misil> cola, RelojSimulacion reloj) throws InterruptedException;
}
