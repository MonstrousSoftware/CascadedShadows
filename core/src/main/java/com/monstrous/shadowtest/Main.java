package com.monstrous.shadowtest;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.monstrous.shadowtest.gui.GUI;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.*;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


public class Main extends ApplicationAdapter {
    public SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;
    private PerspectiveCamera camera;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private float time;
    private SceneSkybox skybox;
    public DirectionalShadowLight light;
    private float camDist;
    public CascadeShadowMap csm;
    private GUI gui;
    private ErrorConsole errors;
    private GLProfiler glProfiler;


    final GLErrorListener customListener = new GLErrorListener() {
        @Override
        public void onError (int error) {
            errors.addMessage("GL error code: "+ error );
        }
    };


    @Override
    public void create() {


        errors = new ErrorConsole();
        if(Settings.useGLprofiler) {
            glProfiler = new GLProfiler(Gdx.graphics);
            glProfiler.enable();
            //glProfiler.setListener(customListener);
            glProfiler.setListener(GLErrorListener.THROWING_LISTENER);
        }

        gui = new GUI(this);

        Gdx.input.setInputProcessor(gui.stage);

        sceneManager = new SceneManager();
        //sceneManager = new SceneManager( new MyPBRShaderProvider(), PBRShaderProvider.createDefaultDepth(24) );

        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/shadowTest.gltf"));
        scene = new Scene(sceneAsset.scene);
        sceneManager.addScene(scene);

        // setup camera (The BoomBox model is very small so you may need to adapt camera settings for your scene)
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camDist = 15f;
        camera.near = 1f;
        camera.far = 1000f;
        sceneManager.setCamera(camera);

        // setup light
        light = new DirectionalShadowLight(Settings.shadowMapSize, Settings.shadowMapSize);
        light.setViewport(Settings.shadowViewportSize,Settings.shadowViewportSize,Settings.shadowNear, Settings.shadowFar);

        light.direction.set(1, -3, 1).nor();
        light.color.set(Color.WHITE);
        sceneManager.environment.add(light);

        if(Settings.cascadedShadows) {
            csm = new CascadeShadowMap(Settings.numCascades);
            sceneManager.setCascadeShadowMap(csm);
        }

        sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f/Settings.inverseShadowBias));

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createOutdoor(light);
        environmentCubemap = iblBuilder.buildEnvMap(1024);
        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        sceneManager.setAmbientLight(1f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
        gui.resize(width, height);
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        time += deltaTime;

        if(Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            Settings.showLightSettingsMenu = !Settings.showLightSettingsMenu;
            gui.showLightMenu(Settings.showLightSettingsMenu);
        }

        // animate camera
        float polar = (time * .05f) % 2f;
        if(polar > 1)
            polar = 2 - polar;
        polar *= MathUtils.PI;
        camera.position.setFromSpherical(MathUtils.PI/4, polar).scl(camDist);
        camera.up.set(Vector3.Y);
        camera.lookAt(Vector3.Zero);
        camera.update();

        if(Settings.cascadedShadows) {
            csm.setCascades(sceneManager.camera, light, 0, Settings.cascadeSplitDivisor);
        }
        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        gui.render(deltaTime);
        errors.render();
    }

    @Override
    public void dispose() {
        sceneManager.dispose();
        sceneAsset.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();
        gui.dispose();
        errors.dispose();
    }

}
