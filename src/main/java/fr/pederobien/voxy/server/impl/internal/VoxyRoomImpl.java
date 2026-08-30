package fr.pederobien.voxy.server.impl.internal;

import java.util.function.Consumer;

import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPreEvent;
import fr.pederobien.voxy.server.impl.VoxyRoom;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyRoomImpl extends ServerElement implements IEventListener {
	private final VocalServer vocalServer;
	private final PlayerListImpl players;
	private final SoundManager soundManager;
	private String name;

	private final IVoxyRoom external;

	/**
	 * Creates the implementation of a voxy room.
	 * 
	 * @param server The server on which this room is created.
	 * @param name   The room name.
	 * @param port   The port number to use. If value is 0, then the server will use the UDP config.
	 */
	protected VoxyRoomImpl(VoxyServerImpl server, String name, int port) {
		super(server);

		this.name = name;

		vocalServer = new VocalServer(this, port);
		players = new PlayerListImpl(this);
		soundManager = new SoundManager(players);
		external = new VoxyRoom(this);

		EventManager.registerListener(this);
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * @return The room name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Opens the vocal server associated to this room if the voxy server is opened.
	 */
	public void initialize() {
		// Opening room's vocal server if and only if the voxy server is opened
		if (getServer().isOpened())
			vocalServer.open();
	}

	/**
	 * Throws a RenameRoomPreEvent associated to the given input parameters.
	 * 
	 * @param newName  The new room's name.
	 * @param source   The source that requires to rename a room.
	 * @param callback The action to execute with event cancellation status as input parameter.
	 * @return True if the event has been cancelled, false otherwise.
	 */
	public boolean raiseRenameRoomPreEvent(String newName, ISource source, Consumer<Boolean> callback) {
		RenameRoomPreEvent event = new RenameRoomPreEvent(external, newName, source);
		EventManager.callEvent(event);
		callback.accept(event.isCancelled());
		return event.isCancelled();
	}

	/**
	 * Set the name of this room.
	 *
	 * @param name The new room's name.
	 */
	public void setName(String name) {
		if (this.name.equals(name))
			return;

		String oldName = this.name;
		this.name = name;

		info("Rooms %s has been renamed as %s", oldName, name);
		EventManager.callEvent(new RenameRoomPostEvent(external, oldName));
	}

	/**
	 * @return The vocal server associated to this room implementation.
	 */
	public VocalServer getVocalServer() {
		return vocalServer;
	}

	/**
	 * @return The implementation of the players list registered in this room.
	 */
	public PlayerListImpl getPlayers() {
		return players;
	}

	/**
	 * @return The room to be used externally.
	 */
	public IVoxyRoom getExternal() {
		return external;
	}

	/**
	 * Method called when the vocal client of a player received an audio sample to dispatch.
	 * 
	 * @param source    The speaking player.
	 * @param sample    The bytes array that contains the audio sample.
	 * @param algorithm The algorithm used to compress the audio sample.
	 */
	public void onPlayerIsSpeaking(VoxyPlayerImpl source, byte[] sample, byte algorithm) {
		soundManager.onPlayerIsSpeaking(source, sample, algorithm);
	}

	/**
	 * Close the underlying vocal server and clear the players list.
	 * 
	 * @return True if the vocal server has been closed successfully.
	 */
	public boolean onServerClosed() {
		players.removeAll();
		return vocalServer.close();
	}

	@EventHandler
	private void onRoomRemoved(RemoveRoomPostEvent event) {
		if (event.getRoom() != this)
			return;

		vocalServer.close();
		vocalServer.dispose();
	}
}
