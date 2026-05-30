import concurrencia.ColaAmenazas;
import concurrencia.ControladorImpactos;
import concurrencia.GeneradorAmenazas;
import concurrencia.Interceptor;
import io.CargadorMisiles;
import io.ConsolaLogger;
import modelo.Misil;
import planificacion.Planificador;
import planificacion.PlanificadorFCFS;
import tiempo.RelojSimulacion;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            int cantidadInterceptores = 2;

            RelojSimulacion reloj = new RelojSimulacion();
            ConsolaLogger logger = new ConsolaLogger(reloj);

            CargadorMisiles cargador = new CargadorMisiles();
            List<Misil> misiles = cargador.cargar("misiles.txt");

            ColaAmenazas colaAmenazas = new ColaAmenazas();
            Planificador planificador = new PlanificadorFCFS();

            GeneradorAmenazas generador = new GeneradorAmenazas(misiles, colaAmenazas, reloj, logger);
            ControladorImpactos controladorImpactos = new ControladorImpactos(misiles, reloj, logger);

            Interceptor[] interceptores = new Interceptor[cantidadInterceptores];
            for (int i = 0; i < cantidadInterceptores; i++) {
                interceptores[i] = new Interceptor(i + 1, colaAmenazas, planificador, reloj, logger);
            }

            reloj.start();
            controladorImpactos.start();
            generador.start();
            for (Interceptor interceptor : interceptores) {
                interceptor.start();
            }

            generador.join();

            while (!simulacionTerminada(misiles, colaAmenazas)) {
                Thread.sleep(300);
            }

            for (Interceptor interceptor : interceptores) {
                interceptor.detener();
            }
            colaAmenazas.despertarInterceptores(cantidadInterceptores);
            controladorImpactos.detener();
            reloj.detener();

            for (Interceptor interceptor : interceptores) {
                interceptor.join();
            }
            controladorImpactos.join();
            reloj.join();

            logger.imprimir("Simulacion finalizada");
        } catch (Exception e) {
            System.err.println("Error en la simulacion: " + e.getMessage());
        }
    }

    private static boolean simulacionTerminada(List<Misil> misiles, ColaAmenazas colaAmenazas) throws InterruptedException {
        if (colaAmenazas.hayPendientes()) {
            return false;
        }

        for (Misil misil : misiles) {
            if (!misil.estaFinalizado()) {
                return false;
            }
        }

        return true;
    }
}
