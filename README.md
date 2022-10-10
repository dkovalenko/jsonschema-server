## JSON validate server. Validates documents using uploaded JSON schema.

A REST-service for validating JSON documents against JSON Schemas.

This REST-service allow to upload JSON Schemas and store them at unique URI and then validate JSON documents against these URIs

Stack: Scala 2.13, tapir, zio, http4s

Persistance: Redis. Start a Redis server by running:
```
docker-compose -f docker-compose-redis.yaml up
```

After Redis you can start the backend server. If sbt is installed, you can use the standard commands:

```shell
sbt compile # build the project
sbt test # run the tests
sbt rs # run the application with hot reload (Main)
```

After the backend start you can open /docs endpoint to see the OpenAPI docs. Also it's possible to test the endpoints using the web UI.

API Routes:
```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```