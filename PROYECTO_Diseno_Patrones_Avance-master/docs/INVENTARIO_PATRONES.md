# Inventario Técnico de Patrones de Diseño

**Proyecto:** Plataforma Integral de Atención Ciudadana Municipal  
**Curso:** Diseño de Patrones | UTP  
**Auditoría de Código:** Inspección directa del código fuente en `src/`.

---

## Resumen Ejecutivo de Implementación

El proyecto implementa un total de **15 patrones de diseño GoF distintos (con el patrón Singleton aplicado en 2 clases independientes) + el patrón de arquitectura Repository**, organizados según la estructura temática del sílabo:

- **Unidad 1:** Principios SOLID, bajo acoplamiento y alta cohesión.
- **Unidad 2 (Creacionales):** 5 patrones (Singleton aplicado en 2 clases, Factory Method, Abstract Factory, Builder, Prototype).
- **Unidad 3 (Estructurales):** 6 patrones (Adapter, Facade, Decorator, Composite, Proxy, Bridge).
- **Unidad 4 (Comportamiento):** 4 patrones (State, Observer, Command, Memento).
- **Unidad 5:** Patrones de asignación de responsabilidades GRASP (Controlador, Creador, Experto en Información, Fabricación Pura, Polimorfismo).
- **Arquitectura:** Patrón Repository con desacoplamiento de persistencia y conmutación transparente a memoria.

Todos los patrones participan activamente en el **flujo real de producción**, a excepción de `Abstract Factory` que se conserva como componente de demostración técnica de familias de productos en `pruebas.DemoPatrones`. Los canales de notificación externa (correo y SMS) son **simulados** mediante trazas en consola (`System.out`), y la plataforma no requiere autenticación ni gestión de credenciales de usuario vecino.

---

## Tabla de Mapeo Técnico Exhaustivo

> [!NOTE]
> Son **15 patrones de diseño GoF distintos (Singleton aplicado en 2 clases) + Repository**. En la tabla se detallan ambas clases Singleton para reflejar su rol específico en la arquitectura.

