package pruebas;

import modelos.Ciudadano;
import patrones.creacionales.builder.Expediente;
import repositorios.RepositorioException;
import repositorios.TramiteRepositorioMemoria;

import java.util.List;

/**
 * Prueba: verifica la logica del repositorio en memoria sin dependencia de BD.
 */
public class PruebaPersistencia {

    private static int ok = 0;
    private static int fallo = 0;

    public static void main(String[] args) {
        TramiteRepositorioMemoria repo = new TramiteRepositorioMemoria();

        Ciudadano c1 = new Ciudadano("12345678", "Ana Garcia", "ana@test.com");
        Ciudadano c2 = new Ciudadano("87654321", "Luis Perez", "luis@test.com");

        // 1. Registrar dos tramites
        Expediente e1 = repo.registrar(c1, "Reclamo");
        Expediente e2 = repo.registrar(c2, "Solicitud");

        verificar("Codigos distintos", !e1.getCodigo().equals(e2.getCodigo()));
        verificar("Codigo e1 tiene formato EXP-", e1.getCodigo().startsWith("EXP-"));
        verificar("Codigo e2 tiene formato EXP-", e2.getCodigo().startsWith("EXP-"));

        // Verificar secuencialidad: e2 debe tener secuencia mayor que e1
        String sec1 = e1.getCodigo().substring(e1.getCodigo().lastIndexOf('-') + 1);
        String sec2 = e2.getCodigo().substring(e2.getCodigo().lastIndexOf('-') + 1);
        verificar("Codigos secuenciales (e2 > e1)", Integer.parseInt(sec2) > Integer.parseInt(sec1));

        // 2. Listar por DNI
        List<Expediente> listaDni = repo.listar("12345678");
        verificar("Listar por DNI devuelve 1 expediente", listaDni.size() == 1);
        verificar("DNI correcto en listado", "12345678".equals(listaDni.get(0).getDni()));

        // 3. Listar todos
        List<Expediente> todos = repo.listar(null);
        verificar("Listar todos devuelve 2 expedientes", todos.size() == 2);

        // 4. Cambiar estado a estado oficial 'Atendido'
        Expediente actualizado = repo.cambiarEstado(e1.getCodigo(), "Atendido");
        verificar("Estado cambiado a Atendido", "Atendido".equals(actualizado.getEstado()));

        // 5. Verificar que el cambio persiste en la lista
        List<Expediente> listaDespues = repo.listar("12345678");
        verificar("Estado persistido en lista", "Atendido".equals(listaDespues.get(0).getEstado()));

        // 6. Verificar que un estado no oficial lanza RepositorioException
        boolean lanzoError = false;
        try {
            repo.cambiarEstado(e1.getCodigo(), "Aprobado");
        } catch (RepositorioException ex) {
            lanzoError = true;
        }
        verificar("Estado no oficial lanza RepositorioException", lanzoError);

        System.out.println("\n=== RESULTADO: " + ok + " OK, " + fallo + " FALLO ===");
    }

    private static void verificar(String descripcion, boolean condicion) {
        if (condicion) {
            System.out.println("[OK]    " + descripcion);
            ok++;
        } else {
            System.out.println("[FALLO] " + descripcion);
            fallo++;
        }
    }
}