package com.monstrous.shadowtest.gui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.monstrous.shadowtest.Main;
import com.monstrous.shadowtest.Settings;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.scene.CascadeShadowMap;

// Debug window for some in-game tweaking
// Typically, these modify members of the Settings class.

public class LightSettingsWindow extends Window {

    private final Skin skin;
    private final Main screen;

    public LightSettingsWindow(String title, Skin skin, Main screen) {
        super(title, skin);
        this.skin = skin;
        this.screen = screen;
        rebuild();
    }

    private void rebuild() {
       clear();


        final Label ALValue = new Label("", skin);
        ALValue.setText(FormatUtils.formatFloat(Settings.ambientLightLevel));
        final Slider alSlider = new Slider(0.0f, 1.0f, 0.1f, false, skin);
        alSlider.setValue(Settings.ambientLightLevel);
        alSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.ambientLightLevel = alSlider.getValue();
                ALValue.setText(FormatUtils.formatFloat(Settings.ambientLightLevel));
                screen.sceneManager.setAmbientLight(Settings.ambientLightLevel);
            }
        });
        add(new Label("ambient light:", skin)).left();add(ALValue); row();
        add(alSlider).colspan(2).width(400f); row();



        final Label DLValue = new Label("", skin);
        DLValue.setText(FormatUtils.formatFloat(Settings.directionalLightLevel));
        final Slider dlSlider = new Slider(0.0f, 2.0f, 0.1f, false, skin);
        dlSlider.setValue(Settings.directionalLightLevel);
        dlSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.directionalLightLevel = dlSlider.getValue();
                DLValue.setText(FormatUtils.formatFloat(Settings.directionalLightLevel));
                screen.light.intensity = Settings.directionalLightLevel;
            }
        });
        add(new Label("directional light:", skin)).left();add(DLValue); row();
        add(dlSlider).colspan(2).width(400f); row();




        final CheckBox CSMcheckBox = new CheckBox("cascaded shadow maps", skin);
        CSMcheckBox.setChecked(Settings.cascadedShadows);
        CSMcheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.cascadedShadows = CSMcheckBox.isChecked();
                if (Settings.cascadedShadows) {
                    if (screen.csm != null)
                        screen.csm.dispose();
                    screen.csm = new CascadeShadowMap(Settings.numCascades);
                    screen.sceneManager.setCascadeShadowMap(screen.csm);
                } else {
                    screen.sceneManager.setCascadeShadowMap(null);
                    screen.csm.dispose();
                    screen.csm = null;
                    //screen.light.setViewport(Settings.shadowViewportSize, Settings.shadowViewportSize, Settings.shadowNear, Settings.shadowFar);
                }
                screen.buildLightBoxInstances();
                rebuild();
            }
        });
        add(CSMcheckBox).left();
        row();

        if(Settings.cascadedShadows) {

            final Label CNValue = new Label("", skin);
            CNValue.setText(Settings.numCascades);
            final Slider cnSlider = new Slider(1.0f, 6.0f, 1f, false, skin);
            cnSlider.setValue(Settings.numCascades);
            cnSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.numCascades = (int) cnSlider.getValue();
                    CNValue.setText(Settings.numCascades);
                    if (Settings.cascadedShadows) {
                        if (screen.csm != null)
                            screen.csm.dispose();
                        screen.csm = new CascadeShadowMap(Settings.numCascades);
                        screen.sceneManager.setCascadeShadowMap(screen.csm);
                        screen.csm.setCascades(screen.playerView, screen.light, 0, Settings.cascadeSplitDivisor);
                        screen.buildLightBoxInstances();
                    }
                }
            });
            add(new Label("number of cascades:", skin)).left();
            add(CNValue);
            row();
            add(cnSlider).colspan(2).width(400f);
            row();


            final Label CSValue = new Label("", skin);
            CSValue.setText(FormatUtils.formatFloat( Settings.cascadeSplitDivisor));
            final Slider csSlider = new Slider(1.0f, 16.0f, 0.1f, false, skin);
            csSlider.setValue(Settings.cascadeSplitDivisor);
            csSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.cascadeSplitDivisor = csSlider.getValue();
                    CSValue.setText(FormatUtils.formatFloat( Settings.cascadeSplitDivisor));
                    screen.csm.setCascades(screen.playerView, screen.light, 0, Settings.cascadeSplitDivisor);
                    screen.buildLightBoxInstances();
                }
            });
            add(new Label("cascade split divisor:", skin)).left();
            add(CSValue);
            row();
            add(csSlider).colspan(2).width(400f);
            row();

            final CheckBox LBCcheckBox = new CheckBox("show cascades", skin);
            LBCcheckBox.setChecked(Settings.debugShowCascades);
            LBCcheckBox.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.debugShowCascades = LBCcheckBox.isChecked();
                    if(Settings.debugShowCascades)
                        Settings.debugShowLightBox = true;
                    screen.buildLightBoxInstances();
                }
            });
            add(LBCcheckBox).left();
            row();

        } else {

            final Label vpValue = new Label("", skin);
            vpValue.setText(  Settings.shadowViewportSize);
            final Slider vpSlider = new Slider(10f, 1000.0f, 10f, false, skin);
            vpSlider.setValue(Settings.shadowViewportSize);
            vpSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.shadowViewportSize = (int) vpSlider.getValue();
                    vpValue.setText( Settings.shadowViewportSize);
                    screen.light.setViewport(Settings.shadowViewportSize, Settings.shadowViewportSize, Settings.shadowNear, Settings.shadowFar);
                    screen.buildLightBoxInstances();
                }
            });
            add(new Label("shadow viewport size:", skin)).left();add(vpValue); row();
            add(vpSlider).colspan(2).width(400f); row();


            final Label nearValue = new Label("", skin);
            nearValue.setText(FormatUtils.formatFloat(Settings.shadowNear));
            final Slider nearSlider = new Slider(0.0f, 1000.0f, 0.1f, false, skin);
            nearSlider.setValue(Settings.shadowNear);
            nearSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.shadowNear = nearSlider.getValue();
                    nearValue.setText(FormatUtils.formatFloat(Settings.shadowNear));
                    screen.light.setViewport(Settings.shadowViewportSize, Settings.shadowViewportSize, Settings.shadowNear, Settings.shadowFar);
                    screen.buildLightBoxInstances();
                }
            });
            add(new Label("shadow near distance:", skin)).left();add(nearValue); row();
            add(nearSlider).colspan(2).width(400f); row();

            final Label farValue = new Label("", skin);
            farValue.setText(FormatUtils.formatFloat(Settings.shadowFar));
            final Slider farSlider = new Slider(0.0f, 1000.0f, 0.1f, false, skin);
            farSlider.setValue(Settings.shadowFar);
            farSlider.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    Settings.shadowFar = farSlider.getValue();
                    farValue.setText(FormatUtils.formatFloat(Settings.shadowFar));
                    screen.light.setViewport(Settings.shadowViewportSize, Settings.shadowViewportSize, Settings.shadowNear, Settings.shadowFar);
                    screen.buildLightBoxInstances();
                }
            });
            add(new Label("shadow far distance:", skin)).left();add(farValue); row();
            add(farSlider).colspan(2).width(400f); row();
        }
        final Label biasValue = new Label("", skin);
        biasValue.setText( Settings.inverseShadowBias);
        final Slider biasSlider = new Slider(1.0f, 5000.0f, 10f, false, skin);
        biasSlider.setValue(Settings.inverseShadowBias);
        biasSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.inverseShadowBias = (int) biasSlider.getValue();
                biasValue.setText( Settings.inverseShadowBias);
                screen.sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1f/Settings.inverseShadowBias));

            }
        });
        add(new Label("1/shadow bias:", skin)).left();add(biasValue); row();
        add(biasSlider).colspan(2).width(400f); row();

        // power of two slider
        final Label smValue = new Label("", skin);
        smValue.setText(  Settings.shadowMapSize );
        final Slider smSlider = new Slider(0f, 5f, 1f, false, skin);
        smSlider.setValue((float)(Math.log(Settings.shadowMapSize/256)/Math.log(2.0)));
        smSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.shadowMapSize = 256 * (int) Math.pow(2, smSlider.getValue());
                smValue.setText( Settings.shadowMapSize);
                screen.light.setShadowMapSize(Settings.shadowMapSize, Settings.shadowMapSize);
            }
        });
        add(new Label("shadow map size:", skin)).left();add(smValue); row();
        add(smSlider).colspan(2).width(400f); row();

        final CheckBox LBcheckBox = new CheckBox("show light box", skin);
        LBcheckBox.setChecked(Settings.debugShowLightBox);
        LBcheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.debugShowLightBox = LBcheckBox.isChecked();
                screen.buildLightBoxInstances();
            }
        });
        add(LBcheckBox).left();
        row();

        final CheckBox fixcheckBox = new CheckBox("fix view frustum", skin);
        fixcheckBox.setChecked(Settings.debugShowFrustum);
        fixcheckBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Settings.debugShowFrustum = fixcheckBox.isChecked();
                screen.fixViewFrustum();
                screen.buildLightBoxInstances();
            }
        });
        add(fixcheckBox).left();
        row();


        pack();
    }


}
