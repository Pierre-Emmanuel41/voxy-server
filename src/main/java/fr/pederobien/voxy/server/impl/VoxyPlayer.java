package fr.pederobien.voxy.server.impl;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class VoxyPlayer implements IVoxyPlayer {
	private final VoxyServer server;
	private final String name;
	private final boolean isDeaf;
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
		this.isMute = isMute;
	}

	@Override
	public boolean isDeaf() {
		return isDeaf;
	}
}
