# RIVAN CSC Manager — Android Phase 1–3

Native Android application in **Kotlin + Jetpack Compose** based on the supplied RIVAN CSC Manager HTML prototype.

## Scope

Phase 4 is intentionally excluded. This build covers the first three phases:

### Phase 1 — Professional UI foundation
- Kotlin + Jetpack Compose + Material 3
- Premium light/dark theme
- Animated login and screen transitions
- Bottom navigation
- Admin + Operator role experiences
- Dashboard cards, quick actions and responsive layouts

### Phase 2 — Working local business app
- Local persistent device storage
- Admin/operator login
- CSC center management
- Operator management + approval/status
- Customer management
- Service management + central customer price
- Application creation/status workflow
- Payments/receipts listing
- Income/expense/profit
- Refund/cancel request and admin approval
- Attendance check-in/check-out
- Camera selfie capture for check-in
- Leave management
- Payroll calculation and paid status
- Document vault records
- Notifications
- Audit log
- Local backup/export/import

### Phase 3 — Advanced local/offline operations
- CSV reports/export
- PDF/print-ready architecture can be extended from the receipt/report screens
- Local snapshot backups
- Backup restore
- Audit history
- Branding controls
- System settings
- Low-end friendly Compose UI and reduced dependency footprint

## Demo credentials

Admin:
- ID: `ADMIN-01`
- Password: `1234`

Operator:
- ID: `OP-001`
- Password: `1234`

Other seeded operators:
- `OP-002 / 1234`
- `OP-003 / 1234` is Pending until approved by Admin.

## Build with GitHub

1. Create a new GitHub repository.
2. Upload this complete project folder.
3. Open **Actions**.
4. Run **RIVAN CSC Manager Android Build** using **Run workflow**.
5. Wait for the build to finish.
6. Open the successful workflow run.
7. Under **Artifacts**, download `RIVAN-CSC-Manager-debug-apk`.

The workflow uses JDK 17, Android SDK 37 and Gradle 9.6.0.

## Open in Android Studio

Open the project folder:

`RIVAN_CSC_Manager_Android`

Use a current Android Studio release compatible with AGP 9.x. The project is configured for:

- AGP 9.5.1
- Kotlin 2.4.21
- Compose BOM 2026.09.00
- compileSdk 37
- minSdk 23
- targetSdk 37

## Important architecture note

The current Phase 1–3 app is intentionally **offline/local-first**. Data is stored on the Android device. It is not yet a 100-CSC cloud synchronized system.

Phase 4 will later add the online backend, PostgreSQL, API, OTP, multi-device synchronization, cloud backups, WhatsApp/SMS provider integration and server-side RBAC/audit enforcement.

## Reference

`reference/RIVAN_CSC_Manager_HTML_Blueprint.html` is the supplied HTML prototype used as the UI/feature blueprint.
