package commitminer.analysis.flow.abstractdomain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.lang3.StringUtils;
import org.deri.iris.api.basics.IPredicate;
import org.deri.iris.api.basics.ITuple;
import org.deri.iris.factory.Factory;
import org.deri.iris.storage.IRelation;
import org.deri.iris.storage.IRelationFactory;
import org.deri.iris.storage.simple.SimpleRelationFactory;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.ScriptNode;

import commitminer.analysis.flow.factories.StoreFactory;
import commitminer.analysis.flow.trace.Trace;
import commitminer.cfg.CFG;
import commitminer.cfg.CFGEdge;
import commitminer.cfg.CFGNode;
import ca.ubc.ece.salt.gumtree.ast.ClassifiedASTNode.Version;

public class Helpers {

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param prop The name of the property to add to the object.
	 */
	public static Store addProp(String prop, BValue propVal, Map<String, Address> ext, Store store, Trace trace) {
		Address propAddr = trace.toAddr(prop);
		store = store.alloc(propAddr, propVal);
		ext.put(prop, propAddr);
		return store;
	}

	/**
	 * Adds a property to the object and allocates the property's value on the
	 * store.
	 * @param propID The node id of the property being added to the object.
	 * @param propVal The value of the property.
	 */
	public static Store addProp(int propID, String prop, BValue propVal, Map<String, Property> ext, Store store, Trace trace) {
		Address propAddr = trace.makeAddr(propID, prop);
		store = store.alloc(propAddr, propVal);
		ext.put(prop, new Property(propID, prop, Change.u(), propAddr));
		return store;
	}

	/**
	 * Creates and allocates a regular function from a closure stack.
	 * @param closures The closure for the function.
	 * @return The function object.
	 */
	public static Store createFunctionObj(Map<IPredicate, IRelation> facts, Closure closure, Store store, Trace trace, Address address, FunctionNode function) {

		Map<String, Property> external = new HashMap<String, Property>();
		store = addProp(function.getID(), "length", Num.inject(Num.top(Change.u()), Change.u(), DefinerIDs.bottom()), external, store, trace);

		InternalFunctionProperties internal = new InternalFunctionProperties(
				Address.inject(StoreFactory.Function_proto_Addr, Change.u(), Change.u(), DefinerIDs.bottom()),
				closure,
				JSClass.CFunction);
		
		registerDefFact(facts, function.getVersion(), address, function.getAbsolutePosition(), "function".length());

		store = store.alloc(address, new Obj(external, internal));

		return store;

	}

	/**
	 * @param statement The statement for which we are registering a fact.
	 * @param identifier The identifier for which we are registering a fact.
	 * @param annotation details about the 
	 */
	private static void registerDefFact(Map<IPredicate, IRelation> facts, 
										Version version, 
										Address address,
										Integer position,
										Integer length) {

		IPredicate predicate = Factory.BASIC.createPredicate("Def", 4);
		IRelation relation = facts.get(predicate);
		if(relation == null) {
			IRelationFactory relationFactory = new SimpleRelationFactory();
			relation = relationFactory.createRelation();
			facts.put(predicate, relation);
		}

		/* Add the new tuple to the relation. */
		ITuple tuple = Factory.BASIC.createTuple(
				Factory.TERM.createString(version.toString()),
				Factory.TERM.createString(address.toString()),
				Factory.TERM.createString(position.toString()),
				Factory.TERM.createString(length.toString()));
		relation.add(tuple);

	}

