package com.monstrous.shadowtest;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class ErrorConsole implements Disposable {
    private static int NUM_MESSAGES = 20;
    private SpriteBatch batch;
    private BitmapFont font;
    private String[] messages;
    private int messageCount;

    public ErrorConsole() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        messages = new String[NUM_MESSAGES];
        messageCount = 0;

    }

    public void render() {
        int x= 0;
        font.setColor(Color.RED);

        batch.begin();

        for(int line = 0; line < NUM_MESSAGES; line++) {
            if(messages[line] != null)
                font.draw(batch, messages[line], x, 100+line*20);
        }
        batch.end();
    }

    public void addMessage(String message){
        if(messageCount < NUM_MESSAGES){
            messages[messageCount] = String.valueOf(messageCount) + " : "+message;
        }
        else {
            for (int line = 0; line < NUM_MESSAGES-1; line++) {
                messages[line] = messages[line+1];      // scroll up
            }
            messages[NUM_MESSAGES-1] = String.valueOf(messageCount) + " : "+message;
        }
        messageCount++;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
