# Wiremock Pact Generator

Captures HTTP request/responses interactions with your WireMocks and generates Pact files so you
can verify, via contract testing, that your mocks are consistent with the real provider.

## Getting started

Add the `wiremock-pact-generator` dependency.

**Maven**:

```xml
<dependency>
    <groupId>com.atlassian.ta</groupId>
    <artifactId>wiremock-pact-generator</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

**Gradle**: 

```
testCompile group: 'com.atlassian.ta', name: 'wiremock-pact-generator', version: '1.0-SNAPSHOT'
```


## Usage

All you need to do is add the `PactGenerator` listener to your Wiremock server, specifying who the consumer and the
provider are:

```java

// 1. Create your wiremock server
wireMockServer = new WireMockServer(...options);

// 2. Add the Wiremock2Pact listener
wireMockServer.addMockServiceRequestListener(
        new PactGenerator("the-consumer", "the-provider")
);

// 3. That's it!.. create your endpoint stubs and use them.

wireMockServer.addStubMapping(get(urlEqualTo("/path/resource/123"))
                .willReturn(aResponse().withStatus(200))
                .build());
...
myClient.getResource("123")
```

After running your tests, you'll find your pact files in the `[build|target]/pacts/` directory.


## Building and testing ##

The project uses Maven 3.3+. We recommend using [mvnvm](http://mvnvm.org/) or similar.

To build the project:

```
>> mvn clean install
```

To run the project tests:

```
>> mvn test
```

## Contributing ##

Find the guide for contributors [here](CONTRIBUTING.md).

## License ##

Copyright (c) 2017 Atlassian and others. Apache 2.0 licensed, see LICENSE.txt file.