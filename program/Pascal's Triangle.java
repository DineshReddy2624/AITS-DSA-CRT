import java.util.*;

class Solution {
    public List<List<Integer>> generate(int numRows)
    {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < numRows; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j <= i; j++) 
            {
                if (j == 0 || j == i) 
                {
                    row.add(1);
                } 
                else 
                {
                    int num1 = result.get(i - 1).get(j - 1);
                    int num2 = result.get(i - 1).get(j);
                    row.add(num1 + num2);
                }
            }
            result.add(row);
        }
        return result;
    }
}
