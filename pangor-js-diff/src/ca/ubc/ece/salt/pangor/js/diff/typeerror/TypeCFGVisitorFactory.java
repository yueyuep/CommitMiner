package ca.ubc.ece.salt.pangor.js.diff.typeerror;

import java.util.Map;

import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.storage.IRelation;

import ca.ubc.ece.salt.pangor.analysis.SourceCodeFileChange;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitor;
import ca.ubc.ece.salt.pangor.cfg.ICFGVisitorFactory;

public class TypeCFGVisitorFactory implements ICFGVisitorFactory {

	@Override
	public ICFGVisitor newInstance(SourceCodeFileChange sourceCodeFileChange,
			Map<IPredicate, IRelation> facts) {
		return new TypeCFGVisitor(sourceCodeFileChange, facts);
	}

}
