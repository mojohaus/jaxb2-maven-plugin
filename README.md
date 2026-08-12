# About the Jaxb2-Maven-Plugin

[![Maven Central](https://img.shields.io/maven-central/v/org.codehaus.mojo/jaxb2-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/org.codehaus.mojo/jaxb2-maven-plugin)
[![Build Status](https://github.com/mojohaus/jaxb2-maven-plugin/actions/workflows/maven.yml/badge.svg)](https://github.com/mojohaus/jaxb2-maven-plugin/actions/workflows/maven.yml)

This Maven plugin uses Jakarta XML Binding to perform one of 2 main tasks:

1. Generate Java classes from XML Schemas (and optionally binding files).
   This is done by delegating work to the XJC tool.
2. Create XML Schemas from annotated Java classes.
   This is done by delegating work to the Schemagen tool.

Both tools were part of the JDK up to and including Java 8, and were removed from it afterwards. The plugin
therefore brings its own copies along as ordinary dependencies (`jaxb-xjc` and `jaxb-jxc`), which is why the
generated code follows the Jakarta XML Binding version those artifacts implement rather than anything supplied
by the JDK in use. Since 3.0.0 that means the `jakarta.xml.bind` namespace; 2.x generated `javax.xml.bind`.

## Documentation

The plugin site, covering configuration, goals and worked examples, is published at
[www.mojohaus.org/jaxb2-maven-plugin](https://www.mojohaus.org/jaxb2-maven-plugin/) and describes the most
recent release.

## Contributing

Issues and pull requests are welcome. For a defect, the single most useful thing you can attach is a minimal
project that reproduces it against the current release — most of the reports that stayed open longest did so
because nobody could reproduce them.

Build and test locally with:

```bash
mvn verify
```

That runs the unit tests. The integration tests under `src/it` are run by the `run-its` profile, as CI does:

```bash
mvn -P run-its verify
```

## Releasing

The plugin follows the standard MojoHaus process, described in
[Performing a Release](https://www.mojohaus.org/development/performing-a-release.html).

The site is published to the `gh-pages` branch of this repository, at the root of that branch, and reflects
the latest release only.