	/**
	 * Runs a script or function by traversing the CFG.
	 * @param cfg
	 * @param state
	 * @return The state after the script has finished.
	 */
	public static State run(CFG cfg, State state) {

		/* Merge state with prior state if needed. */
		State initState = (State)cfg.getEntryNode().getBeforeState();
		if(initState != null) state = state.join(initState);

		/* For terminating a long running analysis. */
		long edgesVisited = 0;

		/* Stores semaphores for tracking the number of incoming edges that
		 * have been traversed to a node. Stands for "I(ncoming) E(dges)
		 * S{emaphore} Map. */
		Map<CFGNode, Integer> iesMap = new HashMap<CFGNode, Integer>();

		/* Initialize the stack for a depth-first traversal. */
		Stack<PathState> stack = new Stack<PathState>();
		for(CFGEdge edge : cfg.getEntryNode().getEdges()) {
			stack.add(new PathState(edge, new HashSet<CFGEdge>(), state));
		}

		/* Set the initial state. */
		cfg.getEntryNode().setBeforeState(state);
		cfg.getEntryNode().setAfterState(state);

		/* Break when the analysis time reaches some limit. */
		while(!stack.isEmpty() && edgesVisited < 100000) {

			PathState pathState = stack.pop();
			edgesVisited++;

//			System.out.println(pathState.edge.toString());

			/* Join the lattice elements from the current edge and 'from'
			 * node. */
			state = pathState.state.join((State)pathState.edge.getBeforeState());
			pathState.edge.setBeforeState(state);

			/* Transfer the abstract state over the edge. */
			state = state.clone().transfer(pathState.edge);
			pathState.edge.setAfterState(state);

			/* Join the abstract states from the 'to' node and the current
			 * edge. */
			state = state.join((State)pathState.edge.getTo().getBeforeState());
			pathState.edge.getTo().setBeforeState(state);

			/* Look up the number of times this node has been visited in the
			 * visitedSemaphores map. */
			Integer semVal = iesMap.get(pathState.edge.getTo());

			/* If it does not exist, put it in the map and initialize the
			 * semaphore value to the number of incoming edges for the node. */
			if(semVal == null) semVal = pathState.edge.getTo().getIncommingEdges();

			/* Decrement the semaphore by one since we visited the node. */
			semVal = semVal - 1;
			iesMap.put(pathState.edge.getTo(), semVal);

			/* Transfer the abstract state over the node. */
			state = state.clone().transfer(pathState.edge.getTo());
			pathState.edge.getTo().setAfterState(state);

			/* Add all unvisited edges to the stack.
			 * We currently only execute loops once. */
			for(CFGEdge edge : pathState.edge.getTo().getEdges()) {

				/* Only visit an edge if the semaphore for the node is zero or if one of the
				* edges is a loop edge. */
				if(!pathState.visited.contains(edge)
						&& (semVal == 0 || edge.isLoopEdge)) {
					Set<CFGEdge> newVisited = new HashSet<CFGEdge>(pathState.visited);
					newVisited.add(edge);
					PathState newState = new PathState(edge, newVisited, state);
					stack.push(newState);
				}

			}

		}

		/* Return the merged state of all exit nodes. */
		for(CFGNode exitNode : cfg.getExitNodes()) {
			state = state.join((State)exitNode.getBeforeState());
		}

		return state;

	}

	/**
	 * @param funVal The address(es) of the function object to execute.
	 * @param selfAddr The value of the 'this' identifier (a set of objects).
	 * @param store The store at the caller.
	 * @param sp Scratchpad memory.
	 * @param trace The trace at the caller.
	 * @return The final state of the closure.
	 */
	public static State applyClosure(Map<IPredicate, IRelation> facts, 
							  BValue funVal, Address selfAddr, 
							  Store store, Scratchpad sp, Trace trace, Control control,
							  Stack<Address> callStack) {

		State state = null;

		/* Get the results for each possible function. */
		for(Address address : funVal.addressAD.addresses) {

			/* Get the function object to execute. */
			Obj functObj = store.getObj(address);

			/* Ignore addresses that don't resolve to objects. */
			if(functObj == null || !(functObj.internalProperties
									 instanceof InternalFunctionProperties)) {
				continue;
			}
			InternalFunctionProperties ifp =
					(InternalFunctionProperties)functObj.internalProperties;

			/* Is this function being called recursively? If so abort. */
			if(callStack.contains(address)) return state;

			/* Push this function onto the call stack. */
			callStack.push(address);

			/* Run the function. */
			State endState = ifp.closure.run(facts, selfAddr, store, sp, trace, control, callStack);

			/* Pop this function off the call stack. */
			callStack.pop();

			if(state == null) state = endState;
			else {
				/* Join the store and scratchpad only. Environment, trace 
				 * and control no longer apply. */
				state.store = state.store.join(endState.store);
				state.scratch = state.scratch.join(endState.scratch);
			}

		}

		return state;

	}

