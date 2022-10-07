# ns-sort

A command line tool to sort namespce `:require` and `:import` forms

## Description

The cli tool to sort namespaces in `:require` and `:import` blocks with
lexicographic order.

The project namespaces have more priority compared with other 3rd party
dependencies.

The plugin supports `.clj`, `.cljs`, `.cljc` files.

## Usage

### Via Docker

`ns-sort` is provided as a Docker image for `amd64` and `arm64`. To run:

```bash
$ docker run --mount type=bind,source="$(pwd)",target=/src --workdir /src -i allisoncasey327/ns-sort <files>
```

## Acknowledgments

Based on the excellent work by ilevd: https://github.com/ilevd/ns-sort
