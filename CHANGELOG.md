<a name="2.2.1"></a>
## [2.2.1](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v2.2.0...v2.2.1) (2019-06-18)


### Bug Fixes

* bump dependencies ([46d229c](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/46d229c))



<a name="2.2.0"></a>
# [2.2.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v2.1.0...v2.2.0) (2018-11-26)


### Features

* Ignore debug headers added by WireMock 2.19.0+ ([a332315](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/a332315))



<a name="2.1.0"></a>
# [2.1.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v2.0.0...v2.1.0) (2018-08-05)


### Features

* support for non-strict application/json serialization ([387531c](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/387531c))



<a name="2.0.0"></a>
# [2.0.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v1.2.0...v2.0.0) (2018-03-20)


### Bug Fixes

* avoid WireMock collision when using with spring-cloud-contract-wiremock ([f90a6ff](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/f90a6ff))


### BREAKING CHANGES

* from now on, users need to explicitly specify WireMock dependency in their projects



<a name="1.2.0"></a>
# [1.2.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v1.1.0...v1.2.0) (2018-02-20)


### Features

* support interaction filtering via request path blacklist ([46bc112](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/46bc112))



<a name="1.1.0"></a>
# [1.1.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v1.0.0...v1.1.0) (2017-12-13)


### Features

* warning in description of interaction with non-configured response ([a6b0265](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/a6b0265))



<a name="1.0.0"></a>
# [1.0.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v0.1.0...v1.0.0) (2017-11-23)


### Bug Fixes

* add support for wiremock from v1.57 to v2.11.0 ([c49bacf](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/c49bacf))



<a name="0.1.0"></a>
# [0.1.0](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v0.0.6...v0.1.0) (2017-09-01)


### Features

* add interaction filtering support ([7e098d0](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/7e098d0))


### BREAKING CHANGES

* The api to create a WireMockPactGenerator has been changed to cater for passing request path filters.

To migrate change this:
wireMockServer.addMockServiceRequestListener(new WireMockPactGenerator("the-consumer", "the-provider"));

To this:
wireMockServer.addMockServiceRequestListener(WireMockPactGenerator.builder("the-consumer", "the-provider").build());



<a name="0.0.6"></a>
## [0.0.6](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v0.0.5...v0.0.6) (2017-08-23)


### Features

* force exit if an unexpected error occurs in the context of wiremock-pact-generator ([388de7b](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/388de7b))
* ignore 'host' header in requests ([24864bf](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/24864bf))
* include unique id in pact filename ([856cfcc](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/856cfcc))



<a name="0.0.5"></a>
## [0.0.5](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v0.0.4...v0.0.5) (2017-08-18)


### Features

* JSON serialization of bodies containing JSON objects/arrays ([7d57938](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/7d57938))



<a name="0.0.4"></a>
## [0.0.4](https://bitbucket.org/atlassian/wiremock-pact-generator/compare/v0.0.3...v0.0.4) (2017-08-17)


### Features

* generate pact files from wiremock requests ([a9378e7](https://bitbucket.org/atlassian/wiremock-pact-generator/commits/a9378e7))


