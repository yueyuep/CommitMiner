package ca.ubc.ece.salt.pangor.analysis;

import java.util.List;
import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Gathers Datalog facts about changes to a source code file. This class should
 * be extended to analyze a file in a particular language.
 */
public abstract class SourceCodeFileAnalysis {

	/**
	 * Perform a single-file analysis.
	 * @param sourceCodeFileChange The source code file change information.
	 * @param facts Stores the facts (predicates and their relations) from this
	 * 				analysis.
	 * @param root The script.
	 * @param cfgs The list of CFGs in the script (one for each function plus
	 * 			   one for the script).
	 */
	public abstract void analyze(SourceCodeFileChange sourceCodeFileChange,
								 Map<IPredicate, IRelation> facts,
								 ClassifiedASTNode root,
								 List<CFG> cfgs) throws Exception;

}