
## Doclet for generation of DocFx ready files for Java source code

#### Usage of doclet as part of usual maven build:  
`mvn javadoc:javadoc`

#### To use doclet standalone, without maven:    
<pre>
javadoc \
-docletpath ./target/docfx-doclet-1.0-SNAPSHOT.jar \
-doclet com.microsoft.doclet.DocFxDoclet \
-sourcepath ./src/main/java \
-outputpath ./target/test-out \
-subpackages com.microsoft.samples
</pre>
Before this action run `mvn clean install` once to put jar into artifactory

#### To use doclet for sources packed into jar  
we need to unpack jar content and download libraries which this jar depends on 
and put them to classpath  

For example for [junit-4.12-sources](https://mvnrepository.com/artifact/junit/junit/4.12) we discovered from Maven central 
that it depends on hamcrest-core-1.3.jar library, so downloaded this hamcrest library, unpack sources jar and run:
<pre>
javadoc \
-docletpath ./target/docfx-doclet-1.0-SNAPSHOT.jar \
-doclet com.microsoft.doclet.DocFxDoclet \
-cp ./hamcrest-core-1.3.jar \
-sourcepath ./junit-4.12-sources \
-outputpath ./target/test-out \
-subpackages org:junit
</pre>

#### For development it could be useful to use DocletRunner placed here
- Create Run/Debug configuration with main class `com.microsoft.doclet.DocletRunner`
- Add `src\test\resources\test-doclet-params.txt` as program arguments of configuration

No we could run/debug doclet versus source code classes located at `com.microsoft.samples` 
as mentioned in `test-doclet-params.txt` config file
