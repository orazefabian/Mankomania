package com.mankomania.game.gamecore.client;

import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.minlog.Log;
import com.mankomania.game.core.network.KryoHelper;
import com.mankomania.game.core.network.messages.ChatMessage;
import com.mankomania.game.core.network.messages.clienttoserver.PlayerReady;
import com.mankomania.game.core.network.messages.servertoclient.*;
import com.mankomania.game.core.network.messages.servertoclient.baseturn.MovePlayerToFieldAfterIntersectionMessage;
import com.mankomania.game.core.network.messages.servertoclient.baseturn.MovePlayerToFieldMessage;
import com.mankomania.game.core.network.messages.servertoclient.baseturn.MovePlayerToIntersectionMessage;
import com.mankomania.game.core.network.messages.servertoclient.baseturn.PlayerCanRollDiceMessage;
import com.mankomania.game.gamecore.MankomaniaGame;
import com.mankomania.game.gamecore.util.Screen;
import com.mankomania.game.gamecore.util.ScreenManager;

import java.io.IOException;
import java.util.ArrayList;

import static com.mankomania.game.core.network.NetworkConstants.*;

/*
 Created by Fabian Oraze on 16.04.20
 */

public class NetworkClient {

    private final Client client;
    private final MessageHandler messageHandler;

    public NetworkClient() {
        client = new Client();
        KryoHelper.registerClasses(client.getKryo());

        this.messageHandler = new MessageHandler(this.client);

        client.start();
        client.addListener(new Listener() {

            @Override
            public void received(Connection connection, Object object) {
                Log.info(object.getClass().getSimpleName());

                if (object instanceof PlayerConnected) {
                    Log.info("player connected");
                    Gdx.app.postRunnable(() -> ScreenManager.getInstance().switchScreen(Screen.LOBBY));

                } else if (object instanceof ChatMessage) {
                    ChatMessage response = (ChatMessage) object;
                    //chat will be updated if message received
                    ClientChat.addText(response.text);

                    Log.info("[ChatMessage] received chat message (connection id: " + connection.getID() + "), text: '" + response.text + "'");
                } else if (object instanceof InitPlayers) {
                    // once game starts each player gets a list from server
                    // and creates a hashMap with the IDs and player objects
                    InitPlayers list = (InitPlayers) object;
                    MankomaniaGame.getMankomaniaGame().getGameData().intPlayers(list.playerIDs);

                } else if (object instanceof Notification) {
                    Notification notification = (Notification) object;
                    MankomaniaGame.getMankomaniaGame().getNotifier().add(notification);

                } else if (object instanceof StartGame) {
                    /*
                     * post a Runnable from networking thread to the libgdx rendering thread
                     */
                    Gdx.app.postRunnable(() -> ScreenManager.getInstance().switchScreen(Screen.MAIN_GAME));
                    // once game starts each player gets a list from server
                    // and creates a hashMap with the IDs and player objects
                    StartGame gameStartedMessage = (StartGame) object;
                    MankomaniaGame.getMankomaniaGame().getGameData().intPlayers((ArrayList<Integer>) gameStartedMessage.getPlayerIds());
                    MankomaniaGame.getMankomaniaGame().getGameData().setLocalPlayer(client.getID());

                    Log.info("[GameStartedMessage] got GameStartedMessage, player array size: " + gameStartedMessage.getPlayerIds().size());
                    Log.info("[GameStartedMessage] Initialized GameData with player id's");
                } else if (object instanceof PlayerCanRollDiceMessage) {
                    PlayerCanRollDiceMessage playerCanRollDiceMessage = (PlayerCanRollDiceMessage) object;

                    Log.info("[PlayerCanRollDiceMessage] Player " + playerCanRollDiceMessage.getPlayerId() + " can roll the dice now!");

                    messageHandler.gotPlayerCanRollDiceMessage(playerCanRollDiceMessage);
                } else if (object instanceof MovePlayerToFieldMessage) {
                    MovePlayerToFieldMessage movePlayerToFieldMessage = (MovePlayerToFieldMessage) object;

                    Log.info("[MovePlayerToFieldMessage] Player " + movePlayerToFieldMessage.getPlayerId() + " got move to " + movePlayerToFieldMessage.getFieldToMoveTo() + " message");

                    messageHandler.gotMoveToFieldMessage(movePlayerToFieldMessage);
                } else if (object instanceof MovePlayerToIntersectionMessage) {
                    MovePlayerToIntersectionMessage movePlayerToIntersectionMessage = (MovePlayerToIntersectionMessage) object;

                    Log.info("[MovePlayerToIntersectionMessage] Player " + movePlayerToIntersectionMessage.getPlayerId() + " got to move to field " +
                            movePlayerToIntersectionMessage.getFieldToMoveTo() + " and has to choose between path 1 = (" + movePlayerToIntersectionMessage.getSelectionOption1() +
                            ") and path 2 = (" + movePlayerToIntersectionMessage.getSelectionOption2() + ")");

                    messageHandler.gotMoveToIntersectionMessage(movePlayerToIntersectionMessage);
                } else if (object instanceof MovePlayerToFieldAfterIntersectionMessage) {
                    MovePlayerToFieldAfterIntersectionMessage movePlayerAfterIntersectionMsg = (MovePlayerToFieldAfterIntersectionMessage) object;

                    Log.info("[MovePlayerToFieldAfterIntersectionMessage] Player " + movePlayerAfterIntersectionMsg.getPlayerId() + " got to move on the field " +
                            movePlayerAfterIntersectionMsg.getFieldToMoveTo() + " directly after the intersection.");

                    messageHandler.gotMoveAfterIntersectionMessage(movePlayerAfterIntersectionMsg);
                }
            }
        });
    }

    public void tryConnectClient() {

        try {
            /*
             * client gets connection parameters form NetworkConstants class from core module
             */
            client.connect(TIMEOUT, IP_HOST, TCP_PORT);

        } catch (IOException e) {
            Log.trace("Client connection error: ", e);
            MankomaniaGame.getMankomaniaGame().getNotifier().add(new Notification("error connecting"));
        }
    }

    // old implementation for ChatScreen, maybe direct it to message handler
    public void sendMsgToServer(ChatMessage msg) {
        client.sendTCP(msg);
    }

    public void sendClientState(PlayerReady ready) {
        client.sendTCP(ready);
    }

    public void disconnect() {
        client.close();
    }

    /**
     * Gets the message handler. Used to get hold of the reference to MessageHandler over the MankomaniGame, so it
     * can be used game wide as kind of a wrapper for network messaging.
     *
     * @return the instance of MessageHandler
     */
    public MessageHandler getMessageHandler() {
        return messageHandler;
    }
}
