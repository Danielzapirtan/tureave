# Native Android planner

This repository includes a native Kotlin Android version of the Romanian shift and leave planner.

## Included

- Large, scrollable monthly calendar designed for phone readability
- Previous/next month navigation
- Four-day cycle: day, night, free, free
- Day status editing: lucrat, lucrat parțial, concediu, liber
- Partial-day hours from 1 to 11
- Monthly realized-hours and leave/free summaries
- Local persistence of day overrides
- Weekend, cycle, status, and selected Romanian public-holiday colors
- Accessible day descriptions for screen readers

## Build

```sh
./gradlew assembleDebug
./gradlew check
./gradlew installDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

The original static web app remains unchanged in `index.html`.
