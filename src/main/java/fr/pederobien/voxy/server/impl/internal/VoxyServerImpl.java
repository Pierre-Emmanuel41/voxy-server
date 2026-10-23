package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import fr.pederobien.communication.interfaces.server.IServerEthernetEndPoint;
import fr.pederobien.messenger.event.NewProtocolClientEvent;
import fr.pederobien.messenger.event.ProtocolServerCloseEvent;
import fr.pederobien.messenger.event.ProtocolServerDisposeEvent;
import fr.pederobien.messenger.event.ProtocolServerOpenEvent;
import fr.pederobien.messenger.impl.Messenger;
import fr.pederobien.messenger.impl.server.EthernetProtocolServerConfig;
import fr.pederobien.messenger.interfaces.server.IEthernetProtocolServer;
import fr.pederobien.protocol.interfaces.IProtocolManager;
import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.utils.event.Logger;
import fr.pederobien.voxy.common.impl.VoxyManagers;
import fr.pederobien.voxy.server.event.VoxyServerCloseEvent;
import fr.pederobien.voxy.server.event.VoxyServerDisposeEvent;
import fr.pederobien.voxy.server.event.VoxyServerOpenEvent;
import fr.pederobien.voxy.server.impl.EffectManager;
import fr.pederobien.voxy.server.impl.VoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyEffect;
import fr.pederobien.voxy.server.interfaces.IVoxyPlayer;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;
import fr.pederobien.voxy.server.interfaces.IVoxyServerConfig;

public class VoxyServerImpl implements IEventListener {
	private final IVoxyServerConfig config;
	private final IEthernetProtocolServer server;
	private final List<VoxyClient> clients;
	private final RoomListImpl rooms;
	private final EffectManager effectManager;
	private final Object lock;

	private final IVoxyServer external;

	/**
	 * Creates the implementation of a voxy server.
	 *
	 * @param name        The server's name.
	 * @param port        The port number to open.
	 * @param certificate The certificate to use to sign/authenticate requests.
	 */
	protected VoxyServerImpl(IVoxyServerConfig config) {
		this.config = config;

		String name = config.getName();
		IServerEthernetEndPoint endPoint = config.getTcpConfig().getPoint();
		IProtocolManager protocolManager = VoxyManagers.instance().getProtocolManager();
		EthernetProtocolServerConfig configuration = Messenger.createEthernetServerConfig(protocolManager, name, endPoint);
		configuration.setConnectionName(name);
		configuration.setConnectionMaxUnstableCounter(config.getTcpConfig().getConnectionMaxUnstableCounter());
		configuration.setConnectionHealTime(config.getTcpConfig().getConnectionHealTime());
		configuration.setLayerInitializer(config.getTcpConfig().getLayerInitializer());
		configuration.setClientValidator(config.getTcpConfig().getClientValidator());
		configuration.setServerMaxUnstableCounter(config.getTcpConfig().getServerMaxUnstableCounter());
		configuration.setServerHealTime(config.getTcpConfig().getServerHealTime());
		server = Messenger.createTcpServer(configuration);

		rooms = new RoomListImpl(this);
		clients = new ArrayList<VoxyClient>();
		effectManager = new EffectManager();
		lock = new Object();

		external = new VoxyServer(this);

		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return server.toString();
	}

	/**
	 * @return The voxy server name.
	 */
	public String getName() {
		return config.getName();
	}

	/**
	 * @return The configuration associated to this voxy server.
	 */
	public IVoxyServerConfig getConfig() {
		return config;
	}

	/**
	 * Open the server to let the clients connect.
	 * 
	 * @return True if the server is successfully opened, false otherwise.
	 */
	public boolean open() {
		AtomicBoolean success = new AtomicBoolean(server.open());
		if (success.get())
			rooms.foreach(room -> success.set(success.get() && room.getVocalServer().open()));

		return success.get();
	}

	/**
	 * Close the server, each player currently connected will be kicked.
	 * 
	 * @return True if the server is successfully closed, false otherwise.
	 */
	public boolean close() {
		AtomicBoolean success = new AtomicBoolean(server.close());

		if (success.get()) {

			// Notifying each client that the server is closing
			for (VoxyClient client : clients)
				client.onServerClosed();

			// Clearing the list of clients
			clients.clear();

			rooms.foreach(room -> success.set(success.get() && room.onServerClosed()));
		}

		return success.get();
	}

	/**
	 * Dispose the server, it cannot be re-opened anymore.
	 * 
	 * @return True if the server is successfully disposed, false otherwise.
	 */
	public boolean dispose() {
		AtomicBoolean success = new AtomicBoolean(server.dispose());
		if (success.get())
			rooms.foreach(room -> success.set(success.get() && room.getVocalServer().dispose()));

		return success.get();
	}

