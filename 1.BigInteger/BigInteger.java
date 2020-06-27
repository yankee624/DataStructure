
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
  
  
public class BigInteger
{
    public static final String QUIT_COMMAND = "quit";
    public static final String MSG_INVALID_INPUT = "Wrong input.";
    public static final Pattern EXPRESSION_PATTERN = Pattern.compile("([+-]?)\\s*([0-9]+)\\s*([+*-])\\s*([+-]?)\\s*([0-9]+)");

    public byte[] val;
    public int leng;
    public boolean neg = false;
  
    // length given
    public BigInteger(int leng)
    {
    	val = new byte[leng];
    	this.leng = leng;
    }
    
    // array of int & sign given
    public BigInteger(int[] num, boolean neg)
    {
    	leng = num.length;
    	val = new byte[leng];
    	for(int i=0; i<leng; i++) {
    		val[i] = (byte) num[i];
    	}
    	this.neg = neg;
    }
    
    // string (sequence of digits) given
    public BigInteger(String s)
    {
    	leng = s.length();
    	val = new byte[leng];
    	for(int i=0; i<leng; i++) {
    		val[i] = (byte) (s.charAt(leng-1-i) - '0');
    	}
    	
    }
  
    // only + +, - -  (two numbers are both + or both -)
    public BigInteger add(BigInteger other)
    {
    	BigInteger result, sm, lg;
    	int i;
    	if(this.leng >= other.leng) {
    		lg = this;
    		sm = other;
    		result = new BigInteger(lg.leng + 1);
    	} else {
    		lg = other;
    		sm = this;
    		result = new BigInteger(lg.leng + 1);
    	}
    	
    	// simply add each digit
    	for(i=0; i<sm.leng; i++) {
    		result.val[i] = (byte) (lg.val[i] + sm.val[i]);
    	}
    	for(; i<lg.leng; i++) {
    		result.val[i] = lg.val[i];
    	}
    	
    	// deal with carry if each digit >=10
    	int carry = 0;
    	for(i=0; i<result.leng; i++) {
    		result.val[i] += carry;
    		if(result.val[i] >= 10) {
    			result.val[i] -= 10;
    			carry = 1;
    		} else {
    			carry = 0;
    		}
    	}
    	// final carry
    	if(carry == 1) {
    		result.val[result.leng-1] = 1; 
    	}
    	
    	// + or -
    	if(lg.neg && sm.neg) {
    		result.neg = true;
    	} else {
    		result.neg = false;
    	}
    	
    	return result;
    }
    // only + +  (two numbers are both positive)
    public BigInteger subtract(BigInteger other)
    {
    	BigInteger result, sm, lg;
    	int i;
    	if(absCompare(this, other) < 0) {
    		sm = this;
    		lg = other;
    		result = new BigInteger(lg.leng);
    		result.neg = true;
    	} else if(absCompare(this, other) > 0) {
    		sm = other;
    		lg = this;
    		result = new BigInteger(lg.leng);
    		result.neg = false;
    	} else {
    		result = new BigInteger(1);
    		result.val[0] = 0;
    		return result;
    	}
    	
    	// simply subtract each digit
    	for(i=0; i<sm.leng; i++) {
    		result.val[i] = (byte) (lg.val[i] - sm.val[i]);
    	}
    	for(; i<lg.leng; i++) {
    		result.val[i] = lg.val[i];
    	}
    	
    	// deal with carry if each digit < 0
    	int carry = 0;
    	for(i=0; i<result.leng; i++) {
    		result.val[i] += carry;
    		if(result.val[i] < 0) {
    			result.val[i] += 10;
    			carry = -1;
    		} else {
    			carry = 0;
    		}
    	}
    	return result;
    }
  
    public BigInteger multiply(BigInteger other)
    {
    	int[] result = new int[this.leng + other.leng];
    	for(int i=0; i<result.length; i++) {
    		result[i] = 0;
    	}
    	
    	for(int i=0; i<other.leng; i++) {
    		int multiplier = other.val[i];
    		for(int j=0; j<this.leng; j++) {
    			result[i+j] += multiplier * this.val[j];
    		}
    	}
    	
    	int carry = 0;
    	for(int i=0; i<result.length; i++) {
    		result[i] += carry;
    		if(result[i] >= 10) {
    			carry = result[i] / 10; // quotient
    			result[i] -= carry * 10; // remainder
    		} else {
    			carry = 0;
    		}
    	}
    	
    	return new BigInteger(result, this.neg != other.neg);
    }
    
