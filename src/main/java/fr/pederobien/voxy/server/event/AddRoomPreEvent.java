package fr.pederobien.voxy.server.event;

import java.util.StringJoiner;

import fr.pederobien.utils.ICancellable;
import fr.pederobien.voxy.server.interfaces.ISource;
import fr.pederobien.voxy.server.interfaces.IVoxyServer;

public class AddRoomPreEvent extends VoxyServerEvent implements ICancellable {
	private final String name;
	private final int port;
	private final ISource source;
	private boolean isCancelled;

	/**
	 * Event thrown when a room is about to be added to the server.
	 *
	 * @param server The server on which the room is about to be added.
	 * @param name   The room's name to add.
	 * @param port   The port number to use for the vocal server.
	 * @param source The source that requires to add a room on the server.
	 */
	public AddRoomPreEvent(IVoxyServer server, String name, int port, ISource source) {
		super(server);

		this.name = name;
		this.port = port;
		this.source = source;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public void setCancelled(boolean isCancelled) {
		this.isCancelled = isCancelled;
	}

	/**
	 * @return The name of the room that is about to be added.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The port number to use for the vocal server.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @return The name of the source involved in this event.
	 */
	public ISource getSource() {
		return source;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("server=" + getServer());
		joiner.add("name=" + getName());
		joiner.add("source=" + getSource().getName());
		return String.format("%s_%s", super.getName(), joiner);
	}
}
