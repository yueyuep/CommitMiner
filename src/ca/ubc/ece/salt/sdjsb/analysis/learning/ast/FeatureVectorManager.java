package ca.ubc.ece.salt.sdjsb.analysis.learning.ast;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.ubc.ece.salt.sdjsb.analysis.learning.apis.Keyword;

/**
 * The {@code FeatureVectorManager} is a pre-processing step for data mining
 * and machine learning. {@code FeatureVectorManager} manages the feature
 * vectors that were generated during the AST analysis.
 *
 * Once all feature vectors have been built, they will contain some meta info
 * (commit {@link #clone()}, file, project, etc.) and zero or more
 * {@code Keyword}s (where a {@code Keyword} = name + context + package.
 *
 * The {@code FeatureVectorManager} filters out {@code FeatureVector}s that
 * aren't wanted (i.e., those that aren't related to a package we are
 * investigating) and features that are not used or hardly used.
 */
public class FeatureVectorManager {

	/**
	 * The packages we want to investigate. FeatureVectorManager
	 * filters out any FeatureVector which does not contain one of these
	 * packages.
	 */
	private List<String> packagesToExtract;

	/** An ordered list of the keywords to print in the feature vector. **/
	private Set<Keyword> keywords;

	/** The feature vectors generated by the AST analysis. **/
	private List<FeatureVector> featureVectors;

	public FeatureVectorManager(List<String> packagesToExtract) {
		this.packagesToExtract = packagesToExtract;
		this.keywords = new HashSet<Keyword>();
		this.featureVectors = new LinkedList<FeatureVector>();
	}

	/**
	 * Adds a feature vector.
	 * @param featureVector The feature vector to be managed by this class.
	 */
	public void registerFeatureVector(FeatureVector featureVector) {
		this.featureVectors.add(featureVector);
	}

	/**
	 * Builds the feature vector header by filtering out features (columns)
	 * that are not used or hardly used.
	 * @return The feature vector header as a CSV list.
	 */
	public String getFeatureVectorHeader() {

		String header = String.join("\t", "ID", "ProjectID", "BuggyFile",
				"RepairedFile", "BuggyCommitID", "RepairedCommitID",
				"FunctionName");

		for(Keyword keyword : this.keywords) {
			header += "\tInserted_" + keyword.toString();
			header += "\tRemoved_" + keyword.toString();
			header += "\tUpdated_" + keyword.toString();
			header += "\tUnchanged_" + keyword.toString();
		}

		return header;

	}

	/**
	 * Builds the feature vector by filtering out feature vectors (rows)
	 * that do not contain the packages specified in {@code packagesToExtract}.
	 * @return The data set as a CSV file.
	 */
	public String getFeatureVector() {

		String dataSet = "";

		for(FeatureVector featureVector : this.featureVectors) {
			dataSet += featureVector.getFeatureVector(keywords) + "\n";
		}

		return dataSet;

	}

	/**
	 * Performs pre-processing operations for data-mining. Specifically,
	 * filters out rows which do not use the specified packages and filters
	 * out columns which do not contain any data.
	 */
	public void preProcess() {

		/* Remove rows that do not reference the packages we are interested in. */

		List<FeatureVector> toRemove = new LinkedList<FeatureVector>();
		for(FeatureVector featureVector : this.featureVectors) {

			/* Check if the feature vector references the any of the interesting packages. */
			if(!containsInterestingPackages(featureVector.insertedKeywordMap.keySet()) &&
					!containsInterestingPackages(featureVector.removedKeywordMap.keySet()) &&
					!containsInterestingPackages(featureVector.updatedKeywordMap.keySet()) &&
					!containsInterestingPackages(featureVector.unchangedKeywordMap.keySet())) {

				/* Schedule this FeatureVector for removal. */
				toRemove.add(featureVector);

			}

		}

		for(FeatureVector featureVector : toRemove) {
			this.featureVectors.remove(featureVector);
		}

		/* Get the set of keywords from all the feature vectors. */

		for(FeatureVector featureVector : this.featureVectors) {
			for(Keyword keyword : featureVector.insertedKeywordMap.keySet()) keywords.add(keyword);
			for(Keyword keyword : featureVector.removedKeywordMap.keySet()) keywords.add(keyword);
			for(Keyword keyword : featureVector.updatedKeywordMap.keySet()) keywords.add(keyword);
			for(Keyword keyword : featureVector.unchangedKeywordMap.keySet()) keywords.add(keyword);
		}

	}

	/**
	 * @param keywords The keywords from a feature vector.
	 * @return True if the keyword set contains one or more keywords from the
	 * 		   one or more of the packages we are interested in.
	 */
	private boolean containsInterestingPackages(Set<Keyword> keywords) {
		for(Keyword keyword : keywords) {
			if (this.packagesToExtract.contains(keyword.api.getName()))
				return true;
		}
		return false;
	}

}
