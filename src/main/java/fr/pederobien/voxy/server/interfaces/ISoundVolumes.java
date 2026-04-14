package fr.pederobien.voxy.server.interfaces;

public interface ISoundVolumes {

	/**
	 * @return The sound volume of the left channel of a stereo audio stream.
	 */
	float getLeft();

	/**
	 * @return The sound volume of the right channel of a stereo audio stream.
	 */
	float getRight();

	/**
	 * @return The sound volume of both channels of a stereo audio stream.
	 */
	float getGlobal();
}
