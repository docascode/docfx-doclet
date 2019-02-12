
set SOURCES_FOLDER="../target/sandbox/unpacked-sources"
set GENERATED_FOLDER="../target/sandbox/generated-yml-files"

echo "Remove folders"
rmdir /S /Q %SOURCES_FOLDER%
rmdir /S /Q %GENERATED_FOLDER%

echo "Create directory to extract sources"
mkdir %SOURCES_FOLDER%

echo "Extract sources from jar file"
pushd %SOURCES_FOLDER%
jar xf ../../../sandbox/libs/junit-4.12-sources.jar
popd

echo "Before this action make sure that doclet jar presents in target folder. So build it preliminary when needed"
echo "Generate yml files"
javadoc ^
-encoding UTF-8 ^
-docletpath ../target/docfx-doclet-1.0-SNAPSHOT-jar-with-dependencies.jar ^
-doclet com.microsoft.doclet.DocFxDoclet ^
-cp ./libs/hamcrest-core-1.3.jar ^
-sourcepath %SOURCES_FOLDER% ^
-outputpath %GENERATED_FOLDER% ^
-subpackages org:junit
