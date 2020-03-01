#! /usr/bin/env bash

set -euo pipefail

main() {
    bash scripts/mypy.sh
    bash scripts/unittest.sh
    sbt "frankenpaxosJVM/assembly"
    sbt "frankenpaxosJVM/bench:compile"
    bash scripts/benchmark_smoke.sh
}

main "$@"
