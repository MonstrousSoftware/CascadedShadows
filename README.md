# CascadedShadows

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

Test of cascaded shadows on different platforms.
Modified gdx-gltf, in particular PBRShader.java and shadows.glsl to make it work with Web GL (gdx-teavm, html).


Specific gradle.properties (changed from liftoff defaults):
    gdxGltfVersion=-SNAPSHOT
    gdxTeaVMVersion=-SNAPSHOT
    teaVMVersion=0.9.2


Note: html (GWT) doesn't work because it uses the original PBRShader.java from gdx-gltf instead of the local modified one from the project.



## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.
- `html`: Web platform using GWT and WebGL. Supports only Java projects.
- `teavm`: Experimental web platform using TeaVM and WebGL.
