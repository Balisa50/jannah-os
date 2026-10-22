#!/data/data/com.termux/files/usr/bin/bash

echo "🚀 Pushing code to GitHub..."
git add -A
git commit -m "Auto-build: $(date +'%Y-%m-%d %H:%M:%S')" || true
git push origin main

echo "⏳ Waiting 10 seconds for the cloud server to boot..."
sleep 10

echo "👀 Watching the build (This takes about 3 minutes)..."
RUN_ID=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')
gh run watch $RUN_ID

echo "📦 Downloading APK..."
gh run download $RUN_ID -n Jannah-APK -D ./apk_output

echo "📂 Moving APK to your phone's Downloads..."
cp ./apk_output/app-debug.apk ~/storage/shared/Download/Jannah-Latest.apk
rm -rf ./apk_output

echo "✅ Done! Go to your Downloads folder and install Jannah-Latest.apk."
