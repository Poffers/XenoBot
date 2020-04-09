package discord.core.game;

import discord.Main;
import discord4j.core.object.entity.Member;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import discord4j.core.object.entity.Message;

public abstract class AbstractGame {

    private final Message gameMessage;

    private final Member[] players;
    private Timer afkTimer;

    private boolean active;
    private int turn = 0;
    private Member playerThisTurn;

    public AbstractGame(Message message, Member[] players) {
        this.gameMessage = message;
        this.players = players;
        this.afkTimer = new Timer();
        this.active = false;
    }

    abstract protected String getGameTitle();

    abstract protected String getForfeitMessage(Member forfeiter);

    //For setting up specific game types (button manager and message listener);
    abstract protected void setup();

    //Games are responsible for calling updateMessageDisplay() on start
    abstract protected void onStart();

    abstract protected void onEnd();

    abstract protected String getBoard();

    public final void start() {
        if (!active) {
            playerThisTurn = players[0];
            active = true;
            startAfkTimer(playerThisTurn);
            setup();
            onStart();
        }
    }

    protected final void win(String winMessage) {
        //some games don't want the board to display at end, so don't call updateMessageDisplay()
        gameMessage.edit(spec -> spec.setEmbed(embed -> {
            embed.setDescription(winMessage);
            embed.setAuthor(getGameTitle(), "", Main.BOT_AVATAR_URL);
        })).block();
        end();
    }

    protected final void tie(String tieMessage) {
        setInfoDisplay(tieMessage);
        end();
    }

    protected final void end() {
        if (active) {
            afkTimer.cancel();
            active = false;
            onEnd();
            GameManager.removeGame(gameMessage);
        }
    }

    public final boolean isActive() {
        return active;
    }

    protected final void setGameDisplay(String text) {
        gameMessage.edit(spec -> spec.setEmbed(embed -> {
            embed.setDescription(text);
            embed.setAuthor(getGameTitle(), "", Main.BOT_AVATAR_URL);
        })).block();
    }

    protected final void setInfoDisplay(String info) {
         setGameDisplay(info + "\n\n" + getBoard());
    }

    protected final Message getGameMessage() {
        return gameMessage;
    }

    protected final Member getPlayerNextTurn() {
        return players[(turn + 1) % players.length];
    }

    protected final Member getPlayerThisTurn() {
        return playerThisTurn;
    }

    //Only works with 2 player games
    protected final Member getOtherUser(Member player) {
        if (player.equals(players[0])) {
            return players[1];
        } else {
            return players[0];
        }
    }

    protected final boolean playerIsInGame(Member player) {
        for (Member p : players) {
            if (p.equals(player)) return true;
        }
        return false;
    }

    protected final void setupNextTurn() {
        playerThisTurn = getPlayerNextTurn();
        turn++;
        startAfkTimer(playerThisTurn);
    }

    private void startAfkTimer(Member player) {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                win(player.getDisplayName() + " failed to go in time. "
                        + getPlayerNextTurn().getDisplayName() + " wins!");
            }
        };
        afkTimer.cancel();
        afkTimer = new Timer();
        afkTimer.schedule(task, TimeUnit.MINUTES.toMillis(15));
    }
    
}

