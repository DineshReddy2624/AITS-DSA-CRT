import java.lang.*;
import java.util.*;
class Node
{
    int key;
    Node left,right;
    public Node(int items)
    {
        key = items;
        left = right = null;
    }

}
class BSTpostorder
{
    static Node insert(Node root,int key)
    {
        if(root == null)
        {
            return new Node(key);
        }
        if(root.key == key)
        {
            return root;
        }
        if(key < root.key)
        {
            root.left = insert(root.left,key);
        }
        else
        {
            root.right = insert(root.right,key);
        }
        return root;
    }
    static void postorder(Node root)
    {
        if(root != null)
        {
            postorder(root.left);
            postorder(root.right);
            System.out.println(" "+root.key);
        }
    }
    public static void main(String args[])
    {
        int[] values = {21, 43, 62, 17, 32, 13, 12, 7, 14, 19, 96, 60, 6, 5, 4, 1, 2};
        Node root = null;
        for (int value : values) {
            System.out.println("Inserting: " + value);
            root = insert(root, value);
        }
        postorder(root);
    }
}
