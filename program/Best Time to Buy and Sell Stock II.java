class Solution 
{
    public int maxProfit(int[] prices) 
    {
        int val = 0;
        for(int i = 1; i<=prices.length-1;i++)
        {
            if(prices[i] > prices[i-1])
            {
                val += prices[i] - prices[i-1];
            }
        }
        return val;
    }
}
