
set SANDBOX_FOLDER="../target/sandbox"

echo "Copy files"
copy docfx.json %SANDBOX_FOLDER%
copy toc.yml %SANDBOX_FOLDER%

echo "Remove folders"
pushd %SANDBOX_FOLDER%
rmdir /S /Q _site
rmdir /S /Q obj

echo "Serve documentation"
docfx docfx.json --serve
popd
