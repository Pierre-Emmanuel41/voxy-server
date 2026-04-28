package fr.pederobien.voxy.server.event;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import fr.pederobien.utils.event.Event;
import fr.pederobien.voxy.server.interfaces.ISoundVolumes;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class VoxyPlayerVolumesChangedEvent extends Event {

	public static class VolumeChange {
		private final IVoxyPlayer speaker;
		private final IVoxyPlayer listener;
		private final ISoundVolumes volumes;

		public VolumeChange(IVoxyPlayer speaker, IVoxyPlayer listener, ISoundVolumes volumes) {
			this.speaker = speaker;
			this.listener = listener;
			this.volumes = volumes;
		}

		/**
		 * @return The speaking player.
		 */
		public IVoxyPlayer getSpeaker() {
			return speaker;
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
	}

	private List<VolumeChange> changes;

	/**
	 * Creates an event thrown when the audio volumes has changed between two players.
	 * 
	 * @param changes A list that contains the new volumes to use for a listening player to hear the speaking player.
	 */
	public VoxyPlayerVolumesChangedEvent(List<VolumeChange> changes) {
		this.changes = changes;
	}

	/**
	 * Creates an event thrown when the audio volumes has changed between two players.
	 * 
	 * @param changes A list that contains the new volumes to use for a listening player to hear the speaking player.
	 */
	public VoxyPlayerVolumesChangedEvent(VolumeChange... changes) {
		this.changes = Arrays.asList(changes);
	}

	/**
	 * @return The listening player.
	 */
	public List<VolumeChange> getChanges() {
		return Collections.unmodifiableList(changes);
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		for (VolumeChange change : changes) {
			StringJoiner changeJoiner = new StringJoiner(",", "{", "}");
			changeJoiner.add("speaker=" + change.getSpeaker().getName());
			changeJoiner.add("listener=" + change.getListener().getName());

			String format = "volumes={left=%s,right=%s,global=%s}";
			double left = change.getVolumes().getLeft();
			double right = change.getVolumes().getRight();
			double global = change.getVolumes().getGlobal();
			changeJoiner.add(String.format(format, left, right, global));
			joiner.add(changeJoiner.toString());
		}

		return String.format("%s_%s", getName(), joiner);
	}
}