	/**
	 * Lifts local variables and function definitions into the environment.
	 * @param env The environment (or closure) in which the function executes.
	 * @param store The current store.
	 * @param function The code we are analyzing.
	 * @param cfgs The control flow graphs for the file. Needed for
	 * 			   initializing lifted functions.
	 * @param trace The program trace including the call site of this function.
	 * @return The new store. The environment is updated directly (no new object is created)
	 */
	public static Store lift(Map<IPredicate, IRelation> facts, Environment env,
										  Store store,
										  ScriptNode function,
										  Map<AstNode, CFG> cfgs,
										  Trace trace) {

		/* Lift variables into the function's environment and initialize to undefined. */
		List<Name> localVars = VariableLiftVisitor.getVariableDeclarations(function);
		for(Name localVar : localVars) {
			Change change = Change.convU(localVar);
			Address address = trace.makeAddr(localVar.getID(), "");
			env.strongUpdateNoCopy(localVar.toSource(), new Variable(localVar.getID(), localVar.toSource(), Change.convU(localVar), new Addresses(address, Change.u())));
			store = store.alloc(address, Undefined.inject(Undefined.top(change), Change.u(), DefinerIDs.bottom()));
		}

		/* Get a list of function declarations to lift into the function's environment. */
		List<FunctionNode> children = FunctionLiftVisitor.getFunctionDeclarations(function);
		for(FunctionNode child : children) {
			if(child.getName().isEmpty()) continue; // Not accessible.
			Address address = trace.makeAddr(child.getID(), "");
			address = trace.modAddr(address, JSClass.CFunction);

			/* The function name variable points to our new function. */
			env.strongUpdateNoCopy(child.getName(), new Variable(child.getID(), child.getName(), Change.convU(child.getFunctionName()), new Addresses(address, Change.u()))); // Env update with env change type
			store = store.alloc(address, Address.inject(address, Change.convU(child), Change.convU(child), DefinerIDs.inject(child.getID())));

			/* Create a function object. */
			Closure closure = new FunctionClosure(cfgs.get(child), env, cfgs);
			store = createFunctionObj(facts, closure, store, trace, address, child);

		}

		return store;

	}

	/**
	 * Analyze functions which are reachable from the environment and that have not
	 * already been analyzed.
	 * @param state The end state of the parent function.
	 * @param visited Prevent circular lookups.
	 */
	public static void analyzeEnvReachable(
			Map<IPredicate, IRelation> facts,
			State state,
			Map<String, Variable> vars,
			Address selfAddr,
			Map<AstNode, CFG> cfgMap,
			Set<Address> visited,
			Set<String> localvars) {
		
		for(Map.Entry<String, Variable> entry : vars.entrySet()) {
			for(Address addr : entry.getValue().addresses.addresses) {
				analyzePublic(facts, state, entry.getKey(), addr, selfAddr, cfgMap, visited, localvars);
			}
		}

	}

	/**
	 * Analyze functions which are reachable from an object property and that have
	 * not already been analyzed.
	 * @param state The end state of the parent function.
	 * @param visited Prevent circular lookups.
	 */
	private static void analyzeObjReachable(
			Map<IPredicate, IRelation> facts,
			State state,
			Map<String, Property> props,
			Address selfAddr,
			Map<AstNode, CFG> cfgMap,
			Set<Address> visited,
			Set<String> localvars) {

		for(Map.Entry<String, Property> entry : props.entrySet()) {
			analyzePublic(facts, state, entry.getKey(), entry.getValue().address, selfAddr, cfgMap, visited, localvars);
		}
		
	}

