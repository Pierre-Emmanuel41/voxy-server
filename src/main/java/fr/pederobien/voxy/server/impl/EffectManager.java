package fr.pederobien.voxy.server.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import fr.pederobien.voxy.common.impl.VoxyManagers;
import fr.pederobien.voxy.server.impl.internal.VoxyEffect;
import fr.pederobien.voxy.server.interfaces.IEffectManager;
import fr.pederobien.voxy.server.interfaces.IVoxyEffect;

public class EffectManager implements IEffectManager {
	private final Map<String, Supplier<IVoxyEffect>> effects;

	/**
	 * Creates an effect manager.
	 */
	public EffectManager() {
		effects = new HashMap<String, Supplier<IVoxyEffect>>();

		fr.pederobien.voxy.common.impl.effects.EffectManager manager = VoxyManagers.instance().getEffectManager();

		for (String name : manager.getEffects())
			register(name, () -> new VoxyEffect(manager.getEffect(name)));
	}

	@Override
	public IVoxyEffect get(String name) {
		Supplier<IVoxyEffect> supplier = effects.get(name);
		return supplier == null ? null : supplier.get();
	}

	@Override
	public List<IVoxyEffect> getEffects() {
		List<IVoxyEffect> list = new ArrayList<IVoxyEffect>();
		for (Map.Entry<String, Supplier<IVoxyEffect>> effect : effects.entrySet())
			list.add(effect.getValue().get());

		return list;
	}

	/**
	 * Register an effect supplier associated to an effect name.
	 * 
	 * @param name     The name of the effect.
	 * @param supplier The object that creates the effect.
	 * @return True if the supplier has been registered successfully, false otherwise.
	 */
	private boolean register(String name, Supplier<IVoxyEffect> supplier) {
		Supplier<IVoxyEffect> registered = effects.get(name);
		if (registered != null)
			return false;

		effects.put(name, supplier);
		return true;
	}
}
