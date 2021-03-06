package commitminer.analysis.flow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.SourceCodeFileAnalysis;
import commitminer.analysis.SourceCodeFileChange;
import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Helpers;
import commitminer.analysis.flow.abstractdomain.State;
import commitminer.analysis.flow.factories.StateFactory;
import commitminer.cfg.CFG;
import commitminer.cfg.ICFGVisitorFactory;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;

/**
 * Performs a flow analysis on the publicly accessible functions in the
 * source code file.
 */
public class ScriptFlowAnalysis extends SourceCodeFileAnalysis {
	
	/** The timeout value. **/
	private static final long TIMEOUT = 60;

	private List<ICFGVisitorFactory> cfgVisitorFactories;

	/**
	 * @param cfgVisitors extract facts from the CFG after the analysis is complete.
	 */
	public ScriptFlowAnalysis(List<ICFGVisitorFactory> cfgVisitorFactories) {
		this.cfgVisitorFactories = cfgVisitorFactories;
	}

	@Override
	public void analyze(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts, ClassifiedASTNode root,
			List<CFG> cfgs) throws Exception {
		
		/* Start a timer for this analysis. We don't want to run forever. */
		StopWatch timer = new StopWatch();
		timer.start();
		
		/* Build a map of AstNodes to CFGs. Used for inter-proc CFA. */
		Map<AstNode, CFG> cfgMap = new HashMap<AstNode, CFG>();
		for(CFG cfg : cfgs) {
			cfg.attachTimer(timer, TIMEOUT);
			cfgMap.put((AstNode)cfg.getEntryNode().getStatement(), cfg);
		}

		/* Create the initial state. */
		State state = StateFactory.createInitialState((ScriptNode) root, cfgMap);
		
		/* Perform the initial analysis and get the publicly accessible methods. */
		state = Helpers.run(cfgMap.get(root), state);

		/* Analyze the publicly accessible methods that weren't analyzed in
		 * the main analysis.
		 * NOTE: Only one level deep. Does not recursively check constructors. */
		Helpers.analyzeEnvReachable(state, state.env.environment, state.selfAddr, cfgMap, new HashSet<Address>(), null);
		
		/* Done with the timer. */
		timer.stop();

		/* Generate facts from the results of the analysis. */
		for(CFG cfg : cfgs) {
			for(ICFGVisitorFactory cfgVF : cfgVisitorFactories) {
				cfg.accept(cfgVF.newInstance(sourceCodeFileChange, facts));
			}
		}

	}

}