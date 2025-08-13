import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;

public class JournalSystem4 {
    static final String DB_URL = "jdbc:mysql://localhost:3306/journaldb";
    static final String DB_USER = "root";
    static final String DB_PASS = "ramani@2004";
    static Connection conn;
    static Scanner scanner = new Scanner(System.in);
    static int loggedInUserId;
    static String loggedInUserRole;

    // ANSI color codes for console styling
    static final String RESET = "\033[0m";
    static final String GREEN = "\033[0;32m";
    static final String RED = "\033[0;31m";
    static final String CYAN = "\033[0;36m";
    static final String YELLOW = "\033[0;33m";
    static final String BOLD = "\033[1m";

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z]+( [a-zA-Z]+)*$");

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println(GREEN + "✔ Connected to MySQL Database" + RESET);
            while (true) mainMenu();
        } catch (Exception e) {
            System.out.println(RED + "✖ Error: " + e.getMessage() + RESET);
            e.printStackTrace();
        }
    }

    static void mainMenu() throws SQLException {
        System.out.println(CYAN + "\n═══════════════════════════════════════");
        System.out.println(BOLD + "     Electronic Journal System");
        System.out.println("═══════════════════════════════════════" + RESET);
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print( GREEN + " Choose an option 👉: " + RESET);
        int option = scanner.nextInt(); scanner.nextLine();
        switch (option) {
            case 1 -> register();
            case 2 -> login();
            case 3 -> {
                System.out.println(GREEN + "✔ Thank you for using the Journal System. Goodbye!" + RESET);
                System.exit(0);
            }
            default -> System.out.println(RED + "✖ Invalid choice. Please try again." + RESET);
        }
    }

    static void register() throws SQLException {
        System.out.println(CYAN + "\n═══════ Register New User ═══════" + RESET);
        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        // Validate inputs
        if (name == null || name.isEmpty()) {
            System.out.println(RED + "✖ Name cannot be empty." + RESET);
            return;
        }
        if (!NAME_PATTERN.matcher(name).matches()) {
            System.out.println(RED + "✖ Invalid name. Use only letters and spaces (e.g., John Doe)." + RESET);
            return;
        }
        if (email == null || email.isEmpty()) {
            System.out.println(RED + "✖ Email cannot be empty." + RESET);
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            System.out.println(RED + "✖ Invalid email format. Must be like user@example.com." + RESET);
            return;
        }
        if (password == null || password.isEmpty()) {
            System.out.println(RED + "✖ Password cannot be empty." + RESET);
            return;
        }
        if (password.length() < 8) {
            System.out.println(RED + "✖ Password must be at least 8 characters long." + RESET);
            return;
        }

        // Check for duplicate email
        String checkEmailSql = "SELECT COUNT(*) FROM users WHERE email=?";
        try (PreparedStatement ps = conn.prepareStatement(checkEmailSql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println(RED + "✖ Email already exists." + RESET);
                return;
            }
        }

        // Insert user with default role 'user'
        String sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,'user')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            System.out.println(GREEN + "✔ Registered successfully!" + RESET);
        } catch (SQLException e) {
            System.out.println(RED + "✖ Registration failed: " + e.getMessage() + RESET);
        }
    }

    static void login() throws SQLException {
        System.out.println(CYAN + "\n═══════ User Login ═══════" + RESET);
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        if (email.equals("admin@journal.com") && password.equals("admin123")) {
            loggedInUserId = -1;
            loggedInUserRole = "editor";
            System.out.println(GREEN + "✔ Logged in as Admin Editor" + RESET);
            editorDashboard();
            return;
        }
        String sql = "SELECT id,role FROM users WHERE email=? AND password=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println(RED + "✖ Invalid credentials." + RESET);
                    return;
                }
                loggedInUserId = rs.getInt("id");
                loggedInUserRole = rs.getString("role");
                System.out.println(GREEN + "✔ Login successful as " + loggedInUserRole + RESET);
            }
        }
        switch (loggedInUserRole) {
            case "user", "author" -> userDashboard();
            case "reviewer" -> reviewerDashboard();
            case "editor" -> editorDashboard();
            default -> System.out.println(RED + "✖ Unknown role." + RESET);
        }
    }

    static void userDashboard() throws SQLException {
        while (true) {
            System.out.println(CYAN + "\n═══════════════════════════════════════");
            System.out.println(BOLD + "         User Dashboard ");
            System.out.println("═══════════════════════════════════════" + RESET);
            
            System.out.println("1. Search Articles");
            System.out.println("2. Submit Article");
            System.out.println("3. View Submitted Articles");
            System.out.println("4. View Published Articles");
            System.out.println("5. Logout");
            System.out.print(YELLOW + "➤ Choose: " + RESET);
            int opt = scanner.nextInt(); scanner.nextLine();
            switch (opt) {
                case 1 -> searchArticles();
                case 2 -> submitArticle();
                case 3 -> viewSubmittedArticles();
                case 4 -> viewPublishedArticles();
                case 5 -> { return; }
                default -> System.out.println(RED + "✖ Invalid choice." + RESET);
            }
        }
    }

    static void searchArticles() throws SQLException {
        System.out.println(CYAN + "\n═══════ Search Articles ═══════" + RESET);
        System.out.print("Enter Keyword: ");
        String kw = scanner.nextLine();
        String sql = "SELECT a.id,a.title,u.name,a.content FROM articles a JOIN users u ON a.author_id=u.id WHERE a.title LIKE ? OR u.name LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + kw + "%");
            ps.setString(2, "%" + kw + "%");
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                found = true;
                System.out.printf("│ ID: %-4d Title: %-20s\n", rs.getInt("id"), rs.getString("title"));
                System.out.printf("│ Author: %-20s\n", rs.getString("name"));
                System.out.println("│ Content: " + rs.getString("content"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            if (!found) {
                System.out.println(RED + "│ ✖ No articles found.         │" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void submitArticle() throws SQLException {
        System.out.println(CYAN + "\n═══════ Submit New Article ═══════" + RESET);
        System.out.print("Title: ");
        String title = scanner.nextLine();
        System.out.print("Content: ");
        String content = scanner.nextLine();
        String sql = "INSERT INTO articles(title,content,author_id,status) VALUES(?,?,?,'submitted')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, loggedInUserId);
            ps.executeUpdate();
            // Update user role to 'author' if currently 'user'
            String updateRoleSql = "UPDATE users SET role='author' WHERE id=? AND role='user'";
            try (PreparedStatement psRole = conn.prepareStatement(updateRoleSql)) {
                psRole.setInt(1, loggedInUserId);
                psRole.executeUpdate();
            }
            // Update loggedInUserRole to reflect the change
            loggedInUserRole = "author";
            System.out.println(GREEN + "✔ Article submitted successfully!" + RESET);
        }
    }

    static void viewSubmittedArticles() throws SQLException {
        System.out.println(CYAN + "\n═══════ Your Submitted Articles ═══════" + RESET);
        String sql = "SELECT id,title FROM articles WHERE author_id=? AND status='submitted'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                found = true;
                System.out.printf("│ ID: %-4d Title: %-20s\n", rs.getInt("id"), rs.getString("title"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            if (!found) {
                System.out.println(RED + "│ ✖ No submitted articles found.│" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void viewPublishedArticles() throws SQLException {
        System.out.println(CYAN + "\n═══════ Published Articles ═══════" + RESET);
        String sql = "SELECT a.id,a.title,u.name,pa.publish_date,pa.publish_details FROM published_articles pa JOIN articles a ON pa.article_id=a.id JOIN users u ON a.author_id=u.id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                System.out.printf("│ ID: %-4d Title: %-20s\n", rs.getInt("id"), rs.getString("title"));
                System.out.printf("│ Author: %-20s\n", rs.getString("name"));
                System.out.println("│ Published: " + rs.getString("publish_date"));
                System.out.println("│ Details: " + rs.getString("publish_details"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void editorDashboard() throws SQLException {
        while (true) {
            System.out.println(CYAN + "\n═══════════════════════════════════════");
            System.out.println(BOLD + "         Editor Dashboard");
            System.out.println("═══════════════════════════════════════" + RESET);
            System.out.println("1. View Submitted Articles");
            System.out.println("2. Assign Article to Reviewer");
            System.out.println("3. Publish Article");
            System.out.println("4. View Published Articles");
            System.out.println("5. View Reviewed Articles");
            System.out.println("6. Add Reviewer Access");
            System.out.println("7. View Reviewer List");
            System.out.println("8. User Management");
            System.out.println("9. Logout");
            System.out.print(YELLOW + "➤ Choose: " + RESET);
            int opt = scanner.nextInt(); scanner.nextLine();
            switch (opt) {
                case 1 -> viewSubmittedArticlesForEditor();
                case 2 -> assignArticleToReviewer();
                case 3 -> publishArticle();
                case 4 -> viewPublishedArticles();
                case 5 -> viewReviewedArticles();
                case 6 -> addReviewerAccess();
                case 7 -> viewReviewerList();
                case 8 -> viewUserDetails();
                case 9 -> { return; }
                default -> System.out.println(RED + "✖ Invalid choice." + RESET);
            }
        }
    }

    static void viewSubmittedArticlesForEditor() throws SQLException {
        System.out.println(CYAN + "\n═══════ Submitted Articles ═══════" + RESET);
        String sql = "SELECT a.id,a.title,u.name FROM articles a JOIN users u ON a.author_id=u.id WHERE a.status='submitted'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                System.out.printf("│ ID: %-4d Title: %-20s\n", rs.getInt("id"), rs.getString("title"));
                System.out.printf("│ Author: %-20s\n", rs.getString("name"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void assignArticleToReviewer() throws SQLException {
        System.out.println(CYAN + "\n═══════ Assign Article to Reviewer ═══════" + RESET);
        System.out.print("Article ID: ");
        int aid = scanner.nextInt(); scanner.nextLine();
        System.out.print("Reviewer email: ");
        String email = scanner.nextLine();
        String sql1 = "SELECT id FROM users WHERE email=? AND role='reviewer'";
        try (PreparedStatement ps1 = conn.prepareStatement(sql1)) {
            ps1.setString(1, email);
            ResultSet rs = ps1.executeQuery();
            if (!rs.next()) {
                System.out.println(RED + "✖ Reviewer not found." + RESET);
                return;
            }
            int reviewerId = rs.getInt("id");
            String sql2 = "UPDATE articles SET reviewer_id=?, status='assigned' WHERE id=?";
            try (PreparedStatement ps2 = conn.prepareStatement(sql2)) {
                ps2.setInt(1, reviewerId);
                ps2.setInt(2, aid);
                ps2.executeUpdate();
                System.out.println(GREEN + "✔ Article assigned successfully!" + RESET);
            }
        }
    }

    static void publishArticle() throws SQLException {
        System.out.println(CYAN + "\n═══════ Publish Article ═══════" + RESET);
        System.out.print("Article ID: ");
        int aid = scanner.nextInt(); scanner.nextLine();
        System.out.print("Details: ");
        String details = scanner.nextLine();

        // Check if article exists
        String checkExistsSql = "SELECT status FROM articles WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(checkExistsSql)) {
            ps.setInt(1, aid);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                System.out.println(RED + "✖ Article ID " + aid + " does not exist." + RESET);
                return;
            }
            String status = rs.getString("status");
            if ("published".equals(status)) {
                System.out.println(RED + "✖ Article ID " + aid + " is already published." + RESET);
                return;
            }
        }

        // Check if article is already in published_articles (extra safety)
        String checkPublishedSql = "SELECT article_id FROM published_articles WHERE article_id=?";
        try (PreparedStatement ps = conn.prepareStatement(checkPublishedSql)) {
            ps.setInt(1, aid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println(RED + "✖ Article ID " + aid + " is already published." + RESET);
                return;
            }
        }

        // Update article status to published
        try (PreparedStatement ps = conn.prepareStatement("UPDATE articles SET status='published' WHERE id=?")) {
            ps.setInt(1, aid);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                System.out.println(RED + "✖ Failed to update article status." + RESET);
                return;
            }
        }

        // Insert into published_articles
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO published_articles(article_id,publish_date,publish_details) VALUES(?,NOW(),?)")) {
            ps.setInt(1, aid);
            ps.setString(2, details);
            ps.executeUpdate();
            System.out.println(GREEN + "✔ Article published successfully!" + RESET);
        } catch (SQLException e) {
            System.out.println(RED + "✖ Publication failed: " + e.getMessage() + RESET);
        }
    }

    static void viewReviewedArticles() throws SQLException {
        System.out.println(CYAN + "\n═══════ Reviewed Articles ═══════" + RESET);
        String sql = "SELECT r.article_id, a.title, r.reviewer_id, u.name AS reviewer, r.comments " +
                     "FROM reviews r JOIN articles a ON r.article_id = a.id JOIN users u ON r.reviewer_id = u.id";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                System.out.printf("│ Article ID: %-4d Title: %-20s\n", rs.getInt("article_id"), rs.getString("title"));
                System.out.printf("│ Reviewer: %-20s\n", rs.getString("reviewer"));
                System.out.println("│ Comments: " + rs.getString("comments"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void addReviewerAccess() throws SQLException {
        System.out.println(CYAN + "\n═══════ Add Reviewer Access ═══════" + RESET);
        System.out.print("Reviewer email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String pwd = scanner.nextLine();
        String sql = "INSERT INTO users(name,email,password,role) VALUES(?,?,?,'reviewer')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.split("@")[0]);
            ps.setString(2, email);
            ps.setString(3, pwd);
            ps.executeUpdate();
            System.out.println(GREEN + "✔ Reviewer added successfully!" + RESET);
        }
    }

    static void viewReviewerList() throws SQLException {
        System.out.println(CYAN + "\n═══════ Reviewer List ═══════" + RESET);
        String sql = "SELECT id, name, email FROM users WHERE role='reviewer'";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            boolean found = false;
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                found = true;
                System.out.printf("│ ID: %-4d Name: %-15s\n", rs.getInt("id"), rs.getString("name"));
                System.out.printf("│ Email: %-20s\n", rs.getString("email"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            if (!found) {
                System.out.println(RED + "│ ✖ No reviewers found.        │" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void viewUserDetails() throws SQLException {
        System.out.println(CYAN + "\n═══════ User Management ═══════" + RESET);
        String sql = "SELECT id, name, email, role FROM users";
        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            System.out.println(CYAN + "┌──────────────────────────────────────┐" + RESET);
            while (rs.next()) {
                System.out.printf("│ ID: %-4d Name: %-15s\n", rs.getInt("id"), rs.getString("name"));
                System.out.printf("│ Email: %-20s Role: %-10s\n", rs.getString("email"), rs.getString("role"));
                System.out.println(CYAN + "├──────────────────────────────────────┤" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────────────┘" + RESET);
        }
    }

    static void reviewerDashboard() throws SQLException {
        while (true) {
            System.out.println(CYAN + "\n═══════════════════════════════════════");
            System.out.println(BOLD + "         Reviewer Dashboard");
            System.out.println("═══════════════════════════════════════" + RESET);
            System.out.println("1. View Assigned Articles");
            System.out.println("2. Submit Review");
            System.out.println("3. View Submitted Reviews");
            System.out.println("4. Logout");
            System.out.print(YELLOW + "➤ Choose: " + RESET);
            int opt = scanner.nextInt(); scanner.nextLine();
            switch (opt) {
                case 1 -> viewAssignedArticles();
                case 2 -> submitReview();
                case 3 -> viewSubmittedReviews();
                case 4 -> { return; }
                default -> System.out.println(RED + "✖ Invalid choice." + RESET);
            }
        }
    }

    static void viewAssignedArticles() throws SQLException {
        System.out.println(CYAN + "\n═══════ Assigned Articles ═══════" + RESET);
        String sql = "SELECT id, title, content FROM articles WHERE reviewer_id=? AND status='assigned'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                found = true;
                System.out.printf("│ ID: %-4d Title: %-20s\n", rs.getInt("id"), rs.getString("title"));
                System.out.println("│ Content: " + rs.getString("content"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            if (!found) {
                System.out.println(RED + "│ ✖ No assigned articles found.│" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }

    static void submitReview() throws SQLException {
        System.out.println(CYAN + "\n═══════ Submit Review ═══════" + RESET);
        System.out.print("Article ID: ");
        int aid = scanner.nextInt(); scanner.nextLine();
        System.out.print("Comments: ");
        String comment = scanner.nextLine();

        // Insert review
        String insertReviewSql = "INSERT INTO reviews(article_id,reviewer_id,comments) VALUES(?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(insertReviewSql)) {
            ps.setInt(1, aid);
            ps.setInt(2, loggedInUserId);
            ps.setString(3, comment);
            ps.executeUpdate();
        }

        // Update article status to 'reviewed'
        String updateArticleSql = "UPDATE articles SET status='reviewed' WHERE id=? AND reviewer_id=? AND status='assigned'";
        try (PreparedStatement ps = conn.prepareStatement(updateArticleSql)) {
            ps.setInt(1, aid);
            ps.setInt(2, loggedInUserId);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println(GREEN + "✔ Review submitted successfully!" + RESET);
            } else {
                System.out.println(RED + "✖ Failed to submit review. Article may not be assigned to you or already reviewed." + RESET);
            }
        }
    }

    static void viewSubmittedReviews() throws SQLException {
        System.out.println(CYAN + "\n═══════ Submitted Reviews ═══════" + RESET);
        String sql = "SELECT r.article_id, a.title, r.comments " +
                     "FROM reviews r JOIN articles a ON r.article_id = a.id " +
                     "WHERE r.reviewer_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loggedInUserId);
            ResultSet rs = ps.executeQuery();
            boolean found = false;
            System.out.println(CYAN + "┌──────────────────────────────┐" + RESET);
            while (rs.next()) {
                found = true;
                System.out.printf("│ Article ID: %-4d Title: %-20s\n", rs.getInt("article_id"), rs.getString("title"));
                System.out.println("│ Comments: " + rs.getString("comments"));
                System.out.println(CYAN + "├──────────────────────────────┤" + RESET);
            }
            if (!found) {
                System.out.println(RED + "│ ✖ No submitted reviews found.│" + RESET);
            }
            System.out.println(CYAN + "└──────────────────────────────┘" + RESET);
        }
    }
}