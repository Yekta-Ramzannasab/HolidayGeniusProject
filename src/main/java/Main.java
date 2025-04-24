import models.*;
import roles.*;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;


public class Main {
    private static  String USER_DATA_FILE = "src/main/java/files/users.txt";
    private static  String ARTISTS_DIR = "src/main/java/files/Artist";
    private static  String PENDING_ARTISTS_FILE = "src/main/java/files/pending_artists.txt";
    private static  String ADMIN_REQUESTS_FILE = "src/main/java/files/admin_requests.txt";
    private static  List<Artist> artists = new ArrayList<>();
    private static  List<User> users = new ArrayList<>();
    private static  List<Artist> pendingArtists = new ArrayList<>();
    private static User currentUser = null;

    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    private static void initializeData() {
        try {
            loadArtists();
            loadUsers();
            loadPendingArtists();

        } catch (IOException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private static void loadArtists() throws IOException {
        Path artistsPath = Paths.get(ARTISTS_DIR);
       // if (!Files.exists(artistsPath)) {
            Files.createDirectories(artistsPath);
        //}

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(artistsPath, Files::isDirectory)) {
            for (Path artistDir : stream) {
                Artist artist = new Artist(artistDir.getFileName().toString());

                // Load artist info
                Path infoFile = artistDir.resolve("info.txt");
                if (Files.exists(infoFile)) {
                    List<String> infoLines = Files.readAllLines(infoFile);
                    // Parse artist info (username, password, email, etc.)
                }

                // Load singles
                Path songsDir = artistDir.resolve("Songs");
                if (Files.exists(songsDir)) {
                    try (DirectoryStream<Path> songFiles = Files.newDirectoryStream(songsDir, "*.txt")) {
                        for (Path songFile : songFiles) {
                            Song song = Song.loadFromFile(songFile);
                            if (song != null) {
                                artist.addSingle(song);

                                // Find corresponding audio file
                                String audioFileName = songFile.getFileName().toString()
                                        .replace(".txt", ".wav");
                                Path audioFile = songsDir.resolve(audioFileName);
                                if (Files.exists(audioFile)) {
                                    song.setAudioFilePath(audioFile.toString());
                                }
                            }
                        }
                    }
                }

                // Load albums
                Path albumsDir = artistDir.resolve("Albums");
                if (Files.exists(albumsDir)) {
                    try (DirectoryStream<Path> albumDirs = Files.newDirectoryStream(albumsDir, Files::isDirectory)) {
                        for (Path albumDir : albumDirs) {
                            Album album = new Album(albumDir.getFileName().toString(),
                                    artist.getName(),
                                    getAlbumYear(albumDir));

                            try (DirectoryStream<Path> albumSongs = Files.newDirectoryStream(albumDir, "*.txt")) {
                                for (Path songFile : albumSongs) {
                                    Song song = Song.loadFromFile(songFile);
                                    if (song != null) {
                                        song.setAlbum(album.getName());
                                        album.addSong(song);
                                    }
                                }
                            }

                            artist.addAlbum(album);
                        }
                    }
                }

                artists.add(artist);
            }
        }
    }

    private static int getAlbumYear(Path albumDir) {

        try {
            String dirName = albumDir.getFileName().toString();
            if (dirName.matches(".*\\d{4}.*")) {
                return Integer.parseInt(dirName.replaceAll(".*?(\\d{4}).*", "$1"));
            }
        } catch (Exception e) {
            System.err.println("Error parsing album year: " + e.getMessage());
        }
        return 2023;
    }

    public static Song loadFromFile(Path songFile) {
        try {
            String title = songFile.getFileName().toString().replace(".txt", "");
            String lyrics = Files.readString(songFile);

            return new Song(title, "", null, lyrics);
        } catch (IOException e) {
            System.err.println("Error loading song: " + e.getMessage());
            return null;
        }
    }

    private static void createDefaultArtists() throws IOException {

        Artist artist1 = new Artist("Default Artist 1");
        artist1.setUsername("artist1");
        artist1.setPassword("1234");
        artist1.setEmail("artist1@example.com");
        artist1.setBio("This is a default artist for testing purposes");


        Song song1 = new Song("Sample Song 1", artist1.getName(), null,
                "This is sample lyrics for song 1\nLine 2\nLine 3");
        song1.setAudioFilePath("src/main/java/files/sample1.wav"); // مسیر فایل صوتی نمونه

        Song song2 = new Song("Sample Song 2", artist1.getName(), null,
                "Lyrics for sample song 2\nAnother line");

        artist1.addSingle(song1);
        artist1.addSingle(song2);


        Album album1 = new Album("Sample Album", artist1.getName(), 2023);
        Song albumSong1 = new Song("Album Song 1", artist1.getName(), album1.getName(),
                "Album song lyrics");
        Song albumSong2 = new Song("Album Song 2", artist1.getName(), album1.getName(),
                "More album lyrics");

        album1.addSong(albumSong1);
        album1.addSong(albumSong2);
        artist1.addAlbum(album1);

        artist1.saveToDirectory();
        artists.add(artist1);

        Artist artist2 = new Artist("Default Artist 2");
        artist2.setUsername("artist2");
        artist2.setPassword("1234");
        artist2.setEmail("artist2@example.com");

        Song song3 = new Song("Test Song", artist2.getName(), null,
                "Test lyrics for artist 2");
        artist2.addSingle(song3);

        artist2.saveToDirectory();
        artists.add(artist2);
    }

    private static void loadUsers() throws IOException {
        Path usersPath = Paths.get(USER_DATA_FILE);
        if (!Files.exists(usersPath)) {
            Files.createFile(usersPath);
            System.out.println("No users registered yet!");
            return;
        }

        List<String> userLines = Files.readAllLines(usersPath);
        for (String line : userLines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                User user = new User(parts[1], parts[2], parts[3], parts[4]);
                user.setRole(parts[0]);
                users.add(user);
            }
        }
    }

