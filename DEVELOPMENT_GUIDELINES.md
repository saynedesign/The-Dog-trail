# Development Guidelines

## 1. Architecture: MVI (Model-View-Intent)
We follow the MVI architecture pattern for all screens.

### Structure
Each feature/screen should have a corresponding package containing:
- **`Contract.kt`**: Defines the contract between UI and ViewModel.
  - `State`: Data class representing the UI state.
  - `Event`: Sealed class representing user actions/intents.
  - `Effect`: Sealed class for one-time side effects (navigation, snackbars).
- **`ViewModel.kt`**: Manages state and handles events.
  - Must extend `ViewModel`.
  - Exposes `StateFlow<State>` and `SharedFlow<Effect>`.
- **`Screen.kt`**: The Composable UI.
  - Should be stateless where possible, receiving state and event callbacks.

## 2. UI & Components
- **Components**: Use standard components from `com.codesmithslabs.thedogtail.components` package.
  - Do not create custom buttons or inputs unless absolutely necessary.
  - Extend existing components if a new variant is needed.
- **Theme**: Use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- **Resources**:
  - **Strings**: NEVER hardcode strings. All UI text must be in `res/values/strings.xml`.
  - **Colors**: Define colors in `ui/theme/Color.kt` and map them in `Theme.kt`.

## 3. Best Practices
- **Compose**:
  - Use `Scaffold` for screen structure.
  - Use `Preview` annotations for UI development.
- **State Management**:
  - State should be immutable.
  - Use `copy()` to update state in ViewModel.
