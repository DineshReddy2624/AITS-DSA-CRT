import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class PhoneBookApp 
{
    private HashMap<String, String> contacts;

    public PhoneBookApp() 
    {
        contacts = new HashMap<>();
    }

    public void addContact(String name, String phoneNumber) 
    {
        contacts.put(name, phoneNumber);
        System.out.println("Contact added successfully.");
    }

    public void searchContact(String name) 
    {
        if (contacts.containsKey(name)) 
        {
            System.out.println("Name: " + name + ", Phone: " + contacts.get(name));
        } 
        else 
        {
            System.out.println("Contact not found.");
        }
    }

    public void deleteContact(String name) 
    {
        if (contacts.containsKey(name)) 
        {
            contacts.remove(name);
            System.out.println("Contact deleted.");
        } 
        else 
        {
            System.out.println("Contact not found.");
        }
    }

    public void displayContacts() 
    {
        if (contacts.isEmpty()) 
        {
            System.out.println("Phone book is empty.");
        } 
        else 
        {
            System.out.println("All Contacts:");
            for (Map.Entry<String, String> entry : contacts.entrySet()) 
            {
                System.out.println("Name: " + entry.getKey() + ", Phone: " + entry.getValue());
            }
        }
    }

    public static void main(String[] args) 
    {
        Scanner sc = new Scanner(System.in);
        PhoneBookApp phoneBook = new PhoneBookApp();
        int choice;

        do {
            System.out.println("\n---- Phone Book Menu ----");
            System.out.println("1. Add Contact");
            System.out.println("2. Search Contact");
            System.out.println("3. Delete Contact");
            System.out.println("4. Display All Contacts");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter phone number: ");
                    String phone = sc.nextLine();
                    phoneBook.addContact(name, phone);
                    break;

                case 2:
                    System.out.print("Enter name to search: ");
                    String searchName = sc.nextLine();
                    phoneBook.searchContact(searchName);
                    break;

                case 3:
                    System.out.print("Enter name to delete: ");
                    String deleteName = sc.nextLine();
                    phoneBook.deleteContact(deleteName);
                    break;

                case 4:
                    phoneBook.displayContacts();
                    break;

                case 5:
                    System.out.println("Exiting Phone Book. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        } while (choice != 5);
    }
}
