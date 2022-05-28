# Arturia Minilab Extension for Bitwig Studio
![Minilab](https://medias.arturia.net/images/products/minilab/minilab-image.png "Minilab")

## Mapping

#### Remote Controls
- Knobs 1-4 and 9-12 are mapped to the Remote Controls Panel
- Shift + Ocatve Up/Down switches between panels
- Shift + Octave Up + Octave Down jumps back to the first Control Panel

#### Track Controls
- Knobs 5-7 and 13-15 control the sends of the selected track
- Knob 8 sets the panning of the selected track
- Knob 16 controls the volume

#### Pads
Press the "Pad 1-8, 9-16" Button to change between the primary and secondary pads.
- Primary (1-8): Shift + Pad: Triggers a user macro. The specific macro can be changed in the settings.
- Secondary (9-16): Shift + Pad 1-6 select the corresponding track in the editor. Pad 7 and 8 scroll the selection bank up or down.

## Installation
1. Download the the [latest](https://github.com/lplath/Minilab_V2/releases/tag/1.1) release and place the .bwextension file into the following location:
   - macOS
   `Documents/Bitwig Studio/Extensions`
   - Windows
   `%USERPROFILE%\Documents\Bitwig Studio\Extensions`
   - Linux
   `~/Bitwig Studio/Extensions`
2. Update the device mapping by going to Settings > Device > Reset. This only needs to be done once.
3. Select the 8th memory slot by selecting Pad 9-16 and then pressing Shift + Pad 8. 
