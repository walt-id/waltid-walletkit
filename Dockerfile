### Configuration

# set --build-args SKIP_TESTS=true to use
ARG SKIP_TESTS

# --- dos2unix-env    # convert line endings from Windows machines
FROM docker.io/rkimf1/dos2unix@sha256:60f78cd8bf42641afdeae3f947190f98ae293994c0443741a2b3f3034998a6ed as dos2unix-env
WORKDIR /convert
COPY gradlew .
RUN dos2unix ./gradlew

# --- build-env
FROM docker.io/gradle:7.6-jdk as build-env

ARG SKIP_TESTS

WORKDIR /appbuild

COPY . /appbuild

# copy converted Windows line endings files
COPY --from=dos2unix-env /convert/gradlew .

# cache Gradle dependencies
VOLUME /home/gradle/.gradle

RUN if [ -z "$SKIP_TESTS" ]; \
    then echo "* Running full build" && gradle -i clean build installDist; \
    else echo "* Building but skipping tests" && gradle -i clean installDist -x test; \
    fi

# --- opa-env
FROM docker.io/openpolicyagent/opa:0.46.1-static as opa-env

# --- iota-env
FROM docker.io/waltid/waltid_iota_identity_wrapper:latest as iota-env

# --- app-env
FROM docker.io/eclipse-temurin:19 AS app-env

WORKDIR /app

COPY --from=opa-env /opa /usr/local/bin/opa

COPY --from=iota-env /usr/local/lib/libwaltid_iota_identity_wrapper.so /usr/local/lib/libwaltid_iota_identity_wrapper.so
RUN ldconfig

COPY --from=build-env /appbuild/build/install/waltid-walletkit /app/
COPY --from=build-env /appbuild/service-matrix.properties /app/
COPY --from=build-env /appbuild/config /app/config


### Execution
EXPOSE 7000 7001 7002 7003 7004 7010

ENTRYPOINT ["/app/bin/waltid-walletkit"]
