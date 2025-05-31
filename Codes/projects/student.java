import java.util.*;

class Student {
    int rollno;
    String name;
    String age;
    int[] marks;
    int totalMarks;
    float percentage;
    String grade;
    int duefees;

    Student(int rollno, String name, String age, int[] marks, int duefees) {
        this.rollno = rollno;
        this.name = name;
        this.age = age;
        this.marks = marks;
        this.duefees = duefees;
        calculateTotalMarks();
        calculatePercentage();
        assignGrade();
    }

    private void calculateTotalMarks() {
        totalMarks = Arrays.stream(marks).sum();
    }

    private void calculatePercentage() {
        percentage = (float) totalMarks / marks.length;
    }

    private void assignGrade() {
        if (percentage >= 90) grade = "A+";
        else if (percentage >= 80) grade = "A";
        else if (percentage >= 70) grade = "B+";
        else if (percentage >= 60) grade = "B";
        else if (percentage >= 50) grade = "C";
        else grade = "F";
    }

    void displayDetails() {
        System.out.println("\nRoll No: " + rollno);
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Marks: " + Arrays.toString(marks));
        System.out.println("Total Marks: " + totalMarks);
        System.out.println("Percentage: " + percentage + "%");
        System.out.println("Grade: " + grade);
        System.out.println("Due Fees: " + duefees);
    }
}

public class StudentManagement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Student[] students = {
            new Student(3001, "Azizes", "20", new int[]{85, 90, 78}, 200),
            new Student(3002, "Rehaman", "21", new int[]{88, 92, 80}, 4000),
            new Student(3003, "Afrid", "22", new int[]{75, 80, 70}, 5000),
            new Student(3004, "Akash", "19", new int[]{90, 95, 88}, 1000),
            new Student(3015, "Chenna", "23", new int[]{82, 85, 80}, 0),
            new Student(3017, "Dharama", "20", new int[]{78, 82, 76}, 3000),
            new Student(3019, "Dinesh", "21", new int[]{80, 85, 90}, 5000),
            new Student(3026, "Guru Mahrendra", "22", new int[]{95, 98, 92}, 15000),
            new Student(3030, "Hammad", "19", new int[]{88, 90, 85}, 0),
            new Student(3057, "Tharun", "20", new int[]{80, 75, 70}, 2000),
            new Student(233001, "Charan", "21", new int[]{85, 80, 90}, 0)
        };

        boolean running = true;

        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1: Search by Roll No");
            System.out.println("2: Show student name with percentage");
            System.out.println("3: Display all students");
            System.out.println("4: Students with due fees (True/False)");
            System.out.println("5: Search by Name");
            System.out.println("6: List students with Grade F");
            System.out.println("7: Show Topper(s)");
            System.out.println("8: List students with Due Fees");
            System.out.println("9: Exit");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine(); 

            switch (choice) {
                case 1:
                    System.out.println("     ");
                    System.out.print("Enter Roll No: ");
                    int rollNo = sc.nextInt();
                    boolean found = false;
                    for (Student s : students) {
                        if (s.rollno == rollNo) {
                            s.displayDetails();
                            found = true;
                            break;
                        }
                    }
                    if (!found) System.out.println("Student not found.");
                    break;

                case 2:
                    System.out.println("     ");
                    System.out.println("\nRoll No | Name | Percentage");
                    for (Student s : students) {
                        System.out.printf("%7d | %-15s | %.2f%%\n", s.rollno, s.name, s.percentage);
                    }
                    break;

                case 3:
                    System.out.println("     ");
                    for (Student s : students) s.displayDetails();
                    break;

                case 4:
                    System.out.println("     ");
                    System.out.println("Students with Due Fees:");
                    for (Student s : students) {
                        System.out.println("Roll No: " + s.rollno + ", Name: " + s.name + ", Due Fees: " + (s.duefees > 0));
                    }
                    break;

                case 5:
                    System.out.println("     ");
                    System.out.print("Enter Name to Search: ");
                    String searchName = sc.nextLine().toLowerCase();
                    boolean nameFound = false;
                    for (Student s : students) {
                        if (s.name.toLowerCase().contains(searchName)) {
                            s.displayDetails();
                            nameFound = true;
                        }
                    }
                    if (!nameFound) System.out.println("No student found with that name.");
                    break;

                case 6:
                    System.out.println("     ");
                    System.out.println("Students with Grade F:");
                    for (Student s : students) {
                        if ("F".equals(s.grade)) {
                            System.out.println(s.name + " (" + s.rollno + ")");
                        }
                        else{
                            System.out.println("No students with Grade F found.");
                        }
                    }
                    break;

                case 7:
                    System.out.println("     ");
                    float highest = Arrays.stream(students).map(s -> s.percentage).max(Float::compare).orElse(0f);
                    System.out.println("Topper(s):");
                    for (Student s : students) {
                        if (s.percentage == highest) {
                            System.out.println(s.name + " (" + s.rollno + ") - " + s.percentage + "%");
                        }
                    }
                    break;

                case 8:
                    System.out.println("     ");
                    System.out.println("Students with Due Fees:");
                    for (Student s : students) {
                        if (s.duefees != 0) {
                            System.out.println(s.name + " (" + s.rollno + ")");
                        }
                    }
                    break;

                case 9:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        sc.close();
    }
}
