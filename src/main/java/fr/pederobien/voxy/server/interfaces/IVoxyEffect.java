package fr.pederobien.voxy.server.interfaces;

import java.util.List;

public interface IVoxyEffect {

	public class ParameterDescription {
		private final String name;
		private final String unit;
		private final Class<?> clazz;

		/**
		 * Creates a parameter of an effect.
		 * 
		 * @param name  The name of the parameter.
		 * @param unit  The unit of the parameter.
		 * @param clazz The data type of the value of the parameter.
		 * @param value The value of the parameter.
		 */
		public ParameterDescription(String name, String unit, Class<?> clazz) {
			this.name = name;
			this.unit = unit;
			this.clazz = clazz;
		}

		/**
		 * @return The name of the parameter.
		 */
		public String getName() {
			return name;
		}

		/**
		 * @return The unit of the parameter.
		 */
		public String getUnit() {
			return unit;
		}

		/**
		 * @return The data type of the value of the parameter.
		 */
		public Class<?> getValueDataType() {
			return clazz;
		}
	}

	/**
	 * @return The name of the effect.
	 */
	String getName();

	/**
	 * Update the value of a parameter.
	 * 
	 * @param name  The name of the parameter to update.
	 * @param value The value of the parameter.
	 * @return True if the parameter has been updated, false otherwise.
	 */
	boolean update(String name, Object value);

	/**
	 * Update the value of a parameter.
	 * 
	 * @param name  The name of the parameter to update.
	 * @param value The value of the parameter.
	 * @return True if the parameter has been updated, false otherwise.
	 */
	boolean update(String name, String value);

	/**
	 * Get the description of a parameter associated to the given name.
	 * 
	 * @param name The name of the parameter.
	 * @return The description of the parameter associated to the given name or null if not registered.
	 */
	ParameterDescription getDescription(String name);

	/**
	 * @return A stand alone list containing the name of each parameters.
	 */
	List<String> getNames();
}
