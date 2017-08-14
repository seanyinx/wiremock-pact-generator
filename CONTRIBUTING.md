## Contributing

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


## Releasing

For project admins only.

1. Decide whether the next version will be major, minor, or patch release according to
[Semver](http://semver.org/).

2. Execute `bin/semver_bump.sh` passing either `major`, `minor`, or `patch` as argument. E.g.:

```
# bin/semver_bump.sh patch
Version bumped from 0.0.1 to 0.0.2
```

3. Commit and push the changes.

4. Once the changes are in master 
[this bamboo build](https://engservices-bamboo.internal.atlassian.com/browse/IM-WPC)
will automatically run and publish the new version.
