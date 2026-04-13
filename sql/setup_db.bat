@echo off
SET MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
SET USER=root
SET PASS=pochito
SET DB=subastas_bd
SET SQL_DIR=D:\Users\Rodri\Desktop\da1\DA1-TPO_Subastas-G16\sql

echo.
echo =========================================
echo  SUBASTAS G16 - Setup Base de Datos MySQL
echo =========================================
echo.

echo [1/3] Verificando conexion a MySQL...
%MYSQL% -u%USER% -p%PASS% -e "SELECT 'Conexion OK' AS status;" 2>nul
IF %ERRORLEVEL% NEQ 0 (
    echo ERROR: No se pudo conectar a MySQL. Verificar que el servicio este corriendo.
    pause
    exit /b 1
)

echo [2/3] Ejecutando schema (tablas)...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\01_schema_migration.sql"
IF %ERRORLEVEL% NEQ 0 (
    echo ERROR en schema. Revisar 01_schema_migration.sql
    pause
    exit /b 1
)
echo    Schema creado OK.

echo [3/3] Cargando datos de prueba...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\02_seed_data.sql"
IF %ERRORLEVEL% NEQ 0 (
    echo ERROR en seed data. Revisar 02_seed_data.sql
    pause  
    exit /b 1
)
echo    Datos cargados OK.

echo.
echo [VERIFICACION] Tablas creadas en subastas_bd:
%MYSQL% -u%USER% -p%PASS% %DB% -e "SHOW TABLES;" 2>nul

echo.
echo [VERIFICACION] Conteo de registros principales:
%MYSQL% -u%USER% -p%PASS% %DB% -e "SELECT 'paises' AS tabla, COUNT(*) AS registros FROM paises UNION SELECT 'personas', COUNT(*) FROM personas UNION SELECT 'clientes', COUNT(*) FROM clientes UNION SELECT 'productos', COUNT(*) FROM productos UNION SELECT 'subastas', COUNT(*) FROM subastas UNION SELECT 'usuarios_auth', COUNT(*) FROM usuarios_auth UNION SELECT 'mediosPago', COUNT(*) FROM mediosPago UNION SELECT 'notificaciones', COUNT(*) FROM notificaciones;" 2>nul

echo.
echo =========================================
echo  Setup completado exitosamente!
echo  Ahora podemos levantar Spring Boot.
echo =========================================
pause
