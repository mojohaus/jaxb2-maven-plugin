# Agent guide for jaxb2-maven-plugin

This file is for automated agents and human contributors working on this repository. It records
what the code and the POMs do not make obvious.

## Layout

This is a single-module Maven plugin. The goals are `xjc` and `testXjc` (`XjcMojo`, `TestXjcMojo`,
in `javageneration`) and `schemagen` and `testSchemagen` (`SchemaGenerationMojo`,
`TestSchemaGenerationMojo`, in `schemageneration`). The shared base class is `AbstractJaxbMojo`.
The XJC and Schemagen tools are not taken from the JDK; they come in as the `jaxb-xjc` and
`jaxb-jxc` dependencies.

- The site is built from `src/site/markdown`, with the FAQ in `src/site/fml/faq.fml`.

## Build gates

- `spotless-maven-plugin` from mojo-parent runs during `process-sources`. Run `mvn spotless:apply`
  before committing. It formats Java with palantir-java-format, sorts the POM, and also reformats
  Markdown, including this file and the README.
- The build compiles with `mojo.java.target` 11, because JAXB 4 needs Java 11.
- The plugin compiles against `mavenVersion` (3.9.16) and declares a literal Maven 3.9.0 as its
  `<prerequisites>`; `minimalMavenBuildVersion` holds the build to the same floor. Raising
  `mavenVersion` must not raise the prerequisite.
- Fields of `AbstractJaxbMojo` and the goal classes are plugin parameters; renaming one changes
  the plugin's configuration API.

## Unit tests

- JUnit 5 only, under `src/test/java`. Test inputs are in `src/test/resources/testdata` and
  `src/test/resources/testschemas`; use `@TempDir` for anything a test writes.
- `mvn verify` runs the unit tests; the integration tests are skipped without `-P run-its`.

## Integration tests

- They live in `src/it`, one directory per test, and run with the `run-its` profile. The invoker
  execution installs the plugin into `target/local-repo` itself, so no separate `mvn install` is
  needed. Run a subset with:

  ```shell
  mvn -B -P run-its verify -Dinvoker.test=xjc-main,xjc-test
  ```

  The build runs the unit tests first, then the two integration tests. There are no `setup-*`
  projects.

- They run in parallel, one per CPU core (`invoker.parallelThreads` 1C), and every forked Maven
  gets `JAVA_TOOL_OPTIONS=-XX:TieredStopAtLevel=1 -XX:+UseSerialGC`. A test must not depend on
  another test's output or on running alone.

- The invoker runs `clean test-compile` in each project, then the `verify.groovy` post-build hook.
  Write `verify.groovy` as `assert` statements. Invoker fails a script whose last expression is
  anything other than `true` or `null`, so a trailing `list.each { }` fails the test; use a `for`
  loop.

- `xjc-with-explicit-locale` is excluded in the POM because it needs a locale setup that is no
  longer dynamic. Adding it back needs a fix, not just removing the exclude.

## Continuous integration

- `.github/workflows/maven.yml` calls the shared `maven-verify.yml@v5` workflow from
  `apache/maven-gh-actions-shared`, with `-P run-its install` as the build goal.
- Branches pushed to this repository build under the `push` event.

## Dependencies

- Dependabot updates are monthly and grouped. The pins in `.github/dependabot.yml`, each with its
  reason in a comment there: Maven core artifacts stay below 3.10, Checkstyle below 10, Maven
  Resolver below 2, SLF4J below 2, and `jguru-shared-algorithms-api` below 0.13.0.

## Pull requests

- Squash and rebase merges are enabled; merge commits are not. Release Drafter, through the shared
  `apache/maven-gh-actions-shared` workflow, builds the release notes from pull request titles
  and labels, so the title should describe the change for a user.

