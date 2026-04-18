# DropOnAir Demo - Android

Native Kotlin demo app showing how to integrate the [DropOnAir Android SDK](https://github.com/DropOnAir/droponair-sdk-android) for end-to-end encrypted messaging.

## What it demonstrates

- Login via the [demo backend](https://github.com/DropOnAir/droponair-demo-backend) (JWT issued on `/api/auth/login`)
- Initialising `DropOnAir` with a token-exchange URL
- Connecting as a user and receiving `onConnected` / `onDisconnected` callbacks
- Sending and receiving end-to-end-encrypted text messages in a chat UI
- Incoming call dialog (accept/reject)

## Requirements

- Android Studio Hedgehog (2023.1.1)+
- Android API 26+ (emulator or device)
- The [demo backend](https://github.com/DropOnAir/droponair-demo-backend) running on `localhost:8180`

## Getting Started

1. Clone this repo and the [demo backend](https://github.com/DropOnAir/droponair-demo-backend)
2. Create a free account at [panel.droponair.com](https://panel.droponair.com) and create an app
3. Edit `app/build.gradle.kts` with your credentials:

```kotlin
buildConfigField("String", "DROPONAIR_APP_ID",         "\"YOUR_APP_ID\"")
buildConfigField("String", "DROPONAIR_PUBLIC_API_KEY", "\"YOUR_PUBLIC_API_KEY\"")
buildConfigField("String", "BACKEND_URL",              "\"http://10.0.2.2:8180\"")
```

> `10.0.2.2` is the Android emulator alias for the host machine's `localhost`.

## Run

1. Start the backend: `cd droponair-demo-backend && ./mvnw spring-boot:run`
2. Open this folder in Android Studio
3. Select the **app** configuration and a running AVD
4. Build & Run (Shift+F10)

## Architecture

```
DemoApplication , Application class, lazy SDK init
LoginActivity   , userId input, SDK init + connect
ChatActivity    , RecyclerView messages, DropOnAirListener callbacks
ChatAdapter     , RecyclerView adapter, isSelf styling
BackendService  , OkHttp login call + JWT caching
```
