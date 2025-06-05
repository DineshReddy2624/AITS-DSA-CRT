import java.lang.*;
import java.util.*;

class BinaryTree 
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

        System.out.println(" " + node.key);
        traverseTree(node.left);
        traverseTree(node.right);
    }

    public static void main(String args[]) 
    {
        BinaryTree tree = new BinaryTree();
        tree.root = tree.new Node(1);
        tree.root.left = tree.new Node(2);
        tree.root.right = tree.new Node(3);
        tree.root.left.left = tree.new Node(4);

        System.out.println("Binary Tree (preorder)");
        tree.traverseTree(tree.root);
    }
}
