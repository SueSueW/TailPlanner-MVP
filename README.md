# TailPlanner-MVP

> Early MVP & technical exploration for the TailPlanner project  
> Status: MVP validation completed, repository kept for reference

## Overview

TailPlanner-MVP contains early Android MVP experiments built to validate the core execution and timeline-recording workflow of the TailPlanner project.

This repository focuses on **feasibility verification**, not on delivering a production-ready application.  
The goal of this MVP was to confirm whether a timeline-centric execution model could effectively support personal productivity and focus tracking.

Specifically, this MVP was used to:
- Verify the feasibility of a **timeline-based execution model**
- Validate **local data persistence** for execution and focus records
- Explore the **correct architectural order** for long-term product development (data → interaction → UI)

This repository is not intended for long-term maintenance or feature expansion.

---

## Tech Stack (MVP Scope)

- **Language:** Kotlin  
- **Platform:** Android (Native)  
- **Persistence:** Room / SQLite (local storage)  
- **Architecture:** MVVM (basic structure)

The technology choices here were made purely to support MVP validation and architectural learning.

---

## Development Summary

- **2024 – 2025:** Concept exploration and platform evaluation  
  Explored web-based and mobile approaches for personal execution tracking and identified key limitations of web-based solutions in high-frequency, single-user scenarios.

- **2025 – 2026:** Android MVP implementation and validation  
  Built a minimal Android MVP to validate database-first architecture and end-to-end execution flow using timeline records as the single source of truth.

---

## Documentation

- **`RETROSPECTIVES.md`**  
  Records major decision points, invalidated assumptions, and iteration learnings during MVP exploration.

- **`CHECKLIST.md`**  
  Execution rules and development constraints derived from MVP experience, intended to guide future formal development.

---

## Relationship to the Main Project

This repository serves as the **experimental and validation foundation** for the formal TailPlanner project.

The production-grade application, long-term roadmap, and ongoing development are hosted in the main repository:

> **TailPlanner** (formal product repository)

---

## Related Documentation

For a detailed summary of the native MVP validation work that informed this repository, see:

- [`docs/FEIYUNATIVE_SUMMARY.md`](docs/FEIYUNATIVE_SUMMARY.md)

This document outlines the scope, technical constraints, and key learnings from the FeiyuNative MVP project, which serves as a feasibility and architectural reference for TailPlanner.

---

## Project Status

This repository represents a **frozen MVP validation stage**.

The core goals of this project have been fulfilled:
- Execution flow feasibility has been validated
- Data model and persistence strategy are proven
- Timeline-centric design assumptions are confirmed in real usage

No new features will be added to this repository.

Further development continues in the **TailPlanner** repository, which focuses on long-term architecture and iterative expansion.
https://github.com/SueSueW/TailPlanner