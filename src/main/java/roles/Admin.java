package roles;


import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Admin {
    private static final String REQUESTS_FILE = "src/main/java/files/admin_requests.txt";

    public static void addRequest(String request) {
        try {
            Files.write(Paths.get(REQUESTS_FILE),
                    (request + "\n---\n").getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error saving admin request: " + e.getMessage());
        }
    }

    public static void viewRequests() {
        try {
            if (!Files.exists(Paths.get(REQUESTS_FILE))) {
                System.out.println("No pending requests.");
                return;
            }

            List<String> requests = Files.readAllLines(Paths.get(REQUESTS_FILE));
            if (requests.isEmpty()) {
                System.out.println("No pending requests.");
                return;
            }

            System.out.println("\n=== Pending Admin Requests ===");
            for (int i = 0; i < requests.size(); i++) {
                if (requests.get(i).equals("---")) {
                    System.out.println("----------------------------");
                    continue;
                }
                System.out.println((i+1) + ". " + requests.get(i));
            }

            System.out.print("\nSelect request to process (number) or 0 to go back: ");
            // اضافه کردن منطق پردازش درخواست‌ها
        } catch (IOException e) {
            System.out.println("Error reading requests: " + e.getMessage());
        }
    }
}
