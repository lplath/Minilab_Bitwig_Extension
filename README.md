# Minilab V2
Arturia Minilab support for Bitwig Studio
![Minilab](https://medias.arturia.net/images/products/minilab/minilab-image.png "Minilab")

This is currently a port of the [Javascript-version](https://github.com/lplath/Arturia_MiniLab_Bitwig) with the intention of adding more features.

## Mapping

### Remote Controls
- The left side of the rotary encoders (1-4 and 9-12) are mapped to the Remote Controls Panel
- Shift + Ocatve Up/Down switches between panels
- Shift + Octave Up + Octave Down jumps to the first Control Panel

### Track Controls
- The upper row on the right (5-8) controls the sends of the selected track
- Knob 15 sets the panning of the selected track
- Knob 16 controls the volume

### Pads
- Shift + Pad: user macro (e.g. *Play*, *Record*, etc)

## Installation
Download the .bwextension file and place it into the following location:
- macOS
`Documents/Bitwig Studio/Extensions`
- Windows
`%USERPROFILE%\Documents\Bitwig Studio\Extensions`
- Linux
`~/Bitwig Studio/Extensions`
