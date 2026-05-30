package io;

import modelo.Misil;
import modelo.ZonaObjetivo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CargadorMisiles {
    public List<Misil> cargar(String rutaArchivo) throws IOException {
        List<Misil> misiles = new ArrayList<Misil>();
        Map<String, ZonaObjetivo> zonas = ZonaObjetivo.crearZonasPredefinidas();

        BufferedReader lector = new BufferedReader(new FileReader(rutaArchivo));
        try {
            String linea = lector.readLine();
            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(";");
                if (partes.length != 5) {
                    throw new IOException("Linea invalida en archivo: " + linea);
                }

                String id = partes[0].trim();
                int tiempoAparicion = Integer.parseInt(partes[1].trim());
                String nombreZona = partes[2].trim();
                int tiempoHastaImpacto = Integer.parseInt(partes[3].trim());
                int tiempoDesactivacion = Integer.parseInt(partes[4].trim());

                ZonaObjetivo zona = zonas.get(nombreZona.toLowerCase());
                if (zona == null) {
                    zona = new ZonaObjetivo(nombreZona, 5);
                }

                misiles.add(new Misil(id, tiempoAparicion, zona, tiempoHastaImpacto, tiempoDesactivacion));
            }
        } finally {
            lector.close();
        }

        return misiles;
    }
}
