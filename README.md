# EclipseXCopy
Eclipse plugin for Unix-style copy and paste (select to copy, middle click to paste).

## License

Eclipse Public License v1.0, this plugin is based on another plugin from Mateusz Matela (https://code.google.com/p/eclipse-mmb-scroller/).

## Installation

Simply copy the plugin JAR file in the plugins directory and (re)start Eclipse. Works with Eclipse Luna (since the plugin relies on a reflection trick, it might not work with all past and future Eclipse versions).

## Details

This plugin targets Windows, it is obviously not needed on Linux, and has not been tested on MacOS.

Very basic plugin allowing to copy to a secondary clipboard by selecting a chunk of text, and pasting by clicking the middle mouse button (text is inserted at the current mouse cursor position).

Of course, there are AutoHotKey scripts and other similar tools which are supposed to provide similar functionality (system-wide), but none of these work well in my experience.
The underlying reason is that they rely on the system clipboard, which causes various issues (timing issues, non-standard text controls, conflicts with "normal" copy and paste operations, etc.).
Many of these issues can be fixed independently, but not all of them (I have spent many hours building a ridiculously complex AutoHotKey script that never fully worked).

So I'm at the point where I'm convinced that this can only be done with some kind of direct programmatic access to the text controls (to get the selected text and insert pasted text).
Which is exactly what this plugin does: it registers a hook in the Eclipse workspace to be notified whenever a SWT text controls is created, and from there does its thing.

Many thanks to Mateusz Matela (https://code.google.com/p/eclipse-mmb-scroller/) for sharing the code for this hook using reflection.
