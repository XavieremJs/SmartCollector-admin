# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# O pom entra sozinho primeiro: enquanto ele nao mudar, o Docker reaproveita
# a camada com as dependencias ja baixadas e o build para em segundos.
COPY pom.xml .
# Este passo existe so para aquecer o cache: se algum plugin nao resolver
# offline, o package abaixo baixa o que faltar. Falhar aqui nao e motivo
# para derrubar o build.
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline || true

COPY src ./src
# Os testes ja rodaram no job anterior do pipeline; repeti-los aqui so
# atrasaria a imagem.
RUN mvn --batch-mode --no-transfer-progress clean package -DskipTests

# O jar do Spring Boot e desmontado em camadas: dependencia muda pouco,
# codigo muda a cada commit. Sem isso cada build reenvia o jar inteiro.
# (jarmode=tools; o antigo layertools esta deprecado desde o Boot 3.3.)
RUN java -Djarmode=tools -jar target/smartcollector-api-*.jar \
        extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Processo sem privilegio: uma falha na aplicacao nao entrega root do container.
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build --chown=spring:spring /build/extracted/dependencies/ ./
COPY --from=build --chown=spring:spring /build/extracted/spring-boot-loader/ ./
COPY --from=build --chown=spring:spring /build/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=spring:spring /build/extracted/application/ ./

EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
