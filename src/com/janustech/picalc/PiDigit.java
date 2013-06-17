package com.janustech.picalc;

public class PiDigit {
	 /**
	 * Computes the nth digit of Pi in base-16.
	 * 
	 * If n < 0, return -1.
	 * 
	 * @param n The digit of Pi to retrieve in base-16.
	 * @return The nth digit of Pi in base-16.
	 */
	public static int piDigit(int n) {
	    if (n < 0) return -1;

	    n -= 1;
	    double x = 4 * piTerm(1, n) - 2 * piTerm(4, n) -
	               piTerm(5, n) - piTerm(6, n);
	    x = x - Math.floor(x);

	    return (int)(x * 16);
	}
	
	private static double piTerm(int j, int n) {
	    // Calculate the left sum
	    double s = 0;
	    for (int k = 0; k <= n; ++k) {
	        int r = 8 * k + j;
	        s += powerMod(16, n-k, r) / (double) r;
	        s = s - Math.floor(s);
	    }

	    // Calculate the right sum
	    double t = 0;
	    int k = n+1;
	    // Keep iterating until t converges (stops changing)
	    while (true) {
	        int r = 8 * k + j;
	        double newt = t + Math.pow(16, n-k) / r;
	        if (t == newt) {
	            break;
	        } else {
	            t = newt;
	        }
	        ++k;
	    }

	    return s+t;
	}

	private static double powerMod(int i, int j, int r) {
		// TODO Auto-generated method stub
		return 0;
	}
}
