
### Example of custom doclet

##### Usage of doclet as part of usual maven build:  
`mvn javadoc:javadoc`

##### To use doclet standalone, without maven:    
<pre>
javadoc \
-docletpath ./target/custom-doclet-1.0-SNAPSHOT.jar \
-doclet by.andd3dfx.doclet.DocFxDoclet \
-sourcepath ./src/main/java \
-subpackages by.andd3dfx.samples
</pre>
Before this action run `mvn clean install` once to put jar into artifactory

##### To use doclet for sources packed into jar  
we need to unpack jar content and download libraries which this jar depends on 
and put them to classpath  

For example for [junit-4.12-sources](https://mvnrepository.com/artifact/junit/junit/4.12) we discovered from Maven central 
that it depends on hamcrest-core-1.3.jar library, so downloaded this hamcrest library, unpack sources jar and run:
<pre>
javadoc \
-docletpath ./target/custom-doclet-1.0-SNAPSHOT.jar \
-doclet by.andd3dfx.doclet.DocFxDoclet \
-cp ./hamcrest-core-1.3.jar \
-sourcepath ./junit-4.12-sources \
-subpackages org:junit
</pre>
