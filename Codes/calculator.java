import java.util.*;

class Calculator {

    // Converts decimal to fraction using continued fraction method
    public static String decimalToFraction(double decimal) {
        double tolerance = 1.0E-6;
        double h1 = 1, h2 = 0;
        double k1 = 0, k2 = 1;
        double b = decimal;

        do {
            double a = Math.floor(b);
            double aux = h1;
            h1 = a * h1 + h2;
            h2 = aux;
            aux = k1;
            k1 = a * k1 + k2;
            k2 = aux;
            b = 1 / (b - a);
        } while (Math.abs(decimal - h1 / k1) > decimal * tolerance);

        return ((int) h1) + "/" + ((int) k1);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================");
        System.out.println("     Welcome to Calculator");
        System.out.println("=================================");

        // Input Section
        System.out.print("Enter first number: ");
        float n1 = scanner.nextFloat();

        System.out.print("Enter second number: ");
        float n2 = scanner.nextFloat();

        System.out.print("Enter operator (+, -, *, /, %): ");
        char operator = scanner.next().charAt(0);

        boolean valid = true;
        float result = 0;

        System.out.println("\nCalculating...");

        // Operation Logic
        switch (operator) {
            case '+':
                result = n1 + n2;
                break;
            case '-':
                result = Math.abs(n1 - n2);
                break;
            case '*':
                result = n1 * n2;
                break;
            case '/':
                if (n2 == 0) {
                    System.err.println("Error: Division by zero is undefined.");
                    valid = false;
                } else {
                    result = n1 / n2;
                }
                break;
            case '%':
                result = n1 % n2;
                break;
            default:
                System.err.println("Invalid operator.");
                valid = false;
        }

        // Output Section
        if (valid) {
            System.out.println("Raw Result: " + result);

            if (result % 1 != 0) {
                System.out.println("\nResult is a decimal.");
                System.out.println("Choose output format:");
                System.out.println("1. Decimal");
                System.out.println("2. Fraction");

                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                if (choice == 1) {
                    System.out.println("Formatted Result (Decimal): " + result);
                } else if (choice == 2) {
                    System.out.println("Formatted Result (Fraction): " + decimalToFraction(result));
                } else {
                    System.out.println("Invalid format choice.");
                }
            } else {
                System.out.println("Result is an integer: " + (int) result);
            }
        }

        System.out.println("\nThank you for using the calculator!");
        System.out.println("=================================");
    }
}
