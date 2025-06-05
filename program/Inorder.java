import java.lang.*;
import java.util.*;
class Inorder
{
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
    Node root;
    void traverseTree(Node node)
    {
        if(node == null) return;
        traverseTree(node.left);
        System.out.println(" "+node.key);
        traverseTree(node.right);
    }

    public static void main(String args[])
    {
        Inorder tree = new Inorder();
        tree.root = tree.new Node(1);
        tree.root.left = tree.new Node(2);
        tree.root.right = tree.new Node(3);
        tree.root.right.left = tree.new Node(4);
        
        System.out.println("Binary tree(Inorder)");
        tree.traverseTree(tree.root);
    }
}
