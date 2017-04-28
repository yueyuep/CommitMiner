package commitminer.analysis.simple;

import java.util.LinkedList;
import java.util.List;

import com.github.gumtreediff.gen.TreeGenerator;
import com.github.gumtreediff.gen.js.RhinoTreeGenerator;
import commitminer.cfg.CFG;
import commitminer.cfg.CFGFactory;

import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode;

/**
 * A CFG factory for Java.
 * NOTE: This class is for testing only and does not build ASTs.
 */
public class SimpleCFGFactory implements CFGFactory {

	@Override
	public List<CFG> createCFGs(ClassifiedASTNode root) {

		/* Store the CFGs from all the functions. */
		List<CFG> cfgs = new LinkedList<CFG>();

		return cfgs;
	}

	/**
	 * @return The GumTree Tree generator (AST parser) for the file extension.
	 */
	@Override
	public TreeGenerator getTreeGenerator(String extension) {

		switch(extension) {
//		case "java":
//			return new JdtTreeGenerator();
		case "js":
			return new RhinoTreeGenerator();
		default:
			return null;
		}

	}

	@Override
	public boolean acceptsExtension(String extension) {

		/* This factory only handles .java files. */
		return (extension.equals("java") || extension.equals("js"));

	}

}
