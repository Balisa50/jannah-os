# SIRAJ (سِرَاج)

SIRAJ is an Android-first intelligent Islamic digital companion in active development.

The direction is a unified experience for conversational interaction, trusted Islamic knowledge, personal context, and permissioned actions. It is not intended to be another static prayer-times dashboard or an ungrounded religious chatbot.

## Development

- Android: Kotlin, Jetpack Compose, Material 3
- Minimum Android: API 26
- Build JDK: 17
- Build debug APK: ./gradlew assembleDebug
- Run unit tests: ./gradlew testDebugUnitTest
- Run lint: ./gradlew lintDebug

The current source tree still contains legacy Jannah OS modules while the SIRAJ architecture and experience are being built incrementally. Do not assume every legacy feature is production-ready.

## Security

Never commit signing keys, passwords, API tokens, local machine configuration, or user data. See SECURITY.md before preparing a release.
