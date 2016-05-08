package ca.ubc.ece.salt.pangor.analysis.flow.factories;

import java.util.Map;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.ScriptNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.State;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Initializes the state from a JavaScript file AST.
 */
public class StateFactory {

	/**
	 * @param script The file under analysis.
	 * @return The initial state ς ∈ State := ρ x σ
	 */
	public static State createInitialState(ScriptNode script, Map<AstNode, CFG> cfgs) {
		Store store = StoreFactory.createInitialStore();
		Environment env = EnvironmentFactory.createInitialEnvironment(script, store, cfgs);
		return new State(store, env);
	}

}