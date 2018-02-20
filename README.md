# WireMock Pact Generator
> Captures HTTP request/responses interactions with your WireMocks and generates Pact files so you
> can verify, via contract testing, that your mocks are consistent with the real provider.

## What is Wiremock Pact Generator
- A WireMock listener that plugs into existing WireMock instances.
- Will capture every request and response made to a WireMock server and save them in Pact format.

## Requirements
- Java 1.8 or higher
- WireMock 1.57 or higher

## Installation

Add the `wiremock-pact-generator` dependency.

### Maven

```xml
<dependency>
    <groupId>com.atlassian.ta</groupId>
    <artifactId>wiremock-pact-generator</artifactId>
    <scope>test</scope>
</dependency>
```

### Gradle

```
testCompile group: 'com.atlassian.ta', name: 'wiremock-pact-generator'
```

## Usage
Add the `PactGenerator` listener to your WireMock server, specifying who the consumer and the provider are.

```java

// 1. Create your WireMock server
wireMockServer = new WireMockServer(...options);

// 2. Add the WireMockPactGenerator listener
wireMockServer.addMockServiceRequestListener(
    WireMockPactGenerator
        .builder("the-consumer", "the-provider")
        .build()
);

// 3. That's it!.. create your endpoint stubs and use them.
wireMockServer.addStubMapping(get(urlEqualTo("/path/resource/123"))
                .willReturn(aResponse().withStatus(200))
                .build()
);
...
myClient.getResource("123")
```

After running your tests, Maven users will find pact files in the `target/pacts` directory and Gradle users will find
pact files in the `build/pacts` directory.

### Filtering interactions

You can provide request path Regex matchers to tell WireMockPactGenerator which pact interactions should be saved
(whitelist) or skipped (blacklist).

```java
myWireMockServer.addMockServiceRequestListener(
    WireMockPactGenerator
        .builder("myConsumer", "myProvider")
        .withRequestPathWhitelist(
            "/rest/.*",
            "/v2/api/.*"
        )
        .withRequestPathBlacklist(
            "/rest/experimental/.*"
        )
        .build()
);
...
// Pact interaction will be generated: Whitelisted
myClient.getResource("/rest/resource/123");

// Pact interaction will be generated: Whitelisted.
myClient.getResource("/v2/api/resources/123");

// NO pact interaction will be generated: Not in whitelist.
myClient.getResource("/internal/api/"); 

// NO pact interaction will be generated: Whitelisted but also blacklisted.
myClient.getResource("/rest/experimental/resource"); 
``` 

You can combine whitelist and blacklist values to match your needs. By default, when no whitelist or blacklist values
 are provided, all interactions will be saved.

## Changelog
See [CHANGELOG.md](CHANGELOG.md)

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md)

## License
See [LICENSE.txt](LICENSE.txt)
