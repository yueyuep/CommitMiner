package ca.ubc.ece.salt.sdjsb.checker;

import java.util.Map;

import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;

import fr.labri.gumtree.actions.TreeClassifier;
import fr.labri.gumtree.io.ParserASTNode;
import fr.labri.gumtree.matchers.MappingStore;
import fr.labri.gumtree.tree.Tree;

/**
 * CheckerContext provides services to concrete instances of AbstractChecker.
 * These services allow the checkers to access data from the GumTree AST
 * differencing.
 * 
 * @author qhanam
 */
public class CheckerContext {
	
	public Tree srcTree;
	public Tree dstTree;
	
	public Map<AstNode, Tree> srcTreeNodeMap;
	public Map<AstNode, Tree> dstTreeNodeMap;

	public MappingStore mappings;
	
	/**
	 * Allows checkers to lookup the differencing class of Tree nodes.
	 */
	public TreeClassifier treeClassifier;

	/**
	 * All the parameters are computed by GumTree.
	 * 
	 * @param srcTree The source AST Tree root. 
	 * @param dstTree The destination AST Tree root. 
	 * @param srcTreeNodeMap A map of AstNodes to Tree nodes in the source
	 * 		  file. CheckerContext provides an AstNode resolution service to 
	 * 		  the checkers.
	 * @param dstTreeNodeMap A map of AstNodes to Tree nodes in the destination
	 * 		  file. CheckerContext provides an AstNode resolution service to
	 * 		  the checkers.
	 * @param treeClassifier The GumTree structure that provides access to the
	 * 		  Tree node classifications (inserted, deleted, updated, modified).
	 * 		  This structure is provided to checkers.
	 * @param mappings The source to destination Tree node mappings. 
	 */
	public CheckerContext(Tree srcTree, Tree dstTree, Map<AstNode, Tree> srcTreeNodeMap, Map<AstNode, Tree> dstTreeNodeMap, TreeClassifier treeClassifier, MappingStore mappings) {
		this.srcTree = srcTree;
		this.dstTree = dstTree;
		
		this.srcTreeNodeMap = srcTreeNodeMap;
		this.dstTreeNodeMap = dstTreeNodeMap;
		
		this.treeClassifier = treeClassifier;
		
		this.mappings = mappings;
	}
	
	/**
	 * Gets the Tree node that maps to the given AstNode.
	 * @param node The AstNode that the Tree node was created from.
	 * @return The Tree node that was created from the given AstNode or 
	 * 		   {@code null} if it does not exist.
	 */
	public Tree getSrcTree(AstNode node) {
        return this.srcTreeNodeMap.get(node);
	}

	/**
	 * Gets the Tree node that maps to the given AstNode.
	 * @param node The AstNode that the Tree node was created from.
	 * @return The Tree node that was created from the given AstNode or 
	 * 		   {@code null} if it does not exist.
	 */
	public Tree getDstTree(AstNode node) {
        return this.dstTreeNodeMap.get(node);
	}
	
	/**
	 * Gets the destination node that maps to the given source node.
	 * @param node The source node.
	 * @return The destination node.
	 */
	public AstNode getDstNodeMapping(AstNode node) {
		Tree src = this.getSrcTree(node);
		Tree dst = this.mappings.getDst(src);
		if(dst == null) return null;
		ParserASTNode<AstNode> dstNode = dst.getASTNode();
		return dstNode.getASTNode();
	}

	/**
	 * Gets the source node that maps to the given destination node.
	 * @param node The destination node.
	 * @return The source node.
	 */
	public AstNode getSrcNodeMapping(AstNode node) {
		Tree dst = this.getDstTree(node);
		Tree src = this.mappings.getDst(dst);
		if(src == null) return null;
		ParserASTNode<AstNode> srcNode = src.getASTNode();
		return srcNode.getASTNode();
	}
	
	/**
	 * Returns the change operation that produced the destination Tree node.
	 * 
	 * @return null if there is no corresponding Tree node for the given 
	 * 		   AstNode. ChangeType.UNCHANGED if the Tree node is not classified
	 * 		   (it does not have a change flag, but could still be part of a
	 * 		   node that was added/deleted/moved/updated).
	 */
	public ChangeType getDstChangeFlag(AstNode node) {
        Tree tree = this.getDstTree(node);
        if(tree == null) return null;
        if(this.treeClassifier.getDstAddTrees().contains(tree)) return ChangeType.INSERT;
        if(this.treeClassifier.getDstMvTrees().contains(tree)) return ChangeType.MOVE;
        if(this.treeClassifier.getDstUpdTrees().contains(tree)) return ChangeType.UPDATE;
        return ChangeType.UNCHANGED;
	}

	/**
	 * Returns the change operation for the node. This method differs from
	 * getDstChangeFlag in that this method will climb the tree until it
	 * can determine the operation that was performed.
	 * @param node The node to classify.
	 * @return The exact change type of the node.
	 */
	public ChangeType getDstChangeOp(AstNode node) {
        do {
            Tree tree = this.getDstTree(node);

            if(tree != null) {
                if(this.treeClassifier.getDstAddTrees().contains(tree)) return ChangeType.INSERT;
                if(this.treeClassifier.getDstMvTrees().contains(tree)) return ChangeType.MOVE;
                if(this.treeClassifier.getDstUpdTrees().contains(tree)) return ChangeType.UPDATE;
            }
        	
        	node = node.getParent();
        } while(!(node instanceof AstRoot));

        return ChangeType.UNCHANGED;
	}
	
	/**
	 * Returns the change operation that produced the source Tree node.
	 * 
	 * @return null if there is no corresponding Tree node for the given 
	 * 		   AstNode. ChangeType.UNCHANGED if the Tree node is not classified
	 * 		   (it is the same in both the source and destination ASTs).
	 */
	public ChangeType getSrcChangeFlag(AstNode node) {
        Tree tree = this.getSrcTree(node);
        if(tree == null) return null;
        if(this.treeClassifier.getSrcDelTrees().contains(tree)) return ChangeType.DELETE;
        if(this.treeClassifier.getSrcMvTrees().contains(tree)) return ChangeType.MOVE;
        if(this.treeClassifier.getSrcUpdTrees().contains(tree)) return ChangeType.UPDATE;
        return ChangeType.UNCHANGED;
	}

	/**
	 * Returns the change operation for the node. This method differs from
	 * getSrcChangeFlag in that this method will climb the tree until it
	 * can determine the operation that was performed.
	 * @param node The node to classify.
	 * @return The exact change type of the node.
	 */
	public ChangeType getSrcChangeOp(AstNode node) {
        do {
            Tree tree = this.getSrcTree(node);

            if(tree != null) {
                if(this.treeClassifier.getSrcDelTrees().contains(tree)) return ChangeType.INSERT;
                if(this.treeClassifier.getSrcMvTrees().contains(tree)) return ChangeType.MOVE;
                if(this.treeClassifier.getSrcUpdTrees().contains(tree)) return ChangeType.UPDATE;
            }
        	
        	node = node.getParent();
        } while(!(node instanceof AstRoot));

        return ChangeType.UNCHANGED;
	}
	
	public enum ChangeType {
		UNCHANGED,
		INSERT,
		UPDATE,
		MOVE,
		DELETE
	}

}
