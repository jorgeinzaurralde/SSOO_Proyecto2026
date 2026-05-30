package modelo;

import java.util.HashMap;
import java.util.Map;

public class ZonaObjetivo {
    private final String nombre;
    private final int criticidad;

    public ZonaObjetivo(String nombre, int criticidad) {
        this.nombre = nombre;
        this.criticidad = criticidad;
    }

    public String getNombre() {
        return nombre;
    }

    public int getCriticidad() {
        return criticidad;
    }

    public static Map<String, ZonaObjetivo> crearZonasPredefinidas() {
        Map<String, ZonaObjetivo> zonas = new HashMap<String, ZonaObjetivo>();
        agregar(zonas, "Hospital", 10);
        agregar(zonas, "Central electrica", 9);
        agregar(zonas, "Planta de agua", 9);
        agregar(zonas, "Deposito militar", 9);
        agregar(zonas, "Aeropuerto", 8);
        agregar(zonas, "Zona residencial", 8);
        agregar(zonas, "Zona industrial", 7);
        agregar(zonas, "Datacenter", 6);
        return zonas;
    }

    private static void agregar(Map<String, ZonaObjetivo> zonas, String nombre, int criticidad) {
        zonas.put(nombre.toLowerCase(), new ZonaObjetivo(nombre, criticidad));
    }
}
