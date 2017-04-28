package commitminer.js.diff.ast;

import java.util.LinkedList;
import java.util.List;

import commitminer.analysis.CommitAnalysis;
import commitminer.analysis.DataSet;
import commitminer.analysis.factories.ICommitAnalysisFactory;
import commitminer.analysis.factories.IDomainAnalysisFactory;

public class AstCommitAnalysisFactory implements ICommitAnalysisFactory {

	private DataSet dataSet;

	public AstCommitAnalysisFactory(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	@Override
	public CommitAnalysis newInstance() {
		List<IDomainAnalysisFactory> domainFactories = new LinkedList<IDomainAnalysisFactory>();
		domainFactories.add(new AstDomainAnalysisFactory());
		return new CommitAnalysis(dataSet, domainFactories);
	}

}