# --- dos2unix-env
# convert line endings from Windows machines
FROM docker.io/rkimf1/dos2unix@sha256:60f78cd8bf42641afdeae3f947190f98ae293994c0443741a2b3f3034998a6ed as dos2unix-env
WORKDIR /convert
COPY gradlew .
RUN dos2unix ./gradlew

FROM openjdk:17-jdk-slim as buildstage
COPY ./ /
# copy converted Windows line endings files
COPY --from=dos2unix-env /convert/gradlew .
RUN ./gradlew installDist

FROM waltid/waltid_iota_identity_wrapper:latest as iota_wrapper
FROM openjdk:17-jdk-slim
ADD https://openpolicyagent.org/downloads/v0.41.0/opa_linux_amd64_static /usr/local/bin/opa
RUN chmod 755 /usr/local/bin/opa
COPY --from=iota_wrapper /usr/local/lib/libwaltid_iota_identity_wrapper.so /usr/local/lib/libwaltid_iota_identity_wrapper.so
RUN ldconfig

COPY service-matrix.properties /waltid-walletkit/
COPY signatory.conf /waltid-walletkit/
COPY --from=buildstage /build/install/ /

WORKDIR /waltid-walletkit
ENV WALTID_WALLET_BACKEND_BIND_ADDRESS=0.0.0.0
ENTRYPOINT ["/waltid-walletkit/bin/waltid-walletkit"]
