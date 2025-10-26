package fr.pederobien.voxy.server.impl;

import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.VoxyPlayerDeafStatusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerMuteStatusChangedEvent;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyPlayer implements IVoxyPlayer {
	private final VoxyServer server;
	private final String name;
	private boolean isDeaf;
	private boolean isMute;

	/***
	 * Creates a player to move from one room to another.
	 *
	 * @param server The server on which the player is connected
	 * @param name   The player's name.
	 * @param isMute The player's mute status.
	 * @param isDeaf The player's deaf status.
	 */
	public VoxyPlayer(VoxyServer server, String name, boolean isMute, boolean isDeaf) {
		this.server = server;
		this.name = name;
		this.isMute = isMute;
		this.isDeaf = isDeaf;
	}

	@Override
	public IVoxyServer getServer() {
		return server;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isMute() {
		return isMute;
	}

	@Override
	public void setMute(boolean isMute) {
		if (this.isMute == isMute)
			return;

		this.isMute = isMute;
		info("Player %s %s itself", name, isMute ? "Muted" : "Unmuted");
		EventManager.callEvent(new VoxyPlayerMuteStatusChangedEvent(this, isMute));
	}

	@Override
	public boolean isDeaf() {
		return isDeaf;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VoxyPlayer))
			return false;

		VoxyPlayer other = (VoxyPlayer) obj;
		return name.equals(other.getName());
	}

	/**
	 * Set if this player is deaf. A VoxyPlayerDeafStatusChangedEvent is thrown to notify each connected client.
	 *
	 * @param isDeaf True if this player is deaf, false otherwise.
	 */
	protected void setDeaf(boolean isDeaf) {
		if (this.isDeaf == isDeaf)
			return;

		this.isDeaf = isDeaf;
		info("Player %s %s itself", name, isDeaf ? "Deaf" : "Undeaf");
		EventManager.callEvent(new VoxyPlayerDeafStatusChangedEvent(this, isDeaf));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String message, Object... args) {
		Logger.info("%s - %s", server, String.format(message, args));
	}
}
