## Como compilar

En Windows PowerShell:

```powershell
$fuentes = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $fuentes
```

## Como ejecutar

```bash
java -cp out Main
```

El archivo `misiles.txt` debe estar en la carpeta principal del proyecto al momento de ejecutar.

## Formato de misiles.txt

El archivo usa separador `;`:

```txt
id;tiempoAparicion;zonaObjetivo;tiempoHastaImpacto;tiempoDesactivacion
M1;1;Hospital;15;4
```

Campos:

- `id`: identificador del misil.
- `tiempoAparicion`: segundo de la simulacion en que aparece.
- `zonaObjetivo`: zona a la que va dirigido.
- `tiempoHastaImpacto`: segundos disponibles antes del impacto.
- `tiempoDesactivacion`: segundos que tarda un interceptor en desactivarlo.

## Clases principales

- `Main`: crea los objetos principales, inicia los hilos y controla la finalizacion.
- `RelojSimulacion`: hilo contador de segundos.
- `ConsolaLogger`: centraliza todos los mensajes de consola.
- `CargadorMisiles`: lee `misiles.txt`.
- `Misil`, `ZonaObjetivo`, `EstadoMisil`: clases del modelo.
- `ColaAmenazas`: cola compartida protegida con semaforos.
- `GeneradorAmenazas`: detecta misiles cuando llega su tiempo de aparicion.
- `Interceptor`: representa un recurso limitado de intercepcion.
- `ControladorImpactos`: revisa impactos en un hilo separado.
- `Planificador` y `PlanificadorFCFS`: deciden que misil atender.

## Semaforos usados

- En `ColaAmenazas`, `mutexCola` protege el acceso exclusivo a la cola.
- En `ColaAmenazas`, `disponibles` avisa a los interceptores cuando hay amenazas disponibles.
- En `Misil`, `mutexEstado` protege el estado de cada misil.
- En `RelojSimulacion`, `mutex` protege el tiempo actual y el indicador de actividad.
- En `ConsolaLogger`, `mutexSalida` evita que mensajes de distintos hilos se mezclen.
- En `Interceptor` y `ControladorImpactos`, un mutex protege el booleano de actividad.

## Planificacion

El algoritmo actual es FCFS: se atiende primero el misil que llego primero a la cola.

El planificador verifica el tiempo actual antes de devolver un misil. Si el misil ya impacto o no esta pendiente, no se asigna a ningun interceptor.

La interfaz `Planificador` permite agregar despues otros algoritmos, por ejemplo por criticidad de zona o por urgencia.

## Control de impactos

El control de impactos se realiza con un hilo separado llamado `ControladorImpactos`.

Este hilo revisa periodicamente los misiles en estado `PENDIENTE` o `EN_ATENCION`. Si el tiempo actual es mayor o igual a `tiempoAparicion + tiempoHastaImpacto`, el misil pasa a `IMPACTADO`.

Un misil impactado no puede desactivarse despues. Si impacta durante la desactivacion, el interceptor no imprime exito.
