# ChommieHub

> Your home away from home.

## 1. Application Overview

ChommieHub is a native Android student accommodation platform designed
to support students throughout the accommodation lifecycle, from
discovery and application through to residence operations.

## 2. Design Considerations

### 2.1 Architecture
### 2.2 User Experience
### 2.3 Validation and Error Handling
### 2.4 Security and Configuration

## 3. Implemented Features

### 3.1 Student Features
> To be completed during student-feature integration.

### 3.2 Residence Manager Features

- Live residence dashboard
- Application review
- Application approval and decline
- Room assignment
- Resident management
- Accommodation management
- Room inventory management

### 3.3 Administrator Features
> To be completed during administrator-feature integration.

## 4. System Architecture

### Backend

Controller → Service → Repository → Google Cloud Firestore

### Android

Fragment → ViewModel → Repository → Retrofit → ASP.NET Core REST API

## 5. Technology Stack

- Kotlin
- Android Views/XML
- ASP.NET Core Web API (.NET 10)
- Google Cloud Firestore
- Retrofit
- Gson
- Android Lifecycle/ViewModel
- xUnit
- Moq
- JUnit
- Mockito-Kotlin
- GitHub Actions

## 6. REST API Integration

The native Android application communicates with the ChommieHub
ASP.NET Core REST API using Retrofit.

The manager implementation currently supports:

- dashboard statistics
- application retrieval
- application review
- room assignment
- resident retrieval
- accommodation management
- room management

## 7. Setup and Configuration

### Backend

The API requires .NET 10.

Firebase service-account credentials are not stored in source control.
Local development uses Google Application Default Credentials through
the `GOOGLE_APPLICATION_CREDENTIALS` environment variable.

### Android

Local Android configuration is stored in:

`android/ChommieHub/local.properties`

For manager development:

`manager.accommodation.id=<Firestore accommodation document ID>`

The Android emulator accesses the local API through:

`http://10.0.2.2:5154/`

## 8. Automated Testing

### Backend

The backend includes xUnit tests using Moq to test manager business
logic independently of Firestore.

### Android

The Android application includes JUnit repository tests using
Mockito-Kotlin to test API data behaviour without requiring a live
backend.

## 9. DevOps and GitHub Actions

GitHub Actions automatically validates the project on remote pushes.

The workflow:

1. Restores the .NET backend.
2. Builds the backend.
3. Runs backend unit tests.
4. Runs Android JVM unit tests.
5. Builds the Android debug APK.

## 10. Screenshots

### Residence Manager

> Manager screenshots to be added here.

### Student

> To be added during integration.

### Administrator

> To be added during integration.

## 11. Demonstration Video

> Final Part 2 demonstration video link will be added before submission.

## 12. AI Usage Declaration

> Final group AI declaration will be added before submission.

## 13. Team Contributions

> Final contribution table will be completed after integration.