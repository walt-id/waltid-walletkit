FROM openjdk:17-jdk-slim as buildstage
COPY ./ /
RUN ./gradlew installDist

FROM openjdk:17-jdk-slim
COPY service-matrix.properties /waltid-wallet-backend/
COPY signatory.conf /waltid-wallet-backend/
COPY --from=buildstage /build/install/ /

WORKDIR /waltid-wallet-backend
ENV WALTID_WALLET_BACKEND_BIND_ADDRESS=0.0.0.0
ENTRYPOINT ["/waltid-wallet-backend/bin/waltid-wallet-backend"]
