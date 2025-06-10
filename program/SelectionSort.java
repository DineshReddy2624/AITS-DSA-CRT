class SelectionSort
{
    public static void main(String args[])
    {
        int[] arr = {5,3,10,1,8,2};
        sort(arr);
    }
    public static void sort(int arr[])
    {
        for(int i = 0; i < arr.length - 1; i++)
        {
            int MI = i;
            for(int j = i + 1; j < arr.length; j++)
            {
                if(arr[j] < arr[MI])
                {
                    MI = j;
                }
            }
            if(MI != i)
            {
                int temp = arr[i];
                arr[i] = arr[MI];
                arr[MI] = temp;
            }
        }
        for(int i = 0; i < arr.length; i++)
        {
            System.out.print(" "+arr[i]);
        }
    }
}
