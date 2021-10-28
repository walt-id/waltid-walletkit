FROM openjdk:16-jdk-slim as buildstage
COPY ./ /
RUN ./gradlew installDist

FROM openjdk:16-jdk-slim
COPY service-matrix.properties /Webwallet-Backend/
COPY signatory.conf /Webwallet-Backend/
COPY --from=buildstage /build/install/ /

WORKDIR /Webwallet-Backend

ENTRYPOINT ["/Webwallet-Backend/bin/Webwallet-Backend"]