# ScriptEngine
A simple script engine for processing and sharing information of purposing actions on Android platform

Demo:

https://github.com/GoodDamn/ScriptEngine/assets/76108467/b1cc9ede-5581-4ce9-b958-94c1773eefdc

Syntax:
  command(whitespace)value

Example:
  font ul

List of available commands now: 
  - font (overridable) - you can attach a specific text style with enum parameter:
    - Valid values for this command:
      - italic (enum)
      - bold (enum)
      - st (Strikethrough) (enum)
      - ul (Underline) (enum)
      - hex-value(#000000 (RGB) or #00000000 (ARGB)) (not constant)
  - font (value or enum-constant)
  - font (value or enum-constant) (int-beginIndex)
  - font (value or enum-constant) (int-beginIndex) (int-endIndex)

- textSize (overridable) - you can set text size for text:
    - textSize (int-size)
    - textSize (int-size) (int-beginIndex)
    - textSize (int-size) (int-beginIndex) (int-endIndex)

- img - you can attach an image(.png, .jpg) for this script and It show up on the screen:
    - img (string-path to image) (int-width) (int-height) (int-x) (int-y)

- gif - you can attach a animatable-gif:
    - gif (string-path) (int-x) (int-y)
  
List will be continued...
