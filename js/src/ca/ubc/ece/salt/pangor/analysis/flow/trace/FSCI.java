package ca.ubc.ece.salt.pangor.analysis.flow.trace;

import java.math.BigInteger;

import org.mozilla.javascript.ast.AstNode;

import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Address;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.BValue;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Environment;
import ca.ubc.ece.salt.pangor.analysis.flow.abstractdomain.Store;

/**
 * Flow-sensitive context-insensitive.
 */
public class FSCI extends Trace {

	public int pp;

	/**
	 * @param pp The program point.
	 */
	public FSCI(int pp) {
		this.pp = pp;
	}

	@Override
	public Trace update(AstNode statement) {
		// Flow sensitive
		return new FSCI(statement.getID());
	}

	@Override
	public Trace update(Environment env, Store store, BValue self, BValue args,
			AstNode statement) {
		// Context insensitive
		return new FSCI(statement.getID());
	}

	@Override
	public Address toAddr() {
		return new Address(BigInteger.valueOf(pp));
	}

	@Override
	public Address makeAddr(int varID) {
		return new Address(BigInteger.valueOf(varID));
	}

}