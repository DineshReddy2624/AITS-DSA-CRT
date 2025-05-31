class Solution {
    public boolean isPalindrome(int x) {
        int a = x;
        int rem =0;
        if(x<0)
        {
            return false;
        }
        while(x != 0)
        {
            int r = x %10;
            rem = rem * 10 + r;
            x = x/10;
        }
        if(a == rem)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
