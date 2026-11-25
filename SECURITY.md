# Security and release-key incident

## Immediate status

The repository previously contained a release keystore and its passwords in tracked source. Treat that signing identity and every password that appeared in Git history as compromised.

This change removes the keystore from the current branch and prevents local signing files from being committed again. It does not erase prior Git history, cached clones, forks, or already-downloaded release APKs.

## Required owner actions

1. Temporarily change the GitHub repository visibility to Private while the incident is cleaned up.
2. Remove the exposed latest release and any public copies of APKs signed with the exposed key.
3. Rotate the Android signing identity. Generate a new keystore locally; never commit it.
4. Configure the four GitHub Actions secrets below only after generating and safeguarding the new keystore:
   - SIRAJ_RELEASE_KEYSTORE_BASE64 (base64-encoded keystore file)
   - SIRAJ_RELEASE_STORE_PASSWORD
   - SIRAJ_RELEASE_KEY_ALIAS
   - SIRAJ_RELEASE_KEY_PASSWORD
5. Rewrite repository history to remove jannah.keystore, local.properties, and any commits containing signing credentials. Coordinate the rewrite with every clone/fork before making the repository public again.

## Existing installations

An APK signed with the old key cannot be updated in place with a newly generated key. Before replacing an installed build, export or otherwise preserve any user data that matters. Do not uninstall the old app until its data is backed up and a migration plan is chosen.

## CI policy

Pull requests and pushes run tests, lint, and a debug build. CI does not publish APKs or create a public release. Release signing is optional and reads credentials from Gradle properties or environment variables only. A release build without signing configuration is intentionally unsigned.
