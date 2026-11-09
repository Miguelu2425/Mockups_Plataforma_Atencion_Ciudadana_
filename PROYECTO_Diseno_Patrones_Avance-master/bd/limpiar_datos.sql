-- ============================================================
-- Script T-SQL  |  Base de datos: Municipalidad
-- Archivo       : bd/limpiar_datos.sql
-- Proposito     : Limpieza de datos de prueba para entorno de desarrollo.
-- ADVERTENCIA   : ESTE SCRIPT ES EXCLUSIVAMENTE PARA DESARROLLO.
--                 ELIMINA TODOS LOS EXPEDIENTES, HISTORIALES Y CIUDADANOS.
--                 NO MODIFICA LA TABLA MAESTRA TipoTramite.
-- ============================================================

USE Municipalidad;
GO

PRINT '============================================================';
PRINT ' ADVERTENCIA: INICIANDO LIMPIEZA DE DATOS DE DESARROLLO';
PRINT ' Se eliminaran todos los expedientes, historiales y ciudadanos.';
PRINT '============================================================';

BEGIN TRY
    BEGIN TRANSACTION;

    -- 1. Eliminar datos en orden de dependencias de clave foranea
    IF OBJECT_ID('dbo.HistorialEstado', 'U') IS NOT NULL
    BEGIN
        DELETE FROM dbo.HistorialEstado;
        DBCC CHECKIDENT ('dbo.HistorialEstado', RESEED, 0);
        PRINT '[OK] Tabla HistorialEstado vaciada e identidad reiniciada a 0.';
    END

    IF OBJECT_ID('dbo.Expediente', 'U') IS NOT NULL
    BEGIN
        DELETE FROM dbo.Expediente;
        DBCC CHECKIDENT ('dbo.Expediente', RESEED, 0);
        PRINT '[OK] Tabla Expediente vaciada e identidad reiniciada a 0.';
    END

    IF OBJECT_ID('dbo.Ciudadano', 'U') IS NOT NULL
    BEGIN
        DELETE FROM dbo.Ciudadano;
        DBCC CHECKIDENT ('dbo.Ciudadano', RESEED, 0);
        PRINT '[OK] Tabla Ciudadano vaciada e identidad reiniciada a 0.';
    END

    -- Confirmar la transaccion
    COMMIT TRANSACTION;

    PRINT '============================================================';
    PRINT ' LIMPIEZA COMPLETADA CON EXITO. TipoTramite permanece intacto.';
    PRINT '============================================================';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;

    PRINT 'ERROR durante la limpieza de datos:';
    PRINT ERROR_MESSAGE();
END CATCH;
GO
