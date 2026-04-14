package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.ISoundVolumes;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerVolumesChangedEvent extends VoxyPlayerEvent {
	private final IVoxyPlayer listener;
	private final ISoundVolumes volumes;

	/**
	 * Creates an event thrown when the audio volumes has changed between two players.
	 * 
	 * @param speaker  The speaking player.
	 * @param listener The listening player.
	 * @param volumes  The volumes with which the listening player shall hear the speaking player.
	 */
	public VoxyPlayerVolumesChangedEvent(IVoxyPlayer speaker, IVoxyPlayer listener, ISoundVolumes volumes) {
		super(speaker);

		this.listener = listener;
		this.volumes = volumes;
	}

	/**
	 * @return The listening player.
	 */
	public IVoxyPlayer getListener() {
		return listener;
	}

	/**
	 * @return The volumes with which the listening player shall hear the speaking player.
	 */
	public ISoundVolumes getVolumes() {
		return volumes;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("speaker=" + getPlayer().getName());
		joiner.add("listener=" + getListener().getName());

		String format = "volumes={left=%s,right=%s,global=%s}";
		joiner.add(String.format(format, getVolumes().getLeft(), getVolumes().getRight(), getVolumes().getGlobal()));
		return String.format("%s_%s", getName(), joiner);
	}
}
