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
XML Catalogs and XSDs from Maven Dependencies
==========================================-->

When XML schemas import other schemas using public URLs or abstract system IDs (such as
`http://example.com/schemas/common.xsd`), XJC needs to resolve these identifiers to local files
without accessing the internet during build time.

XML Catalogs (OASIS XML Catalog and TR9401 plain-text formats) map public IDs and system IDs to
local files.

Since plugin version `4.1.1` (resolving issue #264), the `xjc` goal supports the `maven:` URI scheme
within catalog files, allowing you to reference XML schemas packaged directly inside Maven dependency
JARs without unpacking them first. This feature is compatible with the `maven:` scheme originally
introduced in `highsource/jaxb-tools`.

The `maven:` URI Scheme
-----------------------

A `maven:` URI has the following structure:

```text
maven:groupId:artifactId:type:classifier!/path/inside/jar
```

* **`groupId`**: The Maven group ID of the dependency containing the XSD.
* **`artifactId`**: The Maven artifact ID.
* **`type`**: Optional artifact extension/type (defaults to `jar` if omitted).
* **`classifier`**: Optional artifact classifier (may be omitted or empty).
* **`path/inside/jar`**: The relative path to the schema file (or directory) inside the artifact JAR.

The version of the artifact is automatically inferred from the project's declared `<dependencies>`.

Example 1: OASIS XML Catalog (`catalog.xml`)
--------------------------------------------

Map a public schema URL to an XSD inside a dependency JAR using `<system>`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
    <system systemId="http://example.com/schemas/address.xsd"
            uri="maven:org.example:shared-schemas:jar:!/schemas/address.xsd"/>
</catalog>
```

Or map an entire URL prefix to a directory inside the dependency JAR using `<rewriteSystem>`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog">
    <rewriteSystem systemIdStartString="http://example.com/schemas/"
                   rewritePrefix="maven:org.example:shared-schemas:jar:!/schemas/"/>
</catalog>
```

Example 2: TR9401 Plain Text Catalog (`catalog.cat`)
----------------------------------------------------

```text
REWRITE_SYSTEM "http://example.com/schemas/" "maven:org.example:shared-schemas:jar:!/schemas/"
```

Configuring the Plugin in `pom.xml`
-----------------------------------

1. Declare the artifact containing the XSD as a project `<dependency>`:

```xml
<dependencies>
  <dependency>
    <groupId>org.example</groupId>
    <artifactId>shared-schemas</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

2. Point the `jaxb2-maven-plugin` to your catalog file:

```xml
<plugin>
  <groupId>org.codehaus.mojo</groupId>
  <artifactId>jaxb2-maven-plugin</artifactId>
  <version>4.1.1-SNAPSHOT</version>
  <executions>
    <execution>
      <id>xjc</id>
      <goals>
        <goal>xjc</goal>
      </goals>
      <configuration>
        <catalog>src/main/resources/catalog.xml</catalog>
        <packageName>com.example.model</packageName>
      </configuration>
    </execution>
  </executions>
</plugin>
```

During build execution, the plugin detects the `maven:` URIs, resolves the artifact's local JAR file
from Maven dependencies, rewrites the catalog to concrete `jar:file://` URLs in
`${project.build.directory}/jaxb2/resolved-catalog.xml`, and passes the resolved catalog to XJC.
