import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CalculatorTest
{
	
	public static void main(String args[])
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while (true)
		{
			try
			{
				String input = br.readLine().trim();
				if (input.compareTo("q") == 0)
					break;
				
				// replace unary '-' with '~' 
				input = processUnaryMinus(input);
				command(input);
			}

			catch (Exception e) 
			{
				System.out.println("ERROR");
			}
		}
	}

	/* Perform calculation of given infix expression.
	 * Print the result and the corresponding postfix expression. 
	 */
	private static void command(String input) 
			throws IllegalArgumentException, EmptyStackException
	{
		StringBuilder postfix = new StringBuilder(); // for making postfix expression
		Stack<Long> opnds = new Stack<Long>(); // for saving operands
		Stack<Character> oprs = new Stack<Character>(); // for saving operators
		
		// Read each character of the string input.
		int idx = 0;
		while(true) 
		{
			if(idx >= input.length()) break;
			char ch = input.charAt(idx);
			
			// If whitespace character, skip.
			if(ch==' ' || ch=='\t') 
			{
				idx++;
				continue;
			}
			
			// If number, read the whole number and save it as operand.
			if(Character.isDigit(ch)) 
			{
				StringBuilder sb = new StringBuilder();
				while(idx<input.length() && Character.isDigit(input.charAt(idx))) {
					sb.append(input.charAt(idx));
					idx++;
				}
				long num = Long.parseLong(sb.toString());
				opnds.push(num);
				postfix.append(num + " ");
			}
			
			// If '(', save it as operator.
			else if(ch == '(') 
			{
				oprs.push(ch);
				idx++;
			}
			
			// If other operators, arrange the expression by performing some calculations.
			else if("+-*/%^~)".indexOf(ch) != -1) 
			{
				arrangeExp(opnds, oprs, ch, postfix);
				idx++;
			}
			else throw new IllegalArgumentException("Invalid input character");
		}
		
		// Perform operations with remaining operators in the stack.
		while(!oprs.isEmpty()) {
			operation(opnds, oprs, postfix);
		}

		if(opnds.size()!=1 || !oprs.isEmpty()) {
			throw new IllegalArgumentException("Invalid expression");
		}
		
		// Print the result and the postfix expression.
		System.out.print(postfix.toString().trim());
		System.out.print('\n');
		System.out.println(opnds.pop());
		
	}
	
	/* Perform intermediate calculations, if possible.
	 * It is "possible" when the incoming nextOpr is closing parenthesis, 
	 * or the nexOpr's precedence is lower than the previous operator.
	 * This forces the oprs stack to save operators in increasing precedence order.
	 * nextOpr argument should be one of +, -, *, /, %, ^, ~, )
	 */
	private static void arrangeExp(Stack<Long> opnds, Stack<Character> oprs, char nextOpr, StringBuilder postfix) 
			throws IllegalArgumentException, EmptyStackException
	{
		// If the stack is empty, nothing to calculate.
		if(oprs.isEmpty()) 
		{
			oprs.push(nextOpr);
			return;
		}
		
		// If the next operator is ')',
		// perform operation until you meet '('.
		if(nextOpr == ')') 
		{
			while(oprs.peek() != '(') 
			{
				operation(opnds, oprs, postfix);
			}
			// remove '('
			oprs.pop();
		}
		
		// If the next operator is one of +, -, *, /, %, ^, ~, ...
		else 
		{
			// If you the next operator has lower precedence than the previous operator,
			// perform operation until the next operator has higher precedence.
			while(!oprs.isEmpty() && comparePrec(oprs.peek(), nextOpr) > 0) 
			{
				operation(opnds, oprs, postfix);
			}
			oprs.push(nextOpr);
		}
	}
	
	/* Perform ONE operation using the operator at the top of the oprs stack. */
	private static void operation(Stack<Long> opnds, Stack<Character> oprs, StringBuilder postfix) throws IllegalArgumentException
	{
		char opr = oprs.pop();
		postfix.append(opr + " ");
		long result;
		
		// unary operation
		if(opr == '~') 
		{
			result = - opnds.pop();
			opnds.push(result);
		}
		// binary operation
		else {
			long op2 = opnds.pop();
			long op1 = opnds.pop();
			if(opr == '+') result = op1 + op2;
			else if(opr == '-') result = op1 - op2;
			else if(opr == '*') result = op1 * op2;
			else if(opr == '/') 
			{
				if(op2 == 0) throw new IllegalArgumentException("zero division");
				result = op1 / op2;
			}
			else if(opr == '%') 
			{
				if(op2 == 0) throw new IllegalArgumentException("zero division");
				result = op1 % op2;
			}
			else if(opr == '^') 
			{
				if(op1 == 0 && op2 < 0) throw new IllegalArgumentException("negative power of 0");
				result = (long) Math.pow(op1, op2);
			}
			else throw new IllegalArgumentException();
			
			opnds.push(result);
		}	
	}
	
	
	/* Compare precedence of the previous operator and next (incoming) operator
	 * Returns 1 if the previous operator has higher precedence
	 * Returns -1 if the incoming operator has higher precedence
	 */
	private static int comparePrec(char prev, char next) 
	{
		if(precedence(prev) > precedence(next)) return 1;
		else if(precedence(prev) < precedence(next)) return -1;
		else
		{
			// right associative -> incoming operator has higher precedence
			if(prev == '~' || prev == '^') return -1;
			// left associative -> incoming operator has lower precedence
			else return 1;
		}
	}
	/* Returns precedence of each operators */
	private static int precedence(char opr) {
		if(opr == '^') return 4;
		else if(opr == '~') return 3;
		else if(opr == '*' || opr == '/' || opr == '%') return 2;
		else if(opr == '+' || opr == '-') return 1;
		else if(opr == '(') return 0;
		else throw new IllegalArgumentException();
	}
	
	
	/* Detect unary minuses from the input string.
	 * Replace all the unary minuses with '~'
	 * Returns the modified string.
	 */
	private static String processUnaryMinus(String input) {
		Stack<Integer> unaryMinusIdx = new Stack<>(); // for saving index of unary minuses

		// If minus is in the beginning of the string, it is unary minus
		Pattern startingMinus = Pattern.compile("^\\s*(-)");
		Matcher matcher = startingMinus.matcher(input);
		if(matcher.find()) 
		{
			unaryMinusIdx.push(matcher.start(1));
		}

		// If minus comes after one of +,-,*,/,^,%,(, it is unary minus
		Pattern midMinus = Pattern.compile("[-+*\\/^%(]\\s*(-)");
		matcher = midMinus.matcher(input);
		int i=0;
		// start searching from the previous match (to detect overlapping matches)
		while(matcher.find(i)) 
		{
			unaryMinusIdx.push(matcher.start(1));
			i = matcher.end() - 1; 
		}
		
		// replace unary minuses with ~
		StringBuilder modifiedInput = new StringBuilder(input);
		while(!unaryMinusIdx.isEmpty()) 
		{
			int idx = unaryMinusIdx.pop();
			modifiedInput.setCharAt(idx, '~');
		}
		
		return modifiedInput.toString();
	}
}
