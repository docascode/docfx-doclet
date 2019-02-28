
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


### Standalone

One can execute the `javadoc` command with the command line parameters:

    javadoc \
    -encoding UTF-8 \                                     # Source files encoding (not mandatory parameter)
    -docletpath ./target/docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \      # Set path to jar with doclet
    -doclet com.microsoft.doclet.DocFxDoclet \            # Set name of doclet class
    -classpath <list of jar with dependencies> \          # Put dependencies into classpath
    -sourcepath ./src/test/java \                         # Set localtion of jar with sources
    -outputpath ./target/test-out \                       # Set location of output files
    -excludepackages com\.msdn\..*:com\.ms\.news\..*  \   # List excluded packages separated by ':' (not mandatory parameter)
    -excludeclasses .*SomeClass:com\.ms\..*AnyClass \     # List excluded classes separated by ':' (not mandatory parameter)
    -subpackages com.microsoft.samples                    # Subpackages to recursively load separated by ':'

So to use doclet for sources packed into jar we need to unpack jar content and download its compile dependencies 
and put them to classpath for javadoc run.   

For example for [JUnit-4.12 sources](https://mvnrepository.com/artifact/junit/junit/4.12) we discovered from 
Maven central that this library depends on `hamcrest-core-1.3` library, so download this library, unpack sources jar 
and run next command:

    javadoc \
    -encoding UTF-8 \                                     # Source files encoding
    -docletpath ./docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \     # Set path to jar with doclet
    -doclet com.microsoft.doclet.DocFxDoclet \            # Set name of doclet class
    -cp ./hamcrest-core-1.3.jar \                         # Put dependencies into classpath
    -sourcepath ./junit-4.12-sources \                    # Set localtion of jar with sources 
    -outputpath ./test-out \                              # Set location of output files
    -subpackages org:junit                                # Subpackages to recursively load separated by ':'

See this script `sandbox/generate-yml-files.bat` for details


### Doclet development

For development it could be useful to use `DocletRunner` class.  
It makes javadoc call and takes params from external text file. To use it:  
- Create Run/Debug IDE configuration with main class `com.microsoft.doclet.DocletRunner`
- Add `src\test\resources\test-doclet-params.txt` as program arguments of configuration

Now we could run/debug doclet versus source code classes located at `com.microsoft.samples` package 
as mentioned in `test-doclet-params.txt` config file


### Serve DocFx documentation

- Install `Chocolately` using [its instructions](https://chocolatey.org/docs/installation)  
- Install docfx through `Chocolatey`: `choco install docfx -y`
- Generate set of yml files by java sources using one of aforementioned ways  
- Start DocFx web-server using this script: `sandbox/serve-docs.bat`  
- Connect to http://localhost:8080 to see how it looks like
