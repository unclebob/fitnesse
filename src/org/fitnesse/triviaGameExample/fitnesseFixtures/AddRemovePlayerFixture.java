package org.fitnesse.triviaGameExample.fitnesseFixtures;

import org.fitnesse.triviaGameExample.Game;
import org.fitnesse.triviaGameExample.Player;

public class AddRemovePlayerFixture {
  private String playerName;
  private Game theGame;

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public boolean addPlayer() {
    theGame = StaticGame.theGame;
    Player thePlayer = theGame.addPlayer(playerName);
    return theGame.playerIsPlaying(thePlayer);
  }

  public boolean removePlayer() {
    theGame = StaticGame.theGame;
    Player thePlayer = theGame.getPlayerNamed(playerName);
    theGame.removePlayer(thePlayer);
    return playerWasRemoved(thePlayer);
  }

  private boolean playerWasRemoved(Player aPlayer) {
    return !theGame.playerIsPlaying(aPlayer);
  }

  public int countPlayers() {
    return theGame.getNumberOfPlayers();
  }
}
