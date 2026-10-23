package fr.pederobien.voxy.server.interfaces;

import java.util.List;
import java.util.Optional;

public interface IVoxyServer extends ISource {

	/**
	 * @return The vocal server name.
	 */
	String getName();

	/**
	 * Open the server to let the clients connect.
	 * 
	 * @return True if the server is successfully opened, false otherwise.
	 */
	boolean open();

	/**
	 * Close the server, each player currently connected will be kicked.
	 * 
	 * @return True if the server is successfully closed, false otherwise.
	 */
	boolean close();

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 * 
	 * @return True if the server is successfully disposed, false otherwise.
	 */
	boolean dispose();

	/**
	 * @return True if the server is opened, false otherwise.
	 */
	boolean isOpened();

	/**
	 * @return The port number this server is using. -1 if the server is not yet opened or has been closed.
	 */
	int getPort();

	/**
	 * @return True if this server is disposed, false otherwise.
	 */
	boolean isDisposed();

	/**
	 * @return The list of rooms associated to this server.
	 */
	IRoomList getRooms();

	/**
	 * @return A copy of the players connected to the server.
	 */
	List<IVoxyPlayer> getPlayers();

	/**
	 * Get a player by its name.
	 * 
	 * @param name The name of the player to retrieve
	 * @return An optional containing the player associated to the given name if registered, an empty optional otherwise.
	 */
	Optional<IVoxyPlayer> getPlayerByName(String name);

	/**
	 * @return The object that manages effects.
	 */
	IEffectManager getEffectManager();

	/**
	 * Adds or update an effect on the player play back audio stream.
	 * 
	 * @param player The player whose the play back shall be modified.
	 * @param effect The effect to apply or update.
	 */
	default void addOrUpdateEffect(IVoxyPlayer player, IVoxyEffect effect) {
		addOrUpdateEffect(player, player, effect);
	}

	/**
	 * Adds an effect on the audio stream of the speaking player. If there is an effect already registered for the given name, then it
	 * is updated.
	 * 
	 * @param listening The listening player to notify.
	 * @param speaking  The speaking player, ie the audio stream for which an effect shall be added or updated.
	 * @param effect    The effect to add or update.
	 */
	void addOrUpdateEffect(IVoxyPlayer listening, IVoxyPlayer speaking, IVoxyEffect effect);

	/**
	 * Removes an effect, if registered, on the audio stream of a player.
	 * 
	 * @param listening  The player to notify.
	 * @param speaking   The speaking player, ie the audio stream for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	void removeEffect(IVoxyPlayer listening, IVoxyPlayer speaking, String effectName);

	/**
	 * Removes an effect, if registered, on the play back of a player.
	 * 
	 * @param player     The player whose the playback shall not be modified by the given effect anymore.
	 * @param effectName The name of the effect to remove.
	 */
	default void removeEffect(IVoxyPlayer player, String effectName) {
		removeEffect(player, player, effectName);
	}

	/**
	 * Removes all effects currently applied on the audio stream of the speaking player for the given listening player.
	 * 
	 * @param listening The player that shall remove all effects.
	 * @param speaking  The audio stream on which no effect shall be applied.
	 */
	void clearEffects(IVoxyPlayer listening, IVoxyPlayer speaking);

	/**
	 * For each player in each room, removes all effects a player may have for another player.
	 */
	default void clearEffectsForAllPlayers() {
		for (IVoxyRoom room : getRooms().toList()) {
			List<IVoxyPlayer> players = room.getPlayers().toList();

			for (IVoxyPlayer player1 : players)
				for (IVoxyPlayer player2 : players)
					if (player1 != player2)
						clearEffects(player1, player2);
		}
	}

	/**
	 * Adds an effect on the audio stream of each speaking player registered in the same room as the listening player. If there is an
	 * effect already registered for the given name, then it is updated. The server tracks player joining / leaving the room in which
	 * the player is so that effect are added or removed accordingly.
	 * 
	 * @param listening The listening player to notify.
	 * @param effect    The effect to add or update.
	 */
	void addOrUpdateEffectForEachSpeakingPlayer(IVoxyPlayer listening, IVoxyEffect effect);

	/**
	 * Adds an effect on the audio stream on the speaking player for each listening player. If there is an effect already registered
	 * for the given name, then it is updated. The server tracks player joining / leaving the room in which the player is so that
	 * effect are added or removed.
	 * 
	 * @param speaking The speaking player whose the audio stream shall be modified by an effect.
	 * @param effect   The effect to add or update.
	 */
	void addOrUpdateEffectForEachListeningPlayer(IVoxyPlayer speaking, IVoxyEffect effect);

	/**
	 * Removes an effect on the audio stream of each speaking player registered in the same room as the listening player.
	 * 
	 * @param listening  The player that shall remove an effect on each audio stream it receives.
	 * @param effectName The name of the effect to remove.
	 */
	void removeEffectForEachSpeakingPlayer(IVoxyPlayer listening, String effectName);

	/**
	 * Removes an effect on the audio stream of the speaking player.
	 * 
	 * @param speaking   The player for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	void removeEffectForEachListeningPlayer(IVoxyPlayer speaking, String effectName);
}
