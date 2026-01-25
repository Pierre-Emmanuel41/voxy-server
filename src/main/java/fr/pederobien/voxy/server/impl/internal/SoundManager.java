package fr.pederobien.voxy.server.impl.internal;

import java.util.List;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerSpeakingPostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSpeakingPreEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class SoundManager {
	private final PlayerListImpl players;
	private boolean playBack;

	/**
	 * Creates a sound manager responsible to dispatch player's audio sample.
	 * 
	 * @param players The list of players in a room.
	 */
	public SoundManager(PlayerListImpl players) {
		this.players = players;

		playBack = false;
	}

	/**
	 * Enable or disable the play back. When player back is enabled, a player will here back the audio samples sent to the server.
	 * 
	 * @param playBack True to enable play back, false to disable.
	 */
	public void setPlayBack(boolean playBack) {
		this.playBack = playBack;
	}

	/**
	 * Method called when a player is speaking.
	 * 
	 * @param source    The speaking player.
	 * @param sample    The bytes array that contains the audio sample.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public void onPlayerIsSpeaking(VoxyPlayerImpl source, byte[] sample, byte algorithm) {
		List<IVoxyPlayer> filtered = players.filter(player -> filter(source, player));
		VoxyPlayerSpeakingPreEvent preEvent = new VoxyPlayerSpeakingPreEvent(source.getExternal(), filtered, sample, algorithm);
		EventManager.callEvent(preEvent, new VoxyPlayerSpeakingPostEvent(preEvent));
	}

	/**
	 * Check if the player shall receive the audio sample of the source player.
	 * 
	 * @param source The speaking player.
	 * @param player The player to filter.
	 * @return True if the player pass the filter, false otherwise.
	 */
	private boolean filter(VoxyPlayerImpl source, VoxyPlayerImpl player) {
		if (player.isDeaf())
			return false;

		if (player.equals(source))
			return playBack;

		if (source.isMuteBy(player))
			return false;

		return true;
	}
}
