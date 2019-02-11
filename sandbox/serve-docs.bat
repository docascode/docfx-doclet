
echo "Remove folders"
rmdir /S /Q _site
rmdir /S /Q obj

echo "Serve documentation"
docfx docfx.json --serve
