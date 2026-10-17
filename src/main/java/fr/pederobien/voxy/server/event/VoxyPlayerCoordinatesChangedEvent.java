package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerCoordinatesChangedEvent extends VoxyPlayerEvent {
	private final boolean headOnly;

	/**
	 * Creates an event thrown when the coordinate of a player has changed.
	 * 
	 * @param player   The player's whose the coordinates have changed.
	 * @param position True if the player moved only his head, false if it also moved in space.
	 */
	public VoxyPlayerCoordinatesChangedEvent(IVoxyPlayer player, boolean headOnly) {
		super(player);
		this.headOnly = headOnly;
	}

	/**
	 * @return True if the player moved in one of the spatial direction.
	 */
	public boolean headOnly() {
		return headOnly;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("x=" + getPlayer().getCoordinates().getX());
		joiner.add("y=" + getPlayer().getCoordinates().getY());
		joiner.add("z=" + getPlayer().getCoordinates().getZ());
		joiner.add("yaw=" + getPlayer().getCoordinates().getYaw());
		joiner.add("pitch=" + getPlayer().getCoordinates().getPitch());
		joiner.add("roll=" + getPlayer().getCoordinates().getRoll());
		return String.format("%s_%s", getName(), joiner);
	}
}
