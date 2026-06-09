@echo off
SET MYSQL="C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
SET USER=root
SET PASS=uade
SET DB=subastas_bd
SET SQL_DIR=%~dp0

echo.
echo =========================================
echo  SUBASTAS G16 - Setup Base de Datos MySQL
echo =========================================
echo.

echo [1/5] Verificando conexion a MySQL...
%MYSQL% -u%USER% -p%PASS% -e "SELECT 1;" >nul 2>&1
IF ERRORLEVEL 1 (
    echo [ERROR] No se pudo conectar a MySQL. Verifica usuario y contrasena.
    pause
    exit /b 1
)

echo [2/5] Limpiando y creando base de datos limpia...
%MYSQL% -u%USER% -p%PASS% -e "DROP DATABASE IF EXISTS subastas_bd; CREATE DATABASE subastas_bd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

echo [3/5] Ejecutando estructura legacy...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\01_estructura_legacy_mysql.sql"

echo [3/5] Ejecutando tablas nuevas...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\02_tablas_nuevas.sql"

echo [4/5] Cargando datos iniciales...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\03_datos_iniciales.sql"

echo [5/5] Cargando datos de prueba...
%MYSQL% -u%USER% -p%PASS% --default-character-set=utf8mb4 < "%SQL_DIR%\04_datos_prueba.sql"

echo.
echo [VERIFICACION] Tablas creadas en subastas_bd:
%MYSQL% -u%USER% -p%PASS% %DB% -e "SHOW TABLES;" 2>nul

echo.
echo [VERIFICACION] Conteo de registros principales:
%MYSQL% -u%USER% -p%PASS% %DB% -e "SELECT 'paises' AS tabla, COUNT(*) AS registros FROM paises UNION SELECT 'personas', COUNT(*) FROM personas UNION SELECT 'clientes', COUNT(*) FROM clientes UNION SELECT 'productos', COUNT(*) FROM productos UNION SELECT 'subastas', COUNT(*) FROM subastas UNION SELECT 'usuarios_auth', COUNT(*) FROM usuarios_auth UNION SELECT 'medios_pago', COUNT(*) FROM medios_pago UNION SELECT 'notificaciones', COUNT(*) FROM notificaciones;" 2>nul

echo.
echo =========================================
echo  Setup completado exitosamente!
echo  Ahora podemos levantar Spring Boot.
echo =========================================
pause
