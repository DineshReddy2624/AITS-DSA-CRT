class Solution {
    public double findMedianSortedArrays(int[] nums1, int[] nums2) {
        int[] arr =new int[nums1.length + nums2.length];
        System.arraycopy(nums1, 0, arr, 0, nums1.length);
        System.arraycopy(nums2, 0, arr, nums1.length, nums2.length);
        int n = arr.length;
        Arrays.sort(arr);
        if(n%2==0)
        {
           double result = (arr[n/2-1]+arr[n/2])/2.0;
           return result;
        }
        else
        {
            return(arr[n/2]);
        }
    }
}
