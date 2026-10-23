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
import fr.pederobien.voxy.server.event.VoxyPlayerCoordinatesChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSpeakingEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereEnableChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerSphereRadiusChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent;
import fr.pederobien.voxy.server.event.VoxyPlayerVolumesChangedEvent.VolumeChange;
import fr.pederobien.voxy.server.interfaces.ISoundVolumes;
import fr.pederobien.voxy.server.interfaces.IVoxyEffect;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;

public class SoundManager implements IEventListener {
	private final PlayerListImpl players;
	private final HearTable hearTable;

	/**
	 * Creates a sound manager responsible to dispatch player's audio sample.
	 * 
	 * @param players The list of players in a room.
	 */
	public SoundManager(PlayerListImpl players) {
		this.players = players;

		hearTable = new HearTable();
		EventManager.registerListener(this);
	}

	/**
	 * Method called when a player is speaking.
	 * 
	 * @param source    The speaking player.
	 * @param sample    The bytes array that contains the audio sample.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public void onPlayerIsSpeaking(VoxyPlayerImpl source, byte[] sample, byte algorithm) {
		List<VoxyPlayerImpl> listeners = players.filter(player -> filter(source, player));

		for (VoxyPlayerImpl listener : listeners)
			listener.getVocalClient().onPlayerSpeaking(source.getName(), sample, algorithm);

		// Notifying that a player is speaking
		EventManager.callEvent(new VoxyPlayerSpeakingEvent(source.getExternal()));
	}

	/**
	 * Adds an effect on the audio stream of each speaking player registered in the same room as the listening player. If there is an
	 * effect already registered for the given name, then it is updated.
	 * 
	 * @param listener The listening player to notify.
	 * @param effect   The effect to add or update.
	 */
	public void addOrUpdateEffectForEachSpeakingPlayer(VoxyPlayerImpl listener, IVoxyEffect effect) {
		hearTable.addOrUpdateEffectForEachSpeakingPlayer(listener, effect);
	}

	/**
	 * Adds an effect on the audio stream of the speaking player for each listening player. If there is an effect already registered
	 * for the given name, then it is updated.
	 * 
	 * @param speaker The speaking player to notify.
	 * @param effect  The effect to add or update.
	 */
	public void addOrUpdateEffectForEachListeningPlayer(VoxyPlayerImpl speaker, IVoxyEffect effect) {
		hearTable.addOrUpdateEffectForEachListeningPlayer(speaker, effect);
	}

	/**
	 * Sends a request to the given listener to remove an effect on the audio stream of each player that are in the same room.
	 * 
	 * @param listener   The player that shall remove an effect on each audio stream it receives.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffectForEachSpeakingPlayer(VoxyPlayerImpl listener, String effectName) {
		hearTable.removeEffectForEachSpeakingPlayer(listener, effectName);
	}

	/**
	 * Sends a request to each player that are in the same room as the given speaking player to remove an effect on the audio stream
	 * of speaking player.
	 * 
	 * @param speaker    The player for which an effect shall be applied on the audio stream.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffectForEachListeningPlayer(VoxyPlayerImpl speaker, String effectName) {
		hearTable.removeEffectForEachListeningPlayer(speaker, effectName);
	}

	@EventHandler
	private void onPlayerJoinedRoom(JoinRoomPostEvent event) {
		if (event.getRoom() != players.getRoomImpl().getExternal())
			return;

		hearTable.register(players.getByName(event.getPlayer().getName()));
	}

	@EventHandler
	private void onPlayerLeftRoom(LeaveRoomPostEvent event) {
		if (event.getRoom() != players.getRoomImpl().getExternal())
			return;

		hearTable.unregister(event.getPlayer());
	}

	@EventHandler
	private void onPlayerCoordinatesChanged(VoxyPlayerCoordinatesChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		hearTable.computeVolumes(event.getPlayer(), event.headOnly());
	}

	@EventHandler
	private void onPlayerSoundSphereEnableChanged(VoxyPlayerSphereEnableChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		debug("%s's sound sphere %s", event.getPlayer().getSoundSphere().isEnabled() ? "enabled" : "disabled");
		hearTable.computeVolumes(event.getPlayer(), true);
	}

	@EventHandler
	private void onPlayerSoundSphereRadiusChanged(VoxyPlayerSphereRadiusChangedEvent event) {
		if (players.getByName(event.getPlayer().getName()) == null)
			return;

		IVoxyPlayer player = event.getPlayer();
		double xRadius = player.getSoundSphere().getXRadius();
		double yRadius = player.getSoundSphere().getYRadius();
		double zRadius = player.getSoundSphere().getZRadius();

		String format = "%s's sound sphere radius are now: [xRadius=%s, yRadius=%s, zRadius=%s]";
		debug(format, player.getName(), xRadius, yRadius, zRadius);

		hearTable.computeVolumes(player, true);
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

		if (speaker.isMuteBy(listener))
			return false;

		if (listener.equals(speaker))
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
		private final Map<IVoxyPlayer, SpeakingPlayer> speakingPlayers;
		private final Object lock;

		/**
		 * Creates a hear table. This table is responsible to indicate if a listening player can hear a speaking player.
		 */
		public HearTable() {
			speakingPlayers = new HashMap<IVoxyPlayer, SpeakingPlayer>();
			lock = new Object();
		}

