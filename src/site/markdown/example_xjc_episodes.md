<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
Modular JAXB compilation with dependency episodes
==============================================-->

When several Maven modules compile schemas that reference each other's namespaces, each module
would normally re-generate Java classes for the shared namespace. A JAXB *episode* file tells
XJC that the types of a given namespace already have Java bindings elsewhere, so downstream
modules re-use the upstream model classes instead of generating duplicates.

Since plugin version `4.1.1`, the `xjc` goal can resolve episode files directly from Maven
dependency artifacts, comparable to the `episodes` configuration of other JAXB plugins.

Producer module: generating an episode
--------------------------------------

Have the upstream module generate its model classes and an episode file. The episode is written
to `META-INF/JAXB/<episodeFileName>.xjb` beneath the `outputDirectory` and packaged into the
module's JAR:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>jaxb2-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-a</id>
      <goals>
        <goal>xjc</goal>
      </goals>
      <configuration>
        <packageName>com.example.a</packageName>
        <episodeFileName>schema-a-episode</episodeFileName>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Consumer module: resolving episodes from dependencies
-----------------------------------------------------

Declare the upstream artifact as a regular dependency (the generated code compiles against
the re-used classes), and reference it within the `episodes` parameter:

```xml
<dependencies>
  <dependency>
    <groupId>org.codehaus.mojo.jaxb2.its</groupId>
    <artifactId>xjc-episode-schema-a</artifactId>
    <version>1.0-SNAPSHOT</version>
  </dependency>
</dependencies>

<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>jaxb2-maven-plugin</artifactId>
  <executions>
    <execution>
      <id>generate-b</id>
      <goals>
        <goal>xjc</goal>
      </goals>
      <configuration>
        <packageName>com.example.b</packageName>
        <episodes>
          <episode>
            <groupId>org.codehaus.mojo.jaxb2.its</groupId>
            <artifactId>xjc-episode-schema-a</artifactId>
            <version>1.0-SNAPSHOT</version>
          </episode>
        </episodes>
      </configuration>
    </execution>
  </executions>
</plugin>
```

Episode discovery order
-----------------------

Within each episode artifact, exactly one episode resource is selected using this order:

1. An explicit `<resourcePath>` within the `<episode>` element, when configured.
2. `META-INF/sun-jaxb.episode` (the JAXB reference implementation convention).
3. `META-INF/JAXB/sun-jaxb.episode.xjb`.
4. The single `*.xjb` file directly within `META-INF/JAXB/` (the location produced by this
   plugin), provided exactly one candidate exists.

An explicit episode whose artifact contains no episode resource fails the build, unless
`<optional>true</optional>` is configured. If multiple candidate episodes exist, the build
fails and lists the candidates; resolve the ambiguity by configuring `<resourcePath>`.

The selected episode resource is extracted beneath
`${project.build.directory}/jaxb2/episodes/` in a deterministic
`groupId/artifactId/version` layout, preserving the source entry's timestamp so that
incremental builds re-run XJC only when the episode artifact changes.

Scanning dependencies automatically
-----------------------------------

For projects where every dependency is deliberately a schema model artifact, explicit
declarations can be replaced by a scan of the project's dependencies:

```xml
<configuration>
  <useDependenciesAsEpisodes>true</useDependenciesAsEpisodes>
  <!-- Optional: also scan transitive dependencies. Default false. -->
  <includeTransitiveEpisodes>false</includeTransitiveEpisodes>
</configuration>
```

Dependencies without episode resources are ignored. By default only direct dependencies in
the `compile` or `runtime` scopes are scanned.

Configuration reference
-----------------------

|          Parameter           |                   Default                   |                         Description                          |
|------------------------------|---------------------------------------------|--------------------------------------------------------------|
| `episodes`                   | none                                        | Explicit episode artifacts, resolved through Maven Resolver. |
| `useDependenciesAsEpisodes`  | `false`                                     | Scan eligible project dependencies for episode resources.    |
| `includeTransitiveEpisodes`  | `false`                                     | Include transitive dependencies in the scan.                 |
| `episodeExtractionDirectory` | `${project.build.directory}/jaxb2/episodes` | Root directory for materialized episode files.               |

Each `<episode>` element supports `groupId`, `artifactId`, `version` (all required), `type`
(default `jar`), `classifier`, `resourcePath`, and `optional` (default `false`).

Limitations
-----------

An episode only prevents duplicate Java class generation. The imported XSD documents
themselves must still be resolvable by XJC - through a regular `schemaLocation`, an XML
catalog, or an unpack step.
