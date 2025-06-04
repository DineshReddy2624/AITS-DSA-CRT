class MinStack {
    private Stack<Integer> mainStack;
    private Stack<Integer> minStack;
    public MinStack() 
    {
        mainStack = new Stack<>();
        minStack = new Stack<>();
    }
    public void push(int val) 
    {
        mainStack.push(val);
        if (minStack.isEmpty() || val <= minStack.peek()) 
        {
            minStack.push(val);
        } 
        else 
        {
            minStack.push(minStack.peek());
        }
    }
    public void pop() 
    {
        if (mainStack.isEmpty()) 
        {
            System.out.println("Cannot pop: Stack is empty.");
            return;
        }
        mainStack.pop();
        minStack.pop();
    }
    public int top() {
        if (mainStack.isEmpty()) {
            System.out.println("Cannot get top: Stack is empty.");
            return -1;
        }
        return mainStack.peek();
    }
    public int getMin() {
        if (minStack.isEmpty()) {
            System.out.println("Cannot get minimum: Stack is empty.");
            return -1;
        }
        return minStack.peek();
    }
}

/**
 * Your MinStack object will be instantiated and called as such:
 * MinStack obj = new MinStack();
 * obj.push(val);
 * obj.pop();
 * int param_3 = obj.top();
 * int param_4 = obj.getMin();
 */
