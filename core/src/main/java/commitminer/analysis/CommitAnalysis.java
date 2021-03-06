package commitminer.analysis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import commitminer.analysis.factories.IDomainAnalysisFactory;

/**
 * Gathers facts about one commit from various domains and runs queries in
 * datalog against those facts to synthesize alerts.
 */
public class CommitAnalysis {

	/**
	 * The list of analyses that we will run on this commit. Each
	 * {@code DomainAnalysis} gathers facts for one domain.
	 */
	private List<IDomainAnalysisFactory> domainAnalysisFactories;

	/**
	 * @param dataSet The data set that will generate and store the alerts.
	 * @param domainAnalyses The domains to extract facts from.
	 */
	public CommitAnalysis(List<IDomainAnalysisFactory> domainAnalysisFactories) {
		this.domainAnalysisFactories = domainAnalysisFactories;
	}

	/**
	 * Analyzes the commit and creates alerts.
	 *
	 * The commit is analyzed by each domain analysis. A database of facts is
	 * stored and each domain analysis adds to the database.
	 * @param commit The commit we are analyzing.
	 * @throws Exception
	 */
	public void analyze(Commit commit) throws Exception {

		/* Initialize the fact base that will be filled by the domain analyses. */
		Map<IPredicate, IRelation> facts = new HashMap<IPredicate, IRelation>();

		/* Run each domain analysis on the commit. */
		for(IDomainAnalysisFactory domainAnalysisFactory : domainAnalysisFactories) {
			DomainAnalysis domainAnalysis = domainAnalysisFactory.newInstance();
			domainAnalysis.analyze(commit, facts);
		}

	}

}