| # | Unidad | Patrón | Paquete en `src/` | Nombres Exactos de Clases e Interfaces | Rol en la Arquitectura | Punto Exacto de Invocación en el Flujo Real |
|:---:|:---:|:---|:---|:---|:---|:---|
| **1** | U2 | **Singleton (Conexión BD)** | `patrones.creacionales.singleton` | `ConexionSingleton` | Administrador de conexión única JDBC a SQL Server | `repositorios.RepositorioFactory.crear()` y `controladores.AtencionController.probarConexion()` llamando a `ConexionSingleton.getInstancia().getConexion()`. |
| **2** | U2 | **Singleton (Configuración)** | `patrones.creacionales.singleton` | `ConfiguracionPlataforma` | Almacén global de configuración municipal (Bill Pugh Thread-Safe) | `vistas.Main.<init>()` y `vistas.PanelSistema.actualizarDatos()` invocando `ConfiguracionPlataforma.getInstancia().getNombreMunicipio()`. |
| **3** | U2 | **Factory Method** | `patrones.creacionales.factory`<br>`patrones.creacionales.productos` | `TramiteFactory`<br>`Tramite`, `Reclamo`, `Solicitud` | Fábrica simple desacoplada de productos concretos | `patrones.estructurales.proxy.TramiteProxy.procesar()` invocando `TramiteFactory.crearTramite(tipoTramite).procesar()`. |
| **4** | U2 | **Abstract Factory** | `patrones.creacionales.abstractfactory` | `FabricaAtencion`<br>`FabricaReclamo`, `FabricaSolicitud` | Familia abstracta de productos complementarios | *Exclusivo de demostración técnica:* invocado en `pruebas.DemoPatrones.demostrarPatrones()`. |
| **5** | U2 | **Builder** | `patrones.creacionales.builder` | `Expediente`<br>`Expediente.ExpedienteBuilder` | Construcción por pasos de la entidad inmutable del trámite | `repositorios.TramiteRepositorioMemoria.registrar()` y `TramiteRepositorioSqlServer.registrar()` mediante `new Expediente.ExpedienteBuilder().build()`. |
| **6** | U2 | **Prototype** | `patrones.creacionales.prototype` | `EncuestaSatisfaccion` | Clonación rápida en memoria de encuestas de atención | `patrones.comportamiento.observer.ObservadorEncuesta.actualizar(CambioEstado)` ejecutando `prototipo.clone().enviarEncuesta()` cuando el trámite pasa a *"Cerrado"`. |
| **7** | U3 | **Facade** | `patrones.estructurales.facade` | `MesaPartesFacade`<br>`ResultadoImportacion` | Fachada unificada de alto nivel para el controlador | `controladores.AtencionController.atenderCiudadano()`, `avanzarEstado()`, `deshacerUltimaAccion()` e `importarQuejasAntiguas()`. |
| **8** | U3 | **Adapter** | `patrones.estructurales.adapter` | `AdaptadorSistemaAntiguo`<br>`SistemaAntiguoQuejas`<br>`RegistroQuejaAntigua` | Adaptación de registros antiguos a `Ciudadano` y contrato `Tramite` | `patrones.estructurales.facade.MesaPartesFacade.importarQuejasAntiguas()` adapta cada registro heredado y extrae `getCiudadano()`. |
| **9** | U3 | **Composite** | `patrones.estructurales.composite` | `GrupoTramites` | Tratamiento uniforme de colecciones de trámites como uno solo | `patrones.estructurales.facade.MesaPartesFacade.importarQuejasAntiguas()` agrega adaptadores a `GrupoTramites` y ejecuta `grupo.procesar()`. |
| **10** | U3 | **Proxy** | `patrones.estructurales.proxy` | `TramiteProxy`<br>`ValidadorSolicitud`<br>`SolicitudRechazadaException` | Control de acceso, validación de reglas y creación perezosa | `patrones.comportamiento.command.RegistrarTramiteComando.ejecutar()` ejecuta `new TramiteProxy(ciudadano, tipo).procesar()` antes de persistir. |
| **11** | U3 | **Bridge** | `patrones.estructurales.bridge` | `AlertaCiudadano`<br>`CanalNotificacion`<br>`CanalCorreo`, `CanalSMS` | Desacopla la lógica de alertas del medio de transmisión físico | `patrones.comportamiento.observer.ObservadorCiudadano.actualizar(CambioEstado)` ejecuta `new AlertaCiudadano(new CanalCorreo()).enviar(...)` (simulado por consola). |
| **12** | U3 | **Decorator** | `patrones.estructurales.decorator` | `Notificador`<br>`NotificadorBase`<br>`NotificadorDecorator`<br>`NotificadorSMS` | Enriquecimiento dinámico de canales de notificación | `patrones.comportamiento.observer.ObservadorCiudadano.actualizar(CambioEstado)` ejecuta `new NotificadorSMS(new NotificadorBase()).enviar(...)` (simulado por consola). |
| **13** | U4 | **State** | `patrones.comportamiento.state` | `EstadoTramite`<br>`EstadosTramite`<br>`EstadoEnRevision`<br>`EstadoAtendido`<br>`EstadoCerrado`<br>`SeguimientoTramite` | Transición polimórfica del ciclo de vida del trámite | `patrones.comportamiento.command.AvanzarEstadoComando.ejecutar()` ejecuta `seguimiento.avanzarEstado()`, transicionando `En Revision` -> `Atendido` -> `Cerrado`. |
| **14** | U4 | **Observer** | `patrones.comportamiento.observer` | `ObservadorTramite`<br>`CambioEstado`<br>`ObservadorConsola`<br>`ObservadorCiudadano`<br>`ObservadorEncuesta` | Publicación y suscripción desacoplada ante eventos de cambio | `RegistrarTramiteComando.ejecutar()`, `AvanzarEstadoComando.ejecutar()` y `AvanzarEstadoComando.deshacer()` notifican a la lista de observadores. |
| **15** | U4 | **Command** | `patrones.comportamiento.command` | `Comando`<br>`RegistrarTramiteComando`<br>`AvanzarEstadoComando`<br>`InvocadorComandos` | Encapsulación de acciones y gestión de la pila de deshacer (*Undo*) | `MesaPartesFacade.registrarNuevoTramite()` y `avanzarEstado()` ejecutan vía `invocador.ejecutar(comando)`. `deshacerUltimaAccion()` desapila y revierte. |
| **16** | U4 | **Memento** | `patrones.comportamiento.memento` | `MementoSeguimiento`<br>`HistorialMementos` | Captura y restauración del estado interno del expediente | `AvanzarEstadoComando.ejecutar()` almacena el memento con `historial.guardar(memento)` y `AvanzarEstadoComando.deshacer()` lo restaura con `seguimiento.restaurarMemento(mementoPrevio)`. |
| **—** | Arq | **Repository** | `repositorios` | `TramiteRepositorio`<br>`TramiteRepositorioSqlServer`<br>`TramiteRepositorioMemoria`<br>`RepositorioFactory`<br>`RepositorioException` | Abstracción de acceso a datos y aislamiento de base de datos | Invocado por los comandos `RegistrarTramiteComando` y `AvanzarEstadoComando` para persistir, consultar, actualizar o eliminar trámites. |

---

## Detalle de Flujos de Integración Reales

### 1. Flujo Integrado de Registro (Proxy -> Factory -> Repository -> Observer -> Command)
1. El usuario envía el formulario desde `vistas.PanelRegistro`.
2. `AtencionController.atenderCiudadano()` recibe los datos y delega en `MesaPartesFacade.registrarNuevoTramite()`.
3. La Facade instancia `RegistrarTramiteComando` y lo entrega al `InvocadorComandos`.
4. El comando invoca `TramiteProxy.procesar()`:
   - Si `ValidadorSolicitud` detecta fallos (DNI no numérico, dígitos repetidos, nombre corto, etc.), lanza `SolicitudRechazadaException`. La ejecución se interrumpe: el comando **NO** se persiste, **NO** notifica a observadores y **NO** entra a la pila de deshacer.
   - Si la validación es aprobada, el Proxy delega en `TramiteFactory.crearTramite()`.
5. Se persiste el expediente en `TramiteRepositorio` mediante `repositorio.registrar()`.
6. Se notifica a los observadores (`ObservadorConsola`, `ObservadorCiudadano` mediante Bridge/Decorator).
7. `InvocadorComandos` apila el comando en `pilaDeshacer`.

### 2. Flujo Integrado de Importación (Composite -> Adapter -> Proxy -> Command -> Observer)
1. El usuario pulsa *"Importar quejas antiguas"* en `vistas.PanelImportacion`.
2. `MesaPartesFacade.importarQuejasAntiguas()` obtiene los registros heredados desde `SistemaAntiguoQuejas`.
3. Cada registro se encapsula en un `AdaptadorSistemaAntiguo`, traduciéndolo al contrato `Tramite` y modelo `Ciudadano`.
4. Todos los adaptadores se agregan a `GrupoTramites` (Composite) y se procesan en bloque.
5. Para cada trámite del grupo, se ejecuta `registrarNuevoTramite()` pasando por el ciclo completo:
   - Los registros conformes se importan, se notifican y se apilan como comandos reversibles.
   - Los registros que violan las reglas del Proxy (ej. DNI de 7 dígitos) son rechazados individualmente sin detener el lote.
6. El resultado consolidado se entrega a la interfaz en un `ResultadoImportacion`.

### 3. Flujo Integrado de Avance de Estado y Reversión (State -> Memento -> Observer -> Command)
1. El usuario pulsa *"Avanzar estado"* en `vistas.PanelMisSolicitudes`.
2. Se ejecuta `AvanzarEstadoComando`:
   - Se captura una instantánea `MementoSeguimiento` del estado actual (`En Revision`).
   - El estado transiciona polimórficamente a `Atendido` mediante `EstadoTramite.siguiente()`.
   - Se persiste el cambio en el repositorio y se notifica a los observadores.
   - El comando queda en la pila de deshacer.
3. Al pulsar *"Deshacer última acción"*:
   - `InvocadorComandos.deshacerUltimo()` desapila el comando.
   - `AvanzarEstadoComando.deshacer()` toma el memento previo, restaura el estado en `SeguimientoTramite` y actualiza la persistencia.
   - Se emite una notificación especial con bandera `esReversion = true`.

