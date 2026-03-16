# Work History: Navigation Sync & Recursion Fixes
**Date**: 2026-03-16
**Status**: Resolved

## Problem Summary
The Moqui AI Vue application suffered from several navigation and component loading issues:
- **Recursion Loops**: In certain nested screen structures (like "Meetings"), components would recursively load themselves at Index 0, creating a "hall of mirrors" effect.
- **URL Truncation**: Navigating to deep paths (e.g., `/Meetings/ManageAgendaContainers`) would often revert back to the parent URL (`/Meetings`) because parents asserted themselves before children finished loading.
- **Double-Click Requirement**: Navigation links often required two clicks to update the screen because stale component references in the root registry were causing aborted or ignored loads.
- **Blank Screens**: Race conditions between menu data arrival and active path list state caused deep paths to be truncated, resulting in blank sub-screen areas.

## Changes Implemented

### 1. MoquiAiVue.qvt.js (Core Navigation Logic)
- **Sequential Recursion Guard**: Implemented a check in `loadActive` that walks up the parent hierarchy. It blocks any component from loading a path name already active in a parent, physically preventing the "hall of mirrors" loop.
- **Robust Depth-Based Indexing**: Redesigned how `m-subscreens-active` determines its `activePathIndex`. It now reliably walks up parents to find the nearest active index and increments it, ensuring correct nesting depth (0 -> 1 -> 2) even through intermediate Blueprint/Quasar containers.
- **Stale Component Replacement**: Updated `addSubscreen` in the root component to replace stale component references at the same index. This ensures the current active component always has registration priority, resolving the "double-click" bug.
- **Prefix-Aware Menu Sync**: Updated the `navMenuList` watcher to avoid truncating a deep `currentPathList` if the incoming menu data is just a prefix of the current state. This preserves deep navigation (e.g., staying at `/Meetings/ManageContainers` when the `/Meetings` menu data arrives).
- **Guarded Route Assertion**: Modified the child components to only assert their URL via `$router.replace` if they are the **terminal segment** of the path, preventing parent components from truncating the URL.
- **Prefix Route Filter**: Optimized the `$route` watcher to ignore router updates that are just a prefix of the currently loading URL, preventing "abort loops" during multi-level screen loads.

### 2. BlueprintClient.js (Schema to Component Mapping)
- **Path Index Propagation**: Fixed a bug where the `pathIndex`/`path-index` attribute from the server blueprint was not being mapped to the component props, which had previously caused nested screens to default to index 0.

## Verification Results
- **Initial Load**: Landing on `/aitree` correctly redirects to `/aitree/Home` and displays content immediately.
- **Single-Click Navigation**: All sidebar and toolbar links update the screen on the first click.
- **Recursive Screens**: The "Meetings" screen and its nested tabs ("Manage Containers", "Active Meetings") now load correctly at Index 1 without duplication or shell recursion.
- **URL Stability**: The address bar correctly maintains deep URLs through the entire loading sequence of nested blueprints.
