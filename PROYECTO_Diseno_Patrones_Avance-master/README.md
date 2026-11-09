# Plataforma Integral de Atención Ciudadana Municipal

**Curso:** Diseño de Patrones | **Universidad Tecnológica del Perú (UTP)**  
**Proyecto Integrador de Curso:** Plataforma de Gestión y Seguimiento de Trámites y Reclamos Ciudadanos

---

## 1. Descripción del Proyecto

La **Plataforma Integral de Atención Ciudadana Municipal** es una aplicación de escritorio desarrollada en Java 11 con interfaz gráfica Swing y persistencia opcional en Microsoft SQL Server (con contingencia transparente en memoria). Su propósito es centralizar la recepción, validación, seguimiento, transición de estados y reversión de trámites vecinales (reclamos y solicitudes de servicio), integrando además quejas heredadas de sistemas antiguos.

El sistema sirve como caso de estudio exhaustivo para la aplicación rigurosa de los **patrones de diseño GoF (Creacionales, Estructurales y de Comportamiento)**, principios **SOLID**, patrones de asignación de responsabilidades **GRASP** y el patrón arquitectónico **MVC**.

---

## 2. Requisitos del Entorno

- **Java Development Kit (JDK):** Versión 11 o superior (compatible con JDK 17, 21 y 25).
- **Base de Datos (Opcional):** Microsoft SQL Server 2017, 2019, 2022 o 2025.
  - *Nota:* Si SQL Server no está instalado o no se encuentra disponible, la aplicación inicia y opera automáticamente con un **repositorio en memoria**, garantizando la continuidad del servicio sin errores críticos.
- **Driver JDBC:** Microsoft JDBC Driver 12.8.1 para SQL Server (`lib/mssql-jdbc-12.8.1.jre11.jar`), incluido en el proyecto.
- **Codificación:** UTF-8 en todo el código fuente y scripts.

---

## 3. Instalación y Configuración Paso a Paso

### Paso 1: Configurar la Base de Datos en SQL Server (si se utiliza persistencia física)
1. Abrir **SQL Server Management Studio (SSMS)** o **Azure Data Studio**.
2. Conectarse a la instancia local de SQL Server.
3. Abrir y ejecutar el script T-SQL ubicado en [`bd/script_municipalidad.sql`](bd/script_municipalidad.sql).
   - Este script crea la base de datos `Municipalidad`, las tablas maestras `TipoTramite`, `Ciudadano`, `Expediente`, `HistorialEstado` y el procedimiento almacenado `sp_RegistrarTramite`.
4. **Verificación de red en SQL Server Configuration Manager:**
   - Asegurarse de que el protocolo **TCP/IP** esté habilitado en la configuración de red de SQL Server.
   - Verificar que el puerto TCP asignado sea **1433**.
   - Habilitar el **modo de autenticación mixto** (SQL Server y Windows Authentication) si se conecta mediante credenciales SQL (`sa` o usuario dedicado).

### Paso 2: Configurar las Credenciales Locales
1. En la raíz del repositorio, copiar la plantilla de configuración:
   ```bash
   cp config.properties.example config.properties
   ```
2. Editar `config.properties` con los parámetros locales de su servidor:
   ```properties
   DB_SERVER=localhost
   DB_PORT=1433
   DB_NAME=Municipalidad
   DB_USER=sa
   DB_PASSWORD=tu_contrasena_segura
   ```
   > [!IMPORTANT]
   > El archivo `config.properties` está excluido del control de versiones mediante `.gitignore` para proteger la seguridad de las credenciales de desarrollo.

### Paso 3: Limpieza de Datos de Prueba (Opcional - Solo Desarrollo)
Para restablecer la base de datos a su estado inicial sin reinstalar, ejecute el script:
[`bd/limpiar_datos.sql`](bd/limpiar_datos.sql).
*(Borra `HistorialEstado`, `Expediente` y `Ciudadano`, y reinicia las identidades en 0; no altera `TipoTramite`).*

---

## 4. Compilación y Ejecución

