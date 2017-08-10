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

Pull requests, issues and comments welcome. For pull requests:

* Add tests for new features and bug fixes
* Follow the existing style (checkstyle checking is enabled by default in builds)
* Separate unrelated changes into multiple pull requests

If you add/remove/change any dependency, generate a new `THIRD-PARTY.txt` by executing:

```
mvn license:add-third-party
```

Please ensure that your branch builds successfully before you open your PR. The Pipelines build won't run by default 
on a remote branch, so either enable Pipelines for your fork or run the build locally: 

```
mvn clean verify javadoc:javadoc
```

See the existing [issues](https://bitbucket.org/atlassian/wiremock-pact-generator/issues) for things to start
contributing. If you want to start working on an issue, please assign the ticket to yourself and mark it as `open`
so others know it is in progress.

For bigger changes, make sure you start a discussion first by creating
an issue and explaining the intended change.

Atlassian requires contributors to sign a Contributor License Agreement,
known as a CLA. This serves as a record stating that the contributor is
entitled to contribute the code/documentation/translation to the project
and is willing to have it used in distributions and derivative works
(or is willing to transfer ownership).

Prior to accepting your contributions we ask that you please follow the appropriate
link below to digitally sign the CLA. The Corporate CLA is for those who are
contributing as a member of an organization and the individual CLA is for
those contributing as an individual.

* [CLA for corporate contributors](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=e1c17c66-ca4d-4aab-a953-2c231af4a20b)
* [CLA for individuals](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=3f94fbdc-2fbe-46ac-b14c-5d152700ae5d)

## License ##

Copyright (c) 2017 Atlassian and others. Apache 2.0 licensed, see LICENSE.txt file.