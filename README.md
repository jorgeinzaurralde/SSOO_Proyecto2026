# Simulador de defensa aerea

## Instrucciones rapidas para compilar y ejecutar

Abrir una terminal en la carpeta principal del proyecto:

```powershell
cd C:\UCU\SO\SSOO_Proyecto2026
```

Primero compilar. Este comando crea la carpeta `out` con los archivos `.class`:

```powershell
$fuentes = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $fuentes
```

Despues ejecutar:

```powershell
java -cp out Main
```

El programa va a pedir ingresar un escenario:

```txt
1 - Funcionamiento basico
2 - Saturacion de recursos
3 - Prioridad por holgura y criticidad
```

Importante: si aparece el error `Could not find or load main class Main`, significa que todavia no se compilo o que la carpeta `out` fue borrada. Hay que volver a ejecutar primero el comando de compilacion.

Proyecto simple en Java para una materia de Sistemas Operativos. Simula un sistema de defensa aerea donde aparecen misiles, se cargan en una cola compartida y son atendidos por interceptores limitados.

La sincronizacion se realiza con `java.util.concurrent.Semaphore`. No se usan librerias externas.

## Como compilar

En Windows PowerShell:

```powershell
$fuentes = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $fuentes
```

En Linux, macOS o consolas con soporte para `**`:

```bash
javac -d out src/**/*.java src/*.java
```

## Como ejecutar

```bash
java -cp out Main
```

Al iniciar, el programa pide ingresar el numero de escenario:

```txt
1 - Funcionamiento basico
2 - Saturacion de recursos
3 - Prioridad por holgura y criticidad
```

## Archivos de escenarios

- `misiles.txt`: escenario 1, funcionamiento basico con 2 interceptores.
- `misiles_escenario2.txt`: escenario 2, saturacion con 1 interceptor y varios misiles casi juntos.
- `misiles_escenario3.txt`: escenario 3, caso mas complejo con 2 interceptores, cola acumulada y prioridad por holgura.

## Formato de los archivos de misiles

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

- `Main`: pide el escenario, crea los objetos principales, inicia los hilos y controla la finalizacion.
- `RelojSimulacion`: hilo contador de segundos. En cada segundo pide reordenar la cola.
- `ConsolaLogger`: centraliza todos los mensajes de consola.
- `CargadorMisiles`: lee el archivo `.txt` del escenario elegido.
- `Misil`, `ZonaObjetivo`, `EstadoMisil`: clases del modelo.
- `ColaAmenazas`: cola compartida protegida con semaforos.
- `PriorizadorAmenazas`: calcula prioridad y reordena la cola.
- `GeneradorAmenazas`: detecta misiles cuando llega su tiempo de aparicion.
- `Interceptor`: representa un recurso limitado de intercepcion.
- `ControladorImpactos`: revisa impactos en un hilo separado.
- `Planificador` y `PlanificadorFCFS`: el planificador sigue tomando el primer misil de la cola.

## Formula de prioridad

La cola se reordena usando:

```txt
deadline = tiempoAparicion + tiempoHastaImpacto
tiempoRestante = deadline - tiempoActual
holgura = tiempoRestante - tiempoDesactivacion
presion = max(1, cantidadMisilesPendientes / cantidadInterceptores)

prioridad =
criticidad * (1 + presion * (tiempoDesactivacion / tiempoRestante))
---------------------------------------------------------------
holgura + 1
```

Se atiende primero el misil con mayor prioridad. Si la holgura es menor o igual a cero, el misil queda al final porque con la regla de impacto `>=` ya no llega a desactivarse a tiempo.

## Manejo de criticidades

La criticidad de la zona se usa de dos formas:

1. Como parte de la formula de prioridad. Una zona mas critica aumenta el puntaje del misil, por lo que tiende a quedar antes en la cola.
2. Como regla de proteccion entre amenazas ya detectadas. Antes de ordenar solo por puntaje, el priorizador compara pares de misiles y evita que una amenaza de menor criticidad pase por encima de una amenaza de mayor criticidad si atender primero la amenaza menor haria que la mayor ya no pueda desactivarse a tiempo.

La regla de proteccion se puede resumir asi:

```txt
Si amenazaMenor.criticidad < amenazaMayor.criticidad
y atender amenazaMenor primero hace que amenazaMayor impacte,
entonces amenazaMayor debe quedar antes en la cola.
```

Con esto, la urgencia sigue importando, pero no puede condenar una zona mas critica cuando todavia era posible protegerla.

Importante: esta regla se aplica solo sobre misiles que ya fueron detectados y estan en la cola. El sistema no reserva interceptores para amenazas futuras que todavia no aparecieron en la simulacion.

## Planificacion

El algoritmo `PlanificadorFCFS` se mantiene simple: toma el primer misil de la cola.

La diferencia es que la cola se reordena por prioridad cada segundo y tambien cuando se agregan nuevos misiles. Por eso, para FCFS, el primer elemento de la cola ya representa la amenaza mas conveniente de atender.

## Semaforos usados

- En `ColaAmenazas`, `mutexCola` protege el acceso exclusivo a la cola.
- En `ColaAmenazas`, `disponibles` avisa a los interceptores cuando hay amenazas disponibles.
- En `Misil`, `mutexEstado` protege el estado de cada misil.
- En `RelojSimulacion`, `mutex` protege el tiempo actual y el indicador de actividad.
- En `ConsolaLogger`, `mutexSalida` evita que mensajes de distintos hilos se mezclen.
- En `Interceptor` y `ControladorImpactos`, un mutex protege el booleano de actividad.

## Control de impactos

El control de impactos se realiza con un hilo separado llamado `ControladorImpactos`.

Este hilo revisa periodicamente los misiles en estado `PENDIENTE` o `EN_ATENCION`. Si el tiempo actual es mayor o igual a `tiempoAparicion + tiempoHastaImpacto`, el misil pasa a `IMPACTADO`.

Un misil impactado no puede desactivarse despues. Si impacta durante la desactivacion, el interceptor no imprime exito.

## Restricciones respetadas

- No se usa `synchronized`.
- No se usan monitores, `wait`, `notify` ni `notifyAll`.
- No se usan `ThreadPool`, `ExecutorService`, `BlockingQueue`, `ReentrantLock` ni estructuras concurrentes avanzadas.
- La sincronizacion se hace con `java.util.concurrent.Semaphore`.
