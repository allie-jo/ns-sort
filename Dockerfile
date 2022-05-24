FROM clojure AS uberjar
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN lein uberjar

FROM ghcr.io/graalvm/native-image AS build
ENV NS_SORT_VERSION=1.0.0
WORKDIR /app
COPY --from=uberjar /usr/src/app/target/ns-sort-$NS_SORT_VERSION .
RUN native-image --no-server -J-Xmx8G -jar ns-sort-$NS_SORT_VERSION \
    -H:Name="ns-sort" \
    -H:+ReportExceptionStackTraces \
    --report-unsupported-elements-at-runtime \
    --initialize-at-build-time \
    --no-fallback

FROM fedora
COPY --from=build /app/ns-sort /usr/bin/
ENTRYPOINT ["/usr/bin/ns-sort"]
CMD []
