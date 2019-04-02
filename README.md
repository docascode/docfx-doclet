
## JavaDoc Doclet for DocFX

[![Build status](https://apidrop.visualstudio.com/Toolshed/_apis/build/status/Toolshed-Maven-CI)](https://apidrop.visualstudio.com/Toolshed/_build/latest?definitionId=1633)

This doclet is designed to produce a YAML representation of the Javadoc-generated documentation, that can be integrated into [DocFX](https://dotnet.github.io/docfx/).

## Getting started

The easiest way is to just get the JAR files directly from our [releases](https://github.com/dendeli-msft/docfx-doclet/releases).

Alternatively, you can clone the repository and build it with the help of Maven. You can do so by calling: 

```bash
mvn compile
```

Once the compilation is complete, you will need to generate a JAR file, that can be used alongside `javadoc`. You can do so by calling:

```bash
mvn package
```

This will produce two JAR files that you can use - one with dependencies, and another one without.

## Usage 

### With `maven-javadoc-plugin`

When there is an existing java project where Maven is used as a build tool, one could add `maven-javadoc-plugin` to the root `pom.xml`:

```java
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-javadoc-plugin</artifactId>
  <version>3.0.1</version>
  <configuration>
    <doclet>com.microsoft.doclet.DocFxDoclet</doclet>
    <docletArtifact>
      <groupId>${project.groupId}</groupId>
      <artifactId>${project.artifactId}</artifactId>
      <version>${project.version}</version>
    </docletArtifact>
    <useStandardDocletOptions>false</useStandardDocletOptions>
    <additionalOptions>-outputpath ./generated-files</additionalOptions>
    <!-- Add additional options here when needed -->
  </configuration>
</plugin>
```

The doclet can then be ran with the following command: 

```bash
mvn javadoc:javadoc
```

The generated files will be placed in the `./target/site/apidocs/generated-files` folder  

### Usage of doclet with Gradle javadoc task

For Gradle project put jar with doclet to `libs` folder and add next task to `build.gradle`:

    task generateApiDocs(type: Javadoc) {
      source sourceSets.main.allJava
      classpath = configurations.compile
      options.encoding 'UTF-8'
      destinationDir = file("build/generated-files")
      options.addStringOption("doclet", "com.microsoft.doclet.DocFxDoclet")
      options.docletpath = [file("libs/docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar")]
      dependsOn build
    }

And run doclet using next command: `gradle generateApiDocs`  
In result generated files will be placed into `./build/generated-files` folder  

### Standalone

One can execute the `javadoc` command with the command line parameters:

```bash
javadoc \
-encoding UTF-8 \
-docletpath ./target/docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \
-doclet com.microsoft.doclet.DocFxDoclet \
-classpath <list of jar with dependencies> \
-sourcepath ./src/test/java \
-outputpath ./target/test-out \
-excludepackages com\.msdn\..*:com\.ms\.news\..*  \
-excludeclasses .*SomeClass:com\.ms\..*AnyClass \
-subpackages com.microsoft.samples
```

| Parameter | Description |
|-----------|-------------|
| `encoding` | Encoding for source files (_optional_). |
| `docletpath` | Path to the doclet JAR file. |
| `doclet` | Doclet class name. |
| `classpath` | List of dependencies to be included in the classpath (_optional_). |
| `sourcepath` | Location of the source code that needs to be documented. |
| `outputpath` | The location for the generated YAML files. |
| `excludepackages` | List of excluded packages, separated by a colon (`:`) (_optional_). |
| `excludeclasses` | List of excluded classes, separated by a colon (`:`) (_optional_). |
| `subpackages` | Subpackages to recursively load, separated by a colon (`:`). |


For example, if we would want to generate documentation for [JUnit-4.12 source code](https://mvnrepository.com/artifact/junit/junit/4.12), we would need to account for the fact that the library depends on `hamcrest-core-1.3`, therefore we would download this library, unpack the sources JAR and run the following command:

```bash
javadoc \
-encoding UTF-8 \                                     # Source files encoding
-docletpath ./docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \     # Set path to jar with doclet
-doclet com.microsoft.doclet.DocFxDoclet \            # Set name of doclet class
-cp ./hamcrest-core-1.3.jar \                         # Put dependencies into classpath
-sourcepath ./junit-4.12-sources \                    # Set localtion of jar with sources 
-outputpath ./test-out \                              # Set location of output files
-subpackages org:junit                                # Subpackages to recursively load separated by ':'
```

You can take a look at an [example documentation generation script](https://github.com/dendeli-msft/docfx-doclet/blob/master/sandbox/generate-yml-files.bat) outlining the process above.


## Development

When making changes, it is important to ensure that you are using `DocletRunner` class - it is responsible for makeing the `javadoc` call and takes params from an external configuration file.

To use it:  

- Create Run/Debug IDE configuration with the main class set as `com.microsoft.doclet.DocletRunner`
- Add `src\test\resources\test-doclet-params.txt` as program arguments of configuration

Now we could run/debug doclet against source code classes located in the `com.microsoft.samples` package, as specified in the `test-doclet-params.txt` config file.


### Serving DocFx documentation

1. Get DocFX. You can read about it on the [official site](https://dotnet.github.io/docfx/).
2. Initialize an empty docset, by calling: `docfx init -q`
3. Place the generated YAML files in the `api` folder in the generated docset.
4. Build the content in the folder by calling: `docfx`
5. Serve the content on a local web server: `docfx serve _site`