	/**
	 * @return True if the server is opened, false otherwise.
	 */
	public boolean isOpened() {
		return server.isOpened();
	}

	/**
	 * @return The port number this server is using. -1 if the server is not yet opened or has been closed.
	 */
	public int getPort() {
		return server.getPort();
	}

	/**
	 * @return True if this server is disposed, false otherwise.
	 */
	public boolean isDisposed() {
		return server.isDisposed();
	}

	/**
	 * @return The list of rooms associated to this server.
	 */
	public RoomListImpl getRooms() {
		return rooms;
	}

	/**
	 * @return A copy of the underlying list of players connected to the server.
	 */
	public List<IVoxyPlayer> getPlayers() {
		List<IVoxyPlayer> list = new ArrayList<IVoxyPlayer>();
		synchronized (lock) {
			for (VoxyClient client : clients)
				list.add(client.getPlayer().getExternal());
		}

		return list;
	}

	/**
	 * Get the player implementation associated to the given name.
	 * 
	 * @param name The player's name.
	 * 
	 * @return The associated implementation if registered, null otherwise.
	 */
	public VoxyPlayerImpl getPlayerByName(String name) {
		synchronized (lock) {
			for (VoxyClient client : clients)
				if (client.getPlayer().getName().equals(name))
					return client.getPlayer();
		}

		return null;
	}

	/**
	 * Get the implementation object of the given player interface.
	 * 
	 * @param player The interface of the player.
	 * @return The implementation associated to the given interface.
	 */
	public VoxyPlayerImpl getPlayerImpl(IVoxyPlayer player) {
		synchronized (lock) {
			for (VoxyClient client : clients)
				if (client.getPlayer().getExternal() == player)
					return client.getPlayer();
		}

		// Not found by reference, search by name
		return getPlayerByName(player.getName());
	}

	/**
	 * @return The object that manages effects.
	 */
	public EffectManager getEffectManager() {
		return effectManager;
	}

	/**
	 * Adds an effect on the audio stream of the speaking player. If there is an effect already registered for the given name, then it
	 * is updated.
	 * 
	 * @param listening The name of the listening player to notify.
	 * @param speaking  The name of the speaking player, ie the audio stream for which an effect shall be added or updated.
	 * @param effect    The effect to add or update..
	 */
	public void addOrUpdateEffect(IVoxyPlayer listening, IVoxyPlayer speaking, IVoxyEffect effect) {
		VoxyPlayerImpl listener = getPlayerImpl(listening);
		if (listener == null || listener.getRoom() == null)
			return;

		VoxyPlayerImpl speaker = getPlayerImpl(speaking);
		if (speaker == null || speaker.getRoom() == null || speaker.getRoom() != listener.getRoom())
			return;

		listener.addOrUpdateEffect(speaker, effect);
	}

	/**
	 * Removes an effect, if registered, on the audio stream of a player.
	 * 
	 * @param listening  The name of the player to notify.
	 * @param speaking   The name of the speaking player, ie the audio stream for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffect(IVoxyPlayer listening, IVoxyPlayer speaking, String effectName) {
		VoxyPlayerImpl listener = getPlayerImpl(listening);
		if (listener == null || listener.getRoom() == null)
			return;

		VoxyPlayerImpl speaker = getPlayerImpl(speaking);
		if (speaker == null || speaker.getRoom() == null || speaker.getRoom() != listener.getRoom())
			return;

		listener.removeEffect(speaker, effectName);
	}

	/**
	 * Removes all effects currently applied on the audio stream of the speaking player for the given listening player.
	 * 
	 * @param listening The player that shall remove all effects.
	 * @param speaking  The audio stream on which no effect shall be applied.
	 */
	public void clearEffects(IVoxyPlayer listening, IVoxyPlayer speaking) {
		VoxyPlayerImpl listener = getPlayerImpl(listening);
		if (listener == null || listener.getRoom() == null)
			return;

		VoxyPlayerImpl speaker = getPlayerImpl(speaking);
		if (speaker == null || speaker.getRoom() == null || speaker.getRoom() != listener.getRoom())
			return;

		listener.clearEffects(speaker);
	}

