## Wiremock 2 Pact

Captures HTTP request/responses interactions with your WireMocks and generates Pact files so you
can verify, via contract testing, that your mocks are consistent with the real provider.

### Getting started

Add the `wiremock-pact-generator` dependency.

Maven:

```xml
<dependency>
    <groupId>com.atlassian.ta</groupId>
    <artifactId>wiremock-pact-generator</artifactId>
    <version>1.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

Gradle: 

```
testCompile group: 'com.atlassian.ta', name: 'wiremock-pact-generator', version: '1.0-SNAPSHOT'
```


### Basic usage

All you need to do is add the Wiremock2Pact listener to your Wiremock server, specifying who the consumer and the
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


### Advanced usage

Pending