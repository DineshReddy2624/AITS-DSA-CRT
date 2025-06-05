import java.lang.*;
import java.util.*;

class Binary 
{
    class Node 
    {
        int key;
        Node left, right;

        public Node(int items) 
        {
            key = items;
            left = right = null;
        }
    }

    Node root;

    void traverseTree(Node node) 
    {
        if (node == null) return;

        traverseTree(node.left);
        traverseTree(node.right);
        System.out.println(" " + node.key);
    }

    public static void main(String args[]) 
    {
        Binary tree = new Binary();
        tree.root = tree.new Node(1);
        tree.root.left = tree.new Node(2);
        tree.root.right = tree.new Node(3);
        tree.root.left.left = tree.new Node(4);

        System.out.println("Binary Tree (postorder)");
        tree.traverseTree(tree.root);
    }
}
