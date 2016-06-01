package ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.Map;

/**
 * The abstract domain for storing mappings from identifiers to addresses.
 * i.e. Environment# := String#->P(BValue# | Address#)
 *
 * Identifiers may be
 */
public class Environment {

	/** The possible memory address for each identifier. **/
	public Map<Identifier, Address> environment;

	/**
	 * Creates an empty environment.
	 */
	public Environment() {
		this.environment = new HashMap<Identifier, Address>();
	}

	/**
	 * Creates an environment from an existing set of addresses.
	 * @param env The environment to replicate.
	 */
	private Environment(Map<Identifier, Address> env) {
		this.environment = env;
	}

	@Override
	public Environment clone() {
		Map<Identifier, Address> map = new HashMap<Identifier, Address>(this.environment);
		return new Environment(map);
	}

	/**
	 * Retrieve a variable's address.
	 * @param x The variable.
	 * @return The store address of the var.
	 */
	public Address apply(Identifier x) {
		return this.environment.get(x);
	}

	/**
	 * Performs a strong update on a variable in the environment.
	 * @param variable The variable to update.
	 * @param address The address for the variable.
	 * @return The updated environment.
	 */
	public Environment strongUpdate(Identifier variable, Address address) {
		Map<Identifier, Address> map = new HashMap<Identifier, Address>(this.environment);
		map.put(variable, address);
		return new Environment(map);
	}

	/**
	 * Computes ρ ∪ ρ
	 * @param environment The environment to join with this environment.
	 * @return The joined environments as a new environment.
	 */
	public Environment join(Environment environment) {
		Environment joined = new Environment(this.environment);

		/* Because we are only using one address per variable, environments
		 * should be the same when joined.
		 *
		 * If we want to dynamically store unexpected variables (ie. those
		 * in the global scope from the included JS files), we can merge
		 * variables by merging the BValue they point to. */

		for(Map.Entry<Identifier, Address> entry : environment.environment.entrySet()) {
			if(!this.environment.containsKey(entry.getKey())
					|| this.environment.get(entry.getKey()) != entry.getValue()) {
				System.out.println(entry.getKey().name + ":" + entry.getKey().change + ":" + entry.getValue().addr);
				if(!this.environment.containsKey(entry.getKey())) System.out.println("Does not contain key.");
				else System.out.println(this.environment.get(entry.getKey()).addr);
				throw new Error("environments should be the same");
			}
		}
		return joined;
	}

}