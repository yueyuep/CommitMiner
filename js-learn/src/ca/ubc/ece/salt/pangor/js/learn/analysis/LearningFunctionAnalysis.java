package ca.ubc.ece.salt.pangor.js.learn.analysis;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.ChangeType;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;
import ca.ubc.ece.salt.pangor.js.analysis.FunctionAnalysis;
import ca.ubc.ece.salt.pangor.js.analysis.scope.Scope;
import ca.ubc.ece.salt.pangor.pointsto.PointsToPrediction;

public class LearningFunctionAnalysis extends FunctionAnalysis {

	/** True if this is a destination file analysis. **/
	private boolean dst;

	public LearningFunctionAnalysis(boolean dst) {
		this.dst = dst;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, CFG cfg, Scope<AstNode> scope,
			PointsToPrediction model)
			throws Exception {

		/* Initialize the points-to analysis. It may take some time to build the package model.
		 *
		 *  NOTE: In the future, it might be useful to put this inside
		 *  	  ScopeAnalysis so all analyses have access to detailed
		 *   	  points-to info (for APIs at least). */

		/* TODO: Create a JS function that visits a file and extracts a HashMap<KeywordUse, Integer> of the model. */
//		PointsToPrediction packageModel = new PointsToPrediction(JSAPIFactory.buildTopLevelAPI(),
//				/*TODO:*/new HashMap<KeywordUse, Integer>());
		//APIModelVisitor.getScriptFeatureVector((ScriptNode)scope.getScope());
		//PointsToPrediction packageModel = null;

		/* If the function was inserted or deleted, there is nothing to do. We
		 * only want functions that were repaired. Class-level repairs are left
		 * for later. */
		if(scope.getScope().getChangeType() != ChangeType.INSERTED &&
		   scope.getScope().getChangeType() != ChangeType.REMOVED) {

           /* Visit the function to extract features. */
			LearningAnalysisVisitor.getLearningFacts(facts, sourceCodeFileChange,
					(ScriptNode)scope.getScope(), model, this.dst);

		}

	}

}