	/**
	 * Analyze functions which are reachable from an object property and that have
	 * not already been analyzed.
	 * @param state The end state of the parent function.
	 * @param var The name of the property or variable
	 * @param addr The address pointed to by the property or variable.
	 * @param visited Prevent circular lookups.
	 */
	private static void analyzePublic(
			Map<IPredicate, IRelation> facts,
			State state,
			String name,
			Address addr,
			Address selfAddr,
			Map<AstNode, CFG> cfgMap,
			Set<Address> visited,
			Set<String> localvars) {

		BValue val = state.store.apply(addr);

		/* Do not visit local variables which were declared at a higher
		 * level, and therefore can be analyzed later. */
		if(localvars != null
				&& !localvars.contains(name)
				&& !name.equals("~retval~")
				&& !StringUtils.isNumeric(name)) return;

		/* Avoid circular references. */
		if(visited.contains(addr)) return;
		visited.add(addr);

		for(Address objAddr : val.addressAD.addresses) {
			Obj obj = state.store.getObj(objAddr);

			/* We may need to analyze this function. */
			if(obj.internalProperties.klass == JSClass.CFunction) {

				InternalFunctionProperties ifp = (InternalFunctionProperties)obj.internalProperties;
				FunctionClosure fc = (FunctionClosure)ifp.closure;

				if(ifp.closure instanceof FunctionClosure &&
						fc.cfg.getEntryNode().getBeforeState() == null) {

					/* Create the control domain. */
					Control control = new Control();
					AstNode node = (AstNode)fc.cfg.getEntryNode().getStatement();
					control = control.update(node);
					
					Scratchpad scratch = new Scratchpad(state.scratch.applyReturn(), new BValue[0]);

					/* Analyze the function. Use a fresh call stack because we don't have any knowledge of it. */
					ifp.closure.run(facts, selfAddr, state.store,
									scratch, state.trace, control,
									new Stack<Address>());

					/* Check the function object. */
					// TODO: We ignore this for now. We would have to assume the function is being run as a constructor.

				}
			}

			/* Recursively look for object properties that are functions. */
			analyzeObjReachable(facts, state, obj.externalProperties, addr, cfgMap, visited, null);

		}
		
	}
	
	/**
	 * Collect garbage (ie. values and objects on the store that are not 
	 * reachable from the environment).
	 * @param env
	 * @param store
	 */
	public static Store gc(Environment env, Store store) {

		/* The clean memory values and objects. */
		Map<Address, BValue> bValueStore = new HashMap<Address, BValue>();
		Map<Address, Obj> objectStore = new HashMap<Address, Obj>();

		for(Variable var : env.environment.values()) {
			for(Address addr : var.addresses.addresses) {
				
				BValue value = store.apply(addr);
				
				/* Put the value on the store. */
				bValueStore.put(addr, value);
				
				/* Put the objects on the store. */
				gcObjectProperty(value.addressAD, store, 
								 bValueStore, objectStore);

			}
		}
		
		return new Store(bValueStore, objectStore);

	}
	
	/**
	 * Retain objects in the store that are accessible from objects.
	 * @param addrs Addresses that point to accessible objects.
	 */
	private static void gcObjectProperty(Addresses addrs, Store store, 
								  Map<Address, BValue> bValueStore, 
								  Map<Address, Obj> objectStore) {

		/* Put the objects on the store. */
		for(Address objAddr : addrs.addresses) {

			Obj obj = store.getObj(objAddr);
			
			/* Prevent infinite loops when objects point to themselves. */
			if(!objectStore.containsKey(objAddr)) {

				objectStore.put(objAddr, obj);
				
				/* Put the object's property values on the store. */
				for(Property prop : obj.externalProperties.values()) {

					BValue value = store.apply(prop.address);

					/* Put the value on the store. */
					bValueStore.put(prop.address, value);
					
					/* Put the objects on the store. */
					gcObjectProperty(value.addressAD, store, 
									 bValueStore, objectStore);
					
				}
				
			}

		}
		
	}
		
}