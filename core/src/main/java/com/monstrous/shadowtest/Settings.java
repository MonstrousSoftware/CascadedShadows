package com.monstrous.shadowtest;

public class Settings {

    public static int           shadowMapSize = 4096;
    public static int           shadowViewportSize = 600;
    public static float         shadowNear = 0f;
    public static float         shadowFar = 300f;
    public static int           inverseShadowBias = 5000;

    public static boolean       cascadedShadows = true;
    public static int           numCascades = 2;
    public static float         cascadeSplitDivisor = 6f;

    public static boolean       showLightSettingsMenu = true;


    public static boolean       useGLprofiler = false;      // set to true to get GL errors reported
}
