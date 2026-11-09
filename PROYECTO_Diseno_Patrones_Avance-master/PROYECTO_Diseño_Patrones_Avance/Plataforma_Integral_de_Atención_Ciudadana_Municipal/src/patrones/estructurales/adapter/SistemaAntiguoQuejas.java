package patrones.estructurales.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Patrón Adapter (Adaptee): sistema heredado que conserva su operación original
 * y simula el almacén de quejas vecinales registradas bajo el formato antiguo.
 */
public class SistemaAntiguoQuejas {

    /**
     * Adaptee: registra los datos usando la interfaz del sistema heredado.
     *
     * @param datos datos brutos de la queja en el sistema antiguo
     */
    public void registrarQuejaVecinal(String datos) {
        System.out.println("Registro en sistema antiguo: " + datos);
    }

    /**
     * Retorna el lote de registros heredados simulados del sistema antiguo.
     * Incluye al menos 4 registros con datos ficticios, donde al menos 1 es inválido
     * a propósito para evidenciar el rechazo del Proxy.
     *
     * @return lista inmutable de registros heredados
     */
    public List<RegistroQuejaAntigua> obtenerQuejasHeredadas() {
        List<RegistroQuejaAntigua> lista = new ArrayList<>();
        // Registro 1: Válido
        lista.add(new RegistroQuejaAntigua(
                "LEG-001",
                "74859612",
                "Gonzalo Solar Silva",
                "gonzalo.solar@utp.edu.pe",
                "Alumbrado publico deficiente en avenida principal"
        ));
        // Registro 2: Inválido a propósito (DNI de 7 dígitos)
        lista.add(new RegistroQuejaAntigua(
                "LEG-002",
                "1234567",
                "Vecino Anonimo",
                "invalido@example.com",
                "Baches en pista sin senalizacion"
        ));
        // Registro 3: Válido
        lista.add(new RegistroQuejaAntigua(
                "LEG-003",
                "45891234",
                "Mariana Torres Paz",
                "mariana.torres@example.com",
                "Acumulacion de basura en esquina del parque"
        ));
        // Registro 4: Válido
        lista.add(new RegistroQuejaAntigua(
                "LEG-004",
                "32165498",
                "Roberto Carlos Diaz",
                "roberto.diaz@example.com",
                "Ruidos molestos en local comercial nocturno"
        ));
        return Collections.unmodifiableList(lista);
    }
}

