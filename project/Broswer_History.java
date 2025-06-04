import java.util.*;

class BrowserHistory 
{
    private Stack<String> prevStack;
    private Stack<String> nextStack;
    private String currentPage;

    
    public BrowserHistory(String homepage) 
    {
        prevStack = new Stack<>();
        nextStack = new Stack<>();
        currentPage = homepage;
    }

    public void visit(String url) 
    {
        prevStack.push(currentPage);
        currentPage = url;
        nextStack.clear();
        System.out.println("Visited: " + currentPage);
    }

    public void nextPage() 
    {
        if (!prevStack.isEmpty()) 
        {
            nextStack.push(currentPage);
            currentPage = prevStack.pop();
            System.out.println("Went back to: " + currentPage);
        } 
        else 
        {
            System.out.println("No previous page");
        }
    }

    public void previousPage() 
    {
        if (!nextStack.isEmpty()) 
        {
            prevStack.push(currentPage);
            currentPage = nextStack.pop();
            System.out.println("Went forward to: " + currentPage);
        } 
        else 
        {
            System.out.println("No next page");
        }
    }

    public void checkPage() 
    {
        System.out.println("Current page: " + currentPage);
    }

    public static void main(String[] args) 
    {
        BrowserHistory browser = new BrowserHistory("https://www.google.com/");
        browser.checkPage();

        
        browser.visit("https://www.geeksforgeeks.org/");
        browser.visit("https://www.geeksforgeeks.org/dsa-tutorial-learn-data-structures-and-algorithms/");
        browser.visit("https://www.geeksforgeeks.org/array-data-structure-guide/");

        browser.nextPage();
        browser.nextPage();
        browser.previousPage();
        browser.checkPage();
    }
}
