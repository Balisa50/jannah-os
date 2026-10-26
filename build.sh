#!/bin/bash
echo "Pushing code to GitHub..."
git push origin main

echo "Waiting 10 seconds for the cloud server to start..."
sleep 10

echo "Watching build..."
gh run watch $(gh run list -L 1 --json databaseId --jq '.[0].databaseId') --exit-status

echo "Downloading APK from Release..."
rm -rf ./apk_output
gh release download latest -p "*.apk" -D ./apk_output
cp ./apk_output/*.apk ~/storage/shared/Download/Jannah-Latest.apk
rm -rf ./apk_output
echo "Success. Install Jannah-Latest.apk from your Downloads folder."
