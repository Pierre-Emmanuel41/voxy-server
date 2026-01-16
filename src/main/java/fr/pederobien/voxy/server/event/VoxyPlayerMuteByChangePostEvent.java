package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerMuteByChangePostEvent extends VoxyPlayerEvent {
	private IVoxyPlayer target;
	private final boolean isMute;

	/**
	 * Creates an event thrown when a source player muted/unmuted the target player.
	 * 
	 * @param source The source player that muted/unmuted the the target player.
	 * @param target The player that has been muted/unmuted by the source player.
	 * @param isMute True if the target player has been muted, false otherwise.
	 */
	public VoxyPlayerMuteByChangePostEvent(IVoxyPlayer source, IVoxyPlayer target, boolean isMute) {
		super(source);

		this.target = target;
		this.isMute = isMute;
	}

	/**
	 * @return The player that has been muted/unmuted by the source player.
	 */
	public IVoxyPlayer getTarget() {
		return target;
	}

	/**
	 * @return True if the target player has been muted by the source player, false otherwise.
	 */
	public boolean isMute() {
		return isMute;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("source=" + getPlayer().getName());
		joiner.add("target=" + getTarget().getName());
		joiner.add("isMute=" + isMute());
		return String.format("%s_%s", getName(), joiner);
	}
}
