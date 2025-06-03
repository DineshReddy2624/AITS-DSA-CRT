class Solution {
    public int reverse(int x) {
        int rem = 0;
        boolean isNegative = false;
        if (x<0)
        {
            isNegative = true;
            x = -(x);
        }
        while(x != 0)
        {
            int r = x % 10;
            long temp = (long) rem * 10 + r;
            if (temp > Integer.MAX_VALUE || temp < Integer.MIN_VALUE)    {
                return 0;
            }

            rem = (int) temp;
            x = x / 10;

        }
        if(isNegative)
        {
            rem = -rem;
        }
        return rem;
    }
}