		/**
		 * Registers a player to this hear table.
		 * 
		 * @param speaker The speaking player.
		 */
		public void register(VoxyPlayerImpl speaker) {
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(speaker.getExternal());
				if (speaking != null) {
					return;
				}

				speaking = new SpeakingPlayer(speaker);

				// Step 1: Computing sound volumes between the player to add and all the others
				for (Map.Entry<IVoxyPlayer, SpeakingPlayer> entry : speakingPlayers.entrySet()) {
					speaking.addListener(entry.getValue().getPlayer(), speaker.getSoundSphere().computeVolumes(entry.getKey()));
					entry.getValue().addListener(speaker, entry.getKey().getSoundSphere().computeVolumes(speaker.getExternal()));
				}

				// Step 2: Adding the player to list of players.
				speakingPlayers.put(speaker.getExternal(), speaking);
			}
		}

		/**
		 * Removes the given player from this table.
		 * 
		 * @param player The player to remove.
		 */
		public void unregister(IVoxyPlayer player) {
			synchronized (lock) {

				// Step 1: Removing the speaking player
				SpeakingPlayer speaking = speakingPlayers.remove(player);
				if (speaking == null)
					return;

				// Step 2: Clearing the list of listeners for this player
				speaking.clearListeners();

				// Step 3: Removing the player from the list of listeners of the other players.
				for (Map.Entry<IVoxyPlayer, SpeakingPlayer> entry : speakingPlayers.entrySet())
					entry.getValue().removeListener(speaking.getPlayer());
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
			SpeakingPlayer speaking;

			synchronized (lock) {
				speaking = speakingPlayers.get(speaker);
				if (speaking == null)
					return true;
			}

			ListeningPlayer listening = speaking.getListener(listener);
			if (listening == null)
				return true;

			return listening.getVolumes().getGlobal() > 0.0f;
		}

		/**
		 * Handler to execute when a player moved in the game.
		 * 
		 * @param player   The player that moved.
		 * @param headOnly True if the player moved only his head, false if it also moved in space.
		 */
		public void computeVolumes(IVoxyPlayer player, boolean headOnly) {
			List<VolumeChange> changes = null;

			// reciprocity principle: If players can listen to another player,
			// then theses players can speak to the player.
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(player);
				if (speaking == null)
					return;

				List<ListeningPlayer> listeners = speaking.getListeners();
				if (listeners.isEmpty())
					return;

				// Step 1: Computing how listeners hear the player, skipped if the player only moved his head
				if (!headOnly) {
					for (ListeningPlayer listening : listeners) {
						ISoundVolumes now = player.getSoundSphere().computeVolumes(listening.getPlayer());

						// Computed volumes changed significantly enough to be notified
						if (listening.setVolumes(now)) {
							if (changes == null)
								changes = new ArrayList<VolumeChange>();

							changes.add(new VolumeChange(player, listening.getPlayer(), now));
						}
					}
				}

				// Step 2: how the player hears its listeners (always — orientation may have changed)
				for (ListeningPlayer listening : listeners) {
					SpeakingPlayer listeningAsSpeaking = speakingPlayers.get(listening.getPlayer());
					if (listeningAsSpeaking == null)
						continue;

					ListeningPlayer playerAsListening = listeningAsSpeaking.getListener(player);
					if (playerAsListening == null)
						continue;

					ISoundVolumes now = listening.getPlayer().getSoundSphere().computeVolumes(player);

					// Computed volumes changed significantly enough to be notified
					if (playerAsListening.setVolumes(now)) {
						if (changes == null)
							changes = new ArrayList<VolumeChange>();

						changes.add(new VolumeChange(listening.getPlayer(), player, now));
					}
				}
			}

			if (changes != null)
				EventManager.callEvent(new VoxyPlayerVolumesChangedEvent(changes));
		}

		/**
		 * Adds an effect on the audio stream of each speaking player registered in the same room as the listening player. If there is an
		 * effect already registered for the given name, then it is updated.
		 * 
		 * @param listener The listening player to notify.
		 * @param effect   The effect to add or update..
		 */
		public void addOrUpdateEffectForEachSpeakingPlayer(VoxyPlayerImpl listener, IVoxyEffect effect) {
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(listener.getExternal());
				if (speaking == null)
					return;

				speaking.addOrUpdateEffectForEachSpeakingPlayer(effect);
			}
		}

		/**
		 * Adds an effect on the audio stream of the speaking player for each listening player. If there is an effect already registered
		 * for the given name, then it is updated.
		 * 
		 * @param speaker The speaking player to notify.
		 * @param effect  The effect to add or update..
		 */
		public void addOrUpdateEffectForEachListeningPlayer(VoxyPlayerImpl speaker, IVoxyEffect effect) {
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(speaker.getExternal());
				if (speaking == null)
					return;

				speaking.addOrUpdateEffectForEachListeningPlayer(effect);
			}
		}

		/**
		 * Sends a request to the given listener to remove an effect on the audio stream of each player that are in the same room.
		 * 
		 * @param listener   The player that shall remove an effect on each audio stream it receives.
		 * @param effectName The name of the effect to remove.
		 */
		public void removeEffectForEachSpeakingPlayer(VoxyPlayerImpl listener, String effectName) {
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(listener.getExternal());
				if (speaking == null)
					return;

				speaking.removeEffectForEachSpeakingPlayer(effectName);
			}
		}

		/**
		 * Sends a request to each player that are in the same room as the given speaking player to remove an effect on the audio stream
		 * of speaking player.
		 * 
		 * @param speaker    The player for which an effect shall be applied on the audio stream.
		 * @param effectName The name of the effect to remove.
		 */
		public void removeEffectForEachListeningPlayer(VoxyPlayerImpl speaker, String effectName) {
			synchronized (lock) {
				SpeakingPlayer speaking = speakingPlayers.get(speaker.getExternal());
				if (speaking == null)
					return;

				speaking.removeEffectForEachListeningPlayer(effectName);
			}
		}
	}

	private class ListeningPlayer {
		private static final double VOLUME_GAP = 0.01;
		private final VoxyPlayerImpl impl;
		private ISoundVolumes volumes;

		/**
		 * Creates a listener based on a player and a volumes.
		 * 
		 * @param impl    The player that listens another player.
		 * @param volumes The volumes (left, right, global) used to listen another player
		 */
		public ListeningPlayer(VoxyPlayerImpl impl, ISoundVolumes volumes) {
			this.impl = impl;
			this.volumes = volumes;
		}

		/**
		 * @return The implementation of the player.
		 */
		public VoxyPlayerImpl getPlayerImpl() {
			return impl;
		}

		/**
		 * @return The player that listens
		 */
		public IVoxyPlayer getPlayer() {
			return impl.getExternal();
		}

		/**
		 * @return The volumes with which this listener hears another player.
		 */
		public ISoundVolumes getVolumes() {
			return volumes;
		}

		/**
		 * Update the volumes (left, right and global) with which this listener hears another player.
		 * 
		 * @param volumes The volumes to use.
		 */
		public boolean setVolumes(ISoundVolumes volumes) {
			if (checkVolumeChange(this.volumes, volumes)) {
				this.volumes = volumes;
				return true;
			}

			return false;
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

	private class SpeakingPlayer {
		private final VoxyPlayerImpl impl;
		private final Map<IVoxyPlayer, ListeningPlayer> listeners;
		private final Map<String, IVoxyEffect> effectsForEachListeners;
		private final Map<String, IVoxyEffect> effectsForEachSpeakers;
		private final Object lock;

		/**
		 * Creates a speaking player.
		 * 
		 * @param impl The implementation of the player.
		 */
		public SpeakingPlayer(VoxyPlayerImpl impl) {
			this.impl = impl;
			listeners = new HashMap<IVoxyPlayer, ListeningPlayer>();
			effectsForEachListeners = new HashMap<String, IVoxyEffect>();
			effectsForEachSpeakers = new HashMap<String, IVoxyEffect>();
			lock = new Object();
		}

		/**
		 * @return The implementation of the underlying player.
		 */
		public VoxyPlayerImpl getPlayer() {
			return impl;
		}

		/**
		 * Adds the given listener to the list of listeners.
		 * 
		 * @param player  The player that listens another player.
		 * @param volumes The volumes (left, right and global) with which the listeners hears another player.
		 */
		public void addListener(VoxyPlayerImpl impl, ISoundVolumes volumes) {
			synchronized (lock) {
				if (listeners.get(impl.getExternal()) != null)
					return;

				listeners.put(impl.getExternal(), new ListeningPlayer(impl, volumes));

				// Notifying the new player how it shall hear this player
				for (Map.Entry<String, IVoxyEffect> entry : effectsForEachListeners.entrySet())
					impl.addOrUpdateEffect(getPlayer(), entry.getValue());

				// Notifying this player how it shall the new player
				for (Map.Entry<String, IVoxyEffect> entry : effectsForEachSpeakers.entrySet())
					getPlayer().addOrUpdateEffect(impl, entry.getValue());
			}
		}

		/**
		 * Removes each listener registered in the listeners list.
		 */
		public void clearListeners() {
			synchronized (lock) {
				listeners.clear();
			}
		}

		/**
		 * Removes the listener associated to the given from the list of listeners.
		 * 
		 * @param player The player to remove.
		 */
		public void removeListener(VoxyPlayerImpl player) {
			synchronized (lock) {
				listeners.remove(player.getExternal());

				// Clearing all effect between this player and the removed player
				getPlayer().clearEffects(player);
			}
		}

		/**
		 * @return A copy of the list of listening player.
		 */
		public List<ListeningPlayer> getListeners() {
			List<ListeningPlayer> list = new ArrayList<ListeningPlayer>();
			synchronized (lock) {
				for (Map.Entry<IVoxyPlayer, ListeningPlayer> entry : listeners.entrySet())
					list.add(entry.getValue());
			}

			return list;
		}

		/**
		 * Gets the listening player object associated to the given player.
		 * 
		 * @param player The player whose the listening player shall be retrieved.
		 * @return The ListeningPlayer object if registered, null otherwise.
		 */
		public ListeningPlayer getListener(IVoxyPlayer player) {
			synchronized (lock) {
				return listeners.get(player);
			}
		}

		/**
		 * Adds an effect on the audio stream of each speaking player registered in the same room as the listening player. If there is an
		 * effect already registered for the given name, then it is updated.
		 * 
		 * @param effect The effect to add or update.
		 */
		public void addOrUpdateEffectForEachSpeakingPlayer(IVoxyEffect effect) {
			synchronized (lock) {
				for (ListeningPlayer listening : listeners.values())
					getPlayer().addOrUpdateEffect(listening.getPlayerImpl(), effect);

				effectsForEachSpeakers.put(effect.getName(), effect);
			}
		}

		/**
		 * Adds an effect on the audio stream of the speaking player for each listening player. If there is an effect already registered
		 * for the given name, then it is updated.
		 * 
		 * @param effect The effect to add or update.
		 */
		public void addOrUpdateEffectForEachListeningPlayer(IVoxyEffect effect) {
			synchronized (lock) {
				for (ListeningPlayer listening : listeners.values())
					listening.getPlayerImpl().addOrUpdateEffect(getPlayer(), effect);

				effectsForEachListeners.put(effect.getName(), effect);
			}
		}

		/**
		 * Sends a request to the player to remove an effect on the audio stream of each player that are in the same room.
		 * 
		 * @param effectName The name of the effect to remove.
		 */
		public void removeEffectForEachSpeakingPlayer(String effectName) {
			synchronized (lock) {
				for (ListeningPlayer listening : listeners.values())
					getPlayer().removeEffect(listening.getPlayerImpl(), effectName);

				effectsForEachListeners.remove(effectName);
			}
		}

		/**
		 * Sends a request to each player that are in the same room as the given speaking player to remove an effect on the audio stream
		 * of speaking player.
		 * 
		 * @param effectName The name of the effect to remove.
		 */
		public void removeEffectForEachListeningPlayer(String effectName) {
			synchronized (lock) {
				for (ListeningPlayer listening : listeners.values())
					listening.getPlayerImpl().removeEffect(getPlayer(), effectName);

				effectsForEachListeners.remove(effectName);
			}
		}
	}
}
