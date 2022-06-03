FROM openjdk:17-jdk-slim as buildstage
COPY ./ /
RUN ./gradlew installDist

FROM openjdk:17-jdk-slim
COPY service-matrix.properties /waltid-walletkit/
COPY signatory.conf /waltid-walletkit/
COPY --from=buildstage /build/install/ /

WORKDIR /waltid-walletkit
ENV WALTID_WALLET_BACKEND_BIND_ADDRESS=0.0.0.0
ENTRYPOINT ["/waltid-walletkit/bin/waltid-walletkit"]
