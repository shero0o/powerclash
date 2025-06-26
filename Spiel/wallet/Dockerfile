# 1. Build-Stage (Java 17 + Maven Wrapper)
FROM eclipse-temurin:17-jdk-jammy AS build

# Arbeitsverzeichnis im Container
WORKDIR /app

# 1.1 Maven Wrapper und Konfigurationsverzeichnis kopieren
COPY mvnw ./
COPY .mvn .mvn

# 1.2 Alles Kopieren: POM und Quellcode
COPY pom.xml ./
COPY src ./src

# Macht das Wrapper-Skript ausführbar
RUN chmod +x mvnw

# 1.3 Projekt bauen (JAR erzeugen)
RUN ./mvnw clean package -DskipTests

# -----------------------------------

# 2. Runtime-Stage (nur JRE)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 2.1 Das gebaute JAR aus der Build-Stage übernehmen (Name muss exakt stimmen)
COPY --from=build /app/target/wallet-0.0.1-SNAPSHOT.jar ./wallet.jar

# 2.2 Port, den Spring Boot verwendet
EXPOSE 8092

# 2.3 Startbefehl für den Container
ENTRYPOINT ["java", "-jar", "/app/wallet.jar"]
