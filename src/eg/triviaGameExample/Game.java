package eg.triviaGameExample;

import java.util.ArrayList;
import java.util.Collection;

public class Game {
  private Collection<Player> players;
  private boolean gameHasStarted = false;

  public Game() {
    players = new ArrayList<>();
  }

  public Player addPlayer(String aPlayerName) {
    if (gameHasStarted)
      return null;
    Player aPlayer = new Player(aPlayerName);
    players.add(aPlayer);
    return aPlayer;
  }

  public boolean playerIsPlaying(Player aPlayer) {
    return players.contains(aPlayer);
  }

  public int getNumberOfPlayers() {
    return players.size();
  }

  public Player getPlayerNamed(String playerName) {
    for (Player player : players) {
      if (player.getName().equals(playerName))
        return player;
    }
    return null;
  }

  public String takeTurn(int roll) {
    gameHasStarted = true;
    return "Al";
  }

  public void removePlayer(Player thePlayer) {
    if (gameHasStarted)
      return;

  }

  public boolean gameHasStarted() {
    return gameHasStarted;
  }
}
