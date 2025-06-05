import java.util.Scanner;

public class HappyNumber {
    public static int getSquareSum(int n) {
        int sum = 0;
        while (n != 0) {
            int d = n % 10;
            sum += d * d;
            n /= 10;
        }
        return sum;
    }

    public static boolean isHappy(int n) {
        int slow = n, fast = getSquareSum(n);
        while (fast != 1 && slow != fast) {
            slow = getSquareSum(slow);
            fast = getSquareSum(getSquareSum(fast));
        }
        return fast == 1;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter a number: ");
        int num = sc.nextInt();
        if (isHappy(num)) {
            System.out.println(num + " is a Happy Number.");
        } else {
            System.out.println(num + " is NOT a Happy Number.");
        }
    }
}
