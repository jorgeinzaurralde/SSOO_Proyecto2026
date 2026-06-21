package planificacion;

import modelo.EstadoMisil;
import modelo.Misil;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PriorizadorAmenazas {
    public void reordenar(LinkedList<Misil> cola, int tiempoActual, int cantidadInterceptores) throws InterruptedException {
        Map<Misil, Double> prioridades = new HashMap<Misil, Double>();
        int cantidadPendientes = contarMisilesPendientes(cola);

        for (Misil misil : cola) {
            prioridades.put(misil, calcularPrioridad(misil, tiempoActual, cantidadPendientes, cantidadInterceptores));
        }

        Collections.sort(cola, new ComparadorPrioridad(prioridades));
    }

    public double calcularPrioridad(Misil misil, int tiempoActual, int cantidadMisilesPendientes, int cantidadInterceptores) throws InterruptedException {
        if (misil.getEstado() != EstadoMisil.PENDIENTE) {
            return -1;
        }

        int deadline = misil.getTiempoAparicion() + misil.getTiempoHastaImpacto();
        int tiempoRestante = deadline - tiempoActual;
        int holgura = tiempoRestante - misil.getTiempoDesactivacion();

        if (tiempoRestante <= 0 || holgura <= 0) {
            return -1;
        }

        double presion = Math.max(1.0, (double) cantidadMisilesPendientes / cantidadInterceptores);
        double proporcionTrabajo = (double) misil.getTiempoDesactivacion() / tiempoRestante;
        double criticidad = misil.getZonaObjetivo().getCriticidad();

        return (criticidad * (1 + presion * proporcionTrabajo)) / (holgura + 1);
    }

    private int contarMisilesPendientes(LinkedList<Misil> cola) throws InterruptedException {
        int cantidad = 0;
        for (Misil misil : cola) {
            if (misil.getEstado() == EstadoMisil.PENDIENTE) {
                cantidad++;
            }
        }
        return cantidad;
    }

    private static class ComparadorPrioridad implements Comparator<Misil> {
        private final Map<Misil, Double> prioridades;

        public ComparadorPrioridad(Map<Misil, Double> prioridades) {
            this.prioridades = prioridades;
        }

        public int compare(Misil primero, Misil segundo) {
            double prioridadPrimero = prioridades.get(primero);
            double prioridadSegundo = prioridades.get(segundo);

            if (prioridadPrimero > prioridadSegundo) {
                return -1;
            }
            if (prioridadPrimero < prioridadSegundo) {
                return 1;
            }

            int deadlinePrimero = primero.getTiempoAparicion() + primero.getTiempoHastaImpacto();
            int deadlineSegundo = segundo.getTiempoAparicion() + segundo.getTiempoHastaImpacto();

            if (deadlinePrimero != deadlineSegundo) {
                return deadlinePrimero - deadlineSegundo;
            }

            return segundo.getZonaObjetivo().getCriticidad() - primero.getZonaObjetivo().getCriticidad();
        }
    }
}
