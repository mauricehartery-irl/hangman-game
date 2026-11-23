# Hangman

A responsive, accessible Hangman game made with plain HTML, CSS, and JavaScript. No build tools or dependencies are required.

The repository also includes a native Android app written in Java under `android/`.

## Play

Open `index.html` in a browser, choose letters with the on-screen keys or your keyboard, and press **New game** (or Enter) to play again.

## Features

- Random words across five categories
- Six-part drawing and clear win/loss messages
- On-screen and physical keyboard input
- Responsive layout and screen-reader announcements

## Android APK

Every push to `main` that changes `android/` starts the **Build Android APK** workflow. When it succeeds, download `hangman-debug-apk` from the workflow run's Artifacts section. You can also start a build manually from the repository's Actions tab.

The workflow creates a debug APK for testing and sideloading. It is not a Play Store release APK.