	/**
	 * Adds an effect on the audio stream of each speaking player registered in the same room as the listening player. If there is an
	 * effect already registered for the given name, then it is updated. The server tracks player joining / leaving the room in which
	 * the player is so that effect are added or removed accordingly.
	 * 
	 * @param listening The name of the listening player to notify.
	 * @param effect    The effect to add or update.
	 */
	public void addOrUpdateEffectForEachSpeakingPlayer(IVoxyPlayer listening, IVoxyEffect effect) {
		VoxyPlayerImpl listener = getPlayerImpl(listening);
		if (listener == null || listener.getRoom() == null)
			return;

		listener.getRoom().addOrUpdateEffectForEachSpeakingPlayer(listener, effect);
	}

	/**
	 * Adds an effect on the audio stream on the speaking player for each listening player. If there is an effect already registered
	 * for the given name, then it is updated. The server tracks player joining / leaving the room in which the player is so that
	 * effect are added or removed.
	 * 
	 * @param speaking The name of the speaking player whose the audio stream shall be modified by an effect.
	 * @param effect   The effect to add or update.
	 */
	public void addOrUpdateEffectForEachListeningPlayer(IVoxyPlayer speaking, IVoxyEffect effect) {
		VoxyPlayerImpl speaker = getPlayerImpl(speaking);
		if (speaker == null || speaker.getRoom() == null)
			return;

		speaker.getRoom().addOrUpdateEffectForEachListeningPlayer(speaker, effect);
	}

	/**
	 * Removes an effect on the audio stream of each speaking player registered in the same room as the listening player.
	 * 
	 * @param listening  The name of the player that shall remove an effect on each audio stream it receives.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffectForEachSpeakingPlayer(IVoxyPlayer listening, String effectName) {
		VoxyPlayerImpl listener = getPlayerImpl(listening);
		if (listener == null || listener.getRoom() == null)
			return;

		listener.getRoom().removeEffectForEachSpeakingPlayer(listener, effectName);
	}

	/**
	 * Removes an effect on the audio stream of the speaking player.
	 * 
	 * @param speaking   The name of the player for which an effect shall be removed.
	 * @param effectName The name of the effect to remove.
	 */
	public void removeEffectForEachListeningPlayer(IVoxyPlayer speaking, String effectName) {
		VoxyPlayerImpl speaker = getPlayerImpl(speaking);
		if (speaker == null || speaker.getRoom() == null)
			return;

		speaker.getRoom().removeEffectForEachListeningPlayer(speaker, effectName);
	}

	/**
	 * Removes the player associated to the given name.
	 * 
	 * @param player The player to remove.
	 */
	public void remove(VoxyPlayerImpl player) {
		synchronized (lock) {
			for (int i = 0; i < clients.size(); i++)
				if (clients.get(i).getPlayer() == player) {
					clients.remove(i);
					break;
				}
		}
	}

	/**
	 * Check if a player with the given name is registered on the server.
	 * 
	 * @param name The player's name to check.
	 * 
	 * @return True if the player is registered, false otherwise.
	 */
	public boolean isRegistered(String name) {
		return getPlayerByName(name) != null;
	}

	/**
	 * @return The voxy server to use externally.
	 */
	public IVoxyServer getExternal() {
		return external;
	}

	@EventHandler
	private void onNewClientConnected(NewProtocolClientEvent event) {
		if (event.getServer() != server)
			return;

		synchronized (lock) {
			VoxyClient client = new VoxyClient(this, event.getClient());
			Consumer<Boolean> callback = initialized -> {
				if (initialized) {
					info("Player %s joined the server", client.getPlayer().getName());
					clients.add(client);
				} else {
					info("Failure to initialize connection with client %s, disposing connection", client);
					client.dispose();
				}
			};

			// Adding delay to let the client be ready to handle server's initialization sequence
			try {
				int delay = 500;

				debug("Requiring player's properties in %s ms", delay);
				Thread.sleep(delay);

				client.initialize(callback);
			} catch (Exception e) {
				// Do nothing
			}
		}
	}

	@EventHandler
	private void onServerOpen(ProtocolServerOpenEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerOpenEvent(external));
	}

	@EventHandler
	private void onServerClose(ProtocolServerCloseEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerCloseEvent(external));
	}

	@EventHandler
	private void onServerDispose(ProtocolServerDisposeEvent event) {
		if (event.getServer() != server)
			return;

		EventManager.callEvent(new VoxyServerDisposeEvent(external));
	}

	/**
	 * Print a log using DEBUG level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void debug(String format, Object... args) {
		Logger.debug("%s - %s", this, String.format(format, args));
	}

	/**
	 * Print a log using INFO level
	 *
	 * @param message The message to print.
	 * @param args    The arguments of the message.
	 */
	protected void info(String format, Object... args) {
		Logger.info("%s - %s", this, String.format(format, args));
	}
}
