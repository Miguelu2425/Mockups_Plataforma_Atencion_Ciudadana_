-- ============================================================
-- Script T-SQL  |  Base de datos: Municipalidad
-- Proyecto      : Plataforma Integral de Atencion Ciudadana Municipal
-- Curso         : Diseno de Patrones - Semana 12
-- IDEMPOTENTE: se puede ejecutar varias veces sin error.
-- ============================================================

-- 1. CREAR LA BASE DE DATOS
IF NOT EXISTS (
    SELECT * FROM sys.databases WHERE name = 'Municipalidad'
)
BEGIN
    CREATE DATABASE Municipalidad;
    PRINT 'Base de datos Municipalidad creada correctamente.';
END
ELSE
BEGIN
    PRINT 'La base de datos Municipalidad ya existe.';
END
GO

USE Municipalidad;
GO

-- 2. TABLA: TipoTramite
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'TipoTramite')
BEGIN
    CREATE TABLE TipoTramite (
        id_tipo_tramite INT          IDENTITY(1,1) PRIMARY KEY,
        nombre          VARCHAR(50)  NOT NULL UNIQUE,
        descripcion     VARCHAR(200) NOT NULL
    );
    PRINT 'Tabla TipoTramite creada.';
END
GO

IF NOT EXISTS (SELECT 1 FROM TipoTramite WHERE nombre = 'Reclamo')
BEGIN
    INSERT INTO TipoTramite (nombre, descripcion) VALUES
        ('Reclamo',   'Queja o disconformidad vecinal sobre servicios municipales'),
        ('Solicitud', 'Peticion formal de servicio, licencia o tramite municipal');
    PRINT 'Tipos base insertados.';
END
GO

-- 3. TABLA: Ciudadano
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Ciudadano')
BEGIN
    CREATE TABLE Ciudadano (
        id_ciudadano INT          IDENTITY(1,1) PRIMARY KEY,
        dni          CHAR(8)      NOT NULL UNIQUE,
        nombre       VARCHAR(120) NOT NULL,
        correo       VARCHAR(120) NOT NULL
    );
    PRINT 'Tabla Ciudadano creada.';
END
GO

-- 4. TABLA: Expediente
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Expediente')
BEGIN
    CREATE TABLE Expediente (
        id_expediente   INT         IDENTITY(1,1) PRIMARY KEY,
        codigo          VARCHAR(20) NOT NULL UNIQUE,
        id_ciudadano    INT         NOT NULL
            CONSTRAINT FK_Expediente_Ciudadano
                REFERENCES Ciudadano(id_ciudadano),
        id_tipo_tramite INT         NOT NULL
            CONSTRAINT FK_Expediente_TipoTramite
                REFERENCES TipoTramite(id_tipo_tramite),
        estado          VARCHAR(30) NOT NULL DEFAULT 'En Revision',
        fecha_registro  DATETIME            DEFAULT GETDATE()
    );
    PRINT 'Tabla Expediente creada.';
END
GO

-- 5. TABLA: HistorialEstado
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HistorialEstado')
BEGIN
    CREATE TABLE HistorialEstado (
        id_historial    INT         IDENTITY(1,1) PRIMARY KEY,
        id_expediente   INT         NOT NULL
            CONSTRAINT FK_Historial_Expediente
                REFERENCES Expediente(id_expediente),
        estado_anterior VARCHAR(30) NULL,
        estado_nuevo    VARCHAR(30) NOT NULL,
        fecha_cambio    DATETIME    DEFAULT GETDATE()
    );
    PRINT 'Tabla HistorialEstado creada.';
END
GO

