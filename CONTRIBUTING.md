# Contributing

## Contributor License Agreement

Atlassian requires contributors to sign a Contributor License Agreement, known as a CLA. This serves as a record stating
that the contributor is entitled to contribute the code/documentation/translation to the project and is willing to have
it used in distributions and derivative works (or is willing to transfer ownership).

Prior to accepting your contributions we ask that you please follow the appropriate link below to digitally sign the
CLA. The Corporate CLA is for those who are contributing as a member of an organisation and the individual CLA is for
those contributing as an individual.

* [CLA for corporate contributors](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=e1c17c66-ca4d-4aab-a953-2c231af4a20b)
* [CLA for individuals](https://na2.docusign.net/Member/PowerFormSigning.aspx?PowerFormId=3f94fbdc-2fbe-46ac-b14c-5d152700ae5d)

## Guidelines for pull requests

- Write tests for any changes.
- Follow existing code style and conventions.
- Separate unrelated changes into multiple pull requests.
- For bigger changes, make sure you start a discussion first by creating an issue and explaining the intended change.
- Ensure the build is green before you open your PR. The Pipelines build won't run by default on a remote branch, so
enable Pipelines.
- Use [conventional changelog conventions](https://github.com/bcoe/conventional-changelog-standard/blob/master/convention.md)
in your commit messages.

## Development dependencies

- Java 1.8 or higher
- mvnvm 1.0.9 or higher

## Setting up a development machine

Build the project and run all tests
```
mvn clean install
```

## During development

Commits to this codebase should follow the [conventional changelog conventions](https://github.com/bcoe/conventional-changelog-standard/blob/master/convention.md).

- `mvn verify` - Runs all the tests, checkstyle, and lints commit messages. Execute it before pushing any changes.

If you believe your changes may break the integration with old WireMock versions you can test that locally before it's
verified in the CI. Check the available profiles in `pom.xml`. E.g. verify the tests pass using the oldest supported
version:

`mvn -P wiremock-1.57 verify` 

## Releasing a new version

1. Ensure you are on the master branch

2. Decide whether the next version will be major, minor, or patch release according to
[Semantic Versioning](http://semver.org/).

3. Execute `bin/semver_bump.sh` passing either `major`, `minor`, or `patch` as argument.

```
# bin/semver_bump.sh patch
 1 file changed, 1 insertion(+), 1 deletion(-)
Version bumped from 0.0.1 to 0.0.2
```

3. The new version, the updated `CHANGELOG.md`, and updated `THIRD-PARTY.txt` will be automatically committed.
Push the changes.

4. Once the changes are in master they will automatically be published by the following bamboo build:
[https://engservices-bamboo.internal.atlassian.com/browse/WPG-REL](https://engservices-bamboo.internal.atlassian.com/browse/WPG-REL)
