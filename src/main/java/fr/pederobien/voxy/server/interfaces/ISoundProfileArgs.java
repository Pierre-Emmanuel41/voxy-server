package fr.pederobien.voxy.server.interfaces;

import fr.pederobien.voxy.server.impl.internal.LocalCoordinates;

public interface ISoundProfileArgs {

	/**
	 * @return The sound sphere involved in audio volumes computation.
	 */
	ISoundSphere getSoundSphere();

	/**
	 * @return The name of the player that is listening.
	 */
	String getListener();

	/**
	 * @return The name of the player this is speaking.
	 */
	String getSpeaker();

	/**
	 * @return The coordinates of the speaking player relative to the coordinates of the listening player.
	 */
	LocalCoordinates getLocalCoordinates();

	/**
	 * @return The distance between the speaking player and the listening player normalized with the sound sphere radius.
	 */
	double getDistance();
}