    private static void loadPendingArtists() throws IOException {
        Path pendingPath = Paths.get(PENDING_ARTISTS_FILE);
        if (!Files.exists(pendingPath)) {
            Files.createFile(pendingPath);
            return;
        }

        List<String> pendingLines = Files.readAllLines(pendingPath);
        for (String line : pendingLines) {
            String[] parts = line.split(",");
            if (parts.length >= 5) {
                Artist artist = new Artist(parts[3]); // Using full name as artist name
                pendingArtists.add(artist);
            }
        }
    }

    public static void main (String[] args) {
        initializeData();
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
        scanner.nextLine();
        String role;
        switch (roleChoice) {
            case 1 -> role = "admin";
            case 2 -> role = "artist";
            case 3 -> role = "user";
            default -> {
                System.out.println("Invalid role choice!");
                return;
            }
        }

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter your full name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        if (role.equals("artist")) {
            // Save to pending artists file
            try (PrintWriter writer = new PrintWriter(new FileWriter(PENDING_ARTISTS_FILE, true))) {
                writer.println(String.join(",", role, username, password, name, email));
                System.out.println("Your artist registration request has been sent to admin for approval.");
            } catch (IOException e) {
                System.out.println("Error saving artist request: " + e.getMessage());
            }
        } else {
            // Save to users file
            try (PrintWriter writer = new PrintWriter(new FileWriter(USER_DATA_FILE, true))) {
                writer.println(String.join(",", role, username, password, name, email));
                System.out.println("Registration successful!");
                // Add to users list
                User newUser = new User(username, password, name, email);
                newUser.setRole(role);
                users.add(newUser);
            } catch (IOException e) {
                System.out.println("Error saving user data: " + e.getMessage());
            }
        }
    }

    private static void signInProcess() {
        System.out.println("\n=== Sign In ===");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        try {
            List<String> userLines = Files.readAllLines(Paths.get(USER_DATA_FILE));
            boolean found = false;

            for (String line : userLines) {
                String[] userData = line.split(",");
                if (userData.length >= 5 &&
                        userData[1].equals(username) &&
                        userData[2].equals(password)) {

                    System.out.println("\nLogin successful!");
                    System.out.println("Welcome, " + userData[3] + "!");
                    System.out.println("Role: " + userData[0]);
                    System.out.println("Email: " + userData[4]);

                    currentUser = users.stream()
                            .filter(u -> u.getUsername().equals(username))
                            .findFirst()
                            .orElseGet(() -> {
                                User newUser = new User(username, password, userData[3], userData[4]);
                                newUser.setRole(userData[0]);
                                users.add(newUser);
                                return newUser;
                            });

                    showRoleMenu(userData[0]);
                    found = true;
                    break;
                }
            }

            if (!found) {
                // Check pending artists
                List<String> pendingLines = Files.readAllLines(Paths.get(PENDING_ARTISTS_FILE));
                for (String line : pendingLines) {
                    String[] userData = line.split(",");
                    if (userData.length >= 5 &&
                            userData[1].equals(username) &&
                            userData[2].equals(password)) {
                        System.out.println("\nYour artist account is still pending approval.");
                        return;
                    }
                }
                System.out.println("Invalid username or password!");
            }
        } catch (IOException e) {
            System.out.println("Error reading user data: " + e.getMessage());
        }
    }

    public enum MenuType {
        ADMIN(5, List.of(
                "1. Manage Users",
                "2. Verify Artists",
                "3. View Reports",
                "4. Manage Content",
                "5. Logout"
        )),

        ARTIST(6, List.of(
                "1. Add New Song",
                "2. View Profile",
                "3. Manage Albums",
                "4. View Requests",
                "5. View Statistics",
                "6. Logout"
        )),

        USER(6, List.of(
                "1. Search Songs",
                "2. View Favorites",
                "3. Follow Artists",
                "4. View Following",
                "5. View Charts",
                "6. Logout"
        ));

        public final int logoutOption;
        public final List<String> menuItems;

        MenuType(int logoutOption, List<String> menuItems) {
            this.logoutOption = logoutOption;
            this.menuItems = menuItems;
        }
    }

    private static void showRoleMenu(String role) {
        MenuType menu = switch (role.toLowerCase()) {
            case "admin" -> MenuType.ADMIN;
            case "artist" -> MenuType.ARTIST;
            default -> MenuType.USER;
        };

        while (true) {
            System.out.println("\n=== " + role.toUpperCase() + " MENU ===");
            menu.menuItems.forEach(System.out::println);

            System.out.print("Enter your choice (1-" + menu.logoutOption + "): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == menu.logoutOption) {
                System.out.println("Logged out successfully!");
                currentUser = null;
                break;
            }

            handleMenuChoice(role, choice);
        }
    }

    private static void handleMenuChoice(String role, int choice) {
        switch (role.toLowerCase()) {
            case "admin" -> handleAdminChoice(choice);
            case "artist" -> handleArtistChoice(choice);
            default -> handleUserChoice(choice);
        }
    }

    private static void showAdminMenu() {
        System.out.println("1. Manage Users");
        System.out.println("2. Verify Pending Artists");
        System.out.println("3. View Reports");
        System.out.println("4. Manage Songs");
        System.out.println("5. Logout");
    }

    private static void showArtistMenu() {
        System.out.println("1. Add New Song");
        System.out.println("2. View Profile");
        System.out.println("3. Manage Albums");
        System.out.println("4. View Requests");
    }

    private static void showUserMenu() {
//        System.out.println("\n=== USER MENU ===");
        System.out.println("1. Search Songs");
        System.out.println("2. View Favorites");
        System.out.println("3. Follow Artists");
        System.out.println("4. View Following");
        System.out.println("5. View Charts");
        System.out.println("6. Logout");
        System.out.print("Enter your choice (1-6): ");
    }

