# CascadedShadows

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).





Specific gradle.properties (changed from liftoff defaults):
    gdxGltfVersion=-SNAPSHOT
    gdxTeaVMVersion=-SNAPSHOT
    teaVMVersion=0.9.2

Add to core/build.gradle:
    api 'com.github.tommyettinger:formic:0.1.5'




## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.
- `html`: Web platform using GWT and WebGL. Supports only Java projects.
- `teavm`: Experimental web platform using TeaVM and WebGL.
