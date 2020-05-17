package com.mankomania.game.gamecore.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Align;
import com.esotericsoftware.minlog.Log;
import com.mankomania.game.gamecore.util.Screen;
import com.mankomania.game.gamecore.util.ScreenManager;


import java.util.Random;

public class DiceOverlay {

    int rand_int1;
    public Table DiceOverlay() {
        Table table=new Table();

        Skin skin = new Skin(Gdx.files.internal("skin/terra-mother-ui.json"));
        Texture texture = new Texture(Gdx.files.internal("button1.png"));
        Image image = new Image(texture);
        image.setSize(400, 400);

        table = new Table();
        table.setFillParent(true);
        table.setBackground(new TiledDrawable(skin.getTiledDrawable("tile-a")));
         table.setWidth(Gdx.graphics.getWidth());
         table.setHeight(Gdx.graphics.getHeight());
        table.align(Align.center | Align.top);
        Label dice = new Label("You rolled: ", skin);

        skin.getFont("font").getData().setScale(5, 5);
        Random rand = new Random();
        rand_int1 = rand.nextInt(12) + 1;
        String roll = String.valueOf(rand_int1);
        Label value = new Label(roll, skin);

        table.add(image).padTop(30);
        table.row();
        table.add(dice);
        table.row();
        table.add(value);

        return table;
    }

    public void sendDiceRoll(){
        Log.info("[DiceScreen] Done rolling the dice (rolled a " + rand_int1 + "). Calling the MessageHandlers'");
        //TODO COMMENT OUT WHEN MERGIN WITH GAME-LOOP
        //ScreenManager.getInstance().getGame().getClient().getMessageHandler().sendDiceResultMessage(rand_int1);
    }

}
