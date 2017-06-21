package commitminer.analysis.flow.abstractdomain;

import commitminer.js.annotation.DependencyIdentifier;

/**
 * A variable identifier combined with a change lattice.
 */
public class Identifier implements DependencyIdentifier {

	public Integer definerID;
	public String name;
	public Change change;

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 */
	public Identifier(Integer definerID, String name) {
		this.definerID = definerID;
		this.name = name;
		this.change = Change.bottom();
	}

	/**
	 * Use for standard lookup operations when the change type does not matter.
	 * @param name The name of the identifier to inject.
	 * @param change How the identifier was changed.
	 */
	public Identifier(Integer definerID, String name, Change change) {
		this.definerID = definerID;
		this.name = name;
		this.change = change;
	}

	@Override
	public int hashCode() {
		return this.name.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof Identifier)) return false;
		Identifier right = (Identifier)o;
		return this.name.equals(right.name);
	}

	@Override
	public String getAddress() {
		return definerID.toString();
	}

}