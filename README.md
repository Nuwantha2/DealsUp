project:
  title: "💳 DealsUp - Smart Credit Card Deal Finder (Android App)"
  description: >
    DealsUp is an Android app that helps users discover personalized credit card offers
    and store deals based on their current location, motion, and selected bank cards.

features:
  - 🔐 Firebase-based Login & Registration
  - 🗺️ Deal discovery based on location (GPS + Accelerometer)
  - 📍 View deals on a map or in a card list
  - 💳 Choose your owned credit cards
  - 🔔 Push notifications for nearby deals (even in background)
  - ⚙️ Manage notification preferences
  - 🧾 View all deal notifications history
  - 🧑‍💼 Contact admin or update profile
  - ❌ Delete account securely

tech_stack:
  frontend: Android Studio (Java)
  backend:
    - Firebase Authentication
    - Firebase Firestore
    - Firebase Cloud Messaging (FCM)
  apis:
    - Google Maps API
    - Sensors API (Accelerometer)

workflow:
  - User registers and selects their owned credit cards.
  - Deals are filtered and displayed based on location, motion, card type, and category.
  - Push notifications are triggered when near relevant stores.
  - Users can view, filter, and manage preferences, profiles, and receive alerts.

screenshots:
  welcome_auth:
    - title: Welcome
      path: screenshots/welcome.png
    - title: Sign In
      path: screenshots/signin.png
    - title: Sign Up
      path: screenshots/signup.png
  choose_cards:
    - title: Choose Cards
      path: screenshots/choose_cards.png
  dashboard_filters:
    - title: Dashboard
      path: screenshots/dashboard.png
    - title: Filter by Bank
      path: screenshots/bank_filter.png
    - title: Filter by Category
      path: screenshots/category_filter.png
    - title: Filter by Location
      path: screenshots/location_filter.png
  deals_map:
    - title: Deal Card
      path: screenshots/deal_card.png
    - title: Deal Map View
      path: screenshots/map_view.png
    - title: Navigation
      path: screenshots/navigate.png
  notifications_profile:
    - title: Notification Popup
      path: screenshots/notification_alert.png
    - title: Notifications Page
      path: screenshots/notifications_list.png
    - title: Profile
      path: screenshots/profile.png

project_structure: |
  DealsUp/
  ├── app/
  │   ├── java/com/s23010901/dealsup/
  │   │   ├── MainActivity.java
  │   │   ├── Dashboard.java
  │   │   ├── DealSensorService.java
  │   │   ├── NotificationActivity.java
  │   │   └── ...
  │   ├── res/
  │   │   ├── layout/
  │   │   └── drawable/
  │   └── AndroidManifest.xml
  └── README.md

setup_instructions:
  - Clone repo:
    - command: git clone https://github.com/Nuwantha2/DealsUp.git
  - Open in Android Studio
  - Add your `google-services.json` in `app/`
  - Enable the following in Firebase Console:
    - Firebase Authentication
    - Firebase Firestore
    - Firebase Cloud Messaging (FCM)
  - Build and run on a device/emulator

permissions_required:
  - ACCESS_FINE_LOCATION
  - ACTIVITY_RECOGNITION
  - POST_NOTIFICATIONS (Android 13+)

smart_deal_detection: >
  The app uses the accelerometer to detect motion and triggers nearby deal scans via location.
  Notifications are sent even when the app is closed.

contact:
  email: nuwanthadanajayabandara@gmail.com
