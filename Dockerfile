FROM clojure:lein-2.9.10 AS uberjar
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN lein uberjar

FROM ghcr.io/graalvm/native-image:22.2.0 AS build
ENV NS_SORT_VERSION=1.1.3
WORKDIR /app
COPY --from=uberjar /usr/src/app/target/ns-sort-$NS_SORT_VERSION .
RUN native-image --no-server -J-Xmx8G -jar ns-sort-$NS_SORT_VERSION \
    -H:Name="ns-sort" \
    -H:+ReportExceptionStackTraces \
    --report-unsupported-elements-at-runtime \
    --initialize-at-build-time \
    --no-fallback

FROM alpine:3.16.2
COPY --from=build /app/ns-sort /usr/bin/
RUN apk add --no-cache gcompat=1.0.0-r4
ENTRYPOINT ["/usr/bin/ns-sort"]
CMD []
