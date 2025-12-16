package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.voxy.server.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangePostEvent;
import fr.pederobien.voxy.server.impl.VoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerImpl extends ServerElement {
	private final String name;
	private boolean isMute;
	private boolean isDeaf;

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
