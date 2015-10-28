package ca.ubc.ece.salt.pangor.analysis.classify;

import ca.ubc.ece.salt.pangor.analysis.Alert;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;

/**
 * Stores an alert that notifies the user about a repair that has been found
 * by a checker.
 *
 * The alert includes meta information. The meta information (e.g., project,
 * commit #, file names, etc.) is used to investigate classification.
 */
public abstract class ClassifierAlert extends Alert {

	/** The alert type. **/
	protected String type;

	/** The alert subtype. **/
	protected String subtype;

	/** The function this alert was found in. **/
	protected String functionName;

	/**
	 * An alert is always associated a concrete Checker.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 */
	public ClassifierAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange,
			String functionName, String type, String subtype) {
		super(commit, sourceCodeFileChange);
		this.functionName = functionName;
		this.type = type;
		this.subtype = subtype;
	}

	/**
	 * An alert is always associated a concrete Checker. This constructor
	 * should only be used if making a ClassifierAlert from serial. Otherwise
	 * the other constructor should be used so the ID is automatically
	 * generated.
	 * @param commit
	 * @param sourceCodeFileChange
	 * @param type The checker which generated the alert.
	 * @param subtype A checker may detect more than one repair subtype.
	 * @param id The unique id for the alert.
	 */
	public ClassifierAlert(Commit commit, SourceCodeFileChange sourceCodeFileChange, String functionName, String type, String subtype, int id) {
		super(commit, sourceCodeFileChange, id);
		this.functionName = functionName;
		this.type = type;
		this.subtype = subtype;
	}

	/**
	 * This method serializes the alert. This is useful when writing
	 * a data set to the disk.
	 * @return The serialized version of the alert.
	 */
	public String serialize() {

		String serialized = id + "," + this.commit.projectID + "," + this.commit.projectHomepage
				+ "," + this.commit.buggyCommitID + "," + this.commit.repairedCommitID
				+ "," + this.sourceCodeFileChange.buggyFile + "," + this.sourceCodeFileChange.repairedFile
				+ "," + this.functionName + "," + this.type + "," + this.subtype
				+ "," + this.getLongDescription() + "," + this.getAlertExplanation();

		return serialized;

	}

	/**
	 * The type of the alert.
	 * @return
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * The subtype of the alert.
	 * @return
	 */
	public String getSubType() {
		return this.subtype;
	}

	/**
	 * The type/subtype identifier.
	 * @return
	 */
	public String getIdentifier() {
		return this.type + "_" + this.subtype;
	}

	/**
	 * A short description of the repair alert.
	 * @return The alert type and subtype.
	 */
	public String getShortDescription() {
		return this.type + "_" + this.subtype;
	}

	/**
	 * The long description of the repair alert including the identifier (type
	 * and subtype) and specific details about the alert (e.g. variable names,
	 * locations, etc.).
	 * @return The repair type, subtype and description.
	 */
	public String getLongDescription() {
		return this.getAlertDescription();
	}

	/**
	 * Returns specific details about the alert (e.g., variable names,
	 * locations, etc.).
	 * @return The long description of the alert.
	 */
	protected abstract String getAlertDescription();

	/**
	 * Returns a description of the repair detected by the checker.
	 */
	protected abstract String getAlertExplanation();

	@Override
	public boolean equals(Object o) {
		if(o instanceof ClassifierAlert) {
			ClassifierAlert a = (ClassifierAlert) o;
			return this.type.equals(a.type) && this.subtype.equals(a.subtype);
		}
		return false;
	}

	@Override
	public String toString() {
		return this.type + "_" + this.subtype;
	}

	@Override
	public int hashCode() {
		return this.getLongDescription().hashCode();
	}

}