# Simple Flags

Simple Flags is a Paper plugin by camdaloon for creating, painting, copying, and displaying custom flags.

## Current Version

v1.0.0-2_beta

## Features

- Custom craftable flags
- Creator and player-assigned flag names
- Unique flag identities
- Flag copying with a blank flag
- High-resolution canvas editor
- Multiple pen sizes
- Custom color picker
- Image import from a direct URL
- Persistent flag artwork
- Paper API based architecture

## Crafting

```text
Stick  White Wool  White Wool
Stick  White Wool  White Wool
```

## Flag Copying

Place a designed flag and a blank flag together in the crafting grid to create a new copy with the same artwork and creator information.

## Image Import

The editor supports importing an image from a direct URL. The server must be able to reach the URL.

A normal server-only Paper plugin cannot open a player's computer file picker because the Minecraft client does not expose the player's local filesystem to the server.

## Building

Use the included GitHub Actions workflow to build the plugin.
