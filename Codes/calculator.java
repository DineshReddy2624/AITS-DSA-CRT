import java.util.*;

class calculator 
{
    public static String decimalToFraction(double decimal) {
        double tolerance = 1.0E-6;
        double h1 = 1;
        double h2 = 0;
        double k1 = 0;
        double k2 = 1;
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

    public static void main(String[] args) 
    {
        Scanner s = new Scanner(System.in);
        System.out.println("Enter First number:");
        float n1 = s.nextFloat();
        System.out.println("Enter second number");
        float n2 = s.nextFloat();
        System.out.println("Enter the operator (+,-,*,/,%)");
        char op = s.next().charAt(0);
        boolean valid = true;
        float result = 0;

        switch (op) 
        {
            case '+':
                result = n1 + n2;
                System.out.println("Result: " + result);
                break;
            case '-':
                if (n1 > n2) 
                {
                    result = n1 - n2;
                } 
                else 
                {
                    result = n2 - n1;
                }
                System.out.println("Result: " + result);
                break;
            case '*':
                result = n1 * n2;
                System.out.println("Result: " + result);
                break;
            case '/':
                try 
                {
                    if (n2 == 0) 
                    {
                        valid = false;
                        throw new Exception("Divide by zero is not defined");
                    } 
                    else 
                    {
                        result = n1 / n2;
                        System.out.println("Result: " + result);
                        break;
                    }
                } 
                catch (Exception e) 
                {
                    System.err.println("Not defined");
                    break;
                }
            case '%':
                result = n1 % n2;
                System.out.println("Result: " + result);
                break;
            default:
                valid = false;
                System.out.println("Invalid operator");
                break;
        }

        if (valid) 
        {
            if (result % 1 != 0) 
            {
                System.out.println("Result is a decimal.");
                System.out.println("Choose output format: (1) Decimal (2) Fraction");
                int choice = s.nextInt();
                if (choice == 1) 
                {
                    System.out.println("Result in Decimal: " + result);
                } 
                else if (choice == 2) 
                {
                    System.out.println("Result as Fraction: " + decimalToFraction(result));
                } 
                else 
                {
                    System.out.println("Invalid choice");
                }
            } 
            else 
            {
                System.out.println("Result is an integer: " + (int) result);
            }
        }
    }
}
