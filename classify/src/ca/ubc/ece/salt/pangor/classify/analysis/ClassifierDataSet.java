package ca.ubc.ece.salt.pangor.classify.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.IKnowledgeBase;
import org.deri.iris.api.basics.IQuery;
import org.deri.iris.api.basics.IRule;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.DataSet;

/**
 * The {@code DataSet} manages the alerts that were generated during the
 * analysis.
 */
public class ClassifierDataSet extends DataSet {

	/**
	 * The path to the file where the data set will be cached. This allows us
	 * to limit our memory use and cache results for the future by storing the
	 * keyword extraction results on the disk.
	 */
	private String dataSetPath;

	/** The feature vectors generated by the analysis. **/
	private List<ClassifierFeatureVector> featureVectors;

	/**
	 * Used to produce a data set of the analysis results.
	 * @param dataSetPath The file path to store the data set.
	 * @param supplementaryPath The directory path to store the supplementary
	 * 		  files.
	 * @throws Exception Throws an exception when the {@code dataSetPath}
	 * 					 cannot be read.
	 */
	private ClassifierDataSet(String dataSetPath, List<IRule> rules,
							  List<IQuery> queries) {
		super(rules, queries);
		this.featureVectors = new LinkedList<ClassifierFeatureVector>();
		this.dataSetPath = dataSetPath;
	}

	/**
	 * Adds a feature vector to the data set. If a data set file exist
	 * ({@code dataSetPath}), serializes the feature vector and writes it to
	 * the file. Otherwise, the feature vector is stored in memory in
	 * {@code ClassifierDataSet}.
	 * @param commit The commit that is being analyzed.
	 * @param knowledgeBase The fact database to query.
	 */
	@Override
	protected void registerAlerts(Commit commit, IKnowledgeBase knowledgeBase)
			throws Exception {

		Map<String, ClassifierFeatureVector> featureVectors = new HashMap<String, ClassifierFeatureVector>();

		for(IQuery query : this.queries) {

			IRelation results = knowledgeBase.execute(query);

			/* Iterate through the tuples that are members of the relation and add
			 * them as alerts. */
			for(int i = 0; i < results.size(); i++) {

				ITuple tuple = results.get(i);

				/* Lookup or create the ClassifierFeatureVector. */
				String key = commit.projectID + "_" + commit.repairedCommitID 	// Identifies the commit
							 + "_" + tuple.get(0) + "_" + tuple.get(1); 		// Identifies the class/method
				ClassifierFeatureVector featureVector = featureVectors.get(key);

				/* Add the feature vector if it is not yet in the map. */
				if(featureVector == null) {
					featureVector = new ClassifierFeatureVector(commit,
							tuple.get(0).toString(), 	// Class
							tuple.get(1).toString(), 	// Method
							tuple.get(2).toString(), 	// Type
							tuple.get(3).toString(), 	// Subtype
							tuple.get(4).toString());	// Description
					featureVectors.put(key, featureVector);
				}

			}

		}

		/* Store the feature vectors. */
		for(ClassifierFeatureVector featureVector : featureVectors.values()) {
			if(this.dataSetPath != null) {
				try {
					this.storeClassifierFeatureVector(featureVector);
				} catch (Exception e) {
					System.err.println("Error while writing feature vector: " + e.getMessage());
				}
			}
			else {
				this.featureVectors.add(featureVector);
			}
		}

	}

	/**
	 * Import a data set from a file to this {@code ClassifierDataSet}.
	 * @param dataSetPath The file path where the data set is stored.
	 * @throws Exception Occurs when the data set file cannot be read.
	 */
	public void importDataSet(String dataSetPath) throws Exception {

		try(BufferedReader reader = new BufferedReader(new FileReader(dataSetPath))) {

			for (String serialClassifierFeatureVector = reader.readLine();
					serialClassifierFeatureVector != null;
					serialClassifierFeatureVector = reader.readLine()) {

				ClassifierFeatureVector featureVector = ClassifierFeatureVector.deSerialize(serialClassifierFeatureVector);

				this.featureVectors.add(featureVector);

			}

		}
		catch(Exception e) {
			throw e;
		}

	}

	/**
	 * Stores the feature vector in the file specified by {@code dataSetPath}.
	 * This method is synchronized because it may be used by several
	 * GitProjectAnalysis thread at the same time, which may cause race
	 * conditions when writing to the output file.
	 *
	 * @param featureVector The feature vector to be managed by this class.
	 */
	private synchronized void storeClassifierFeatureVector(ClassifierFeatureVector featureVector) throws Exception {

		/* The path to the file may not exist. Create it if needed. */
		File path = new File(this.dataSetPath);
		path.getParentFile().mkdirs();
		path.createNewFile();

		/* May throw IOException if the path does not exist. */
		PrintStream stream = new PrintStream(new FileOutputStream(path, true));

		/* Write the data set. */
		stream.println(featureVector.serialize());

		/* Finished writing the feature vector. */
		stream.close();

	}

}