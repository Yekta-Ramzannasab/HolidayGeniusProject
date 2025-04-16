import models.Song;
import models.Album;
import roles.Artist;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Main {
    private static final String USER_DATA_FILE = "src/main/java/files/users.txt";
    private static final String ARTISTS_DIR = "src/main/java/files/Artist";
    private static final List<Artist> artists = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    static {
        try {
            loadArtists();
        } catch (IOException e) {
            System.err.println("Error loading artists: " + e.getMessage());
        }
    }

    private static void loadArtists() throws IOException {
        Path artistsPath = Paths.get(ARTISTS_DIR);
        if (!Files.exists(artistsPath)) {
            System.out.println("Artists directory not found!");
            return;
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artistsPath, Files::isDirectory)) {
            for (Path artistDir : stream) {
                artists.add(Artist.loadFromDirectory(artistDir));
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the Genius!");

        while (true) {
            System.out.println("\n1. Sign Up");
            System.out.println("2. Sign In");
            System.out.println("3. Exit");
            System.out.print("Enter your choice (1-3): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> signUpProcess();
                case 2 -> signInProcess();
                case 3 -> {
                    System.out.println("Exiting...");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice! Please enter 1-3.");
            }
        }
    }
    private static void signUpProcess() {
        System.out.println("\n=== Sign Up ===");
        System.out.println("Choose your role:");
        System.out.println("1. Admin");
        System.out.println("2. Artist");
        System.out.println("3. User");
        System.out.print("Enter your role (1-3): ");

        int roleChoice = scanner.nextInt();
        scanner.nextLine(); // consume
        String role;
        switch (roleChoice) {
            case 1:
                role = "admin";
                break;
            case 2:
                role = "artist";
                break;
            case 3:
                role = "user";
                break;
            default:
                System.out.println("Invalid role choice!");
                return;
        }
        scanner.nextLine();
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_DATA_FILE, true))) {
            writer.println(String.join(",", role, username, password, name, email));
            System.out.println("Registration successful!");
        } catch (IOException e) {
                       System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static void signInProcess() {
        System.out.println("\n=== Sign In ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try (Scanner fileScanner = new Scanner(new File(USER_DATA_FILE))) {
            boolean found = false;

            while (fileScanner.hasNextLine()) {
                String[] userData = fileScanner.nextLine().split(",");
                if (userData.length >= 5 &&
                        userData[1].equals(username) &&
                        userData[2].equals(password)) {

                    System.out.println("\nLogin successful!");
                    System.out.println("Welcome, " + userData[3] + "!");
                    System.out.println("Role: " + userData[0]);
                    System.out.println("Email: " + userData[4]);

                    showRoleMenu(userData[0]);
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("Invalid username or password!");
            }
        } catch (FileNotFoundException e) {
            System.out.println("No users registered yet!");
        }
    }

    private static void showRoleMenu(String role) {
        while (true) {
            System.out.println("\n=== " + role.toUpperCase() + " MENU ===");

            switch (role) {
                case "admin" -> {
                    System.out.println("1. Manage Users");
                    System.out.println("2. Verify Artists");
                    System.out.println("3. View Reports");
                }
                case "artist" -> {
                    System.out.println("1. Add New Song");
                    System.out.println("2. View Profile");
                    System.out.println("3. Manage Albums");
                }
                case "user" -> {
                    System.out.println("1. Search Songs");
                    System.out.println("2. View Favorites");
                    System.out.println("3. Follow Artists");
                }
            }

            System.out.println("4. Logout");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 4) {
                System.out.println("Logged out successfully!");
                break;
            } else if (role.equals("user") && choice == 1) {
                searchAndDisplaySongs();
            } else {
                System.out.println("Feature coming soon!");
            }
        }
    }


    // [Previous signUpProcess(), signInProcess(), showRoleMenu() methods remain exactly the same]

    private static void searchAndDisplaySongs() {
        System.out.println("\nAvailable Artists:");
        for (int i = 0; i < artists.size(); i++) {
            System.out.println((i + 1) + ". " + artists.get(i).getName());
        }

        System.out.print("Select an artist (enter number): ");
        int artistChoice = scanner.nextInt();
        scanner.nextLine();

        if (artistChoice < 1 || artistChoice > artists.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Artist artist = artists.get(artistChoice - 1);
        System.out.println("\nOptions for " + artist.getName() + ":");
        System.out.println("1. View All Songs");
        System.out.println("2. View Albums");
        System.out.print("Enter your choice: ");
        int option = scanner.nextInt();
        scanner.nextLine();

        if (option == 1) {
            showAllSongs(artist);
        } else if (option == 2) {
            showAlbums(artist);
        } else {
            System.out.println("Invalid option!");
        }
    }

    private static void showAllSongs(Artist artist) {
        List<Song> allSongs = new ArrayList<>();

        // Add album songs
        artist.getAlbums().forEach(album -> allSongs.addAll(album.getSongs()));

        // Add singles
        allSongs.addAll(artist.getSingles());

        displaySongs(allSongs);
    }

    private static void showAlbums(Artist artist) {
        List<Album> albums = artist.getAlbums();
        if (albums.isEmpty()) {
            System.out.println("No albums available.");
            return;
        }

        System.out.println("\nAlbums:");
        for (int i = 0; i < albums.size(); i++) {
            System.out.println((i + 1) + ". " + albums.get(i).getName());
        }

        System.out.print("Select an album (enter number): ");
        int albumChoice = scanner.nextInt();
        scanner.nextLine();

        if (albumChoice < 1 || albumChoice > albums.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        displaySongs(albums.get(albumChoice - 1).getSongs());
    }

    private static void displaySongs(List<Song> songs) {
        System.out.println("\nSongs:");
        for (int i = 0; i < songs.size(); i++) {
            Song song = songs.get(i);
            System.out.println((i + 1) + ". " + song.getTitle() +
                    (song.getAlbum() != null ? " (Album: " + song.getAlbum() + ")" : " (Single)"));
        }

        System.out.print("Select a song to view lyrics (enter number): ");
        int songChoice = scanner.nextInt();
        scanner.nextLine();

        if (songChoice < 1 || songChoice > songs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Song selectedSong = songs.get(songChoice - 1);
        System.out.println("\n=== " + selectedSong.getTitle() + " ===");
        if (selectedSong.getAlbum() != null) {
            System.out.println("Album: " + selectedSong.getAlbum());
        }
        System.out.println(selectedSong.getLyrics());
        System.out.println("====================");
    }
}