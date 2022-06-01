FROM openjdk:17-jdk-slim as buildstage
COPY ./ /
RUN ./gradlew installDist

FROM openjdk:17-jdk-slim
COPY service-matrix.properties /waltid-wallet-kit/
COPY signatory.conf /waltid-wallet-kit/
COPY --from=buildstage /build/install/ /

WORKDIR /waltid-wallet-kit
ENV WALTID_WALLET_BACKEND_BIND_ADDRESS=0.0.0.0
ENTRYPOINT ["/waltid-wallet-kit/bin/waltid-wallet-kit"]
