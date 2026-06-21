package modelo;

import java.util.concurrent.Semaphore;

public class Misil {
    private final String id;
    private final int tiempoAparicion;
    private final ZonaObjetivo zonaObjetivo;
    private final int tiempoHastaImpacto;
    private final int tiempoDesactivacion;

    private EstadoMisil estado;
    private final Semaphore mutexEstado;

    public Misil(String id, int tiempoAparicion, ZonaObjetivo zonaObjetivo, int tiempoHastaImpacto, int tiempoDesactivacion) {
        this.id = id;
        this.tiempoAparicion = tiempoAparicion;
        this.zonaObjetivo = zonaObjetivo;
        this.tiempoHastaImpacto = tiempoHastaImpacto;
        this.tiempoDesactivacion = tiempoDesactivacion;
        this.estado = EstadoMisil.NO_DETECTADO;
        this.mutexEstado = new Semaphore(1);
    }

    public String getId() {
        return id;
    }

    public int getTiempoAparicion() {
        return tiempoAparicion;
    }

    public ZonaObjetivo getZonaObjetivo() {
        return zonaObjetivo;
    }

    public int getTiempoHastaImpacto() {
        return tiempoHastaImpacto;
    }

    public int getTiempoDesactivacion() {
        return tiempoDesactivacion;
    }

    public int getTiempoImpacto() {
        return tiempoAparicion + tiempoHastaImpacto;
    }

    public EstadoMisil getEstado() throws InterruptedException {
        mutexEstado.acquire();
        try {
            return estado;
        } finally {
            mutexEstado.release();
        }
    }

    public void setEstado(EstadoMisil nuevoEstado) throws InterruptedException {
        mutexEstado.acquire();
        try {
            estado = nuevoEstado;
        } finally {
            mutexEstado.release();
        }
    }

    public boolean puedeSerAtendido(int tiempoActual) throws InterruptedException {
        mutexEstado.acquire();
        try {
            return estado == EstadoMisil.PENDIENTE && puedeFinalizarAntesDelImpacto(tiempoActual);
        } finally {
            mutexEstado.release();
        }
    }

    public boolean marcarEnAtencionSiPuede(int tiempoActual) throws InterruptedException {
        mutexEstado.acquire();
        try {
            if (estado == EstadoMisil.PENDIENTE && puedeFinalizarAntesDelImpacto(tiempoActual)) {
                estado = EstadoMisil.EN_ATENCION;
                return true;
            }
            return false;
        } finally {
            mutexEstado.release();
        }
    }

    public boolean marcarImpactadoSiCorresponde(int tiempoActual) throws InterruptedException {
        mutexEstado.acquire();
        try {
            boolean puedeImpactar = estado == EstadoMisil.PENDIENTE || estado == EstadoMisil.EN_ATENCION;
            if (puedeImpactar && tiempoActual >= getTiempoImpacto()) {
                estado = EstadoMisil.IMPACTADO;
                return true;
            }
            return false;
        } finally {
            mutexEstado.release();
        }
    }

    public boolean marcarDesactivadoSiNoImpacto(int tiempoActual) throws InterruptedException {
        mutexEstado.acquire();
        try {
            if (estado == EstadoMisil.EN_ATENCION && tiempoActual < getTiempoImpacto()) {
                estado = EstadoMisil.DESACTIVADO;
                return true;
            }
            return false;
        } finally {
            mutexEstado.release();
        }
    }

    public boolean estaFinalizado() throws InterruptedException {
        mutexEstado.acquire();
        try {
            return estado == EstadoMisil.DESACTIVADO || estado == EstadoMisil.IMPACTADO;
        } finally {
            mutexEstado.release();
        }
    }

    private boolean puedeFinalizarAntesDelImpacto(int tiempoActual) {
        return tiempoActual + tiempoDesactivacion < getTiempoImpacto();
    }
}
