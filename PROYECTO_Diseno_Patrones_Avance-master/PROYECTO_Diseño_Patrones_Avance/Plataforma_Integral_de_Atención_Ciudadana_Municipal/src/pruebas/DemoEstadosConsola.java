package pruebas;

import controladores.AtencionController;
import patrones.creacionales.builder.Expediente;

/**
 * Demostracion completa del flujo de un tramite con State, Observer,
 * Bridge, Decorator y Prototype.
 */
public class DemoEstadosConsola {

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println(" PLATAFORMA INTEGRAL DE ATENCION CIUDADANA MUNICIPAL");
        System.out.println(" DEMOSTRACION: PATRONES STATE Y OBSERVER CONECTADOS");
        System.out.println("======================================================================");

        AtencionController controller = new AtencionController();
        System.out.println("Origen de persistencia: " + controller.estadoConexion());
        System.out.println();

        // 1. Registro del tramite
        System.out.println(">>> 1. REGISTRO DEL TRAMITE (Estado inicial: En Revision)");
        Expediente exp = controller.atenderCiudadano(
                "74859612",
                "Gonzalo Solar Silva",
                "gonzalo.solar@utp.edu.pe",
                "reclamo"
        );
        System.out.println("-> Expediente creado: " + exp.getCodigo() + " | Estado: " + exp.getEstado());
        System.out.println();

        // 2. Primer avance de estado: En Revision -> Atendido
        System.out.println(">>> 2. AVANZAR ESTADO (En Revision -> Atendido)");
        Expediente atendido = controller.avanzarEstado(exp.getCodigo());
        System.out.println("-> Expediente actualizado: " + atendido.getCodigo() + " | Estado: " + atendido.getEstado());
        System.out.println();

        // 3. Segundo avance de estado: Atendido -> Cerrado
        System.out.println(">>> 3. AVANZAR ESTADO (Atendido -> Cerrado)");
        Expediente cerrado = controller.avanzarEstado(exp.getCodigo());
        System.out.println("-> Expediente actualizado: " + cerrado.getCodigo() + " | Estado: " + cerrado.getEstado());
        System.out.println();

        // 4. Tercer intento de avance (desde estado terminal Cerrado)
        System.out.println(">>> 4. INTENTO DE AVANCE DESDE ESTADO TERMINAL (Cerrado)");
        Expediente finalExp = controller.avanzarEstado(exp.getCodigo());
        System.out.println("-> Estado resultante: " + finalExp.getEstado());
        System.out.println("======================================================================");
    }
}