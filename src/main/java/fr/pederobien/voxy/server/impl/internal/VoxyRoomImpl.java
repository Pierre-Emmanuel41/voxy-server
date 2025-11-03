package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.utils.event.EventHandler;
import fr.pederobien.utils.event.EventManager;
import fr.pederobien.utils.event.IEventListener;
import fr.pederobien.voxy.server.event.RemoveRoomPostEvent;
import fr.pederobien.voxy.server.event.RenameRoomPostEvent;
import fr.pederobien.voxy.server.impl.VoxyRoom;
import fr.pederobien.voxy.server.interfaces.IVoxyRoom;

public class VoxyRoomImpl extends ServerElement implements IEventListener {
	private final VocalServer serverImpl;
	private final PlayerListImpl playersImpl;
	private String name;

	private final IVoxyRoom external;

	/**
	 * Creates the implementation of a voxy room.
	 * 
	 * @param server The server on which this room is created.
	 * @param name   The room name.
	 */
	protected VoxyRoomImpl(VoxyServerImpl server, String name) {
		super(server);

		this.name = name;

		serverImpl = new VocalServer(this);
		serverImpl.open();

		playersImpl = new PlayerListImpl(this);
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
		return serverImpl;
	}

	/**
	 * @return The implementation of the players list registered in this room.
	 */
	public PlayerListImpl getPlayers() {
		return playersImpl;
	}

	/**
	 * @return The room to be used externally.
	 */
	public IVoxyRoom getExternal() {
		return external;
	}

	@EventHandler
	private void onRoomRemoved(RemoveRoomPostEvent event) {
		if (event.getRoom() != this)
			return;

		serverImpl.close();
		serverImpl.dispose();
	}
}
