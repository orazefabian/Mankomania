package com.mankomania.game.gamecore.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.utils.Array;
import com.mankomania.game.gamecore.MankomaniaGame;
import com.mankomania.game.gamecore.fieldoverlay.FieldOverlay;


public class MainGameScreen extends AbstractScreen {
    public PerspectiveCamera cam;
    public ModelBatch modelBatch;
    public Environment environment;
    public CameraInputController camController;
    public AssetManager assets;
    public boolean loading;
    public ModelInstance instance;
    public Array<ModelInstance> instances = new Array<ModelInstance>();
    public Model model;
    public MankomaniaGame game;

    private SpriteBatch spriteBatch;
    private FieldOverlay fieldOverlay;

    public MainGameScreen() {
        create();
    }


    public void create() {
        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1.0f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1.0f, -0 - 8f, -0.2f));
        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(60, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0.0f, 160.0f, 20.0f);
        cam.lookAt(0, 0, 0);
        //cam.rotate(-90, 90,0, 0);
        cam.near = 20.0f;
        cam.far = 300.0f;
        cam.update();

        camController = new CameraInputController(cam);
        // Gdx.input.setInputProcessor(camController);
        assets = new AssetManager();
        assets.load("board/board.g3db", Model.class);
        loading = true;

        this.spriteBatch = new SpriteBatch();

        this.fieldOverlay = new FieldOverlay();
        this.fieldOverlay.create();

        this.fieldOverlay.selectField(10); // testing selection, TODO: implement on touch selection

        // use a InputMultiplexer to delegate a list of InputProcessors.
        // "Delegation for an event stops if a processor returns true, which indicates that the event was handled."
        // add other needed InputPreprocessors here
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.fieldOverlay);
        multiplexer.addProcessor(this.camController);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        if (loading && assets.update()) {
            doneLoading();
        }
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);
        modelBatch.render(instances, environment);
        modelBatch.end();
        camController.update();

        // enabling blending, so transparency can be used (batch.setAlpha(x))
        this.spriteBatch.enableBlending();

        // start SpriteBatch and render overlay after model batch, so the overlay gets rendered "above" the 3d models
        this.spriteBatch.begin();
        this.fieldOverlay.render(spriteBatch);
        this.spriteBatch.end();

        // this.fieldOverlay.scroll(3.5f);
    }

    private void doneLoading() {
        Model board = assets.get("board/board.g3db", Model.class);
        ModelInstance boardInstance = new ModelInstance(board);
        this.instances.add(boardInstance);
        this.loading = false;
    }

    @Override
    public void dispose() {
        this.model.dispose();
        this.modelBatch.dispose();
    }
}