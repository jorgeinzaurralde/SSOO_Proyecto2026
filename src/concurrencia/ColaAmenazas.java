package concurrencia;

import modelo.Misil;
import planificacion.Planificador;
import planificacion.PriorizadorAmenazas;
import tiempo.RelojSimulacion;

import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ColaAmenazas {
    private final LinkedList<Misil> cola;
    private final Semaphore mutexCola;
    private final Semaphore disponibles;
    private PriorizadorAmenazas priorizador;
    private RelojSimulacion reloj;
    private int cantidadInterceptores;

    public ColaAmenazas() {
        this.cola = new LinkedList<Misil>();
        this.mutexCola = new Semaphore(1);
        this.disponibles = new Semaphore(0);
        this.priorizador = null;
        this.reloj = null;
        this.cantidadInterceptores = 1;
    }

    public void configurarPriorizador(PriorizadorAmenazas priorizador, RelojSimulacion reloj, int cantidadInterceptores) {
        this.priorizador = priorizador;
        this.reloj = reloj;
        this.cantidadInterceptores = cantidadInterceptores;
    }

    public void agregar(Misil misil) throws InterruptedException {
        mutexCola.acquire();
        try {
            cola.addLast(misil);
            reordenarSinBloquear();
        } finally {
            mutexCola.release();
        }
        disponibles.release();
    }

    public void agregarTodos(List<Misil> misiles) throws InterruptedException {
        if (misiles.isEmpty()) {
            return;
        }

        mutexCola.acquire();
        try {
            for (Misil misil : misiles) {
                cola.addLast(misil);
            }
            reordenarSinBloquear();
        } finally {
            mutexCola.release();
        }
        disponibles.release(misiles.size());
    }

    public Misil obtenerProximo(Planificador planificador, RelojSimulacion reloj) throws InterruptedException {
        disponibles.acquire();

        mutexCola.acquire();
        try {
            reordenarSinBloquear();
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

    public void reordenarPorPrioridad() throws InterruptedException {
        mutexCola.acquire();
        try {
            reordenarSinBloquear();
        } finally {
            mutexCola.release();
        }
    }

    private void reordenarSinBloquear() throws InterruptedException {
        if (priorizador != null && reloj != null && cola.size() > 1) {
            priorizador.reordenar(cola, reloj.getTiempoActual(), cantidadInterceptores);
        }
    }
}
