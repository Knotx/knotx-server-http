[![Build Status](https://dev.azure.com/knotx/Knotx/_apis/build/status/Knotx.knotx-server-http?branchName=master)](https://dev.azure.com/knotx/Knotx/_build/latest?definitionId=8&branchName=master)
[![CodeFactor](https://www.codefactor.io/repository/github/knotx/knotx-server-http/badge)](https://www.codefactor.io/repository/github/knotx/knotx-server-http)
[![codecov](https://codecov.io/gh/Knotx/knotx-server-http/branch/master/graph/badge.svg)](https://codecov.io/gh/Knotx/knotx-server-http)
[![Gradle Status](https://gradleupdate.appspot.com/Knotx/knotx-server-http/status.svg)](https://gradleupdate.appspot.com/Knotx/knotx-server-http/status)

# Knot.x HTTP Server
Server is essentially a "heart" of Knot.x. It's scalable (vertically and horizontally), 
plugable (easy to extend), fault-tolerant 
and highly efficient reactive server based on [Vert.x](https://vertx.io/).

Like any HTTP server, Knot.x HTTP Server allows you to define supported routes. Defining routes 
and security is done with [Open API 3](https://github.com/OAI/OpenAPI-Specification) standard.

Knot.x Server enables plugging in custom behaviour for each supported route.
Each defined [`path`](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md#pathItemObject)
is processed by the chain of [Handlers](https://vertx.io/docs/apidocs/io/vertx/core/Handler.html)
that reacts on the request and is able to shape the response.

![server flow](misc/server-flow.png)

**Tree of content**  
* [Modules](#modules)
* [How does it work](#how-does-it-work)
* [How to configure](#how-to-configure)
  + [Http Server Options](#http-server-options)
    - [Server Port Configuration](#server-port-configuration)
  + [Dropping the requests](#dropping-the-requests)
  + [Routing Specification](#routing-specification)
  + [Routing Operations](#routing-operations)
  + [Routing Security](#routing-security)
    - [Example security setup](#example-security-setup)

## Modules
You will find docs in the `README.md` files inside each of following modules:
- [Server API](https://github.com/Knotx/knotx-server-http/tree/master/api) that defines contracts. 
- [Server core](https://github.com/Knotx/knotx-server-http/tree/master/core) that contains basic  
handlers and HTTP Server implementation.
- [Common](https://github.com/Knotx/knotx-server-http/tree/master/common) contains reusable helper methods grouped into modules (e.g. [Knot.x Server Common Placeholders](https://github.com/Knotx/knotx-server-http/tree/master/common/placeholders)).

## How does it work
Knot.x HTTP Server is a [verticle](http://vertx.io/docs/apidocs/io/vertx/core/Verticle.html)
that listens for HTTP requests. It is responsible for handling the whole HTTP traffic that goes to 
the Knot.x instance.

Once the HTTP request comes to the Server following actions are happening:
- Server checks if there is a [routing path](#routing-specification) defined for the incoming request path
and method. If no path supports this request, `404 Not Found` is returned in the response.
- Server checks whether a [routing operation](#routing-operations) has been defined for the incoming request.
If no handlers are defined in the operations for this route, `501 Not Implemented` is returned in the response.
- Handlers processing starts. In case of operation handler failure, [failure handlers](#handling-failures)
are triggered.
- Handling is continued until any Handler ends (completes) the response. If none does, 
`500 Internal Server Error` is returned.

## How to configure
To run Knot.x HTTP Server, simply add an entry in the [`modules` configuration](https://github.com/Knotx/knotx-launcher#modules-configuration):

```hocon
modules = {
  server = "io.knotx.server.KnotxServerVerticle"
}
```

Details of the Knot.x HTTP Server can be configured with [`KnotxServerOptions`](/core/docs/asciidoc/dataobjects.adoc#knotxserveroptions)
in the configuration file:
```hocon
config.server.options.config {
  # options defined here
}
```
More details about the module configuration can be found in [Knot.x Launcher](https://github.com/Knotx/knotx-launcher#modules-configuration).

### Http Server Options
HTTP options such as port, connection windows size, default version of ALPN (HTTP/2, HTTP/1) etc. 
can be configured with Vert.x [`HttpServerOptions`](http://vertx.io/docs/vertx-core/dataobjects.html#HttpServerOptions).

E.g. following configuration specifies the port Server is listening at:
```hocon
config.server.options.config {
  serverOptions {
    port = 8080
  }
}
```
#### Server Port Configuration 
Knot.x HTTP Server port can also be specified by the system property `knotx.port` that takes 
precedence over the value in the configuration file.
```
java -Dknotx.port=9999 ...
```

### Dropping the requests
Knot.x Server implements the backpressure mechanism. It drops / rejects requests after exceeding a 
certain number of requests at a time.
If Knot.x Server can NOT process incoming requests fast enough (the requests buffer limit has been reached), 
Server drops requests and responds with a configurable response code (by default `429, "Too Many Requests"`).

Dropping requests can be configured with [`DropRequestOptions`](core/docs/asciidoc/dataobjects.adoc#droprequestoptions)

After the buffer slots are released, new requests will be accepted and finally processed.

> That solution prevent `OutOfMemoryError` errors when there are too many requests (e.g. during the peak hours). 
Additionally response times should be more stable when system is under high stress.

### Routing Specification
Knot.x Routing is defined with [Open API 3](https://github.com/OAI/OpenAPI-Specification) standard. 
`routingSpecificationLocation` is the path to the YAML Open API specification.
It can be an absolute path, a local path or remote url (with `HTTP`).

```hocon
config.server.options.config {
  routingSpecificationLocation = /openapi.yaml
}
```

### Routing Operations
[`Routing Operation`](core/docs/asciidoc/dataobjects.adoc#routingoperationoptions) 
defines handlers and failure handlers involved in the processing of HTTP request for a specific
[operation](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md#operationObject)
defined in the [Routing Specification](#routing-specification) (linking is done
by `operationId`):

> Open Api YAML config:
```yaml
...
paths:
  /example*:
    get:
      operationId: my-example-operation
...
```
> Routing Operations entry in Knot.x conf
```hocon
config.server.options.config.routingOperations = ${routingOperations} [
  {
    operationId = my-example-operation
    handlers = [
      # list of handlers that handles the request in chain
    ]
    failureHandlers = [
      # list of failure handlers
    ]
  }
]
```

#### Routing Handler
Routing Handler is nothing else than a basic Vert.x [Handler](https://vertx.io/docs/apidocs/io/vertx/core/Handler.html) that operate on the [`RoutingContext`](https://vertx.io/docs/apidocs/io/vertx/ext/web/RoutingContext.html).
You may find more information about it in the [API docs](https://github.com/Knotx/knotx-server-http/tree/master/api#routing-handlers).
Each `handler` is configured with [Routing Handler Options](/core/docs/asciidoc/dataobjects.adoc#routinghandleroptions).

#### Routing Order
Knot.x server makes use of the [`OpenAPI3RouterFactory`](https://vertx.io/docs/apidocs/io/vertx/ext/web/api/contract/openapi3/OpenAPI3RouterFactory.html) in order to combine OpenAPI routes specification with configured handlers. According to `OpenAPI3RouterFactory` docs, handlers are loaded in this order:

> 1. Body handler (Customizable with this#setBodyHandler(BodyHandler)
> 2. Custom global handlers configurable with this#addGlobalHandler(Handler)
> 3. Global security handlers defined in upper spec level
> 4. Operation specific security handlers
> 5. Generated validation handler
> 6. User handlers or "Not implemented" handler

Also, `operations` in the server are mounted in the definition order inside yaml specification.

### Routing Security
Security for each operation defined in the [Open API specification](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.2.md#security-requirement-object)
is implemented in a form of [`AuthHandlers`](https://vertx.io/docs/apidocs/io/vertx/ext/web/handler/AuthHandler.html).
In order to add security for the operation, you need to implement [`AuthHandlerFactory`](https://github.com/Knotx/knotx-server-http/blob/master/api/src/main/java/io/knotx/server/api/security/AuthHandlerFactory.java)
that provides `AuthHandler` of a proper type.

#### Example security setup
> Open Api YAML config:
```yaml
...
paths:
  /example*:
    get:
      operationId: my-example-operation
      security:
          - myExampleBasicAuth: []
...
components:
  securitySchemes:
    myExampleBasicAuth:
      type: http
      scheme: basic
```

> Routing Operations entry in Knot.x conf
```hocon
config.server.options.config.securityHandlers = [
  {
    schema = myExampleBasicAuth
    factory = myBasicAuthHandlerFactory
    config = {
      # any config passed to the AuthHandler
    }
  }
]
```

Also, you need to implement and **register** [`AuthHandlerFactory`](api#creating-auth-handler).
For the example above:
 - `name` would be `myBasicAuthHandlerFactory`

### Global Handlers
[`Global Handlers`](core/docs/asciidoc/dataobjects.adoc#globalhandlers) 
defines handlers that will be applied before any [`routing operation`](core/docs/asciidoc/dataobjects.adoc#routingoperationoptions).

Please note that you should not add a body handler inside that list. If you want to modify the 
body use [`routing operation`](core/docs/asciidoc/dataobjects.adoc#routingoperationoptions) handlers.

```hocon
config.server.options.config.globalHandlers = [
  {
    name = myGlobalHandler
    config {
      // some configuration passed via factory to the handler
    }
  }
]
```

Each `handler` is specified with [Routing Handler Options](/core/docs/asciidoc/dataobjects.adoc#routinghandleroptions).

#### Access Logs
To log every request incoming to the Knot.x HTTP Server setup [`LoggerHandler`](https://vertx.io/docs/apidocs/io/vertx/reactivex/ext/web/handler/LoggerHandler.html)
as a [`globalHandler`](#global-handlers). Use `loggerHandler` factory and [`Access Log Options`](core/docs/asciidoc/dataobjects.adoc#accesslogoptions)
structure in order to configure it to your needs.

```hocon
config.server.options.config.globalHandlers = [
  {
    name = loggerHandler
    config {
      immediate = true
      format = DEFAULT
    }
  }
]
```

By default, access logs are logged to the `knotx-access.log` in the logs directory. To find more about
default logger configuration, please see [Knot.x Launcher logback settings](https://github.com/Knotx/knotx-launcher#logback-settings).

Of course you may also use `loggerHandler` factory to log access to only specific routes via 
[`routing operation`](core/docs/asciidoc/dataobjects.adoc#routingoperationoptions).
