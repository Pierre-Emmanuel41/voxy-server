package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteByChangePostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangePostEvent;
import fr.pederobien.voxy.server.impl.VoxyPlayer;
import fr.pederobien.voxy.server.interfaces.ICoordinates;
import fr.pederobien.voxy.server.interfaces.ISoundSphere;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerImpl extends ServerElement {
	private final String name;
	private final List<VoxyPlayerImpl> muteByPlayers;
	private final ICoordinates coordinates;
	private final ISoundSphere soundSphere;
	private boolean isMute;
	private boolean isDeaf;
	private Object lock;

	private final IVoxyPlayer external;

	/***
	 * Creates the implementation of a VoxyPlayer.
	 *
	 * @param server The server on which the player is connected.
	 * @param name   The player's name.
	 * @param isMute The player's mute status.
	 * @param isDeaf The player's deaf status.
	 */
	public VoxyPlayerImpl(VoxyServerImpl server, String name, boolean isMute, boolean isDeaf) {
		super(server);

		this.name = name;
		this.isMute = isMute;
		this.isDeaf = isDeaf;

		lock = new Object();
		muteByPlayers = new ArrayList<VoxyPlayerImpl>();
		coordinates = new Coordinates(this);
		soundSphere = new SoundSphere(this);
		external = new VoxyPlayer(this);
	}

	/**
	 * @return The player's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return True if the player's microphone is enabled, false if it is disabled.
	 */
	public boolean isMute() {
		return isMute;
	}

	/**
	 * Set if this player is mute. A VoxyPlayerMuteStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isMute True if this player is mute, false otherwise.
	 */
	public void setMute(boolean isMute) {
		this.isMute = isMute;
		info("Player %s %s itself", name, isMute ? "muted" : "unmuted");
		EventManager.callEvent(new VoxyPlayerMuteStatusChangePostEvent(external, isMute));
	}

	/**
	 * Set if this player is muted/unmuted by the source player.
	 * 
	 * @param source The player who muted/unmuted this player
	 * @param isMute True if the source player muted this player, false otherwise.
	 */
	public void setMuteBy(VoxyPlayerImpl source, boolean isMute) {
		if (isMute)
			synchronized (lock) {
				muteByPlayers.add(source);
			}
		else
			synchronized (lock) {
				muteByPlayers.remove(source);
			}

		info("Player %s %s player %s", source.getName(), isMute ? "muted" : "unmuted", name);
		EventManager.callEvent(new VoxyPlayerMuteByChangePostEvent(source.getExternal(), external, isMute));
	}

	/**
	 * Check if this player is muted by another player.
	 * 
	 * @param other The player to check.
	 * 
	 * @return True if the other player muted this player, false otherwise.
	 */
	public boolean isMuteBy(VoxyPlayerImpl other) {
		synchronized (lock) {
			return muteByPlayers.contains(other);
		}
	}

	/**
	 * @return True if the player's speakers are enabled, false otherwise.
	 */
	public boolean isDeaf() {
		return isDeaf;
	}

	/**
	 * Set if this player is deaf. A VoxyPlayerDeafStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isDeaf True if this player is deaf, false otherwise.
	 */
	public void setDeaf(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		this.isDeaf = isDeaf;
		info("Player %s %s itself", name, isDeaf ? "deaf" : "undeaf");
		EventManager.callEvent(new VoxyPlayerDeafStatusChangedEvent(external, isDeaf));
	}

	/**
	 * @return The coordinate that represent the player location in game.
	 */
	public ICoordinates getCoordinates() {
		return coordinates;
	}

	/**
	 * @return The sound sphere associated to this player.
	 */
	public ISoundSphere getSoundSphere() {
		return soundSphere;
	}

	/**
	 * @return The player to be used externally.
	 */
	public IVoxyPlayer getExternal() {
		return external;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VoxyPlayerImpl))
			return false;

		VoxyPlayerImpl other = (VoxyPlayerImpl) obj;
		return name.equals(other.getName());
	}
}
