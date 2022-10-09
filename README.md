## JSON validate server. Validates documents using uploaded JSON schema.
Stack: Scala 2.13, tapir, zio, http4s

```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

If you don't have [sbt](https://www.scala-sbt.org) installed already, you can use the provided sbtx wrapper script:

```shell
./sbtx -h # shows an usage of a wrapper script
./sbtx compile # build the project
./sbtx test # run the tests
./sbtx run # run the application (Main)
./sbtx rs # run the application with hot reload (Main)
```

For more details check the [sbtx usage](https://github.com/dwijnand/sbt-extras#sbt--h) page.

Otherwise, if sbt is already installed, you can use the standard commands:

```shell
sbt compile # build the project
sbt test # run the tests
sbt rs # run the application with hot reload (Main)
```

After the server start go to /docs endpoint to see the OpenAPI docs and to test the endpoints using the web UI.