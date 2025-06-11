import java.util.*;
import java.lang.*;
class main 
{
    public static void main(String[] args) 
	{
        sanner s = new scanner(System.in);
        int row1 = s.nextInt();
        int col1 = s.nextInt();
        int[][] mat1 = new int[row1][col1];
        for (int i = 0; i < row1; i++) 
		{
            for (int j = 0; j < col1; j++) 
			{
                mat1[i][j] = s.nextInt();
            }
        }
        int row2 = s.nextInt();
        int col2 = s.nextInt();
        int[][] mat2 = new int[row2][col2];
        for (int i = 0; i < row2; i++) 
		{
            for (int j = 0; j < col2; j++) 
			{
                mat2[i][j] = s.nextInt();
            }
        }
        int[][] result = new int[row1][col2];
        for (int i = 0; i < row1; i++) 
		{
            for (int j = 0; j < col2; j++) 
			{
                for (int k = 0; k < col1; k++) 
				{
                    result[i][j] += mat1[i][k] + mat2[k][j];
                }
            }
        }
        for (int i = 0; i < row1; i++) 
		{
            for (int j = 0; j < col2; j++) 
			{
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }
    }
}
