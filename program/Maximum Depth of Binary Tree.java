class Solution 
{
    public int maxDepth(TreeNode root) 
    {
        if (root == null) return 0;

        int leftnode = maxDepth(root.left);
        int rightnode = maxDepth(root.right);

        return 1 + Math.max(leftnode, rightnode);
    }
}