### Opción A: Compilación desde Terminal (Java 11+)
Desde la raíz del repositorio, compile todas las clases con el classpath que incluye el driver JDBC en `lib/`:
```bash
javac --release 11 -encoding UTF-8 -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" (Get-ChildItem -Path "PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

### Opción B: Compilación con Apache Ant / NetBeans
El proyecto cuenta con el script estándar Ant en el subproyecto:
```bash
ant -f "PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/build.xml" compile
```
*(O abriendo directamente la carpeta `Plataforma_Integral_de_Atención_Ciudadana_Municipal` desde Apache NetBeans).*

### Ejecutar la Aplicación Principal (Swing UI)
```bash
java -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" vistas.Main
```

### Ejecutar la Suite Completa de Pruebas Automatizadas
```bash
java -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" pruebas.EjecutarTodasLasPruebas
```

### Ejecutar Demostraciones en Consola
- **Demostración de los 15 patrones del curso (Singleton en 2 clases) + Repository:**
  ```bash
  java -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" pruebas.DemoPatrones
  ```
- **Demostración de Importación (Composite -> Adapter -> Proxy -> Command -> Observer):**
  ```bash
  java -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" pruebas.DemoImportacionConsola
  ```
- **Diagnóstico detallado de Conexión a Base de Datos:**
  ```bash
  java -cp "lib/*;PROYECTO_Diseño_Patrones_Avance/Plataforma_Integral_de_Atención_Ciudadana_Municipal/src" pruebas.DiagnosticoConexion
  ```

---

## 5. Arquitectura del Sistema

La solución adopta el patrón arquitectónico **MVC (Modelo - Vista - Controlador)** coordinado con una capa de fachada (`MesaPartesFacade`) que desacopla la presentación de las reglas del dominio y la persistencia:

```text
[ VISTA (Swing) ]
  Main, PanelRegistro, PanelImportacion, PanelMisSolicitudes, PanelHistorialAcciones, PanelSistema
       |
       v (invoca exclusivamente)
[ CONTROLADOR ]
  AtencionController (GRASP Controlador)
       |
       v (delega en)
[ FACHADA DE SERVICIO ]
  MesaPartesFacade (Patrón Facade)
       |
  +----+------------------------+-------------------------+
  |                             |                         |
  v                             v                         v
[ COMANDOS & MEMENTO ]       [ SEGUIMIENTO & STATE ]    [ ADAPTER & COMPOSITE ]
  RegistrarTramiteComando       SeguimientoTramite        GrupoTramites (Composite)
  AvanzarEstadoComando          Estados: En Revision      AdaptadorSistemaAntiguo
  InvocadorComandos             -> Atendido -> Cerrado    SistemaAntiguoQuejas
  HistorialMementos             (Patrón State)
       |                                |
       |                                v
       |                    [ OBSERVADORES (Observer) ]
       |                      ObservadorConsola
       |                      ObservadorCiudadano (Bridge & Decorator)
       |                      ObservadorEncuesta (Prototype)
       v
[ VALIDACIÓN & PROXY ]
  TramiteProxy -> ValidadorSolicitud -> TramiteFactory (Factory Method)
       |
       v
[ REPOSITORIO (Repository) ]
  TramiteRepositorio (Interfaz)
    ├── TramiteRepositorioSqlServer (JDBC / sp_RegistrarTramite / ConexionSingleton)
    └── TramiteRepositorioMemoria (Colección atómica en memoria)
```

### Flujo de Registro de un Trámite (Diagrama ASCII)

```text
Usuario en PanelRegistro
   │
   ▼ 1. ingresar datos (DNI, Nombre, Correo, Tipo)
AtencionController.atenderCiudadano(...)
   │
   ▼ 2. registrarNuevoTramite(Ciudadano, Tipo)
MesaPartesFacade
   │
   ▼ 3. crear y ejecutar
RegistrarTramiteComando
   │
   ▼ 4. procesar()
TramiteProxy
   ├──► ValidadorSolicitud: valida formato DNI, nombre, correo, tipo y dígitos idénticos.
   │    └── Si falla: lanza SolicitudRechazadaException (NO persiste, NO apila, NO notifica).
   │
   ▼ 5. Acceso concedido
TramiteFactory.crearTramite(...) ──► Reclamo / Solicitud
   │
   ▼ 6. registrar(Ciudadano, Tipo)
TramiteRepositorio (SQL Server o Memoria)
   │
   ▼ 7. notificar(CambioEstado)
Observadores
   ├──► ObservadorConsola: imprime traza en System.out
   ├──► ObservadorCiudadano: AlertaCiudadano (Bridge) + NotificadorSMS (Decorator)
   └──► ObservadorEncuesta: Clona encuesta (Prototype) si pasa a 'Cerrado'
   │
   ▼ 8. apilar comando exitoso
