#!/data/data/com.termux/files/usr/bin/bash

echo "Pushing code to GitHub..."
git add -A
git commit -m "Auto-build: $(date +'%Y-%m-%d %H:%M:%S')" || true
git push origin main

echo "Waiting for cloud server to start..."
sleep 10

echo "Watching build..."
RUN_ID=$(gh run list --limit 1 --json databaseId --jq '.[0].databaseId')
gh run watch $RUN_ID

STATUS=$(gh run view $RUN_ID --json conclusion --jq '.conclusion')
if [ "$STATUS" != "success" ]; then
    echo "Build failed. Run: gh run view $RUN_ID --log-failed"
    exit 1
fi

echo "Downloading APK..."
gh run download $RUN_ID -n Jannah-APK -D ./apk_output

echo "Moving APK to Downloads..."
cp ./apk_output/app-debug.apk ~/storage/shared/Download/Jannah-Latest.apk
rm -rf ./apk_output

echo "Success. Install Jannah-Latest.apk from your Downloads folder."