-- 6. PROCEDIMIENTO: sp_RegistrarTramite
IF OBJECT_ID('dbo.sp_RegistrarTramite', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_RegistrarTramite;
GO

CREATE PROCEDURE dbo.sp_RegistrarTramite
    @dni          CHAR(8),
    @nombre       VARCHAR(120),
    @correo       VARCHAR(120),
    @tipo_tramite VARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @id_ciudadano    INT;
    DECLARE @id_tipo_tramite INT;
    DECLARE @codigo          VARCHAR(20);
    DECLARE @id_expediente   INT;
    DECLARE @secuencia       INT;

    BEGIN TRY
        BEGIN TRAN;

        -- Resolver o crear ciudadano
        SELECT @id_ciudadano = id_ciudadano FROM Ciudadano WHERE dni = @dni;
        IF @id_ciudadano IS NULL
        BEGIN
            INSERT INTO Ciudadano (dni, nombre, correo)
            VALUES (@dni, @nombre, @correo);
            SET @id_ciudadano = SCOPE_IDENTITY();
        END
        ELSE
        BEGIN
            -- Actualizar correo si cambio
            UPDATE Ciudadano
            SET nombre = @nombre, correo = @correo
            WHERE id_ciudadano = @id_ciudadano;
        END

        -- Resolver tipo de tramite (capitalizado)
        SELECT @id_tipo_tramite = id_tipo_tramite FROM TipoTramite WHERE nombre = @tipo_tramite;
        IF @id_tipo_tramite IS NULL
            RAISERROR('Tipo de tramite no encontrado: %s', 16, 1, @tipo_tramite);

        -- Calcular secuencia con bloqueo para evitar duplicados
        -- Formato: EXP-<anio>-<seq 4 digitos>  ej. EXP-2026-0001
        SELECT @secuencia = COUNT(*) + 1
        FROM Expediente WITH (UPDLOCK, HOLDLOCK);

        SET @codigo = 'EXP-' + CAST(YEAR(GETDATE()) AS VARCHAR(4))
                    + '-' + RIGHT('0000' + CAST(@secuencia AS VARCHAR(4)), 4);

        -- Insertar expediente en estado inicial 'En Revision'
        INSERT INTO Expediente (codigo, id_ciudadano, id_tipo_tramite, estado)
        VALUES (@codigo, @id_ciudadano, @id_tipo_tramite, 'En Revision');
        SET @id_expediente = SCOPE_IDENTITY();

        -- Registrar estado inicial en historial (NULL -> 'En Revision')
        INSERT INTO HistorialEstado (id_expediente, estado_anterior, estado_nuevo)
        VALUES (@id_expediente, NULL, 'En Revision');

        COMMIT TRAN;

        -- Resultado con columna correo incluida
        SELECT e.id_expediente, e.codigo, c.dni, c.nombre AS ciudadano,
               c.correo AS correo,
               t.nombre AS tipo_tramite, e.estado, e.fecha_registro
        FROM  Expediente  e
        JOIN  Ciudadano   c ON c.id_ciudadano    = e.id_ciudadano
        JOIN  TipoTramite t ON t.id_tipo_tramite = e.id_tipo_tramite
        WHERE e.id_expediente = @id_expediente;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRAN;
        DECLARE @msg   NVARCHAR(500) = ERROR_MESSAGE();
        DECLARE @sev   INT           = ERROR_SEVERITY();
        DECLARE @state INT           = ERROR_STATE();
        RAISERROR(@msg, @sev, @state);
    END CATCH
END
GO

-- 7. PROCEDIMIENTO: sp_CambiarEstado (Valida estados oficiales)
IF OBJECT_ID('dbo.sp_CambiarEstado', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_CambiarEstado;
GO

CREATE PROCEDURE dbo.sp_CambiarEstado
    @codigo       VARCHAR(20),
    @estado_nuevo VARCHAR(30)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @id_expediente   INT;
    DECLARE @estado_anterior VARCHAR(30);

    -- Validacion de estados oficiales permitidos
    IF @estado_nuevo NOT IN ('En Revision', 'Atendido', 'Cerrado')
    BEGIN
        RAISERROR('Estado no valido: %s. Solo se permiten: En Revision, Atendido, Cerrado.', 16, 1, @estado_nuevo);
        RETURN;
    END

    BEGIN TRY
        BEGIN TRAN;

        -- Buscar expediente con bloqueo
        SELECT @id_expediente = e.id_expediente, @estado_anterior = e.estado
        FROM   Expediente e WITH (UPDLOCK)
        WHERE  e.codigo = @codigo;

        IF @id_expediente IS NULL
            RAISERROR('Expediente no encontrado: %s', 16, 1, @codigo);

        -- Actualizar estado
        UPDATE Expediente
        SET    estado = @estado_nuevo
        WHERE  id_expediente = @id_expediente;

        -- Registrar en historial
        INSERT INTO HistorialEstado (id_expediente, estado_anterior, estado_nuevo)
        VALUES (@id_expediente, @estado_anterior, @estado_nuevo);

        COMMIT TRAN;

        -- Devolver fila actualizada con columna correo
        SELECT e.id_expediente, e.codigo, c.dni, c.nombre AS ciudadano,
               c.correo AS correo,
               t.nombre AS tipo_tramite, e.estado, e.fecha_registro
        FROM  Expediente  e
        JOIN  Ciudadano   c ON c.id_ciudadano    = e.id_ciudadano
        JOIN  TipoTramite t ON t.id_tipo_tramite = e.id_tipo_tramite
        WHERE e.id_expediente = @id_expediente;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRAN;
        DECLARE @msg   NVARCHAR(500) = ERROR_MESSAGE();
        DECLARE @sev   INT           = ERROR_SEVERITY();
        DECLARE @state INT           = ERROR_STATE();
        RAISERROR(@msg, @sev, @state);
    END CATCH
END
GO

-- 8. PROCEDIMIENTO: sp_ListarTramites
IF OBJECT_ID('dbo.sp_ListarTramites', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ListarTramites;
GO

CREATE PROCEDURE dbo.sp_ListarTramites
    @dni CHAR(8) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.id_expediente, e.codigo, c.dni, c.nombre AS ciudadano,
           c.correo AS correo,
           t.nombre AS tipo_tramite, e.estado, e.fecha_registro
    FROM  Expediente  e
    JOIN  Ciudadano   c ON c.id_ciudadano    = e.id_ciudadano
    JOIN  TipoTramite t ON t.id_tipo_tramite = e.id_tipo_tramite
    WHERE @dni IS NULL OR c.dni = @dni
    ORDER BY e.fecha_registro DESC;
END
GO

-- 9. PROCEDIMIENTO: sp_ObtenerTramite
IF OBJECT_ID('dbo.sp_ObtenerTramite', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_ObtenerTramite;
GO

CREATE PROCEDURE dbo.sp_ObtenerTramite
    @codigo VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    SELECT e.id_expediente, e.codigo, c.dni, c.nombre AS ciudadano,
           c.correo AS correo,
           t.nombre AS tipo_tramite, e.estado, e.fecha_registro
    FROM  Expediente  e
    JOIN  Ciudadano   c ON c.id_ciudadano    = e.id_ciudadano
    JOIN  TipoTramite t ON t.id_tipo_tramite = e.id_tipo_tramite
    WHERE e.codigo = @codigo;
END
GO

-- 10. PROCEDIMIENTO: sp_EliminarTramite (Patron Command - Deshacer registro)
IF OBJECT_ID('dbo.sp_EliminarTramite', 'P') IS NOT NULL
    DROP PROCEDURE dbo.sp_EliminarTramite;
GO

CREATE PROCEDURE dbo.sp_EliminarTramite
    @codigo VARCHAR(20)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @id_expediente INT;
    DECLARE @estado        VARCHAR(30);

    BEGIN TRY
        BEGIN TRAN;

        -- Buscar expediente con bloqueo
        SELECT @id_expediente = e.id_expediente, @estado = e.estado
        FROM   Expediente e WITH (UPDLOCK)
        WHERE  e.codigo = @codigo;

        IF @id_expediente IS NULL
            RAISERROR('Expediente no encontrado: %s', 16, 1, @codigo);

        -- Solo se puede eliminar/deshacer si aun esta en estado inicial
        IF @estado <> 'En Revision'
            RAISERROR('Solo se puede deshacer el registro de un tramite En Revision', 16, 1);

        -- Borrar primero de HistorialEstado por integridad referencial
        DELETE FROM HistorialEstado
        WHERE  id_expediente = @id_expediente;

        -- Borrar el expediente (se conserva el ciudadano para historico)
        DELETE FROM Expediente
        WHERE  id_expediente = @id_expediente;

        COMMIT TRAN;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRAN;
        DECLARE @msg   NVARCHAR(500) = ERROR_MESSAGE();
        DECLARE @sev   INT           = ERROR_SEVERITY();
        DECLARE @state INT           = ERROR_STATE();
        RAISERROR(@msg, @sev, @state);
    END CATCH
END
GO

PRINT 'Script municipalidad actualizado correctamente.';
GO