    private static void handleAdminChoice(int choice) {
        switch (choice) {
            case 1 -> manageUsers();
            case 2 -> verifyArtists();
            case 3 -> viewReports();
            case 4 -> manageContent();
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void handleArtistChoice(int choice) {
        switch (choice) {
            case 1 -> addNewSong();
            case 2 -> viewArtistProfile();
            case 3 -> manageAlbums();
            case 4 -> viewRequests();
            case 5 -> viewStatistics();
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void handleUserChoice(int choice) {
        switch (choice) {
            case 1 -> searchAndDisplaySongs();
            case 2 -> viewFavorites();
            case 3 -> followArtists();
            case 4 -> viewFollowing();
            case 5 -> viewCharts();
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void manageUsers() {
        System.out.println("\n=== Manage Users ===");
        if (users.isEmpty()) {
            System.out.println("No users registered yet.");
            return;
        }

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            System.out.println((i + 1) + ". " + user.getUsername() + " (" + user.getRole() + ")");
        }

        System.out.print("\nSelect a user to manage (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > users.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        User selectedUser = users.get(choice - 1);
        System.out.println("\nUser: " + selectedUser.getUsername());
        System.out.println("Role: " + selectedUser.getRole());
        System.out.println("Name: " + selectedUser.getFullName());
        System.out.println("Email: " + selectedUser.getEmail());

        System.out.println("\nOptions:");
        System.out.println("1. Change role");
        System.out.println("2. Delete user");
        System.out.println("3. Back");

        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1 -> {
                System.out.print("Enter new role (admin/artist/user): ");
                String newRole = scanner.nextLine();
                selectedUser.setRole(newRole);
                saveAllUsers();
                System.out.println("Role updated successfully!");
            }
            case 2 -> {
                users.remove(choice - 1);
                saveAllUsers();
                System.out.println("User deleted successfully!");
            }
            case 3 -> { /* Do nothing, will return */ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void submitLyricChange(Song song, String newLyrics) {
        Artist artist = findArtistByName(song.getArtist());
        String request = "Lyric change request for: " + song.getTitle() +
                "\nArtist: " + song.getArtist() +
                "\nUser: " + currentUser.getUsername() +
                "\nNew Lyrics:\n" + newLyrics;

        if (artist != null) {
            artist.addLyricRequest(new LyricRequest(song, newLyrics, currentUser.getUsername()));
            try {
                artist.saveToDirectory();
                System.out.println("Request sent to artist: " + artist.getName());
            } catch (IOException e) {
                Admin.addRequest(request);
                System.out.println("Sent to admin (artist save failed)");
            }
        } else {
            Admin.addRequest(request);
            System.out.println("Artist not found! Sent to admin.");
        }
    }


    private static void submitComment(Song song, String commentText) {
        String request = "New comment for: " + song.getTitle() +
                "\nArtist: " + song.getArtist() +
                "\nUser: " + currentUser.getUsername() +
                "\nComment:\n" + commentText;

        Admin.addRequest(request);
        System.out.println("Comment sent to admin for approval.");
    }

    private static void saveAllUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_DATA_FILE))) {
            for (User user : users) {
                writer.println(String.join(",",
                        user.getRole(),
                        user.getUsername(),
                        user.getPassword(),
                        user.getFullName(),
                        user.getEmail()));
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void verifyArtists() {
        System.out.println("\n=== Pending Artists ===");
        if (pendingArtists.isEmpty()) {
            System.out.println("No pending artist requests.");
            return;
        }

        try {
            List<String> pendingLines = Files.readAllLines(Paths.get(PENDING_ARTISTS_FILE));
            for (int i = 0; i < pendingLines.size(); i++) {
                String[] parts = pendingLines.get(i).split(",");
                if (parts.length >= 5) {
                    System.out.println((i + 1) + ". " + parts[3] + " (" + parts[4] + ")");
                }
            }

            System.out.print("Select an artist to approve/reject (enter number): ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice < 1 || choice > pendingLines.size()) {
                System.out.println("Invalid choice!");
                return;
            }

            String[] parts = pendingLines.get(choice - 1).split(",");
            System.out.println("\nArtist: " + parts[3]);
            System.out.println("Email: " + parts[4]);
            System.out.print("Approve this artist? (y/n): ");
            String decision = scanner.nextLine();

            if (decision.equalsIgnoreCase("y")) {
                // Create the artist
                Artist artist = new Artist(parts[3]);
                artist.setUsername(parts[1]);
                artist.setPassword(parts[2]);
                artist.setEmail(parts[4]);

                // Create artist directory
                Path artistPath = Paths.get(ARTISTS_DIR, artist.getName());
                Files.createDirectories(artistPath);

                // Save artist info
                artist.saveToDirectory();

                // Add to artists list
                artists.add(artist);

                // Add to users list
                User artistUser = new User(parts[1], parts[2], parts[3], parts[4]);
                artistUser.setRole("artist");
                users.add(artistUser);

                // Save to users file

                try (PrintWriter writer = new PrintWriter(new FileWriter("src\\main\\java\\files\\users.txt", true))) {
                    writer.println(String.join(",", "artist", parts[1], parts[2], parts[3], parts[4]));
                }

                // Remove from pending
                pendingLines.remove(choice - 1);
                Files.write(Paths.get(PENDING_ARTISTS_FILE), pendingLines);

                pendingArtists.removeIf(a -> a.getName().equals(parts[3]));
                System.out.println("Artist approved successfully!");
            } else {
                // Just remove from pending
                pendingLines.remove(choice - 1);
                Files.write(Paths.get(PENDING_ARTISTS_FILE), pendingLines);

                pendingArtists.removeIf(a -> a.getName().equals(parts[3]));
                System.out.println("Artist request rejected.");
            }
        } catch (IOException e) {
            System.out.println("Error processing artist request: " + e.getMessage());
        }
    }

    private static void viewReports() {
        System.out.println("\n=== Reports ===");

        // User statistics
        long adminCount = users.stream().filter(u -> u.getRole().equals("admin")).count();
        long artistCount = users.stream().filter(u -> u.getRole().equals("artist")).count();
        long userCount = users.stream().filter(u -> u.getRole().equals("user")).count();

        System.out.println("\nUser Statistics:");
        System.out.println("Admins: " + adminCount);
        System.out.println("Artists: " + artistCount);
        System.out.println("Regular Users: " + userCount);
        System.out.println("Total Users: " + users.size());

        // Artist statistics
        System.out.println("\nArtist Statistics:");
        System.out.println("Total Artists: " + artists.size());
        if (!artists.isEmpty()) {
            Artist mostPopular = artists.stream()
                    .max(Comparator.comparing(Artist::getFollowers))
                    .orElse(null);
            System.out.println("Most Popular Artist: " +
                    (mostPopular != null ? mostPopular.getName() + " (" + mostPopular.getFollowers() + " followers)" : "N/A"));
        }

        // Song statistics
        List<Song> allSongs = getAllSongs();
        System.out.println("\nSong Statistics:");
        System.out.println("Total Songs: " + allSongs.size());
        if (!allSongs.isEmpty()) {
            Song mostViewed = allSongs.stream()
                    .max(Comparator.comparing(Song::getViews))
                    .orElse(null);
            System.out.println("Most Viewed Song: " +
                    (mostViewed != null ? mostViewed.getTitle() + " by " + mostViewed.getArtist() +
                            " (" + mostViewed.getViews() + " views)" : "N/A"));

            Song mostLiked = allSongs.stream()
                    .max(Comparator.comparing(Song::getLikes))
                    .orElse(null);
            System.out.println("Most Liked Song: " +
                    (mostLiked != null ? mostLiked.getTitle() + " by " + mostLiked.getArtist() +
                            " (" + mostLiked.getLikes() + " likes)" : "N/A"));
        }
    }

    private static List<Song> getAllSongs() {
        List<Song> allSongs = new ArrayList<>();
        for (Artist artist : artists) {
            allSongs.addAll(artist.getSingles());
            for (Album album : artist.getAlbums()) {
                allSongs.addAll(album.getSongs());
            }
        }
        return allSongs;
    }

    private static void addNewSong() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            System.out.println("Artist profile not found!");
            return;
        }

        System.out.println("\n=== Add New Song ===");
        System.out.print("Enter song title: ");
        String title = scanner.nextLine();

        System.out.print("Enter lyrics: ");
        String lyrics = scanner.nextLine();

        System.out.print("Is this song part of an album? (y/n): ");
        String isAlbumSong = scanner.nextLine();

        Song newSong;
        if (isAlbumSong.equalsIgnoreCase("y")) {
            System.out.println("\nAvailable Albums:");
            List<Album> albums = artist.getAlbums();
            if (albums.isEmpty()) {
                System.out.println("No albums available. Creating as single.");
                newSong = new Song(title, artist.getName(), null, lyrics);
            } else {
                for (int i = 0; i < albums.size(); i++) {
                    System.out.println((i + 1) + ". " + albums.get(i).getName());
                }
                System.out.print("Select album (enter number) or 0 to create as single: ");
                int albumChoice = scanner.nextInt();
                scanner.nextLine();

                if (albumChoice > 0 && albumChoice <= albums.size()) {
                    Album selectedAlbum = albums.get(albumChoice - 1);
                    newSong = new Song(title, artist.getName(), selectedAlbum.getName(), lyrics);
                    selectedAlbum.addSong(newSong);
                } else {
                    newSong = new Song(title, artist.getName(), null, lyrics);
                }
            }
        } else {
            newSong = new Song(title, artist.getName(), null, lyrics);
        }

        System.out.print("Do you want to add an audio file? (y/n): ");
        String addAudio = scanner.nextLine();
        if (addAudio.equalsIgnoreCase("y")) {
            System.out.print("Enter audio file path: ");
            String audioPath = scanner.nextLine();
            newSong.setAudioFilePath(audioPath);
        }

        artist.addSingle(newSong);
        try {
            artist.saveToDirectory();
            System.out.println("Song added successfully!");
        } catch (IOException e) {
            System.out.println("Error saving song: " + e.getMessage());
        }
    }

    private static Artist getCurrentArtist() {
        return artists.stream()
                .filter(a -> a.getName().equals(currentUser.getFullName()))
                .findFirst()
                .orElse(null);
    }

    private static void viewArtistProfile() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            System.out.println("Artist profile not found!");
            return;
        }

        System.out.println("\n=== Artist Profile ===");
        System.out.println("Name: " + artist.getName());
        System.out.println("Followers: " + artist.getFollowers());
        System.out.println("\nBio:");
        System.out.println(artist.getBio() != null ? artist.getBio() : "No bio yet.");
        System.out.println("\nMost Popular Songs:");

        List<Song> popularSongs = artist.getMostPopularSongs(3);
        for (int i = 0; i < popularSongs.size(); i++) {
            System.out.println((i + 1) + ". " + popularSongs.get(i).getTitle() +
                    " (" + popularSongs.get(i).getViews() + " views)");
        }

        System.out.println("\nOptions:");
        System.out.println("1. Edit Bio");
        System.out.println("2. View Requests");
        System.out.println("3. Back");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> {
                System.out.print("Enter new bio: ");
                String newBio = scanner.nextLine();
                artist.setBio(newBio);
                try {
                    artist.saveToDirectory();
                    System.out.println("Bio updated successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving bio: " + e.getMessage());
                }
            }
            case 2 -> viewRequests();
            case 3 -> { /* Do nothing, will return */ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void manageAlbums() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            System.out.println("Artist profile not found!");
            return;
        }

        System.out.println("\n=== Manage Albums ===");
        System.out.println("1. Create New Album");
        System.out.println("2. View Existing Albums");
        System.out.println("3. Back");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> createNewAlbum(artist);
            case 2 -> viewExistingAlbums(artist);
            case 3 -> { /* Do nothing, will return */ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void createNewAlbum(Artist artist) {
        System.out.print("\nEnter album name: ");
        String albumName = scanner.nextLine();

        System.out.print("Enter release year: ");
        int year = scanner.nextInt();
        scanner.nextLine();

        Album newAlbum = new Album(albumName, artist.getName(), year);
        artist.addAlbum(newAlbum);

        try {
            artist.saveToDirectory();
            System.out.println("Album created successfully!");
        } catch (IOException e) {
            System.out.println("Error saving album: " + e.getMessage());
        }
    }

    private static void viewExistingAlbums(Artist artist) {
        List<Album> albums = artist.getAlbums();
        if (albums.isEmpty()) {
            System.out.println("No albums available.");
            return;
        }

        System.out.println("\nYour Albums:");
        for (int i = 0; i < albums.size(); i++) {
            System.out.println((i + 1) + ". " + albums.get(i).getName() +
                    " (" + albums.get(i).getYear() + ") - " +
                    albums.get(i).getSongs().size() + " songs");
        }

        System.out.print("\nSelect an album to manage (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > albums.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Album selectedAlbum = albums.get(choice - 1);
        System.out.println("\n=== " + selectedAlbum.getName() + " ===");
        System.out.println("Year: " + selectedAlbum.getYear());
        System.out.println("Songs:");

        List<Song> songs = selectedAlbum.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            System.out.println((i + 1) + ". " + songs.get(i).getTitle() +
                    " (" + songs.get(i).getViews() + " views)");
        }

        System.out.println("\nOptions:");
        System.out.println("1. Add Song");
        System.out.println("2. Remove Song");
        System.out.println("3. Back");

        int option = scanner.nextInt();
        scanner.nextLine();

        switch (option) {
            case 1 -> {
                System.out.print("Enter song title: ");
                String title = scanner.nextLine();

                System.out.print("Enter lyrics: ");
                String lyrics = scanner.nextLine();

                Song newSong = new Song(title, artist.getName(), selectedAlbum.getName(), lyrics);
                selectedAlbum.addSong(newSong);

                try {
                    artist.saveToDirectory();
                    System.out.println("Song added to album successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving album: " + e.getMessage());
                }
            }
            case 2 -> {
                System.out.print("Enter song number to remove: ");
                int songNum = scanner.nextInt();
                scanner.nextLine();

                if (songNum < 1 || songNum > songs.size()) {
                    System.out.println("Invalid song number!");
                } else {
                    selectedAlbum.removeSong(songs.get(songNum - 1));
                    try {
                        artist.saveToDirectory();
                        System.out.println("Song removed from album successfully!");
                    } catch (IOException e) {
                        System.out.println("Error saving album: " + e.getMessage());
                    }
                }
            }
            case 3 -> { /* Do nothing, will return */ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void manageContent() {
        System.out.println("\n=== Manage Content ===");
        System.out.println("1. View All Songs");
        System.out.println("2. Remove Inappropriate Content");
        System.out.println("3. Back");
        System.out.print("Enter your choice: ");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1 -> viewAllSongsAdmin();
            case 2 -> removeContent();
            case 3 -> { /**/ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void viewAllSongsAdmin() {
        List<Song> allSongs = getAllSongs();
        if (allSongs.isEmpty()) {
            System.out.println("No songs available.");
            return;
        }

        System.out.println("\nAll Songs:");
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            System.out.println((i + 1) + ". " + song.getTitle() +
                    " by " + song.getArtist() +
                    (song.getAlbum() != null ? " (Album: " + song.getAlbum() + ")" : ""));
        }
    }

    private static void removeContent() {
        List<Song> allSongs = getAllSongs();
        if (allSongs.isEmpty()) {
            System.out.println("No songs available to remove.");
            return;
        }

        System.out.println("\nSelect song to remove:");
        for (int i = 0; i < allSongs.size(); i++) {
            System.out.println((i + 1) + ". " + allSongs.get(i).getTitle());
        }

        System.out.print("Enter song number to remove (0 to cancel): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > allSongs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Song songToRemove = allSongs.get(choice - 1);
        Artist artist = findArtistByName(songToRemove.getArtist());

        if (artist != null) {
            // Remove from singles or albums
            boolean removed = artist.getSingles().removeIf(s -> s.getTitle().equals(songToRemove.getTitle()));

            if (!removed) {
                for (Album album : artist.getAlbums()) {
                    removed = album.getSongs().removeIf(s -> s.getTitle().equals(songToRemove.getTitle()));
                    if (removed) break;
                }
            }

            if (removed) {
                try {
                    artist.saveToDirectory();
                    System.out.println("Song removed successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving changes: " + e.getMessage());
                }
            } else {
                System.out.println("Song not found in artist's collection!");
            }
        } else {
            System.out.println("Artist not found!");
        }
    }
    private static void viewStatistics() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            System.out.println("Artist profile not found!");
            return;
        }

        System.out.println("\n=== Artist Statistics ===");
        System.out.println("Name: " + artist.getName());
        System.out.println("Followers: " + artist.getFollowers());

        // Song statistics
        List<Song> allSongs = new ArrayList<>(artist.getSingles());
        artist.getAlbums().forEach(album -> allSongs.addAll(album.getSongs()));

        System.out.println("\nTotal Songs: " + allSongs.size());
        if (!allSongs.isEmpty()) {
            Song mostPopular = allSongs.stream()
                    .max(Comparator.comparing(Song::getViews))
                    .orElse(null);
            System.out.println("Most Popular Song: " +
                    (mostPopular != null ?
                            mostPopular.getTitle() + " (" + mostPopular.getViews() + " views)" :
                            "N/A"));

            Song mostLiked = allSongs.stream()
                    .max(Comparator.comparing(Song::getLikes))
                    .orElse(null);
            System.out.println("Most Liked Song: " +
                    (mostLiked != null ?
                            mostLiked.getTitle() + " (" + mostLiked.getLikes() + " likes)" :
                            "N/A"));

            long totalViews = allSongs.stream().mapToInt(Song::getViews).sum();
            long totalLikes = allSongs.stream().mapToInt(Song::getLikes).sum();
            System.out.println("Total Views: " + totalViews);
            System.out.println("Total Likes: " + totalLikes);
        }

        // Album statistics
        System.out.println("\nAlbums:");
        if (artist.getAlbums().isEmpty()) {
            System.out.println("No albums released yet.");
        } else {
            for (Album album : artist.getAlbums()) {
                int albumViews = album.getSongs().stream().mapToInt(Song::getViews).sum();
                int albumLikes = album.getSongs().stream().mapToInt(Song::getLikes).sum();
                System.out.println("- " + album.getName() +
                        " (" + album.getYear() + "): " +
                        album.getSongs().size() + " songs, " +
                        albumViews + " views, " +
                        albumLikes + " likes");
            }
        }
    }

    private static void viewRequests() {
        Artist artist = getCurrentArtist();
        if (artist == null) {
            System.out.println("Artist profile not found!");
            return;
        }

        System.out.println("\n=== Pending Requests ===");

        List<LyricRequest> lyricRequests = artist.getLyricRequests();
        List<CommentRequest> commentRequests = artist.getCommentRequests();

        if (lyricRequests.isEmpty() && commentRequests.isEmpty()) {
            System.out.println("No pending requests.");
            return;
        }

        System.out.println("\nLyric Change Requests:");
        for (int i = 0; i < lyricRequests.size(); i++) {
            LyricRequest req = lyricRequests.get(i);
            System.out.println((i + 1) + ". " + req.getSong().getTitle() +
                    " (Requested by: " + req.getRequestedBy() + ")");
        }

        System.out.println("\nComment Requests:");
        for (int i = 0; i < commentRequests.size(); i++) {
            CommentRequest req = commentRequests.get(i);
            System.out.println((lyricRequests.size() + i + 1) + ". " +
                    req.getSong().getTitle() + " - " + req.getComment().getText());
        }

        System.out.print("\nSelect a request to review (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > lyricRequests.size() + commentRequests.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        if (choice <= lyricRequests.size()) {
            // Lyric request
            LyricRequest req = lyricRequests.get(choice - 1);
            System.out.println("\nCurrent Lyrics:");
            System.out.println(req.getSong().getLyrics());
            System.out.println("\nProposed Lyrics:");
            System.out.println(req.getNewLyrics());

            System.out.print("\nApprove this change? (y/n): ");
            String decision = scanner.nextLine();

            if (decision.equalsIgnoreCase("y")) {
                artist.approveLyricRequest(choice - 1);
                try {
                    artist.saveToDirectory();
                    System.out.println("Lyrics updated successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving changes: " + e.getMessage());
                }
            } else {
                artist.rejectLyricRequest(choice - 1);
                System.out.println("Request rejected.");
            }
        } else {
            // Comment request
            int commentIndex = choice - lyricRequests.size() - 1;
            CommentRequest req = commentRequests.get(commentIndex);
            System.out.println("\nSong: " + req.getSong().getTitle());
            System.out.println("Comment: " + req.getComment().getText());

            System.out.print("\nApprove this comment? (y/n): ");
            String decision = scanner.nextLine();

            if (decision.equalsIgnoreCase("y")) {
                artist.approveCommentRequest(commentIndex);
                try {
                    artist.saveToDirectory();
                    System.out.println("Comment added successfully!");
                } catch (IOException e) {
                    System.out.println("Error saving changes: " + e.getMessage());
                }
            } else {
                artist.rejectCommentRequest(commentIndex);
                System.out.println("Request rejected.");
            }
        }
    }


    private static void searchAndDisplaySongs() {
        System.out.println("\n=== Search Songs ===");
        System.out.println("1. Search by Artist");
        System.out.println("2. Search by Title");
        System.out.println("3. Back to Menu");
        System.out.print("Enter your choice: ");

        int searchChoice = scanner.nextInt();
        scanner.nextLine();

        switch (searchChoice) {
            case 1 -> searchByArtist();
            case 2 -> searchByTitle();
            case 3 -> { /* */ }
            default -> System.out.println("Invalid choice!");
        }
    }

    private static void searchByArtist() {
        if (artists.isEmpty()) {
            System.out.println("No artists available yet.");
            return;
        }

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


    private static void searchByTitle() {
        System.out.print("\nEnter song title to search: ");
        String searchTerm = scanner.nextLine().toLowerCase();

        List<Song> matchingSongs = new ArrayList<>();
        for (Artist artist : artists) {
            for (Song song : artist.getSingles()) {
                if (song.getTitle().toLowerCase().contains(searchTerm)) {
                    matchingSongs.add(song);
                }
            }
            for (Album album : artist.getAlbums()) {
                for (Song song : album.getSongs()) {
                    if (song.getTitle().toLowerCase().contains(searchTerm)) {
                        matchingSongs.add(song);
                    }
                }
            }
        }

        if (matchingSongs.isEmpty()) {
            System.out.println("No songs found matching your search.");
            return;
        }

        System.out.println("\nFound " + matchingSongs.size() + " songs:");
        for (int i = 0; i < matchingSongs.size(); i++) {
            Song song = matchingSongs.get(i);
            System.out.println((i + 1) + ". " + song.getTitle() + " by " + song.getArtist() +
                    (song.getAlbum() != null ? " (Album: " + song.getAlbum() + ")" : " (Single)"));
        }

        System.out.print("\nSelect a song to view (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > matchingSongs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        displaySongDetails(matchingSongs.get(choice - 1));
    }

    private static void showAllSongs(Artist artist) {
        List<Song> allSongs = new ArrayList<>(artist.getSingles());
        artist.getAlbums().forEach(album -> allSongs.addAll(album.getSongs()));

        if (allSongs.isEmpty()) {
            System.out.println("No songs available for this artist yet.");
            return;
        }

        System.out.println("\nAll Songs by " + artist.getName() + ":");
        for (int i = 0; i < allSongs.size(); i++) {
            Song song = allSongs.get(i);
            System.out.println((i + 1) + ". " + song.getTitle() +
                    (song.getAlbum() != null && !song.getAlbum().isEmpty() ?
                            " (Album: " + song.getAlbum() + ")" : ""));
        }

        System.out.print("\nSelect a song to view (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > allSongs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        displaySongDetails(allSongs.get(choice - 1));
    }

    private static void showAlbums(Artist artist) {
        List<Album> albums = artist.getAlbums();
        if (albums.isEmpty()) {
            System.out.println("No albums available for this artist.");
            return;
        }

        System.out.println("\nAlbums by " + artist.getName() + ":");
        for (int i = 0; i < albums.size(); i++) {
            System.out.println((i + 1) + ". " + albums.get(i).getName() +
                    " (" + albums.get(i).getYear() + ") - " +
                    albums.get(i).getSongs().size() + " songs");
        }

        System.out.print("\nSelect an album to view (enter number) or 0 to go back: ");
        int albumChoice = scanner.nextInt();
        scanner.nextLine();

        if (albumChoice == 0) return;
        if (albumChoice < 1 || albumChoice > albums.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Album selectedAlbum = albums.get(albumChoice - 1);
        List<Song> albumSongs = selectedAlbum.getSongs();

        System.out.println("\nSongs in " + selectedAlbum.getName() + ":");
        for (int i = 0; i < albumSongs.size(); i++) {
            System.out.println((i + 1) + ". " + albumSongs.get(i).getTitle());
        }

        System.out.print("\nSelect a song to view (enter number) or 0 to go back: ");
        int songChoice = scanner.nextInt();
        scanner.nextLine();

        if (songChoice == 0) return;
        if (songChoice < 1 || songChoice > albumSongs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        displaySongDetails(albumSongs.get(songChoice - 1));
    }

    private static void displaySongDetails(Song song) {

        song.incrementViews();

        System.out.println("\n=== " + song.getTitle() + " ===");
        System.out.println("Artist: " + song.getArtist());
        if (song.getAlbum() != null) {
            System.out.println("Album: " + song.getAlbum());
        }
        System.out.println("Views: " + song.getViews());
        System.out.println("Likes: " + song.getLikes());

        System.out.println("\nLyrics:");
        System.out.println(song.getLyrics());

        System.out.println("\nComments:");
        if (song.getComments().isEmpty()) {
            System.out.println("No comments yet.");
        } else {
            for (Comment comment : song.getComments()) {
                System.out.println("- " + comment.getUsername() + ": " + comment.getText());
            }
        }

        while (true) {
            System.out.println("\nOptions:");
            System.out.println("1. Like this song");
            System.out.println("2. Add comment");
            System.out.println("3. Suggest lyric change");
            System.out.println("4. Play audio (if available)");
            System.out.println("5. Back to previous menu");
            System.out.print("Enter your choice (1-5): ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> {
                    song.incrementLikes();
                    currentUser.likeSong(song.getTitle());
                    System.out.println("You liked this song!");


                    updateArtistData(song);
                }
                case 2 -> addCommentToSong(song);
                case 3 -> suggestLyricChange(song);
                case 4 -> {
                    if (song.hasAudio()) {
                        song.playAudio();
                    } else {
                        System.out.println("No audio file available for this song.");
                    }
                }
                case 5 -> {
                    return;
                }
                default -> System.out.println("Invalid choice! Please enter 1-5.");
            }
        }
    }


    private static void updateArtistData(Song song) {
        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(song.getArtist()))
                .findFirst()
                .orElse(null);

        if (artist != null) {
            try {
                artist.saveToDirectory();
            } catch (IOException e) {
                System.out.println("Error updating artist data: " + e.getMessage());
            }
        }
    }

    private static void addCommentToSong(Song song) {
        System.out.print("Enter your comment: ");
        String commentText = scanner.nextLine();
        Comment newComment = new Comment(currentUser.getUsername(), commentText);

        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(song.getArtist()))
                .findFirst()
                .orElse(null);

        if (artist != null) {
            if (currentUser.getRole().equals("admin")) {

                song.addComment(newComment);
                System.out.println("Comment added successfully (admin privilege).");
            } else {

                artist.addCommentRequest(new CommentRequest(song, newComment));
                System.out.println("Your comment has been sent to the artist for approval.");
            }

            try {
                artist.saveToDirectory();
            } catch (IOException e) {
                System.out.println("Error saving comment: " + e.getMessage());
            }
        } else {
            System.out.println("Artist not found!");
        }
    }

    private static void approveArtists() {
        try {
            List<String> pending = Files.readAllLines(Paths.get(PENDING_ARTISTS_FILE));
            if (pending.isEmpty()) {
                System.out.println("No pending artist requests.");
                return;
            }

            System.out.println("\nPending Artist Requests:");
            for (int i = 0; i < pending.size(); i++) {
                String[] parts = pending.get(i).split(",");
                System.out.println((i+1) + ". " + parts[3] + " (" + parts[1] + ")");
            }

            System.out.print("\nSelect artist to approve (number) or 0 to cancel: ");
            int selection = scanner.nextInt();
            scanner.nextLine();

            if (selection > 0 && selection <= pending.size()) {
                String[] parts = pending.get(selection-1).split(",");
                Artist artist = new Artist(parts[3]);
                artist.setUsername(parts[1]);
                artist.setPassword(parts[2]);
                artist.setEmail(parts[4]);

                Path artistDir = Paths.get(ARTISTS_DIR, artist.getName());
                Files.createDirectories(artistDir);
                artist.saveToDirectory();

                pending.remove(selection-1);
                Files.write(Paths.get(PENDING_ARTISTS_FILE), pending);

                System.out.println("Artist approved successfully!");
            }
        } catch (IOException e) {
            System.out.println("Error approving artist: " + e.getMessage());
        }
    }

    private static void suggestLyricChange(Song song) {
        System.out.println("Current lyrics:");
        System.out.println(song.getLyrics());
        System.out.print("\nEnter your suggested lyrics: ");
        String newLyrics = scanner.nextLine();

        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(song.getArtist()))
                .findFirst()
                .orElse(null);

        if (artist != null) {
            artist.addLyricRequest(new LyricRequest(song, newLyrics, currentUser.getUsername()));
            try {
                artist.saveToDirectory();
                System.out.println("Lyric change request sent to artist.");
            } catch (IOException e) {
                System.out.println("Error saving request: " + e.getMessage());
            }
        } else {
            System.out.println("Artist not found!");
        }
    }

    private static void viewFavorites() {
        List<String> favoriteSongs = currentUser.getLikedSongs();
        if (favoriteSongs.isEmpty()) {
            System.out.println("You haven't liked any songs yet.");
            return;
        }

        System.out.println("\nYour Favorite Songs:");
        for (int i = 0; i < favoriteSongs.size(); i++) {
            System.out.println((i + 1) + ". " + favoriteSongs.get(i));
        }

        System.out.print("\nSelect a song to view (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > favoriteSongs.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        String songTitle = favoriteSongs.get(choice - 1);
        Song song = findSongByTitle(songTitle);
        if (song != null) {
            displaySongDetails(song);
        } else {
            System.out.println("Song not found in database.");
        }
    }

    private static Song findSongByTitle(String title) {
        for (Artist artist : artists) {
            for (Song song : artist.getSingles()) {
                if (song.getTitle().equals(title)) {
                    return song;
                }
            }
            for (Album album : artist.getAlbums()) {
                for (Song song : album.getSongs()) {
                    if (song.getTitle().equals(title)) {
                        return song;
                    }
                }
            }
        }
        return null;
    }

    private static void followArtists() {
        System.out.println("\nAvailable Artists:");
        for (int i = 0; i < artists.size(); i++) {
            Artist artist = artists.get(i);
            System.out.println((i + 1) + ". " + artist.getName() +
                    " (" + artist.getFollowers() + " followers)");
        }

        System.out.print("Select an artist to follow/unfollow (enter number): ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice < 1 || choice > artists.size()) {
            System.out.println("Invalid choice!");
            return;
        }

        Artist artist = artists.get(choice - 1);
        if (currentUser.getFollowedArtists().contains(artist.getName())) {
            currentUser.unfollowArtist(artist.getName());
            artist.setFollowers(artist.getFollowers() - 1);
            System.out.println("Unfollowed " + artist.getName());
        } else {
            currentUser.followArtist(artist.getName());
            artist.setFollowers(artist.getFollowers() + 1);
            System.out.println("Followed " + artist.getName());
        }

        try {
            artist.saveToDirectory();
        } catch (IOException e) {
            System.out.println("Error saving artist data: " + e.getMessage());
        }
    }

    private static void viewFollowing() {
        if (currentUser.getFollowedArtists().isEmpty()) {
            System.out.println("You're not following any artists yet.");
            return;
        }

        System.out.println("\nArtists you follow:");
        for (int i = 0; i < currentUser.getFollowedArtists().size(); i++) {
            System.out.println((i + 1) + ". " + currentUser.getFollowedArtists().get(i));
        }

        System.out.print("\nSelect an artist to view details (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;

        if (choice > 0 && choice <= currentUser.getFollowedArtists().size()) {
            String artistName = currentUser.getFollowedArtists().get(choice - 1);
            Artist artist = findArtistByName(artistName);
            if (artist != null) {
                System.out.println("\n=== " + artist.getName() + " ===");
                System.out.println("Followers: " + artist.getFollowers());
                System.out.println("Bio: " + (artist.getBio() != null ? artist.getBio() : "No bio available."));
            } else {
                System.out.println("Artist not found!");
            }
        } else {
            System.out.println("Invalid choice!");
        }
    }
    private static Artist findArtistByName(String name) {
        return artists.stream()
                .filter(a -> a.getName().equals(name))
                .findFirst()
                .orElse(null);
    }


    private static void viewCharts() {
        List<Song> allSongs = getAllSongs();
        allSongs.sort((s1, s2) -> Integer.compare(s2.getViews(), s1.getViews()));

        System.out.println("\n=== Top Songs Chart ===");
        for (int i = 0; i < Math.min(10, allSongs.size()); i++) {
            Song song = allSongs.get(i);
            System.out.println((i + 1) + ". " + song.getTitle() +
                    " by " + song.getArtist() +
                    " (" + song.getViews() + " views, " +
                    song.getLikes() + " likes)");
        }

        System.out.print("\nSelect a song to view (enter number) or 0 to go back: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 0) return;
        if (choice < 1 || choice > Math.min(10, allSongs.size())) {
            System.out.println("Invalid choice!");
            return;
        }

        displaySongDetails(allSongs.get(choice - 1));
    }
}