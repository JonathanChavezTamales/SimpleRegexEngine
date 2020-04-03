import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;

public class Machine {
	private State initialState;
	private State finalState;
	private HashSet<State> currentStates = new HashSet<State>();
	private String regularExpression;
	private String regularExpressionRPN = ""; // Reverse Polish Notation
	
	public Machine(String regexp) {
		this.regularExpression = regexp;
		try {
			this.regularExpressionRPN = infixToPostfix(regexp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		buildNFA(this.regularExpressionRPN);
		this.currentStates.add(this.initialState);
	}
	
	public Machine(State q0, State qn, String toAppend) { // Receives initial and accept state
		this.initialState = q0;
		this.finalState = qn;
		this.regularExpressionRPN += toAppend;
	}
	
	private String infixToPostfix(String infix) throws Exception {
		if(infix.equals("")) {
			throw new Exception("No se puede la vacia");
		}
		
		Stack<Character> stack = new Stack<Character>();
		int parenthesis = 0;
		
		String postfix = "";
		if(infix.charAt(0) == '('){
			stack.push(infix.charAt(0));
			parenthesis++;
		} 
		else if(infix.charAt(0) != '*' && infix.charAt(0) != '+' && infix.charAt(0) != ')') {
			postfix += infix.charAt(0);
		} 
		else {
			throw new Exception("Fail al inicio");
		}
		
		for(int i=1; i<infix.length(); i++) {
			char current = infix.charAt(i);
			
			if(current == ')') {
				parenthesis--;
				if(parenthesis < 0) {
					throw new Exception("Paenthesis not balanced");
				} 
				else {
					while(stack.peek()!='(') {
						postfix += stack.pop();
					}
					stack.pop(); //Pops the remaining (
				}
			} 
			else if(current == '('){
				parenthesis++;
				stack.push(current);
			} 
			else if(current == '*'){ // Precedence 1
				stack.push(current);
			} 
			else if(current == '+') { // Precedence 3
				
				if(!stack.empty()) {
					while(stack.peek() == '.' || stack.peek() == '*') {
						postfix += stack.pop();
						if(stack.empty()) {
							break;
						}
					}
				}
				stack.push(current);
			} 
			else { // Any character concat (Precedence 2)
				if(infix.charAt(i-1) == '(' || infix.charAt(i-1) == '+') {
					postfix += current;
				} else { // Implicit concatenation
					if(!stack.empty()) {
						while(stack.peek() == '*') {
							postfix += stack.pop();
							if(stack.empty()) {
								break;
							}
						}
					}
					stack.push('.');
					postfix += current;
				}
			}		
		}
		while(!stack.empty()) {
			postfix += stack.pop();
		}
		return postfix;
	}
	
	public void buildNFA(String postfix) {
		Stack<Machine> stack = new Stack<Machine>();
		for(int i=0; i<postfix.length(); i++) {
			char current = postfix.charAt(i);
			switch(current) {
				case '.':{
					Machine n2 = stack.pop();
					Machine n1 = stack.pop();
					n1.concat(n2);
					stack.push(n1);
					break;
				}
				case '+':{
					
					Machine n2 = stack.pop();
					Machine n1 = stack.pop();
					
					n1.join(n2);
					stack.push(n1);
					break;
				}
				case '*':{
					Machine n1 = stack.pop();
					n1.star();
					stack.push(n1);
					break;
				}
				default:{ // Any character (creates the basic machine)
					State q0 = new State("q0a", false);
					State q1 = new State("q1a", true);
					q0.addState(current, q1);
					Machine n = new Machine(q0, q1, Character.toString(current));
					stack.push(n);
					break;
				}
			}
		}
		
		Machine finalMachine = stack.pop();
		this.initialState = finalMachine.initialState;
		this.finalState = finalMachine.finalState;
	}
	
	public State getInitialState() {
		return initialState;
	}
	public State getFinalState() {
		return finalState;
	}
	public HashSet<State> getCurrentStates() {
		return currentStates;
	}
	public String getRegularExpressionRPN() {
		return regularExpressionRPN;
	}
	
	private void join(Machine n2) {
		State q0 = new State("q0+", false);
		State qn = new State("qn+", true);
		//Adds epsilon transitions (\0) 
		q0.addState('.', this.initialState);
		q0.addState('.', n2.initialState);
		this.finalState.addState('.', qn); // New final state
		this.finalState.setFinal(false);
		n2.finalState.addState('.', qn); // New final state
		n2.finalState.setFinal(false);
		this.initialState = q0;
		this.finalState = qn;
		this.regularExpressionRPN += n2.regularExpressionRPN + '+';
	}
	
	private void concat(Machine n2) {
		
		this.finalState.addState('.', n2.initialState);
		this.finalState.setFinal(false);
		this.finalState = n2.finalState;
		this.regularExpressionRPN += n2.regularExpressionRPN + '.';
	}
	
	private void star() {
		State q0 = new State("q0*", false);
		State qn = new State("qn*", true);
		//Adds epsilon transitions (\0) 
		q0.addState('.', qn);
		q0.addState('.', this.initialState);
		this.finalState.addState('.', qn);
		this.finalState.addState('.', this.initialState);
		this.finalState.setFinal(false);
		this.initialState = q0;
		this.finalState = qn;
		this.regularExpressionRPN += '*';
	}
	
	public void moveEpsOnce() {
		Iterator<State> it = this.currentStates.iterator(); // State in the currentStates
		HashSet<State> newCurrentStates = new HashSet<State>();
		newCurrentStates.addAll(this.currentStates);
		
		while(it.hasNext()) {
			State current = it.next();
			if(current.getTransitions().containsKey('.')) {
				ArrayList<State> epsilonNeighbors = current.getTransitions().get('.'); // Epsilon neighbours of current states
				newCurrentStates.addAll(epsilonNeighbors);
			}
		}
		this.currentStates = newCurrentStates;
	}
	
	public void move(Character symbol, int epsDepth) {
		//Updates current states given a symbol, not epsilon
		//epsDepth: int, the number of epsilon moves will execute before the symbol (useful when multiple epsilon transitions are together)
		if(symbol == '.') return;
		
		//First we move epsilons
		for(int i=0; i<epsDepth; i++) {
			this.moveEpsOnce();
		}
		
		//Then we transition given the symbol
		Iterator<State> it = this.currentStates.iterator(); // State in the currentStates
		HashSet<State> newCurrentStates = new HashSet<State>();
		
		while(it.hasNext()) {
			State current = it.next();
			if(current.getTransitions().containsKey(symbol)) {
				ArrayList<State> symbolNeighbors = current.getTransitions().get(symbol);
				newCurrentStates.addAll(symbolNeighbors);
			}
		}
		
		this.currentStates = newCurrentStates;
		
		//Finally we move epsilons after transition
		for(int i=0; i<epsDepth; i++) {
			this.moveEpsOnce();
		}
		
			
	}
	
	public boolean test(String str, int epsDepth) {
		//Always acompanied of an epsilon transition at the beginning and at the end, not sure if will always work.
		//Check epsDepth meaning on this.move definition
		
		if(str.contentEquals("")) { // Empty string only moves eps
			for(int i=0; i<epsDepth; i++) {
				this.moveEpsOnce();
			}
		}
		
		for(int i=0; i<str.length(); i++) {
			this.move(str.charAt(i), epsDepth);
		}
		
		
		Iterator<State> it = this.currentStates.iterator();
		while(it.hasNext()) {
			if(it.next().isFinal()) {
				this.currentStates = new HashSet<State>();
				this.currentStates.add(initialState);
				return true;
			}
		}
		
		this.currentStates = new HashSet<State>();
		this.currentStates.add(initialState);
		
		
		
		return false;
	}
	
	
	public void draw() { //BFS that draws the graph
		HashSet<State> visited = new HashSet<State>();
		Queue<State> queue = new LinkedList<State>();
		
		State current = this.initialState;
		queue.add(current);
		visited.add(current);
		
		System.out.println("### DRAW ###");
		
		
		while(!queue.isEmpty()) {
			current = queue.poll();
			System.out.println(current);
			for(ArrayList<State> s : current.getTransitions().values()) {
				for(int i=0; i<s.size(); i++) {
					if(!visited.contains(s.get(i))) {
						visited.add(s.get(i));
						
						queue.add(s.get(i));
					}
				}
			}
		}
		System.out.println("###########");
	}
	
	public String toString() {
		String s = "---\n";
		s += "Machine: " + this.regularExpression + "\n";
		s += "reverse polish notation: " + this.regularExpressionRPN;
		s += "\n---";
		return s;
	}
	
	
	
}
