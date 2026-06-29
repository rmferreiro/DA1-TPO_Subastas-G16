@echo off
setlocal

if exist "C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.3\jbr" (
  set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2024.1.3\jbr"
) else if exist "C:\Program Files\Android\Android Studio\jbr" (
  set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
) else (
  echo ERROR: No se encontro JDK 17.
  echo Instala Java 17 o configura JAVA_HOME manualmente.
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"
cd /d "%~dp0"

echo Usando Java:
java -version
echo.
mvn spring-boot:run %*
