package com.monstrous.shadowtest;

public class Settings {

    public static float         ambientLightLevel = 0.2f;
    public static float         directionalLightLevel = 0.8f;

    // shadows
    public static int           shadowMapSize = 1024;
    public static int           shadowViewportSize = 200;
    public static float         shadowNear = 0f;
    public static float         shadowFar = 300f;
    public static int           inverseShadowBias = 5000;

    // cascaded shadows
    public static boolean       cascadedShadows = true;
    public static int           numCascades = 2;
    public static float         cascadeSplitDivisor = 6f;

    public static boolean       debugShowLightBox = false;
    public static boolean       debugShowCascades = false;
    public static boolean       debugShowFrustum = false;

    public static boolean       showLightSettingsMenu = true;


    public static boolean       useGLprofiler = false;      // set to true to get GL errors reported (NB big impact on frame rate)
}
