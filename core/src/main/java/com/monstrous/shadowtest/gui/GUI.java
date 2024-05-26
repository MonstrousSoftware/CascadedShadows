package com.monstrous.shadowtest.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.monstrous.shadowtest.Main;
import com.monstrous.shadowtest.Settings;

public class GUI {
    public Stage stage;
    private Skin skin;

    private Label fpsLabel;
    private LightSettingsWindow lightWindow;


    public GUI(Main screen) {
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        lightWindow = new LightSettingsWindow("Light Settings [L]", skin, screen);
    }

    private void rebuild() {
        stage.clear();

        String labelType  = "default";

        Table screenTable = new Table();
        screenTable.setFillParent(true);

        screenTable.add(new Label("FPS: ", skin, labelType)).left().pad(5);
        fpsLabel = new Label("", skin, labelType);
        screenTable.add(fpsLabel).left();
        screenTable.row();

        screenTable.bottom().left();
        screenTable.pack();

        stage.addActor(screenTable);
        showLightMenu(Settings.showLightSettingsMenu);
    }

    private void updateLabels(){
        fpsLabel.setText( Gdx.graphics.getFramesPerSecond());
    }

    public void render(float deltaTime) {
        updateLabels();

        stage.act(deltaTime);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        rebuild();

    }

    public void showLightMenu(boolean mode){
        if(mode) {
            stage.addActor(lightWindow);
            lightWindow.setPosition(stage.getWidth()-lightWindow.getWidth(),
                stage.getHeight()-lightWindow.getHeight());
        } else
            lightWindow.remove();
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
