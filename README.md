
### Example of custom doclet

ListClassesDoclet used for just printing class names to console

To perform build of project and put jar to artifactory run: `mvn clean install`

To use custom doclet run: `mvn javadoc:javadoc`

Use this doclet standalone, without maven:  
<pre>
javadoc \
-docletpath ./target/custom-doclet-1.0-SNAPSHOT.jar \
-doclet by.andd3dfx.doclet.OldCustomDoclet \
-classpath "${JAVA_HOME}/lib/tools.jar" \
-sourcepath ./src/main/java \
-subpackages by.andd3dfx.samples
</pre>
