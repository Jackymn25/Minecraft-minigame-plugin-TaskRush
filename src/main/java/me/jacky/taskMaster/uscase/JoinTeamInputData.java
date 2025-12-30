package me.jacky.taskMaster.uscase;

import org.bukkit.entity.Player;

/**
 * Input data object for the join team use case.
 * Encapsulates the player and selected team index.
 */
public class JoinTeamInputData {

    /** Player who is joining a team. */
    private final Player player;

    /** Selected team result index. */
    private final int res;

    /**
     * Creates a new JoinTeamInputData instance.
     *
     * @param inputPlayer the player who joins a team
     * @param inputRes the selected team index
     */
    public JoinTeamInputData(final Player inputPlayer, final int inputRes) {
        this.player = inputPlayer;
        this.res = inputRes;
    }

    /**
     * Returns the player who is joining a team.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the selected team index.
     *
     * @return the team index result
     */
    public int getRes() {
        return res;
    }
}
