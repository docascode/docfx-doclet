
## Doclet for generation of DocFx ready files for Java source code

### Usage of doclet as part of usual maven build:
`mvn javadoc:javadoc`  
Generated files placed into `./target/site/apidocs/generated-files`  
For details - see how `maven-javadoc-plugin` configured in `pom.xml`

### To use doclet standalone, without maven:
<pre>
javadoc \
-encoding UTF-8 \
-docletpath ./target/docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \
-doclet com.microsoft.doclet.DocFxDoclet \
-sourcepath ./src/test/java \
-outputpath ./target/test-out \
-excludepackages com\.msdn\..*:com\.ms\.news\..*  \   # Not mandatory parameter
-excludeclasses .*SomeClass:com\.ms\..*AnyClass       # Not mandatory parameter
-subpackages com.microsoft.samples
</pre>
Before this action run `mvn clean install` once to put jar into artifactory

### To use doclet for sources packed into jar
we need to unpack jar content and download libraries which this jar depends on 
and put them to classpath  

For example for [junit-4.12-sources](https://mvnrepository.com/artifact/junit/junit/4.12) we discovered from Maven central 
that it depends on hamcrest-core-1.3.jar library, so downloaded this hamcrest library, unpack sources jar and run:
<pre>
javadoc \
-encoding UTF-8 \
-docletpath ./docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar \
-doclet com.microsoft.doclet.DocFxDoclet \
-cp ./hamcrest-core-1.3.jar \
-sourcepath ./junit-4.12-sources \
-outputpath ./test-out \
-excludepackages com\.msdn\..*:com\.ms\.news\..*  \   # Not mandatory parameter
-excludeclasses .*SomeClass:com\.ms\..*AnyClass       # Not mandatory parameter
-subpackages org:junit
</pre>

In addition see this script: `sandbox/generate-yml-files.bat`

### For development it could be useful to use DocletRunner placed here
- Create Run/Debug configuration with main class `com.microsoft.doclet.DocletRunner`
- Add `src\test\resources\test-doclet-params.txt` as program arguments of configuration

No we could run/debug doclet versus source code classes located at `com.microsoft.samples` 
as mentioned in `test-doclet-params.txt` config file

### Serve DocFx documentation
According to instruction from [Getting started page](https://dotnet.github.io/docfx/tutorial/docfx_getting_started.html) 
install Chocolately firstly  
After that use this script for generated files: `sandbox/serve-docs.bat`  
Connect to http://localhost:8080 to see how it looks like
