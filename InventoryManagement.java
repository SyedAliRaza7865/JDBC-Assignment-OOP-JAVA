
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class InventoryManagement {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/retail_db";
        String username = "root";
        String password = "244001Mysql@";

        try (
                Scanner sc = new Scanner(System.in);
                Connection con = DriverManager.getConnection(
                        url, username, password
                )
        ) {
            System.out.println("Connected to MySQL successfully!");

            int choice;

            do {
                System.out.println("\n--- INVENTORY MANAGEMENT ---");
                System.out.println("1. Insert 3 Products");
                System.out.println("2. Search by Category (Quantity < 20)");
                System.out.println("3. Add Stock");
                System.out.println("4. Search and Update Price");
                System.out.println("5. Display Total Inventory Value");
                System.out.println("6. Delete Product if Quantity is Zero");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");

                choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {

                    case 1: {
                        String sql = "INSERT INTO Product " +
                                "(product_id, product_name, category, quantity, price) " +
                                "VALUES (?, ?, ?, ?, ?)";

                        try (PreparedStatement ps =
                                     con.prepareStatement(sql)) {

                            for (int i = 1; i <= 3; i++) {
                                System.out.println(
                                        "Enter details of product " + i
                                );

                                System.out.print("Product ID: ");
                                int id = sc.nextInt();
                                sc.nextLine();

                                System.out.print("Product Name: ");
                                String name = sc.nextLine();

                                System.out.print("Category: ");
                                String category = sc.nextLine();

                                System.out.print("Quantity: ");
                                int quantity = sc.nextInt();

                                System.out.print("Price: ");
                                double price = sc.nextDouble();
                                sc.nextLine();

                                ps.setInt(1, id);
                                ps.setString(2, name);
                                ps.setString(3, category);
                                ps.setInt(4, quantity);
                                ps.setDouble(5, price);

                                try {
                                    int result = ps.executeUpdate();
                                    System.out.println(
                                            "Product inserted successfully!"
                                    );
                                } catch (SQLException e) {
                                    if (e.getErrorCode() == 1062) {
                                        System.out.println(
                                                "Duplicate product ID! Not inserted."
                                        );
                                    } else {
                                        throw e;
                                    }
                                }
                            }
                        }
                        break;
                    }

                    case 2: {
                        System.out.print("Enter category: ");
                        String category = sc.nextLine();

                        String sql = "SELECT * FROM Product " +
                                "WHERE category = ? AND quantity < 20 " +
                                "ORDER BY price DESC";

                        try (PreparedStatement ps =
                                     con.prepareStatement(sql)) {

                            ps.setString(1, category);

                            try (ResultSet rs = ps.executeQuery()) {
                                boolean found = false;

                                while (rs.next()) {
                                    found = true;
                                    System.out.println(
                                            rs.getInt("product_id") + " | " +
                                                    rs.getString("product_name") + " | " +
                                                    rs.getString("category") + " | " +
                                                    rs.getInt("quantity") + " | " +
                                                    rs.getDouble("price")
                                    );
                                }

                                if (!found) {
                                    System.out.println("No matching products.");
                                }
                            }
                        }
                        break;
                    }

                    case 3: {
                        System.out.print("Enter product ID: ");
                        int id = sc.nextInt();

                        System.out.print("Quantity to add: ");
                        int added = sc.nextInt();
                        sc.nextLine();

                        String sql = "UPDATE Product " +
                                "SET quantity = quantity + ? " +
                                "WHERE product_id = ?";

                        try (PreparedStatement ps =
                                     con.prepareStatement(sql)) {

                            ps.setInt(1, added);
                            ps.setInt(2, id);

                            int result = ps.executeUpdate();

                            if (result > 0) {
                                System.out.println("Stock updated successfully!");
                            } else {
                                System.out.println("Product does not exist.");
                            }
                        }
                        break;
                    }

                    case 4: {
                        System.out.print("Enter product ID: ");
                        int id = sc.nextInt();

                        System.out.print("Enter new price: ");
                        double newPrice = sc.nextDouble();
                        sc.nextLine();

                        if (newPrice <= 0) {
                            System.out.println(
                                    "Price must be greater than zero."
                            );
                            break;
                        }

                        String searchSql =
                                "SELECT * FROM Product WHERE product_id = ?";

                        try (PreparedStatement ps =
                                     con.prepareStatement(searchSql)) {

                            ps.setInt(1, id);

                            try (ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    System.out.println("Product found:");
                                    System.out.println(
                                            rs.getString("product_name") +
                                                    ", Category: " +
                                                    rs.getString("category") +
                                                    ", Quantity: " +
                                                    rs.getInt("quantity") +
                                                    ", Old Price: " +
                                                    rs.getDouble("price")
                                    );

                                    String updateSql =
                                            "UPDATE Product SET price = ? " +
                                                    "WHERE product_id = ?";

                                    try (PreparedStatement ups =
                                                 con.prepareStatement(updateSql)) {

                                        ups.setDouble(1, newPrice);
                                        ups.setInt(2, id);
                                        ups.executeUpdate();
                                    }

                                    System.out.println("Updated product details:");

                                    try (PreparedStatement ups =
                                                 con.prepareStatement(searchSql)) {

                                        ups.setInt(1, id);

                                        try (ResultSet updated = ups.executeQuery()) {
                                            if (updated.next()) {
                                                System.out.println(
                                                        updated.getInt("product_id") +
                                                                " | " +
                                                                updated.getString("product_name") +
                                                                " | " +
                                                                updated.getString("category") +
                                                                " | " +
                                                                updated.getInt("quantity") +
                                                                " | " +
                                                                updated.getDouble("price")
                                                );
                                            }
                                        }
                                    }
                                } else {
                                    System.out.println("Product does not exist.");
                                }
                            }
                        }
                        break;
                    }

                    case 5: {
                        String sql =
                                "SELECT SUM(quantity * price) AS total_value " +
                                        "FROM Product";

                        try (PreparedStatement ps =
                                     con.prepareStatement(sql);
                             ResultSet rs = ps.executeQuery()) {

                            if (rs.next()) {
                                System.out.println(
                                        "Total Inventory Value: " +
                                                rs.getDouble("total_value")
                                );
                            }
                        }
                        break;
                    }

                    case 6: {
                        System.out.print("Enter product ID to delete: ");
                        int id = sc.nextInt();
                        sc.nextLine();

                        String sql = "DELETE FROM Product " +
                                "WHERE product_id = ? AND quantity = 0";

                        try (PreparedStatement ps =
                                     con.prepareStatement(sql)) {

                            ps.setInt(1, id);

                            int result = ps.executeUpdate();

                            if (result > 0) {
                                System.out.println(
                                        "Product deleted successfully!"
                                );
                            } else {
                                System.out.println(
                                        "Product not found or quantity is not zero. " +
                                                "Cannot delete."
                                );
                            }
                        }
                        break;
                    }

                    case 0:
                        System.out.println("Exiting program.");
                        break;

                    default:
                        System.out.println("Invalid choice.");
                }

            } while (choice != 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
