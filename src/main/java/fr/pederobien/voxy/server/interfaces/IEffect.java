package fr.pederobien.voxy.server.interfaces;

import java.util.Map;

public interface IEffect {

	/**
	 * @return The name of the effect associated to this holder.
	 */
	String getEffectName();

	/**
	 * @return A map that gather effect parameter's name / parameter's value.
	 */
	Map<String, Object> getParametersMap();
}
