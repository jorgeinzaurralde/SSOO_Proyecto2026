import concurrencia.ColaAmenazas;
import concurrencia.ControladorImpactos;
import concurrencia.GeneradorAmenazas;
import concurrencia.Interceptor;
import io.CargadorMisiles;
import io.ConsolaLogger;
import modelo.Misil;
import planificacion.Planificador;
import planificacion.PlanificadorFCFS;
import planificacion.PriorizadorAmenazas;
import tiempo.RelojSimulacion;

import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try {
            RelojSimulacion reloj = new RelojSimulacion();
            ConsolaLogger logger = new ConsolaLogger(reloj);
            int numeroEscenario = leerNumeroEscenario(logger);
            int cantidadInterceptores = obtenerCantidadInterceptores(numeroEscenario);
            String archivoMisiles = obtenerArchivoMisiles(numeroEscenario);

            CargadorMisiles cargador = new CargadorMisiles();
            List<Misil> misiles = cargador.cargar(archivoMisiles);

            ColaAmenazas colaAmenazas = new ColaAmenazas();
            Planificador planificador = new PlanificadorFCFS();
            PriorizadorAmenazas priorizador = new PriorizadorAmenazas();
            colaAmenazas.configurarPriorizador(priorizador, reloj, cantidadInterceptores);
            reloj.configurarColaAmenazas(colaAmenazas);

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

    private static int leerNumeroEscenario(ConsolaLogger logger) {
        logger.imprimirSinTiempo("Seleccione el escenario de simulacion:");
        logger.imprimirSinTiempo("1 - Funcionamiento basico");
        logger.imprimirSinTiempo("2 - Saturacion de recursos");
        logger.imprimirSinTiempo("3 - Prioridad por holgura y criticidad");
        logger.imprimirSinTiempo("Ingrese numero de escenario: ");

        Scanner scanner = new Scanner(System.in);
        try {
            int numero = Integer.parseInt(scanner.nextLine().trim());
            if (numero >= 1 && numero <= 3) {
                return numero;
            }
        } catch (Exception e) {
            logger.imprimirSinTiempo("Entrada invalida. Se usara el escenario 1.");
        }

        return 1;
    }

    private static int obtenerCantidadInterceptores(int numeroEscenario) {
        if (numeroEscenario == 2) {
            return 1;
        }
        if (numeroEscenario == 3) {
            return 2;
        }
        return 2;
    }

    private static String obtenerArchivoMisiles(int numeroEscenario) {
        if (numeroEscenario == 2) {
            return "misiles_escenario2.txt";
        }
        if (numeroEscenario == 3) {
            return "misiles_escenario3.txt";
        }
        return "misiles.txt";
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
