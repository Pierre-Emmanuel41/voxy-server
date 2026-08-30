package fr.pederobien.voxy.server.impl.effects;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import fr.pederobien.voxy.server.interfaces.IEffect;

public abstract class Effect implements IEffect {
	private final String name;
	private final Map<String, Object> map;

	/**
	 * Creates a holder for the parameters of an effect.
	 * 
	 * @param name The effect name.
	 */
	public Effect(String name) {
		this.name = name;
		map = new HashMap<String, Object>();
	}

	@Override
	public String getEffectName() {
		return name;
	}

	@Override
	public Map<String, Object> getParametersMap() {
		return map;
	}

	@Override
	public String toString() {
		StringJoiner joiner = new StringJoiner(",", "{", "}");
		joiner.add("effect=" + getEffectName());
		for (Map.Entry<String, Object> entry : map.entrySet())
			joiner.add(String.format("%s=%s", entry.getKey(), entry.getValue()));

		return joiner.toString();
	}

	/**
	 * Adds an entry to the underlying map.
	 * 
	 * @param name  The parameter's name.
	 * @param value The parameter's value.
	 */
	protected void add(String name, Object value) {
		map.put(name, value);
	}
}
