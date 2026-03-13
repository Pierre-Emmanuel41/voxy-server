package fr.pederobien.voxy.server.impl.internal;

import fr.pederobien.messenger.interfaces.IRequestMessage;
import fr.pederobien.messenger.interfaces.server.IProtocolClient;
import fr.pederobien.protocol.interfaces.IError;
import fr.pederobien.protocol.interfaces.IIdentifier;
import fr.pederobien.voxy.common.impl.VoxyErrors;
import fr.pederobien.voxy.common.impl.VoxyIdentifiers;
import fr.pederobien.voxy.common.impl.requests.AcknowledgementRequest;

public class ClientWrapper extends ServerElement {
	private final IProtocolClient client;

	/**
	 * Creates a wrapper for the given client.
	 * 
	 * @param client The client to wrap.
	 */
	public ClientWrapper(VoxyServerImpl server, IProtocolClient client) {
		super(server);
		this.client = client;
	}

	/**
	 * @return The wrapped client.
	 */
	protected IProtocolClient getClient() {
		return client;
	}

	/**
	 * Sends a request to the remote.
	 * 
	 * @param identifier The request's identifier.
	 * @param payload    The request's payload.
	 */
	protected void send(VoxyIdentifiers identifier, Object payload) {
		send(getRequest(identifier, payload));
	}

	/**
	 * Answers to the remote with ACKNOWLEDGMENT request identifier and CANCELLED error code.
	 * 
	 * @param messageID  The identifier of the message received from the remote.
	 * @param identifier The identifier to which acknowledging.
	 */
	protected void deny(int messageID, IIdentifier identifier, IError error) {
		acknowledgment(messageID, identifier, error);
	}

	/**
	 * Answers to the remote with ACKNOWLEDGMENT request identifier and CANCELLED error code.
	 * 
	 * @param messageID  The identifier of the message received from the remote.
	 * @param identifier The identifier to which acknowledging.
	 */
	protected void cancelled(int messageID, IIdentifier identifier) {
		acknowledgment(messageID, identifier, VoxyErrors.CANCELLED);
	}

	/**
	 * Answers to the remote with ACKNOWLEDGMENT request identifier and NO_ERROR error code.
	 * 
	 * @param messageID  The identifier of the message received from the remote.
	 * @param identifier The identifier to which acknowledging.
	 */
	protected void noError(int messageID, IIdentifier identifier) {
		acknowledgment(messageID, identifier, VoxyErrors.NO_ERROR);
	}

	/**
	 * Creates an acknowledgment request to send back to the client.
	 * 
	 * @param identifier The identifier to which acknowledging.
	 * @param error      The request error code.
	 * 
	 * @return The request message to send back to the client.
	 */
	protected IRequestMessage createAcknowledgementRequest(IIdentifier identifier, IError error) {
		return getRequest(VoxyIdentifiers.ACKNOWLEDGEMENT, error, new AcknowledgementRequest(identifier));
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param request The request to send to the remote.
	 */
	protected void send(IRequestMessage request) {
		if (client.getConnection() == null)
			return;

		try {
			client.getConnection().send(request);
		} catch (Exception e) {
			// IllegalStateException means connection has been closed
			if (!(e instanceof IllegalStateException))
				error("An exception occurred while sending a request to client %s, message: %s", client, e.getMessage());
		}
	}

	/**
	 * Send the given request to the remote.
	 *
	 * @param messageID The identifier of the message received from the remote.
	 * @param request   The request to send to the remote.
	 */
	protected void answer(int messageID, IRequestMessage request) {
		if (client.getConnection() == null)
			return;

		try {
			client.getConnection().answer(messageID, request);
		} catch (Exception e) {
			// IllegalStateException means connection has been closed
			if (!(e instanceof IllegalStateException))
				error("An exception occurred while answering to client %s, message: %s", client, e.getMessage());
		}
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param error      The request error.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, IError error, Object payload) {
		return client.getRequest(identifier, error, payload);
	}

	/**
	 * Creates a request associated to the given identifier, if supported by at least one protocol, and set its error code and
	 * payload.
	 *
	 * @param identifier The request identifier.
	 * @param payload    The request payload.
	 * @return The request ready to be sent to the server or null if the identifier is not supported.
	 */
	protected IRequestMessage getRequest(IIdentifier identifier, Object payload) {
		return getRequest(identifier, VoxyErrors.NO_ERROR, payload);
	}

	/**
	 * Parse the given bytes array to get the payload.
	 *
	 * @param data The raw bytes array to parse.
	 * @return The payload parsed, or null if a ClassCastException occurred.
	 */
	@SuppressWarnings("unchecked")
	protected <T> T parse(byte[] data) {
		try {
			return (T) client.parse(data).getPayload();
		} catch (ClassCastException e) {
			return null;
		} catch (Exception e) {
			error("An exception occurred while casting request payload: %s", e.getMessage());
			return null;
		}
	}

	/**
	 * Answers to the remote with ACKNOWLEDGMENT request identifier and the given error code.
	 * 
	 * @param messageID  The identifier of the message received from the remote.
	 * @param identifier The identifier to which acknowledging.
	 * @param error      The request error code.
	 */
	private void acknowledgment(int messageID, IIdentifier identifier, IError error) {
		answer(messageID, createAcknowledgementRequest(identifier, error));
	}
}
