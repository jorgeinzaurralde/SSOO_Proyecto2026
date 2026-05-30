package concurrencia;

import modelo.Misil;
import planificacion.Planificador;
import tiempo.RelojSimulacion;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ColaAmenazas {
    private final LinkedList<Misil> cola;
    private final Semaphore mutexCola;
    private final Semaphore disponibles;

    public ColaAmenazas() {
        this.cola = new LinkedList<Misil>();
        this.mutexCola = new Semaphore(1);
        this.disponibles = new Semaphore(0);
    }

    public void agregar(Misil misil) throws InterruptedException {
        mutexCola.acquire();
        try {
            cola.addLast(misil);
        } finally {
            mutexCola.release();
        }
        disponibles.release();
    }

    public Misil obtenerProximo(Planificador planificador, RelojSimulacion reloj) throws InterruptedException {
        disponibles.acquire();

        mutexCola.acquire();
        try {
            return planificador.seleccionar(cola, reloj);
        } finally {
            mutexCola.release();
        }
    }

    public boolean hayPendientes() throws InterruptedException {
        mutexCola.acquire();
        try {
            return !cola.isEmpty();
        } finally {
            mutexCola.release();
        }
    }

    public void despertarInterceptores(int cantidadInterceptores) {
        disponibles.release(cantidadInterceptores);
    }
}
