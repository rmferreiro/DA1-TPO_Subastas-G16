# Etapa de compilación (Build)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos primero el pom.xml para aprovechar el caché de Docker
COPY pom.xml .

# Descargamos las dependencias (esto acelera futuras compilaciones)
RUN mvn dependency:go-offline -B

# Copiamos el código fuente
COPY src ./src

# Compilamos la aplicación saltando los tests para que sea más rápido
RUN mvn clean package -DskipTests

# Etapa de ejecución (Run)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el .jar generado de la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto que usa Spring Boot por defecto
EXPOSE 8080

# Comando para iniciar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
