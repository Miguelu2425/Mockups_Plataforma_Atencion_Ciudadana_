package pruebas;

import modelos.Ciudadano;
import patrones.comportamiento.observer.ObservadorTramite;
import patrones.creacionales.builder.Expediente;
import patrones.estructurales.facade.MesaPartesFacade;
import patrones.estructurales.proxy.SolicitudRechazadaException;
import patrones.estructurales.proxy.ValidadorSolicitud;
import repositorios.TramiteRepositorioMemoria;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pruebas: valida el comportamiento del Patrón Proxy y la clase ValidadorSolicitud
 * tanto de forma aislada como en la orquestación integral con Command y Observer.
 */
public class PruebaProxy {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" INICIANDO PRUEBAS DEL PATRON PROXY Y VALIDACION");
        System.out.println("======================================================================");

        probarValidacionDniSieteDigitos();
        probarValidacionDniConLetras();
        probarValidacionDniDigitosIguales();
        probarValidacionNombreVacio();
        probarValidacionNombreConNumeros();
        probarValidacionNombreMuyCorto();
        probarValidacionCorreoInvalido();
        probarValidacionTipoDesconocido();
        probarRegistroValidoYDeshacer();

        System.out.println("\n=== RESULTADO PRUEBA PROXY: " + ok + " OK, " + fallo + " FALLO ===");
        if (fallo > 0) {
            throw new AssertionError("PruebaProxy finalizo con " + fallo + " fallos.");
        }
    }

    private static void probarValidacionDniSieteDigitos() {
        System.out.println("\n--- [1] Rechazo por DNI de 7 dígitos ---");
        Ciudadano c = new Ciudadano("1234567", "Carlos Lopez", "carlos@test.com");
        ejecutarYVerificarRechazo(c, "reclamo", "8 dígitos numéricos");
    }

    private static void probarValidacionDniConLetras() {
        System.out.println("\n--- [2] Rechazo por DNI con letras ---");
        Ciudadano c = new Ciudadano("1234567A", "Carlos Lopez", "carlos@test.com");
        ejecutarYVerificarRechazo(c, "solicitud", "8 dígitos numéricos");
    }

    private static void probarValidacionDniDigitosIguales() {
        System.out.println("\n--- [3] Rechazo por DNI con 8 dígitos idénticos ---");
        Ciudadano c = new Ciudadano("00000000", "Carlos Lopez", "carlos@test.com");
        ejecutarYVerificarRechazo(c, "solicitud", "DNI no permitido");
    }

    private static void probarValidacionNombreVacio() {
        System.out.println("\n--- [4] Rechazo por nombre vacío ---");
        Ciudadano c = new Ciudadano("74859612", "", "carlos@test.com");
        ejecutarYVerificarRechazo(c, "reclamo", "nombre");
    }

    private static void probarValidacionNombreConNumeros() {
        System.out.println("\n--- [5] Rechazo por nombre con caracteres numéricos ---");
        Ciudadano c = new Ciudadano("74859612", "Juan 123 Perez", "juan@test.com");
        ejecutarYVerificarRechazo(c, "reclamo", "nombre");
    }

    private static void probarValidacionNombreMuyCorto() {
        System.out.println("\n--- [6] Rechazo por nombre con menos de 3 caracteres ---");
        Ciudadano c = new Ciudadano("74859612", "Al", "al@test.com");
        ejecutarYVerificarRechazo(c, "solicitud", "nombre");
    }

    private static void probarValidacionCorreoInvalido() {
        System.out.println("\n--- [7] Rechazo por correo electrónico inválido ---");
        Ciudadano c = new Ciudadano("74859612", "Ana Maria Rios", "correo_sin_arroba.com");
        ejecutarYVerificarRechazo(c, "reclamo", "correo");
    }

    private static void probarValidacionTipoDesconocido() {
        System.out.println("\n--- [8] Rechazo por tipo de trámite desconocido ---");
        Ciudadano c = new Ciudadano("74859612", "Ana Maria Rios", "ana@test.com");
        ejecutarYVerificarRechazo(c, "consulta_tributaria", "desconocido");
    }

    private static void ejecutarYVerificarRechazo(Ciudadano c, String tipo, String palabraClaveMotivo) {
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        AtomicInteger notificaciones = new AtomicInteger(0);
        ObservadorTramite obs = cambio -> notificaciones.incrementAndGet();

        MesaPartesFacade facade = new MesaPartesFacade(repo, Collections.singletonList(obs));

        boolean excepcionLanzada = false;
        try {
            facade.registrarNuevoTramite(c, tipo);
        } catch (SolicitudRechazadaException ex) {
            excepcionLanzada = true;
            boolean contienePalabra = ex.getMessage().toLowerCase().contains(palabraClaveMotivo.toLowerCase());
            afirmar(contienePalabra, "Motivo de rechazo contiene '" + palabraClaveMotivo + "'");
        } catch (Exception ex) {
            afirmar(false, "Se esperaba SolicitudRechazadaException pero fue " + ex.getClass().getSimpleName());
        }

        afirmar(excepcionLanzada, "Proxy denego el acceso y lanzo SolicitudRechazadaException");
        afirmar(repo.listar(null).isEmpty(), "No se registro ningun expediente en el repositorio");
        afirmar(notificaciones.get() == 0, "Los observadores no recibieron ninguna notificacion");
        afirmar(!facade.puedeDeshacer(), "La pila de deshacer permanece vacia (puedeDeshacer es false)");
    }

    private static void probarRegistroValidoYDeshacer() {
        System.out.println("\n--- [9] Registro válido: autorización Proxy y apilado en deshacer ---");
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();
        AtomicInteger notificaciones = new AtomicInteger(0);
        ObservadorTramite obs = cambio -> notificaciones.incrementAndGet();

        MesaPartesFacade facade = new MesaPartesFacade(repo, Collections.singletonList(obs));
        Ciudadano c = new Ciudadano("74859612", "Gonzalo Solar Silva", "gonzalo.solar@utp.edu.pe");

        Expediente exp = facade.registrarNuevoTramite(c, "reclamo");

        afirmar(exp != null, "Expediente creado tras autorizacion del Proxy");
        afirmar(repo.listar(null).size() == 1, "Repositorio contiene 1 expediente");
        afirmar(notificaciones.get() == 1, "Observador recibio la notificacion de registro");
        afirmar(facade.puedeDeshacer(), "Comando valido fue apilado en deshacer (puedeDeshacer es true)");

        // Deshacer el registro autorizado
        String accion = facade.deshacerUltimaAccion();
        afirmar(accion != null && accion.contains("EXP-"), "Accion deshecha contiene el codigo del expediente");
        afirmar(repo.listar(null).isEmpty(), "Expediente fue eliminado del repositorio tras deshacer");
        afirmar(!facade.puedeDeshacer(), "Pila de deshacer vuelve a quedar vacia");
    }

    private static void afirmar(boolean condicion, String mensaje) {
        if (condicion) {
            ok++;
            System.out.println("[OK]    " + mensaje);
        } else {
            fallo++;
            System.err.println("[FALLO] " + mensaje);
        }
    }
}

