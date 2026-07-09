# BugTracker

A mobile Bug Tracker app for Android built with Kotlin and Jetpack Compose, developed as a university assignment (CS 4405 – Mobile Applications, University of the People).

## Overview

BugTracker allows users to create, read, update, and delete (CRUD) issue tickets, modeled after apps like GitHub Issues and Jira Mobile. The app supports offline-first operation with local persistence via Room, remote sync via Retrofit, and full lifecycle-safe state management.

## Features

- Create, update, and delete bug tickets with title, description, priority, and status
- Offline-first architecture — all data is saved locally first
- Remote sync with a REST backend (Retrofit)
- Per-card "Unsynced" badge indicating pending server sync
- Form state preserved across rotation and process death (SavedStateHandle)
- Retry mechanism for failed sync attempts

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose + Material 3 |
| Local storage | Room 2.6.1 |
| Remote API | Retrofit 2.11.0 + Gson |
| Architecture | MVVM + Repository pattern |
| Build | AGP 8.7.2, KSP, Gradle Kotlin DSL |

## Branch Structure

| Branch | Description |
|---|---|
| `main` | Stable release branch |
| `feature/room-database` | Local Room database layer |
| `feature/local-ui` | ViewModel and offline UI (v1.0) |
| `feature/retrofit-sync` | Retrofit sync layer and repository (v1.1) |
| `feature/error-handling` | Retry logic and lifecycle management (v1.2) |
| `hotfix/clear-draft-on-dismiss` | Fix draft state on dialog dismiss (v1.2.1) |
| `feature/polish` | Final code quality improvements (v2.0) |
| `feature/mockapi-integration` | Live backend integration via MockAPI.io (v3.0, in progress) |

## Version History

| Tag | Description |
|---|---|
| `v1.0` | Offline-only MVP with local Room CRUD |
| `v1.1` | Basic Retrofit sync layer with repository pattern |
| `v1.2` | Error handling and lifecycle management |
| `v1.2.1` | Patch: clear draft state on dialog dismiss |
| `v2.0` | Final polished release |
