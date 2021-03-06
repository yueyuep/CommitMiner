package commitminer.analysis.flow.trace;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

import org.mozilla.javascript.ast.AstNode;

import commitminer.analysis.flow.abstractdomain.Address;
import commitminer.analysis.flow.abstractdomain.Environment;
import commitminer.analysis.flow.abstractdomain.Obj;
import commitminer.analysis.flow.abstractdomain.Store;

/**
 * Flow-sensitive stack-based k-CFA with h-sensitive heap
 */
public class StackCFA extends Trace {

	/** Call-site sensitivity. **/
	private int k;

	/** Heap sensitiviy. **/
	private int h;

	/** Program-point. **/
	private int pp;

	/** Trace (partial call stack). **/
	private Queue<Integer> tr;

	public StackCFA(int k, int h, int pp, Queue<Integer> tr) {
		if(h > k) throw new Error("heap sensitivity must be <= stack sensitivity");
		this.k = k;
		this.h = h;
		this.pp = pp;
		this.tr = tr;
	}

	@Override
	public Trace update(int pp) {
		// Flow-sensitive
		return new StackCFA(k, h, pp, tr);
	}

	@Override
	public Trace update(Environment env, Store store, Address selfAddr, 
			AstNode call) {
		Queue<Integer> trp = new LinkedList<Integer>(this.tr);
		trp.add(call.getID());
		if(trp.size() > k) trp.remove();
		return new StackCFA(k, h, call.getID(), trp);
	}

	@Override
	public Address toAddr(String prop) {
		return new Address(intsToBigInteger(tr, pp).shiftLeft(32), prop);
	}

	@Override
	public Address makeAddr(int varID, String prop) {
		return new Address(intsToBigInteger(tr, pp).shiftLeft(32).add(BigInteger.valueOf(varID)), prop);
	}

}