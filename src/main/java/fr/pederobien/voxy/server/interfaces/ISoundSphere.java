package fr.pederobien.voxy.server.interfaces;

public interface ISoundSphere {

	/**
	 * Enable or disable the sound sphere. If disabled, the {@link #computeVolumes(IVoxyPlayer)} methods returns 1 for each level.
	 * 
	 * @param isEnabled True to enable the sound sphere, false to disable it.
	 */
	void setEnabled(boolean isEnabled);

	/**
	 * @return True if this sphere is enabled (enable sound volumes computation), false otherwise.
	 */
	boolean isEnabled();

	/**
	 * Set the x radius of the sphere, if the x-distance between two players is greater than the given value, the players will not be
	 * able to hear each other.
	 * 
	 * @return The x-radius, the maximum x-distance between two players at which they can hear each other.
	 */
	double getXRadius();

	/**
	 * Set the y radius of the sphere, if the y-distance between two players is greater than the given value, the players will not be
	 * able to hear each other.
	 * 
	 * @return The y-radius, the maximum y-distance between two players at which they can hear each other.
	 */
	double getYRadius();

	/**
	 * Set the z radius of the sphere, if the z-distance between two players is greater than the given value, the players will not be
	 * able to hear each other.
	 * 
	 * @return The z-radius, the maximum z-distance between two players at which they can hear each other
	 */
	double getZRadius();

	/**
	 * Set the x,y,z-radius of this sound sphere, the x,y,z-radius represents the maximum distance on the x,y,z-axis between two
	 * players at which they can hear each other. The radius shall be strictly positive.
	 * 
	 * @param xRadius The x-radius, the maximum x-distance between two players at which they can hear each other.
	 * @param yRadius The y-radius, the maximum y-distance between two players at which they can hear each other.
	 * @param zRadius The z-radius, the maximum z-distance between two players at which they can hear each other.
	 */
	void setRadius(double xRadius, double yRadius, double zRadius);

	/**
	 * Set the sound profile of the left channel of a stereo audio stream.
	 * 
	 * @param leftProfile The profile to follow.
	 */
	void setLeftProfile(ISoundProfile leftProfile);

	/**
	 * Set the sound profile of the right channel of a stereo audio stream.
	 * 
	 * @param rightProfile The profile to follow.
	 */
	void setRightProfile(ISoundProfile rightProfile);

	/**
	 * Set the sound profile of both channels of a stereo audio stream.
	 * 
	 * @param globalProfile The profile to follow.
	 */
	void setGlobalProfile(ISoundProfile globalProfile);

	/**
	 * Check if the given player is inside this sound sphere. If so, then the left, right and global volumes are computed.
	 * 
	 * @param player The player whose coordinates shall be checked.
	 * @return An object that gather the computed left, right and global sound volumes.
	 */
	ISoundVolumes computeVolumes(IVoxyPlayer player);
}
