import java.util.*;

class TaskSchedulerUsingQueue 
{
    private ArrayList<Integer> queue;

    public TaskSchedulerUsingQueue()
    {
        queue = new ArrayList<>();
    }
    public void addTask(int task) 
    {
        queue.add(task);
        System.out.println("Added task: " + task);
    }
    public void removeTask(int task) 
    {
        queue.remove(Integer.valueOf(task));
        System.out.println("Removed task: " + task);
    }
    public int getSize() 
    {
        return queue.size();
    }
    public void displayTasks() 
    {
        System.out.println("Current tasks in queue: " + queue);
    }
    public void verifyAllTasks(Scanner sc) 
    {
        int i = 0;
        while (i < queue.size()) 
        {
            int task = queue.get(i);
            System.out.print("Is task " + task + " completed? (yes/no): ");
            String response = sc.next();

            if (response.equals("yes")) 
            {
                queue.remove(i); // Remove and don't increment i
                System.out.println("Task " + task + " marked as completed and removed.");
            } 
            else 
            {
                System.out.println("Task " + task + " is still pending.");
                break; 
            }
        }
    }
    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);
        TaskSchedulerUsingQueue scheduler = new TaskSchedulerUsingQueue();
        scheduler.addTask(1);
        scheduler.addTask(2);
        scheduler.addTask(3);
        System.out.println("\nAll tasks before verification");
        scheduler.displayTasks();
        System.out.println("\nTask Completion Verification");
        scheduler.verifyAllTasks(sc);
        System.out.println("\nRemaining tasks after verification");
        scheduler.displayTasks();
        System.out.println("Size of the task queue: " + scheduler.getSize());
    }
}
