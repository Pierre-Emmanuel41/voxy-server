package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerCoordinateChangedEvent extends VoxyPlayerEvent {
	private final boolean position;
	private final boolean head;

	/**
	 * Creates an event thrown when the coordinate of a player has changed.
	 * 
	 * @param player   The player's whose the coordinates have changed.
	 * @param position True if the player moved in one of the spatial direction.
	 * @param head     True if the player moved his head in one of the spatial direction.
	 */
	public VoxyPlayerCoordinateChangedEvent(IVoxyPlayer player, boolean position, boolean head) {
		super(player);
		this.position = position;
		this.head = head;
	}

	/**
	 * @return True if the player moved in one of the spatial direction.
	 */
	public boolean positionChanged() {
		return position;
	}

	/**
	 * @return True if the player moved his head in one of the spatial direction.
	 */
	public boolean headMoved() {
		return head;
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
