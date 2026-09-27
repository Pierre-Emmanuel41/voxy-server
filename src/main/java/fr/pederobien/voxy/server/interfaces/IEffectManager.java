package fr.pederobien.voxy.server.interfaces;

import java.util.List;

public interface IEffectManager {

	/**
	 * Retrieve the effect associated to the given name. The source of effects of this manager is defined by effects manager in
	 * package fr.pederobien.voxy.common.impl.effects. If an effect is not present in this manager, check if the effect is missing in
	 * the low level effect manager.
	 * 
	 * @param name The name of the effect to retrieve.
	 * @return The effect associated to the given name or null if there is no effect registered for the given name.
	 */
	IVoxyEffect get(String name);

	/**
	 * @return A list that contains all effects registered.
	 */
	List<IVoxyEffect> getEffects();
}
