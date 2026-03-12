# ol.dirs CLJD Runtime Workspace

This `dart/` directory is the ClojureDart runtime workspace for compiling and testing `ol.dirs`.

Run commands from this directory:

```bash
cd dart
```

Initialize or regenerate Dart runtime files:

```bash
clojure -M:cljd init
```

Compile the CLJD sources:

```bash
clojure -M:cljd compile
```

Run the CLJD tests:

```bash
clojure -M:cljd:cljd-test test
```

Clean generated artifacts:

```bash
clojure -M:cljd clean
```
