package ca.ubc.ece.salt.pangor.analysis.simple;

import java.util.List;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;
import ca.ubc.ece.salt.pangor.analysis.Commit;
import ca.ubc.ece.salt.pangor.analysis.Facts;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileAnalysis;
import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.CFG;

/**
 * Performs a simple analysis of a source code file. The analysis registers
 * a pattern for each file, which is the type name of the root node.
 */
public class SimpleSrcFileAnalysis extends SourceCodeFileAnalysis<SimpleAlert> {

	public SimpleSrcFileAnalysis() { }

	@Override
	public void analyze(Commit commit, SourceCodeFileChange sourceCodeFileChange, Facts<SimpleAlert> facts, ClassifiedASTNode root, List<CFG> cfgs) throws Exception {
		String pattern = "Source Root: " + root.getASTNodeType();
		facts.addPattern(new SimplePattern(commit, sourceCodeFileChange, pattern));
		facts.addPreCondition(new SimplePattern(commit, sourceCodeFileChange, pattern));
	}

}
