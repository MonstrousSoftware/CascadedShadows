package com.monstrous.shadowtest;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.profiling.GLErrorListener;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.monstrous.shadowtest.gui.GUI;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.*;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;


public class Main extends ApplicationAdapter {
    public SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;
    private PerspectiveCamera camera;
    public  PerspectiveCamera playerView;       // points to either camera or frozenCamera
    private PerspectiveCamera frozenCamera;
    private CameraInputController camController;
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
    private boolean autoRotateMode = true;
    private ModelBatch modelBatch;
    private Array<ModelInstance> instances;


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



        sceneManager = new SceneManager();

        // create scene
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/shadowTest.gltf"));
        scene = new Scene(sceneAsset.scene);
        sceneManager.addScene(scene);

        // setup camera
        camera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camDist = 15f;
        camera.near = 1f;
        camera.far = 1000f;
        sceneManager.setCamera(camera);

        frozenCamera = new PerspectiveCamera(60f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        frozenCamera.near = 1f;
        frozenCamera.far = 100f;
        frozenCamera.position.setFromSpherical(MathUtils.PI/4, 0).scl(camDist);
        frozenCamera.up.set(Vector3.Y);
        frozenCamera.lookAt(Vector3.Zero);
        frozenCamera.update();

        playerView = camera;


        camController = new CameraInputController(camera);
        camController.scrollFactor = -0.5f;

        InputMultiplexer im = new InputMultiplexer();
        im.addProcessor(gui.stage);
        im.addProcessor(camController);
        Gdx.input.setInputProcessor(im);

        // setup light
        light = new DirectionalShadowLight(Settings.shadowMapSize, Settings.shadowMapSize);
        light.setViewport(Settings.shadowViewportSize,Settings.shadowViewportSize,Settings.shadowNear, Settings.shadowFar);

        light.direction.set(1, -1.5f, 1).nor();
        light.color.set(Color.WHITE);
        light.intensity = Settings.directionalLightLevel;
        sceneManager.environment.add(light);

        PointLight pointLight = new PointLight();
        pointLight.color.set(Color.RED);
        pointLight.position.set(0,3,0);
        pointLight.intensity = 100f;
        sceneManager.environment.add(pointLight);

        pointLight = new PointLight();
        pointLight.color.set(Color.BLUE);
        pointLight.position.set(-14,3,0);
        pointLight.intensity = 100f;
        sceneManager.environment.add(pointLight);

        pointLight = new PointLight();
        pointLight.color.set(Color.GREEN);
        pointLight.position.set(14,3,5);
        pointLight.intensity = 100f;
        sceneManager.environment.add(pointLight);


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

        sceneManager.setAmbientLight(Settings.ambientLightLevel);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));

        // setup skybox
        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);

        modelBatch = new ModelBatch();
        instances = new Array<>();
        buildLightBoxInstances();
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
        gui.resize(width, height);
        Gdx.app.log("resize", "");
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        time += deltaTime;

        if(Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            Settings.showLightSettingsMenu = !Settings.showLightSettingsMenu;
            gui.showLightMenu(Settings.showLightSettingsMenu);
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT))
            autoRotateMode = false;
        if(Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
            autoRotateMode = true;
            camDist = camera.position.len();
        }

        // animate camera
        if(autoRotateMode) {
            float polar = (time * .05f) % 2f;
            if(polar > 1)
                polar = 2 - polar;
            polar *= MathUtils.PI;
            camera.position.setFromSpherical(MathUtils.PI/4, polar).scl(camDist);
            camera.up.set(Vector3.Y);
            camera.lookAt(Vector3.Zero);
            camera.update();
        } else {
            camController.update();
        }


        if(Settings.cascadedShadows) {
            csm.setCascades(playerView, light, 0, Settings.cascadeSplitDivisor);
        }
        else {
            light.setCenter(Vector3.Zero); // keep shadow light on origin so that we have shadows
        }

        // render
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        sceneManager.update(deltaTime);
        sceneManager.render();

        modelBatch.begin(camera);
        modelBatch.render(instances);
        modelBatch.end();

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


    // take a snapshot of the current camera view so that the frustum can be seen from 'God mode'
    public void fixViewFrustum(){
        frozenCamera.position.set(camera.position);
        frozenCamera.near = camera.near;
        frozenCamera.far = 100f;  // for demonstration purposes use a small far value
        frozenCamera.direction.set(camera.direction);
        frozenCamera.up.set(camera.up);
        frozenCamera.update();

        if(Settings.debugShowFrustum)
            playerView = frozenCamera;
        else
            playerView = camera;

    }

    public void buildLightBoxInstances() {
        instances.clear();
        if(Settings.debugShowLightBox) {

            DirectionalShadowLight shadowLight = sceneManager.getFirstDirectionalShadowLight();

            // force the light.camera and hence its frustum to be set to the correct position and direction
            shadowLight.begin();
            shadowLight.end();

            addShadowBox(shadowLight, Color.GREEN);
        }

        if(Settings.cascadedShadows && Settings.debugShowCascades) {
            csm.setCascades(playerView, light, 0, Settings.cascadeSplitDivisor);

            for (DirectionalShadowLight light : csm.lights) {
                light.begin();
                light.end();

                addShadowBox(light, Color.ORANGE);
            }
        }

        // create a frustum model for the perspective camera view
        if(Settings.debugShowFrustum) {
            Model viewModel = createFrustumModel(Color.BLUE, frozenCamera.frustum.planePoints);
            ModelInstance viewInstance = new ModelInstance(viewModel);
            instances.add(viewInstance);
        }
    }

    private void addShadowBox(DirectionalShadowLight light, Color color){
        // create a frustum model (box shape, since the camera is orthogonal) for the directional shadow light
        Model frustumModel = createFrustumModel(color, light.getCamera().frustum.planePoints);
        ModelInstance instance = new ModelInstance(frustumModel);
        instances.add(instance);

        ModelBuilder modelBuilder = new ModelBuilder();

        Material mat = new Material(new ColorAttribute(ColorAttribute.Diffuse, color));

        // add sphere as light source
        float sz = 2.0f;
        // show the light source at the centre of the near plane
        Vector3 lightPos = new Vector3(light.direction).scl(light.getCamera().near).add(light.getCamera().position);
        Model ball = modelBuilder.createSphere(sz, sz, sz, 6, 6, mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(ball, lightPos));

        // light direction as arrow
        Vector3 to = new Vector3(light.direction).scl(10f);
        Model arrow = modelBuilder.createArrow(Vector3.Zero, to, mat, VertexAttributes.Usage.Position | VertexAttributes.Usage.ColorPacked);
        instances.add(new ModelInstance(arrow, lightPos));
    }

    // from libgdx tests
    private static Model createFrustumModel (final Color color, final Vector3... p) {
        ModelBuilder builder = new ModelBuilder();
        builder.begin();
        MeshPartBuilder mpb = builder.part("", GL20.GL_LINES, VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            new Material(new ColorAttribute(ColorAttribute.Diffuse, color)));
        mpb.vertex(p[0].x, p[0].y, p[0].z, 0, 0, 1, p[1].x, p[1].y, p[1].z, 0, 0, 1, p[2].x, p[2].y, p[2].z, 0, 0, 1, p[3].x,
            p[3].y, p[3].z, 0, 0, 1, // near
            p[4].x, p[4].y, p[4].z, 0, 0, -1, p[5].x, p[5].y, p[5].z, 0, 0, -1, p[6].x, p[6].y, p[6].z, 0, 0, -1, p[7].x, p[7].y,
            p[7].z, 0, 0, -1);
        mpb.index((short)0, (short)1, (short)1, (short)2, (short)2, (short)3, (short)3, (short)0);
        mpb.index((short)4, (short)5, (short)5, (short)6, (short)6, (short)7, (short)7, (short)4);
        mpb.index((short)0, (short)4, (short)1, (short)5, (short)2, (short)6, (short)3, (short)7);
        return builder.end();
    }


}
