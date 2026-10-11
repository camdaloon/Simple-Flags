# Simple Flags

**Version:** 1.0.0-8_beta  
**Author:** camdaloon

Adds in simple flags to Minecraft.

## Requirements

- Paper 1.21.11 or newer with the native Java Edition dialog API
- Java 21

## Features

- Craft a blank flag with two sticks in the first column and four white wool in the other two columns (`SWW` / `SWW`).
- Right-click a Simple Flag to open Minecraft's native custom dialog screen, not an inventory.
- View a colored 9 × 5 preview of the saved flag design.
- Paint a selected canvas pixel by X/Y coordinates and a hexadecimal color.
- Fill the canvas with a chosen color.
- Rename and save the design to the flag.
- Cancel to discard edits.

## Important editor limitations

Minecraft's native dialog screens do not support freehand mouse drawing or arbitrary custom PNG backgrounds/buttons. This version uses the native dialog widgets and colored text to show the canvas; it does not reproduce the supplied mockup pixel-for-pixel. A resource pack is not included because this implementation does not require one. Freehand drawing and a fully texture-based canvas would require a different client-side UI approach.

## Build

The GitHub Actions workflow builds the plugin JAR using Java 21 and Gradle. Download the `SimpleFlags` JAR from the workflow artifact and place it in the server's `plugins` folder.
