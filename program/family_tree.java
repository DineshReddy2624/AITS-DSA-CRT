import java.util.*;

class Inorder {
    class Node {
        String name;
        Node left, right;

        public Node(String name) {
            this.name = name;
            left = right = null;
        }
    }

    Node root;

    void traverseTree(Node node) {
        if (node == null) return;

        traverseTree(node.left);
        System.out.println(" " + node.name);
        traverseTree(node.right);
    }

    public static void main(String args[]) {
        Inorder tree = new Inorder();

        tree.root = tree.new Node("Grandfather");
        tree.root.left = tree.new Node("Father");
        tree.root.right = tree.new Node("Uncle");

        tree.root.left.left = tree.new Node("You");
        tree.root.left.right = tree.new Node("Sibling");

        tree.root.right.left = tree.new Node("Cousin1");
        tree.root.right.right = tree.new Node("Cousin2");

        System.out.println("Family Tree (Inorder Traversal):");
        tree.traverseTree(tree.root);
    }
}
