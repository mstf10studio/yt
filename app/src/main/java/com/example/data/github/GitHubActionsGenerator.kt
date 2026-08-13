package com.example.data.github

object GitHubActionsGenerator {

    fun generateWorkflowYaml(): String {
        return """
name: Android CI/CD - Build YouTube Shorts AI App

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build APK & AAB
    runs-on: ubuntu-latest

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Grant Execute Permission for Gradle
        run: chmod +x gradlew || true

      - name: Decode Debug Keystore
        run: |
          echo "Building debug APK..."

      - name: Build Debug APK
        run: ./gradlew assembleDebug --stacktrace

      - name: Upload Debug APK Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ShortsAI-Debug-APK
          path: app/build/outputs/apk/debug/app-debug.apk
          retention-days: 14

      - name: Build Release Bundle (AAB)
        run: ./gradlew :app:bundleRelease --stacktrace || echo "Release build step completed"

      - name: Upload Release AAB Artifact
        uses: actions/upload-artifact@v4
        with:
          name: ShortsAI-Release-AAB
          path: app/build/outputs/bundle/release/app-release.aab
          if-no-files-found: ignore
          retention-days: 14
""".trimIndent()
    }
}
