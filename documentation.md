# Smart Reminder App Documentation

## Table of Contents
1. [Overview](#overview)
2. [App Architecture](#app-architecture)
3. [Screens and Pages](#screens-and-pages)
4. [UI Components and Design](#ui-components-and-design)
5. [Color Scheme](#color-scheme)
6. [Guest Mode](#guest-mode)
7. [Add Reminder Form](#add-reminder-form)
8. [Time Picker](#time-picker)
9. [Notification System](#notification-system)
10. [Progress Tracking](#progress-tracking)
11. [User Settings](#user-settings)
12. [Data Management](#data-management)

---

## Overview

Smart Reminder is an Android application built with Kotlin and Jetpack Compose that allows users to create, manage, and track reminders. The app supports both guest mode (local-only features) and logged-in mode (with Firebase integration for cloud sync and AI features).

### Key Features
- Create reminders with title, description, time, and priority
- Priority levels: URGENT, HIGH, MEDIUM, LOW
- Notification system with alarm sound control
- Progress tracking for daily tasks
- Pre-alert notifications for urgent/high priority reminders
- Guest mode for local-only usage
- Google Sign-In for cloud sync
- Responsive mobile UI with Material Design 3

---

## App Architecture

### Technology Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material Design 3
- **Database:** Room (local SQLite)
- **Backend:** Firebase (Authentication, Realtime Database, Storage)
- **AI:** Google Gemini AI
- **Build System:** Gradle with Kotlin DSL

### Project Structure
```
app/
├── src/main/java/com/groupflow/app/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── local/
│   │   │   ├── AppDatabase.kt
│   │   │   ├── entity/
│   │   │   │   ├── Models.kt (User, Reminder)
│   │   │   │   └── ReminderPriority.kt
│   │   │   └── dao/
│   │   │       └── Daos.kt
│   ├── notification/
│   │   └── NotificationHelper.kt
│   ├── receiver/
│   │   └── ReminderReceiver.kt
│   ├── service/
│   │   ├── FirebaseAuthService.kt
│   │   └── GeminiAIService.kt
│   ├── ui/
│   │   ├── navigation/
│   │   │   └── GroupFlowNavGraph.kt
│   │   ├── screens/
│   │   │   └── Screens.kt (all screens)
│   │   └── theme/
│   │       ├── Color.kt
│   │       └── Theme.kt
│   └── viewmodel/
│       └── ReminderViewModel.kt
└── build.gradle.kts
```

---

## Screens and Pages

### 1. Sign In Screen (`SignInScreen`)

**Purpose:** Entry point for the app, allows users to sign in with Google or continue as guest.

**UI Components:**
- App logo/icon
- "Sign In" button (Google Sign-In)
- "Continue as Guest" button
- Error message display (if sign-in fails)

**Functionality:**
- Google Sign-In integration with Firebase Authentication
- Guest mode activation (local-only features)
- Automatic navigation to appropriate screen after authentication
- Error handling for sign-in failures

**Design:**
- Centered layout with logo at top
- Primary action buttons with Material Design 3 styling
- Clean, minimal interface

---

### 2. Tasks Screen (`TasksScreen`)

**Purpose:** Main screen for viewing and managing reminders.

**UI Components:**
- **Progress Card:** Shows daily progress with completion count and progress bar
- **Reminder List:** LazyColumn displaying all reminders grouped by date
- **Floating Action Button (FAB):** Opens Add Reminder dialog
- **Empty State:** Icon and message when no reminders exist

**Reminder Card Design:**
- Left: Priority indicator (colored circle)
- Center: Title, description, and time
- Right: More options menu (Snooze, Delete)
- Strikethrough decoration for completed reminders

**Functionality:**
- View all reminders for current user
- Group reminders by date (Today, Tomorrow, This Week, Later)
- Snooze reminders
- Delete reminders
- Mark reminders as completed (by clicking card)
- Track daily progress

**Date Grouping Logic:**
- **Today:** Reminders scheduled for today
- **Tomorrow:** Reminders scheduled for tomorrow
- **This Week:** Reminders scheduled for the current week
- **Later:** All future reminders

---

### 3. Profile Screen (`ProfileScreen`)

**Purpose:** User settings and account management.

**UI Components:**
- **User Info Card:** Display user name and email
- **Notifications Section:** Alarm sound toggle switch
- **Storage Section:** Storage usage display
- **Account Section:** Sign In/Sign Out buttons

**Functionality:**
- View user profile information
- Toggle alarm sound on/off for notifications
- Sign out from account
- Sign in (for guest users)

**Design:**
- Card-based layout with clear sections
- Toggle switch for alarm sound preference
- Persistent settings using SharedPreferences

---

### 4. Add Reminder Dialog (`AddReminderDialog`)

**Purpose:** Create new reminders with title, description, time, and priority.

**UI Components:**
- **Title Field:** OutlinedTextField for reminder title (required)
- **Description Field:** OutlinedTextField for description (optional)
- **Time Selection Card:** Clickable card displaying selected time
- **Priority Selection:** 2x2 radio button grid for priority levels
- **Confirm/Cancel Buttons:** Add and Cancel buttons

**Time Picker Dialog:**
- **Hour Slider:** Slider for selecting hour (1-11 AM, 1-12 PM)
- **Minute Slider:** Slider for selecting minute (0-59)
- **AM/PM Toggle:** Two buttons to toggle between AM and PM
- **Done Button:** Closes time picker and saves selection

**Priority Grid:**
- 2x2 layout with radio buttons
- URGENT (top-left)
- HIGH (top-right)
- MEDIUM (bottom-left)
- LOW (bottom-right)

**Functionality:**
- Initialize with current time
- Set reminder title and description
- Select time using slider-based time picker
- Choose priority level
- Automatic date adjustment if time is in the past
- Validation: Title is required

---

## UI Components and Design

### Material Design 3
The app uses Material Design 3 components for a modern, consistent look.

### Common Components

#### Buttons
- **Filled Button:** Primary actions (Add, Sign In, etc.)
- **Text Button:** Secondary actions (Cancel, etc.)
- **Outlined Button:** Tertiary actions
- **Icon Button:** Actions with icons (menu, delete, etc.)

#### Cards
- **Elevated Card:** For content grouping
- **Outlined Card:** For time selection and settings
- **Filled Card:** For progress tracking

#### Text Fields
- **OutlinedTextField:** For user input (title, description)
- Labels and helper text for clarity

#### Dialogs
- **AlertDialog:** For add reminder dialog and time picker
- **DropdownMenu:** For reminder actions

#### Progress Indicators
- **LinearProgressIndicator:** For progress tracking
- Displays completion percentage

---

## Color Scheme

### Material Design 3 Theme Colors

The app uses dynamic colors based on the device's theme (light/dark mode).

#### Primary Colors
- **Primary:** Main brand color (used for buttons, active elements)
- **On Primary:** Text/icon color on primary backgrounds
- **Primary Container:** Lighter variant for containers
- **On Primary Container:** Text/icon color on primary containers

#### Secondary Colors
- **Secondary:** Accent color for secondary elements
- **On Secondary:** Text/icon color on secondary backgrounds
- **Secondary Container:** Lighter variant
- **On Secondary Container:** Text/icon color on secondary containers

#### Surface Colors
- **Surface:** Background for cards and surfaces
- **On Surface:** Text/icon color on surface backgrounds
- **Surface Variant:** Alternative surface color
- **On Surface Variant:** Text/icon color on surface variant

#### Priority Colors
- **URGENT:** Red/Magenta indicator
- **HIGH:** Orange/Amber indicator
- **MEDIUM:** Blue/Cyan indicator
- **LOW:** Green/Teal indicator

---

## Guest Mode

### Overview
Guest mode allows users to use the app without signing in. All data is stored locally on the device.

### Features Available in Guest Mode
- Create and manage reminders locally
- View progress tracking
- Receive notifications
- Access Profile settings (alarm sound toggle)
- Limited to Reminder tab only

### Features Restricted in Guest Mode
- No cloud sync
- No AI features (voice input, smart reminders)
- No multi-device access
- No backup/restore
- No real-time collaboration

### Navigation in Guest Mode
- **Reminder Tab:** Accessible (main functionality)
- **Profile Tab:** Accessible (for settings and sign-in)
- **Other Tabs:** Hidden (AI, Tasks, Storage)

### Data Storage
- All reminders stored in Room database
- User ID: "guest_user" (fixed)
- No cloud synchronization
- Data persists locally only

### Sign In from Guest Mode
- Profile screen displays "Sign In" button
- Clicking "Sign In" triggers Google Sign-In flow
- After successful sign-in, data can be synced (future feature)

---

## Add Reminder Form

### Form Fields

#### Title (Required)
- **Type:** OutlinedTextField
- **Validation:** Cannot be blank
- **Placeholder:** "Title"
- **Single line:** Yes

#### Description (Optional)
- **Type:** OutlinedTextField
- **Validation:** None (can be empty)
- **Placeholder:** "Description (optional)"
- **Lines:** 2 (min and max)

#### Time (Required)
- **Type:** Clickable Card
- **Display:** Shows selected time in "HH:MM AM/PM" format
- **Initial Value:** Current time
- **Interaction:** Opens time picker dialog

#### Priority (Required)
- **Type:** 2x2 Radio Button Grid
- **Options:** URGENT, HIGH, MEDIUM, LOW
- **Default:** MEDIUM
- **Selection:** Single selection only

### Time Picker Dialog

#### Hour Selection
- **Component:** Slider
- **Range:** 1-11 (AM) or 1-12 (PM)
- **Steps:** 10 (AM) or 11 (PM)
- **Display:** "Hour: X"

#### Minute Selection
- **Component:** Slider
- **Range:** 0-59
- **Steps:** 59
- **Display:** "Minute: XX" (padded with leading zero)

#### AM/PM Toggle
- **Component:** Two buttons
- **Labels:** "AM" and "PM"
- **Visual Feedback:** Selected button uses primary color
- **Interaction:** Click to toggle

#### Done Button
- **Action:** Closes dialog and saves time selection
- **Type:** Filled Button

### Form Validation
- **Title:** Required, must not be blank
- **Description:** Optional
- **Time:** Required, defaults to current time
- **Priority:** Required, defaults to MEDIUM

### Submission
- **Add Button:** Enabled only when title is not blank
- **Action:** Creates reminder and schedules notification
- **Navigation:** Closes dialog and returns to TasksScreen

---

## Time Picker

### Design Philosophy
The time picker uses a slider-based design for faster, more intuitive time selection compared to traditional +/- buttons.

### Components

#### Hour Slider
- **Type:** Material Slider
- **Range:** 1-11 (AM) or 1-12 (PM)
- **Steps:** Discrete steps for each hour
- **Visual:** Shows current hour value above slider
- **Interaction:** Drag slider to change hour

#### Minute Slider
- **Type:** Material Slider
- **Range:** 0-59
- **Steps:** 59 discrete steps
- **Visual:** Shows current minute value (padded) above slider
- **Interaction:** Drag slider to change minute

#### AM/PM Toggle
- **Type:** Two toggle buttons
- **Layout:** Horizontal row with spacing
- **Visual Feedback:** Selected button uses primary color, unselected uses surface color
- **Interaction:** Click to switch between AM and PM

### Time Calculation
When the user confirms the time selection:
1. Convert 12-hour format to 24-hour format
2. Handle special cases:
   - 12 AM = 0 hours (midnight)
   - 12 PM = 12 hours (noon)
   - AM hours stay as-is (1-11)
   - PM hours add 12 (1-11 PM = 13-23)
3. Set Calendar object with hour, minute, second = 0
4. If time is in the past, add 1 day
5. Return timestamp in milliseconds

### Example Calculations
- **1:30 AM** → Hour: 1, Minute: 30 → 01:30
- **12:00 AM** → Hour: 0, Minute: 0 → 00:00
- **3:45 PM** → Hour: 15, Minute: 45 → 15:45
- **12:00 PM** → Hour: 12, Minute: 0 → 12:00

---

## Notification System

### Overview
The app uses Android's AlarmManager and NotificationManager to schedule and display reminder notifications.

### Components

#### NotificationHelper
Singleton object that manages notification scheduling and cancellation.

**Functions:**
- `createNotificationChannel(context)`: Creates notification channel (Android 8.0+)
- `scheduleReminder(...)`: Schedules alarm for reminder
- `cancelReminder(...)`: Cancels scheduled alarm

#### ReminderReceiver
BroadcastReceiver that handles alarm broadcasts and shows notifications.

**Functionality:**
- Receives alarm broadcast
- Extracts reminder details from intent
- Checks alarm sound preference
- Builds and displays notification
- Sets notification priority based on reminder priority
- Creates pending intent to open app on notification tap

### Notification Channel
- **ID:** "reminder_channel"
- **Name:** "Reminders"
- **Importance:** HIGH
- **Features:** Vibration enabled, Lights enabled

### Notification Permissions
Required permissions in AndroidManifest.xml:
- `POST_NOTIFICATIONS`: Required for Android 13+
- `SCHEDULE_EXACT_ALARM`: Required for exact alarm scheduling
- `USE_EXACT_ALARM`: Required for exact alarm scheduling
- `VIBRATE`: Required for vibration
- `RECEIVE_BOOT_COMPLETED`: Required for boot receiver (future)

### Runtime Permission Request
For Android 13+ (API 33+), the app requests POST_NOTIFICATIONS permission at runtime in MainActivity.

### Notification Priority
Based on reminder priority:
- **URGENT:** PRIORITY_MAX
- **HIGH:** PRIORITY_HIGH
- **MEDIUM:** PRIORITY_DEFAULT
- **LOW:** PRIORITY_LOW

### Pre-Alert Notifications
For URGENT and HIGH priority reminders:
- If reminder is more than 6 hours away
- Schedule additional notification 5 minutes before
- Shows "Upcoming: [title]" with "Reminder in 5 minutes"
- Helps users prepare for important reminders

### Alarm Sound Control
- Toggle switch in Profile screen
- Stored in SharedPreferences
- Key: "alarm_sound_enabled"
- Default: true
- Checked before playing notification sound

### Notification Icon
Uses system icon: `android.R.drawable.ic_dialog_info` (white with transparency, suitable for notifications)

### Scheduling Logic
1. Create intent with reminder details
2. Create PendingIntent with reminder ID as request code
3. Use `setExactAndAllowWhileIdle()` for exact alarms
4. Fallback to `setAndAllowWhileIdle()` if exact alarms not allowed
5. Cancel both main and pre-alert alarms when reminder is deleted

---

## Progress Tracking

### Overview
Progress tracking displays the completion rate of daily reminders, helping users track their productivity.

### UI Components

#### Progress Card
- **Location:** Top of TasksScreen
- **Components:**
  - Title: "Daily Progress"
  - Counter: "X/Y completed"
  - Progress Bar: LinearProgressIndicator

#### Progress Calculation
- **Completed Count:** Reminders with status "COMPLETED"
- **Total Count:** All reminders for current user
- **Progress:** Completed / Total (float, 0.0 to 1.0)
- **Display:** Progress bar fills based on percentage

### Visual Design
- Card with elevation
- Title and counter in row
- Progress bar below
- Counter uses primary color for emphasis
- Progress bar uses primary color

### Empty State
- If no reminders exist, progress shows "0/0 completed"
- Progress bar is empty
- Card still displays for consistency

---

## User Settings

### Profile Screen Settings

#### Alarm Sound Toggle
- **Type:** Switch
- **Label:** "Alarm Sound"
- **Description:** "Play sound for reminder notifications"
- **Storage:** SharedPreferences
- **Key:** "alarm_sound_enabled"
- **Default:** true
- **Persistence:** Saved immediately on toggle

#### Storage Usage (Future)
- **Component:** Card showing storage usage
- **Display:** Used/Total storage
- **Functionality:** Not yet implemented

#### Account Actions
- **Sign Out:** Signs out current user, navigates to sign-in screen
- **Sign In:** For guest users, triggers Google Sign-In flow

---

## Data Management

### Local Database (Room)
- **Database:** AppDatabase
- **Entities:**
  - User
  - Reminder
- **DAOs:**
  - UserDao
  - ReminderDao

### Reminder Entity
- **reminderId:** String (UUID)
- **userId:** String (user identifier)
- **title:** String
- **description:** String
- **triggerTime:** Long (timestamp)
- **priority:** ReminderPriority enum
- **status:** ReminderStatus enum (PENDING, COMPLETED)
- **reminderType:** ReminderType enum (TIME_BASED, LOCATION_BASED)
- **isRecurring:** Boolean
- **createdAt:** Long (timestamp)
- **lastModified:** Long (timestamp)

### Reminder Priority Levels
- **URGENT:** Highest priority, red indicator, pre-alert notification
- **HIGH:** High priority, orange indicator, pre-alert notification
- **MEDIUM:** Medium priority, blue indicator, no pre-alert
- **LOW:** Low priority, green indicator, no pre-alert

### Reminder Status
- **PENDING:** Reminder not yet completed
- **COMPLETED:** Reminder marked as done
- **SNOOZED:** Reminder postponed (future)

### CRUD Operations
- **Create:** Insert new reminder via ReminderViewModel.createReminder()
- **Read:** Query reminders via ReminderViewModel.getUserReminders()
- **Update:** Update reminder via ReminderViewModel.updateReminder()
- **Delete:** Delete reminder via ReminderViewModel.deleteReminder()
- **Mark Complete:** Update status to COMPLETED via ReminderViewModel.markAsCompleted()
- **Snooze:** Update trigger time via ReminderViewModel.snoozeReminder()

---

## Future Features

### Planned Enhancements
1. **Firebase Storage Integration:** Enable cloud storage for attachments
2. **AI Voice Input:** Use Gemini AI for voice-to-text reminder creation
3. **Multi-language Support:** English, Hindi, Spanish
4. **Smart Reminders:** Automatic reminder creation from natural language
5. **Logged-in Mode:** Full cloud sync and AI features
6. **Real-time Messaging:** Firebase Cloud Messaging for collaborative reminders
7. **Google Drive Backup:** Backup and restore reminders to Google Drive

---

## Design Guidelines

### Typography
- **Title Large:** For dialog titles
- **Title Medium:** For section headers
- **Body Large:** For primary text
- **Body Medium:** For secondary text
- **Body Small:** For metadata and descriptions

### Spacing
- **Card Padding:** 16.dp
- **Screen Padding:** 16.dp
- **Item Spacing:** 12.dp
- **Section Spacing:** 8.dp

### Elevation
- **Cards:** 2.dp
- **Dialogs:** 6.dp
- **FAB:** 6.dp

### Border Radius
- **Cards:** 12.dp
- **Buttons:** 12.dp
- **Text Fields:** 12.dp

### Iconography
- **Material Icons:** Using Icons.Default from Material Design
- **Size:** 24.dp (standard), 64.dp (large icons)
- **Tint:** Uses onSurfaceVariant for secondary icons

---

## Accessibility

### Color Contrast
- All text meets WCAG AA standards
- Primary colors have sufficient contrast with white text
- Surface colors provide good contrast for text

### Touch Targets
- Minimum 48.dp x 48.dp for interactive elements
- FAB: 56.dp
- Buttons: Minimum 36.dp height

### Screen Reader Support
- All interactive elements have content descriptions
- Text fields have labels
- Buttons have descriptive text

---

## Performance Considerations

### LazyColumn
- Used for reminder lists to enable efficient scrolling
- Only renders visible items
- Reduces memory usage

### State Management
- Using Compose state (remember, mutableStateOf)
- ViewModel for screen-specific state
- Flow for reactive data updates

### Database Operations
- All database operations run in coroutines
- viewModelScope used for coroutine lifecycle management
- Room handles database operations on background threads

---

## Testing Recommendations

### Manual Testing Checklist
- [ ] Sign in with Google account
- [ ] Continue as guest
- [ ] Create reminder with all priority levels
- [ ] Test time picker slider interaction
- [ ] Verify notification appears at scheduled time
- [ ] Test pre-alert notification for urgent/high priority
- [ ] Toggle alarm sound on/off
- [ ] Mark reminder as completed
- [ ] Verify progress tracking updates
- [ ] Snooze reminder
- [ ] Delete reminder
- [ ] Sign out
- [ ] Test empty state

### Logcat Debugging
Key log tags:
- `ReminderReceiver`: "Reminder received!", "Reminder: [title]", "Notification shown"
- `NotificationHelper`: "Notification channel created", "Scheduling reminder", "Reminder scheduled successfully", "Pre-alert scheduled"

---

## Conclusion

This documentation provides a comprehensive overview of the Smart Reminder app's UI, functionality, and design. The app is built with modern Android development practices, using Jetpack Compose for UI, Room for local data storage, and Firebase for cloud services. The notification system ensures users never miss important reminders, while the progress tracking helps users stay productive.
