import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class State {
	
	private HashMap<Character, ArrayList<State>> transitions;
	private boolean isFinal;
	private String name;
	
	public State(String name, boolean isFinal) {
		this.name = name;
		this.isFinal = isFinal;
		this.transitions = new HashMap<Character, ArrayList<State>>();
	}
	
	public void addState(Character symbol, State state) {
		if(this.transitions.containsKey(symbol)) {
			this.transitions.get(symbol).add(state);
		} else {
			ArrayList<State> newTransition = new ArrayList<State>();
			newTransition.add(state);
			this.transitions.put(symbol, newTransition);
		}
	}

	public HashMap<Character, ArrayList<State>> getTransitions() {
		return transitions;
	}

	public void setTransitions(HashMap<Character, ArrayList<State>> transitions) {
		this.transitions = transitions;
	}

	public boolean isFinal() {
		return isFinal;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		String s = "---\n";
		s += "State: " + this.name + "\n";
		s += "final: "+ isFinal + "\n";
		s += "transitions: ";
		for(Entry<Character, ArrayList<State>> entry : transitions.entrySet()) {
			for(int i=0; i<entry.getValue().size(); i++) {
				s += entry.getKey();
				s += ",";
			}
		}
		s += "\n---";
		return s;
	}
	
}
