package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.server.event.JoinRoomPostEvent;
import fr.pederobien.voxy.server.event.LeaveRoomPostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerCoordinateChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSpeakingPostEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSpeakingPreEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereEnableChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereRadiusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent.VolumeChange;
import fr.pederobien.voxy.server.interfaces.ISoundVolumes;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class SoundManager implements IEventListener {
	private final PlayerListImpl players;
	private final HearTable hearTable;
	private boolean playback;

	/**
	 * Creates a sound manager responsible to dispatch player's audio sample.
	 * 
	 * @param players The list of players in a room.
	 */
	public SoundManager(PlayerListImpl players) {
		this.players = players;

		hearTable = new HearTable();
		playback = false;

		EventManager.registerListener(this);
	}

	/**
	 * Enable or disable the play back. When player back is enabled, a player will here back the audio samples sent to the server.
	 * 
	 * @param playback True to enable play back, false to disable.
	 */
	public void setPlayBack(boolean playback) {
		debug("Playback %s on room %s", playback ? "enabled" : "disabled", players.getRoomImpl().getName());
		this.playback = playback;
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

	@EventHandler
	private void onPlayerJoinedRoom(JoinRoomPostEvent event) {
		if (event.getRoom() != players.getRoomImpl().getExternal())
			return;

		// Step 1: Getting the list of players already registered in the room
		List<IVoxyPlayer> others = new ArrayList<IVoxyPlayer>();
		for (IVoxyPlayer player : players.toList())
			if (player != event.getPlayer())
				others.add(player);

		// Step 2: Checking if players can hear each other and volumes.
		for (IVoxyPlayer other : others) {
			hearTable.register(other, event.getPlayer(), other.getSoundSphere().computeVolumes(event.getPlayer()));
			hearTable.register(event.getPlayer(), other, event.getPlayer().getSoundSphere().computeVolumes(other));
		}
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom() != players.getRoomImpl().getExternal())
			return;

		hearTable.unregister(event.getPlayer());
	}

	@EventHandler
	private void onPlayerCoordinatesChanged(VoxyPlayerCoordinateChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		hearTable.computeVolumes(event.getPlayer(), event.positionChanged());
	}

	@EventHandler
	private void onPlayerSoundSphereEnableChanged(VoxyPlayerSphereEnableChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		debug("%s's sound sphere %s", event.getPlayer().getSoundSphere().isEnabled() ? "enabled" : "disabled");
		hearTable.updatePlayerVolumes(event.getPlayer());
	}

	@EventHandler
	private void onPlayerSoundSphereRadiusChanged(VoxyPlayerSphereRadiusChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		IVoxyPlayer player = event.getPlayer();
		double xRadius = event.getPlayer().getSoundSphere().getXRadius();
		double yRadius = event.getPlayer().getSoundSphere().getYRadius();
		double zRadius = event.getPlayer().getSoundSphere().getZRadius();

		String format = "%s's sound sphere radius are now: [xRadius=%s, yRadius=%s, zRadius=%s]";
		debug(format, player.getName(), xRadius, yRadius, zRadius);

		hearTable.updatePlayerVolumes(player);
	}

	/**
	 * Check if the player shall receive the audio sample of the source player.
	 * 
	 * @param speaker  The speaking player.
	 * @param listener The player to filter.
	 * @return True if the player pass the filter, false otherwise.
	 */
	private boolean filter(VoxyPlayerImpl speaker, VoxyPlayerImpl listener) {
		if (listener.isDeaf())
			return false;

		if (listener.equals(speaker))
			return playback;

		if (speaker.isMuteBy(listener))
			return false;

		return hearTable.canHear(speaker.getExternal(), listener.getExternal());
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	private void debug(String format, Object... args) {
		Logger.debug(3, format, args);
	}

	private class HearTable {
		private static final double VOLUME_GAP = 0.01;
		private final Map<IVoxyPlayer, Map<IVoxyPlayer, ISoundVolumes>> table;
		private final Object lock;

		/**
		 * Creates a hear table. This table is responsible to indicate if a listening player can hear a speaking player.
		 */
		public HearTable() {
			table = new HashMap<IVoxyPlayer, Map<IVoxyPlayer, ISoundVolumes>>();

			lock = new Object();
		}

		/**
		 * Registers a canHear status for the given speaker and listener.
		 * 
		 * @param speaker  The speaking player.
		 * @param listener The listening player.
		 * @param canHear  True if the listening player can hear the speaking player.
		 */
		public void register(IVoxyPlayer speaker, IVoxyPlayer listener, ISoundVolumes volumes) {
			synchronized (lock) {
				Map<IVoxyPlayer, ISoundVolumes> listeners = table.get(speaker);
				if (listeners == null) {
					listeners = new HashMap<IVoxyPlayer, ISoundVolumes>();
					table.put(speaker, listeners);
				}

				// Listener shall be unique
				if (listeners.containsKey(listener))
					return;

				listeners.put(listener, volumes);
			}
		}

		/**
		 * Removes the given from this table.
		 * 
		 * @param player The player to remove.
		 */
		public void unregister(IVoxyPlayer player) {
			synchronized (lock) {
				table.remove(player);

				for (Map.Entry<IVoxyPlayer, Map<IVoxyPlayer, ISoundVolumes>> entry : table.entrySet())
					entry.getValue().remove(player);
			}
		}

		/**
		 * Indicates if a player can hear another player.
		 * 
		 * @param speaker  The speaking player.
		 * @param listener The listening player.
		 * @return True if the listening player can hear the speaking player, false otherwise.
		 */
		public boolean canHear(IVoxyPlayer speaker, IVoxyPlayer listener) {
			synchronized (lock) {
				Map<IVoxyPlayer, ISoundVolumes> listeners = table.get(listener);
				if (listeners == null)
					return false;

				ISoundVolumes volumes = listeners.get(speaker);
				return volumes == null ? false : volumes.getGlobal() != 0;
			}
		}

		/**
		 * Handler to execute when a player moved in the game.
		 * 
		 * @param player   The player that moved.
		 * @param position True if the player moved in one of the spatial direction.
		 */
		public void computeVolumes(IVoxyPlayer player, boolean position) {
			synchronized (lock) {

				// Step 1: Updating the volumes for the other players if player's position has changed and their sound sphere is enabled
				if (position) {
					for (Map.Entry<IVoxyPlayer, Map<IVoxyPlayer, ISoundVolumes>> entry : table.entrySet()) {
						if (entry.getValue().containsKey(player) && entry.getKey().getSoundSphere().isEnabled()) {
							ISoundVolumes before = entry.getValue().get(player);
							ISoundVolumes now = entry.getKey().getSoundSphere().computeVolumes(player);

							// Checking if volumes has changed enough to notify the client
							if (checkVolumeChange(before, now)) {
								entry.getValue().put(player, now);

								EventManager.callEvent(new VoxyPlayerVolumesChangedEvent(new VolumeChange(player, entry.getKey(), now)));
							}
						}
					}
				}

				// Step 2: Updating volumes for this player if its sound sphere is enabled
				if (player.getSoundSphere().isEnabled())
					updatePlayerVolumes(player);
			}
		}

		/**
		 * Compute the volumes of the other player for the given player.
		 * 
		 * @param listener     The player for which the audio volumes of the other players shall be computed.
		 * @param sphereEnable True if the player's sound sphere is enabled, false otherwise.
		 */
		public void updatePlayerVolumes(IVoxyPlayer listener) {
			List<VolumeChange> changes = new ArrayList<VolumeChange>();
			synchronized (lock) {
				Map<IVoxyPlayer, ISoundVolumes> listeners = table.get(listener);
				if (listeners == null)
					return;

				for (Map.Entry<IVoxyPlayer, ISoundVolumes> entry : listeners.entrySet()) {
					IVoxyPlayer speaker = entry.getKey();
					ISoundVolumes before = entry.getValue();
					ISoundVolumes now = listener.getSoundSphere().computeVolumes(speaker);

					// Checking if volumes has changed enough to notify the client
					if (checkVolumeChange(before, now)) {
						entry.setValue(now);
						changes.add(new VolumeChange(speaker, listener, now));
					}
				}
			}

			if (!changes.isEmpty())
				EventManager.callEvent(new VoxyPlayerVolumesChangedEvent(changes));
		}

		/**
		 * Check if the actual audio volumes is significantly different from the previous audio volumes.
		 * 
		 * @param before The audio volumes of the previous check.
		 * @param now    The actual audio volumes.
		 * @return True if the audio volumes is significantly different from the previous audio volumes check.
		 */
		private boolean checkVolumeChange(ISoundVolumes before, ISoundVolumes now) {
			if (before == null)
				return false;

			double leftDiff = Math.abs(before.getLeft() - now.getLeft());
			double rightDiff = Math.abs(before.getRight() - now.getRight());
			double globalDiff = Math.abs(before.getGlobal() - now.getGlobal());

			return leftDiff > VOLUME_GAP || rightDiff > VOLUME_GAP || globalDiff > VOLUME_GAP;
		}
	}
}