InvocadorComandos.pilaDeshacer.push(comando)
```

---

## 6. Inventario de Patrones de Diseño Implementados (15 Patrones + Repository)

El proyecto implementa un total de **15 patrones de diseño GoF distintos (con el patrón Singleton aplicado en 2 clases independientes) + el patrón de arquitectura Repository**, organizados según la estructura temática del sílabo:

- **Unidad 1:** Principios SOLID, bajo acoplamiento y alta cohesión.
- **Unidad 2 (Creacionales):** 5 patrones (Singleton en 2 clases, Factory Method, Abstract Factory, Builder, Prototype).
- **Unidad 3 (Estructurales):** 6 patrones (Adapter, Facade, Decorator, Composite, Proxy, Bridge).
- **Unidad 4 (Comportamiento):** 4 patrones (State, Observer, Command, Memento).
- **Unidad 5:** Patrones de asignación de responsabilidades GRASP (Controlador, Creador, Experto en Información, Fabricación Pura, Polimorfismo).
- **Arquitectura:** Patrón Repository con desacoplamiento de persistencia y contingencia transparente en memoria.

| # | Unidad | Patrón | Tipo / Rol | Clases Reales en el Código | Propósito en el Flujo Real |
|:---:|:---:|:---|:---:|:---|:---|
| **1** | U2 | **Singleton** | Creacional | `ConexionSingleton` | Garantiza una única instancia de conexión activa a Microsoft SQL Server en toda la aplicación. |
| **2** | U2 | **Singleton** | Creacional | `ConfiguracionPlataforma` | Almacena los parámetros organizacionales del municipio; thread-safe vía Bill Pugh Holder. |
| **3** | U2 | **Factory Method** | Creacional | `TramiteFactory` | Instancia el producto concreto (`Reclamo` o `Solicitud`) desacoplando a los clientes de las clases concretas. |
| **4** | U2 | **Abstract Factory** | Creacional | `FabricaAtencion`, `FabricaReclamo`, `FabricaSolicitud` | Define familias de productos complementarios (*exclusivo de demostración técnica en `pruebas.DemoPatrones`*). |
| **5** | U2 | **Builder** | Creacional | `Expediente.ExpedienteBuilder` | Construye paso a paso la entidad inmutable `Expediente` asegurando la coherencia de todos sus atributos. |
| **6** | U2 | **Prototype** | Creacional | `EncuestaSatisfaccion` | Clona dinámicamente (`clone()`) la encuesta de satisfacción para enviarla al ciudadano cuando su trámite se cierra. |
| **7** | U3 | **Facade** | Estructural | `MesaPartesFacade` | Punto único de entrada de alto nivel; simplifica la interacción coordinando repositorio, comandos, estados y observadores. |
| **8** | U3 | **Adapter** | Estructural | `AdaptadorSistemaAntiguo`, `SistemaAntiguoQuejas` | Traduce quejas vecinales del formato heredado al modelo `Ciudadano` y contrato `Tramite` actual para su importación. |
| **9** | U3 | **Composite** | Estructural | `GrupoTramites` | Agrupa lotes de trámites heredados y los procesa de manera uniforme mediante una misma interfaz. |
| **10** | U3 | **Proxy** | Estructural | `TramiteProxy`, `ValidadorSolicitud` | Controla el acceso al registro: valida reglas de negocio, autoriza o deniega con excepciones, y retrasa la creación real. |
| **11** | U3 | **Decorator** | Estructural | `Notificador`, `NotificadorBase`, `NotificadorDecorator`, `NotificadorSMS` | Añade dinámicamente el canal SMS sobre el canal base de la plataforma para notificaciones ciudadanas (*simulado por consola*). |
| **12** | U3 | **Bridge** | Estructural | `AlertaCiudadano`, `CanalNotificacion`, `CanalCorreo`, `CanalSMS` | Desacopla la abstracción de alertas de los canales concretos de entrega Correo y SMS (*simulado por consola*). |
| **13** | U4 | **State** | Comportamiento | `EstadoTramite`, `EstadoEnRevision`, `EstadoAtendido`, `EstadoCerrado`, `SeguimientoTramite` | Modela el ciclo de vida del trámite: *"En Revision"* -> *"Atendido"* -> *"Cerrado"*, resolviendo polimórficamente cada transición. |
| **14** | U4 | **Observer** | Comportamiento | `ObservadorTramite`, `CambioEstado`, `ObservadorConsola`, `ObservadorCiudadano`, `ObservadorEncuesta` | Notifica automáticamente a múltiples componentes desacoplados ante registros, cambios de estado o reversiones. |
| **15** | U4 | **Command** | Comportamiento | `Comando`, `RegistrarTramiteComando`, `AvanzarEstadoComando`, `InvocadorComandos` | Encapsula operaciones como objetos ejecutables y reversibles, gestionando la pila de deshacer (*Undo*). |
| **16** | U4 | **Memento** | Comportamiento | `MementoSeguimiento`, `HistorialMementos` | Captura y restaura instantáneas del estado interno del expediente para permitir reversiones de estado seguras. |
| **—** | Arq | **Repository** | Arquitectura | `TramiteRepositorio`, `TramiteRepositorioSqlServer`, `TramiteRepositorioMemoria`, `RepositorioFactory` | Abstrae la persistencia física permitiendo conmutar entre base de datos SQL Server y memoria de forma transparente. |

---

## 7. Principios SOLID en el Proyecto (Unidad 1: SOLID, Bajo Acoplamiento, Alta Cohesión)

| Principio SOLID | Definición Concreta | Evidencia en Clases Reales |
|---|---|---|
| **S - Single Responsibility** | Cada clase tiene una sola razón de cambio y un único propósito bien delimitado. | `ValidadorSolicitud` valida reglas de entrada; `ConexionSingleton` solo gestiona conexiones JDBC; `HistorialMementos` únicamente almacena instantáneas. |
| **O - Open/Closed** | El sistema está abierto a la extensión pero cerrado a la modificación. | Para añadir un nuevo canal de notificación basta con crear una clase que implemente `CanalNotificacion` sin alterar `AlertaCiudadano` (Bridge); igual con nuevos observadores en `ObservadorTramite`. |
| **L - Liskov Substitution** | Las subclases o implementaciones deben ser sustituibles por sus tipos base sin alterar el comportamiento. | `TramiteRepositorioMemoria` sustituye completamente a `TramiteRepositorioSqlServer` bajo la interfaz `TramiteRepositorio` sin que `MesaPartesFacade` note la diferencia. |
| **I - Interface Segregation** | Los clientes no deben verse obligados a depender de métodos que no utilizan. | `Tramite` define únicamente `procesar()`; `ObservadorTramite` define solo `actualizar(CambioEstado)`; `Comando` define solo `ejecutar()`, `deshacer()` y `getDescripcion()`. |
| **D - Dependency Inversion** | Los módulos de alto nivel no deben depender de los de bajo nivel, sino de abstracciones. | `AtencionController` depende de `MesaPartesFacade`; `MesaPartesFacade` depende de `TramiteRepositorio` (interfaz) y de `List<ObservadorTramite>`, nunca de clases concretas de base de datos. |

---

## 8. Patrones GRASP en el Proyecto (Unidad 5: GRASP)

| Patrón GRASP | Definición Concreta | Evidencia en Clases Reales |
|---|---|---|
| **Controlador** | Primer objeto detrás de la capa de interfaz gráfica que recibe y coordina las solicitudes del usuario. | `controladores.AtencionController`: intermediario exclusivo entre las vistas Swing y la lógica del dominio. |
| **Creador** | Asigna la responsabilidad de crear una instancia a la clase que la almacena, agrega o usa directamente. | `TramiteFactory` crea `Reclamo` y `Solicitud`; `FabricaAtencion` crea familias de trámites; `ExpedienteBuilder` ensambla `Expediente`. |
| **Experto en Información** | Asigna una responsabilidad a la clase que posee la información necesaria para llevarla a cabo. | `Expediente` mantiene los datos del trámite; `SeguimientoTramite` conoce el estado actual y su avance; `EstadoTramite` conoce si es terminal. |
| **Fabricación Pura** | Clase inventada que no representa un concepto del dominio del negocio para mantener alta cohesión y bajo acoplamiento. | `TramiteRepositorioMemoria`, `InvocadorComandos`, `HistorialMementos`, `ValidadorSolicitud`, `EstiloVista`, `AccionDeshacer`. |
| **Polimorfismo** | Distribuye responsabilidades basadas en el tipo usando operaciones polimórficas en lugar de condicionales explícitos. | `Tramite.procesar()`, `EstadoTramite.siguiente()`, `CanalNotificacion.enviar()`, `ObservadorTramite.actualizar()`, `Comando.ejecutar()` y `Comando.deshacer()`. |
| **Alta Cohesión** | Mantiene las responsabilidades de cada clase estrechamente enfocadas y comprensibles. | `PanelImportacion` solo gestiona la vista de importación; `EstiloVista` solo administra métricas y tipografías. |
| **Bajo Acoplamiento** | Minimiza las dependencias entre componentes para facilitar el mantenimiento y las pruebas. | Las vistas ignoran por completo los patrones estructurales y de base de datos, interactuando únicamente con el controlador. |

---

## 9. Estructura de Carpetas

```text
PROYECTO_Diseno_Patrones_Avance/
├── .gitignore                                  <- Configuración de exclusión de Git
├── README.md                                   <- Documentación integral del proyecto
├── config.properties.example                   <- Plantilla de credenciales sin datos sensibles
├── bd/
│   ├── script_municipalidad.sql                <- Script T-SQL oficial de creación de BD y tablas
│   └── limpiar_datos.sql                       <- Script auxiliar para reiniciar datos de prueba
├── docs/
│   ├── INVENTARIO_PATRONES.md                  <- Inventario técnico de los 15 patrones (Singleton en 2 clases) + Repository
│   └── CASOS_PRUEBA.md                         <- Matriz completa de casos de prueba P01-Pn
├── lib/
│   └── mssql-jdbc-12.8.1.jre11.jar             <- Driver JDBC oficial para SQL Server
└── PROYECTO_Diseño_Patrones_Avance/
    └── Plataforma_Integral_de_Atención_Ciudadana_Municipal/
        └── src/
            ├── controladores/
            │   └── AtencionController.java     <- Controlador MVC (GRASP Controlador)
            ├── modelos/
            │   └── Ciudadano.java              <- Entidad de dominio
            ├── patrones/
            │   ├── creacionales/
            │   │   ├── abstractfactory/        <- Abstract Factory
            │   │   ├── builder/                <- Builder (Expediente y Builder)
            │   │   ├── factory/                <- Factory Method (TramiteFactory)
            │   │   ├── productos/              <- Productos (Tramite, Reclamo, Solicitud)
            │   │   ├── prototype/              <- Prototype (EncuestaSatisfaccion)
            │   │   └── singleton/              <- Singleton (ConexionSingleton, ConfiguracionPlataforma)
            │   ├── estructurales/
            │   │   ├── adapter/                <- Adapter (AdaptadorSistemaAntiguo, SistemaAntiguoQuejas)
            │   │   ├── bridge/                 <- Bridge (AlertaCiudadano, CanalNotificacion, etc.)
            │   │   ├── composite/              <- Composite (GrupoTramites)
            │   │   ├── decorator/              <- Decorator (Notificador, NotificadorSMS, etc.)
            │   │   ├── facade/                 <- Facade (MesaPartesFacade, ResultadoImportacion)
            │   │   └── proxy/                  <- Proxy (TramiteProxy, ValidadorSolicitud, Excepcion)
            │   └── comportamiento/
            │       ├── command/                <- Command (Invocador, Registrar, Avanzar)
            │       ├── memento/                <- Memento (MementoSeguimiento, HistorialMementos)
            │       ├── observer/               <- Observer (ObservadorTramite, Consola, Ciudadano, etc.)
            │       └── state/                  <- State (EstadoTramite, EnRevision, Atendido, Cerrado)
            ├── repositorios/                   <- Patrón Repository (Memoria, SQL Server, Factory)
            ├── vistas/                         <- Interfaz Swing modular (Main, Paneles, EstiloVista)
            └── pruebas/                        <- Suites automatizadas y ejecutor integral
