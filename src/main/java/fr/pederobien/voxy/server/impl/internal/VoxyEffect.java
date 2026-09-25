package fr.pederobien.voxy.server.impl.internal;

import java.util.ArrayList;
import java.util.List;

import fr.pederobien.voxy.common.impl.effects.EffectParameter;
import fr.pederobien.voxy.server.interfaces.IVoxyEffect;

public class VoxyEffect implements IVoxyEffect {
	private final fr.pederobien.voxy.common.impl.effects.Effect effect;

	/**
	 * Creates a wrapper for the given effect.
	 * 
	 * @param effect The effect to wrap.
	 */
	public VoxyEffect(fr.pederobien.voxy.common.impl.effects.Effect effect) {
		this.effect = effect;
	}

	@Override
	public String getName() {
		return effect.getName();
	}

	@Override
	public boolean update(String name, Object value) {
		EffectParameter parameter = effect.getParameter(name);
		if (parameter == null)
			return false;

		try {
			parameter.setValue(value);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public boolean update(String name, String value) {
		EffectParameter parameter = effect.getParameter(name);
		if (parameter == null)
			return false;

		try {
			parameter.fromString(value);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public ParameterDescription getDescription(String name) {
		EffectParameter parameter = effect.getParameter(name);
		if (parameter == null)
			return null;

		return new ParameterDescription(name, parameter.getUnit(), parameter.getValueDataType());
	}

	@Override
	public List<String> getNames() {
		List<String> names = new ArrayList<String>();
		for (EffectParameter parameter : effect.getParameters())
			names.add(parameter.getName());

		return names;
	}

	@Override
	public String toString() {
		return effect.toString();
	}

	/**
	 * @return The wrapped effect.
	 */
	fr.pederobien.voxy.common.impl.effects.Effect unwrap() {
		return effect;
	}
}
