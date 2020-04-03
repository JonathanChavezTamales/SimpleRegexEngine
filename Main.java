public class Main {

	public static void main(String[] args) {

		if(args.length == 3){
			System.out.println(replace(args[0], args[1], args[2]));
		}
		
		/*
		System.out.println(replace("abaa aab aaanbbb", "aba*+bbb*", "ccc"));
		System.out.println(replace("cabaaaaa", "caba+ab*", "x"));
		System.out.println(replace("abaaaabaaanbbb", "aba*+bbb*", "ccc"));
		System.out.println(replace("a", "ab*", "x"));
		System.out.println(replace("abnnnnnccccb", "(ab+c)b", "x"));
		System.out.println(replace("abnnnnnccccb", "(ab+c)*b", "x"));
		*/
		
	}
	
	public static String replace(String str, String pattern, String replacement) {
		
		Machine m = new Machine(pattern);
		
		int last_i = -1;
		int last_j = -1;
		
		for(int i = 0; i<str.length(); i++) { // Empty wont be replaced, thats why index starts on 1
			for(int j=i; j<str.length(); j++) {
				String substr = str.substring(i, j+1);
				if(m.test(substr, 5)) { // while subword matches regex, store the matching indexes
					last_i = i;
					last_j = j;
					
					if(last_i == 0 && last_j == 0 && str.length() == 1 ) { //Dont know if this works properly
						return replacement;
					}
				} else if (last_i != -1 && last_j != -1){
					//Replace the first ocurrence, then append the next processed piece (recursively)
					return str.substring(0, last_i) + replacement + replace(str.substring(last_j+1, str.length()), pattern, replacement);
				}
			}
			
		}
		
		
		return "";
	}

}
