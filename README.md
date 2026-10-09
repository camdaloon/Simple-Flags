# Simple Flags

Simple Flags is a Paper plugin by camdaloon for crafting, painting, naming, copying, and importing custom flags.

## Version

v1.0.0-6_beta

## Features

- Custom flag crafting recipe
- Flag creator, name, and unique item identity
- Designed flag copying with a blank flag
- Inventory canvas with pen-size, shape, and fill tool selection
- Color picker
- Flag naming prompt
- Image import from a direct URL, restricted by `uploadflag`
- GUI click and drag protection to prevent taking editor controls
- Included resource-pack texture assets based on the supplied PNG mockups

## Crafting

```text
Stick  White Wool  White Wool
Stick  White Wool  White Wool
```

## Upload permission

The `uploadflag` permission defaults to operators. Players without it see `Insufficient permissions` in the upload button tooltip and cannot use the upload action.

## Resource pack

The companion resource pack contains the supplied editor, name-bar, Save, and Cancel PNG assets. It is a texture asset pack; the plugin's current editor is still implemented as an inventory GUI, so the PNGs are not yet a full replacement for Minecraft's inventory screen.

## Build

Use the included GitHub Actions workflow or run `gradle build`.


## Custom GUI resources

The companion resource pack is named `Simple-Flags-RP-v1.0.0-6_beta.zip`. It contains the supplied Flag Painter artwork. Paper inventory APIs cannot render a standalone PNG as an interactive inventory background or provide free mouse-drag drawing; this release keeps the canvas slot-based while the custom GUI rendering is being developed.