    // compare absolute value.
    // if num1 < num2, -1 
    // if num1 > num2, 1 
    // if num1 == num2, 0
    static int absCompare(BigInteger num1, BigInteger num2) {
    	int result = 0;
    	if(num1.leng < num2.leng) {
    		result = -1;
    	} else if(num1.leng > num2.leng) {
    		result = 1;
    	} else {
    		for(int i=num1.leng-1; i>=0; i--) {
    			if(num1.val[i] < num2.val[i]) {
    				result = -1;
    				break;
    			} else if(num1.val[i] > num2.val[i]) {
    				result = 1;
    				break;
    			}
    		}
    	}
    	return result;
    }
  
    @Override
    public String toString()
    {
    	StringBuffer sb = new StringBuffer();
    	if(neg) {
    		sb.append("-");
    	}
    	int i = leng-1;
    	// skip zeros that come first
    	while(val[i] == 0) {
    		i--;
    		// value is 0
    		if(i < 0) {
    			return "0";
    		}
    	}
    	for(; i>=0; i--) {
			sb.append(val[i]);	
    	}
    	return sb.toString();
    }
  
    static BigInteger evaluate(String input) throws IllegalArgumentException
    {
    	BigInteger result = null;
    	Matcher match = EXPRESSION_PATTERN.matcher(input);
    	if(match.find()) {
    		BigInteger num1 = new BigInteger(match.group(2));
		    BigInteger num2 = new BigInteger(match.group(5));
		    if(match.group(1).equals("-")) {
		    	num1.neg = true;
		    }
		    if(match.group(4).equals("-")) {
		    	num2.neg = true;
		    }
		    
		    ///////// CALCULATE /////////
		    // add, subtract
		    if(match.group(3).equals("+")) {
		    	// + + +, - + -
		    	if(num1.neg == num2.neg) {
		    		result = num1.add(num2);
		    	} 
		    	else {
		    		// - + +
		    		if(num1.neg) {
		    			num1.neg = false;
		    			result = num2.subtract(num1);
		    		}
		    		// + + -
		    		else {
		    			num2.neg = false;
		    			result = num1.subtract(num2);
		    		}
		    	}
		    	
		    }else if(match.group(3).equals("-")) {
		    	if(num1.neg != num2.neg) {
		    		// - - +
		    		if(num1.neg) {
		    			num2.neg = true;
		    			result = num1.add(num2);
		    		}
		    		// + - -
		    		else {
		    			num2.neg = false;
		    			result = num1.add(num2);
		    		}
		    	} else {
		    		// + - +
		    		if(!num1.neg) {
		    			result = num1.subtract(num2);
		    		}
		    		// - - -
		    		else {
		    			num1.neg = false;
		    			num2.neg = false;
		    			result = num2.subtract(num1);
		    		}
		    	}
		    }
		    // multiply
		    else {
		    	result = num1.multiply(num2);
		    }
		    
    	} else {
    		throw new IllegalArgumentException();
    	}
	    return result;
    }
    
  
    public static void main(String[] args) throws Exception
    {
        try (InputStreamReader isr = new InputStreamReader(System.in))
        {
            try (BufferedReader reader = new BufferedReader(isr))
            {
                boolean done = false;
                while (!done)
                {
                    String input = reader.readLine();
  
                    try
                    {
                        done = processInput(input);
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.err.println(MSG_INVALID_INPUT);
                    }
                }
            }
        }
    }
  
    static boolean processInput(String input) throws IllegalArgumentException
    {
        boolean quit = isQuitCmd(input);
  
        if (quit)
        {
            return true;
        }
        else
        {
            BigInteger result = evaluate(input);
            System.out.println(result.toString());
  
            return false;
        }
    }
  
    static boolean isQuitCmd(String input)
    {
        return input.equalsIgnoreCase(QUIT_COMMAND);
    }
    
    
    
}
