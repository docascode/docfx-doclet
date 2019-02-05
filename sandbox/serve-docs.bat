
set SOURCES_FOLDER="./unpacked-sources"
set GENERATED_FOLDER="./generated-yml-files"

echo "Remove folders"
rmdir /S /Q _site
rmdir /S /Q obj

echo "Serve documentation"
docfx docfx.json --serve