```

---

## 10. Seguridad y Confidencialidad

1. **Aislamiento de Credenciales:**
   - El archivo de configuración `config.properties` se encuentra permanentemente ignorado por Git.
   - Solo se versiona `config.properties.example` con marcadores de posición (`tu_usuario`, `tu_contrasena`).
2. **Protección de Datos en Trazas y Registros:**
   - La clase `ConexionSingleton` y los diagnósticos enmascaran automáticamente las contraseñas en cualquier cadena URL (`password=***`).
   - El método `isPasswordDefinido()` informa únicamente si la variable está definida o no, sin exponer nunca su valor en consola ni interfaz gráfica.
3. **Datos Ficticios en Demostraciones y Pruebas:**
   - Todos los registros de ejemplo, pruebas e importaciones del sistema antiguo emplean nombres, DNIs y correos inventados (ej. `74859612 / gonzalo.solar@utp.edu.pe`, `45891234 / Mariana Torres Paz`), sin utilizar información real de ninguna persona.

---

## 11. Limitaciones Conocidas

1. **Canales de Notificación Externos Simulados:**
   - El envío de correos electrónicos y mensajes SMS (patrones Bridge y Decorator) se ejecuta de forma simulada en consola (`System.out`), sin conexión real a gateways SMTP o SMS comerciales.
2. **Entorno de Escritorio:**
   - La aplicación está diseñada como cliente de escritorio Java Swing estándar; no dispone de API REST ni servidor web.
3. **Control de Acceso de Usuarios:**
   - No implementa autenticación ni inicio de sesión con roles (administrador, cajero, vecino); el control de acceso implementado mediante el patrón **Proxy** se enfoca en la validación técnica y de negocio de los datos del ciudadano que ingresa cada solicitud.
