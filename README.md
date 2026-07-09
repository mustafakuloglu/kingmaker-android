# Kingmaker

An Android app that watches your outreach queue in the background and surfaces the
single highest-priority next action as a floating pop-up — over whatever app you're
currently using — so you can approve, edit, or skip it without breaking flow.

## How it works

- A backend syncs contact/interaction data from MCPs, then periodically runs a
  Claude agent over that data to rank the next best outreach actions.
- The Android app polls for the top action while backgrounded and shows it as a
  system overlay: who it's for, why now, and a pre-drafted message you can edit
  before sending.
- Opening the app itself shows a dashboard (active goal, queue, contact stats) and
  pauses the popup polling — no point popping a card over the app you're already
  looking at.

## Architecture

```mermaid
graph TB
    subgraph MCPs["External MCPs"]
        LinkedIn[LinkedIn]
        Gmail[Gmail]
        WhatsApp[WhatsApp]
    end

    subgraph Backend["Backend (FastAPI)"]
        SyncJob["MCP sync job<br/>(periodic)"]
        AnalysisJob["Priority analysis job<br/>(Claude Sonnet 4.5, periodic)"]
        LocalStore[("Local data store")]
        CheckAction["GET /check_action"]
        TakeAction["POST /take_action"]
        Dashboard["GET /dashboard"]
    end

    subgraph Android["Android App"]
        MonitorService["MonitorService<br/>(foreground service, background poll)"]
        Popup["Overlay popup<br/>(Jetpack Compose)"]
        MainActivity["MainActivity<br/>(dashboard screen)"]
    end

    LinkedIn --> SyncJob
    Gmail --> SyncJob
    WhatsApp --> SyncJob
    SyncJob --> LocalStore
    LocalStore <--> AnalysisJob
    LocalStore --> CheckAction
    LocalStore --> Dashboard
    TakeAction --> LinkedIn
    TakeAction --> Gmail
    TakeAction --> WhatsApp

    MonitorService -- "poll" --> CheckAction
    CheckAction -- "next action" --> MonitorService
    MonitorService --> Popup
    Popup -- "Send (edited message)" --> TakeAction
    MainActivity -- "poll" --> Dashboard
```

## Popup flow

```mermaid
sequenceDiagram
    participant Service as MonitorService
    participant Backend
    participant Contact as Contact (via MCP)

    loop while app is backgrounded
        Service->>Backend: GET /check_action
        alt action available
            Backend-->>Service: status: ACTION, id, who, message, ...
            Backend->>Backend: mark action as delivered
            Service->>Service: show popup overlay
            alt user taps Send (optionally edited)
                Service->>Backend: POST /take_action { id, message }
                Backend->>Contact: send message via MCP
            else user taps Skip
                Service->>Service: dismiss locally (no network call)
            end
        else no action ready
            Backend-->>Service: status: NO_ACTION
        end
    end
```

## Project structure

```
app/src/main/java/com/example/kingmaker/
├── MainActivity.kt              # Dashboard screen + permission rationale dialogs
├── service/
│   ├── MonitorService.kt        # Foreground service, background polling loop
│   ├── BackendClient.kt         # HTTP client for the 3 backend endpoints
│   ├── NextAction.kt            # /check_action response model
│   ├── DashboardData.kt         # /dashboard response model
│   └── OverlayLifecycleOwner.kt # Lets a Service host a ComposeView
└── ui/
    ├── popup/OverlayPopupContent.kt   # The floating action card
    └── dashboard/DashboardScreen.kt   # The home screen
```

## Running it

**Android app** — open the project root in Android Studio and run the `app`
configuration on a device. On first launch it'll ask (via an in-app dialog, not a
raw system prompt) for the "display over other apps" and notification permissions
it needs to show the popup.

**Backend** — point `BackendClient.BASE_URL` at wherever the server is running, e.g.:

```bash
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

The phone and the backend host need to be on the same network. Cleartext HTTP is
enabled for development (`usesCleartextTraffic="true"`) since this talks to a
local/LAN server — not something to ship as-is.

## API contract

| Endpoint | Method | Purpose |
|---|---|---|
| `/check_action` | GET | Returns the highest-priority undelivered action, or `{"status":"NO_ACTION"}` |
| `/take_action` | POST | `{"id": ..., "message": "..."}` — sends the (possibly edited) message via MCP |
| `/dashboard` | GET | Active goal, queued people, contact stats for the home screen |
