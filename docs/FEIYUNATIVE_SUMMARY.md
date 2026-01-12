# FeiyuNative (飞鱼计划 · 原生验证版)

FeiyuNative is an **Android-native MVP project** built to validate the feasibility of a long-term personal focus and planning system.

This project prioritizes **stability, data safety, and real-world usability** over visual polish or feature completeness.

---

## Project Positioning

**Role:** Native MVP / feasibility validation  
**Scope:** Single-user, offline-first, long-term usage  
**Status:** Feature-frozen MVP

FeiyuNative serves as a **technical and architectural validation project**, rather than a finalized product.

---

## What This MVP Validates

### 1. Data Safety as a First-Class Constraint

- Local-first storage using **Room + WAL**
- Manual database export / import
- Forced WAL checkpoint before export
- Verified data integrity across:
  - App updates
  - Uninstall / reinstall cycles

> This MVP confirms that long-term personal data can remain fully user-controlled and recoverable.

---

### 2. Reliable Focus Tracking on Android

- Foreground Service–based focus timer
- Android 8–14 compatibility (targetSdk 36)
- Single active focus session enforced
- UI state synchronized with timer lifecycle

> This validates that precise, interruption-resistant focus tracking is achievable without cloud dependency.

---

### 3. Timeline as a Usable Execution History

- Date-based timeline view
- Chronological order: oldest → newest
- Manual edit / append / delete supported

> Timeline design confirmed as a viable foundation for reconstructing daily execution history.

---

### 4. Aggregation Without Premature Complexity

- Daily total focus time
- Item-level aggregation
- Pie chart + legend
- Statistics logic extracted into reusable components

> This MVP intentionally limits analytics to validate correctness before expansion.

---

## Implemented Functional Scope

### Plan / Section / Item
- Create Plans
- Group Items via Sections
- Timer-enabled Items

### Focus Timer
- Start / stop lifecycle
- Real-time UI updates

### Timeline
- View / edit execution records

### Settings
- Data export (SAF)
- Data import (SAF)
- Post-import restart prompt

---

## Technical Stack

- **UI:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Persistence:** Room
- **Async:** Kotlin Coroutines + Flow
- **Navigation:** Navigation Compose
- **Timing:** Foreground Service

---

## Project Structure (Simplified)

app/
├─ core/ # database, backup
├─ data/ # entity / dao / repository
├─ ui/ # screens, components, navigation
└─ service/ # focus timer service


---

## Current State

- Feature-complete MVP
- Stability verified through real usage
- UI intentionally minimal
- **Feature development frozen**

---

## Relation to TailPlanner

FeiyuNative functions as a **native feasibility and validation project**.

Key learnings from this MVP directly inform the design and architecture of the next-stage project: **TailPlanner (灵尾记)**.

FeiyuNative may serve as:
- A reference implementation
- A data migration source
- A behavioral validation baseline

---

## Design Principles

- Data integrity over appearance
- Offline-first by default
- No premature cloud sync
- No over-abstraction
- **Data > UI > New features**

*********Add FeiyuNative MVP summary documenting validation scope and learnings*******

For ongoing development, see the TailPlanner repository:
- https://github.com/SueSueW/TailPlanner
