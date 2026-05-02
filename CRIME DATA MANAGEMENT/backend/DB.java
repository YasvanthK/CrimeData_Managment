import java.sql.*;

public class DB {

    private static final String URL      = "jdbc:mysql://localhost:3306/crimedb?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "KY2007";   // <-- change this

    // ── Get connection ──────────────────────────────────────────────────────────
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // ── ADD a crime ─────────────────────────────────────────────────────────────
    public static String addCrime(String type, String location, String description, String status) {
        String sql = "INSERT INTO crimes (type, location, description, status) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, location);
            ps.setString(3, description);
            ps.setString(4, status);
            ps.executeUpdate();
            return "{\"success\":true,\"message\":\"Crime case added successfully.\"}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── VIEW all crimes ─────────────────────────────────────────────────────────
    public static String getAllCrimes() {
        String sql = "SELECT * FROM crimes ORDER BY id DESC";
        StringBuilder json = new StringBuilder("[");
        try (Connection con = getConnection();
             Statement st  = con.createStatement();
             ResultSet rs  = st.executeQuery(sql)) {
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                json.append("{")
                    .append("\"id\":")          .append(rs.getInt("id"))               .append(",")
                    .append("\"type\":\"")      .append(escape(rs.getString("type")))  .append("\",")
                    .append("\"location\":\"")  .append(escape(rs.getString("location"))).append("\",")
                    .append("\"description\":\"").append(escape(rs.getString("description"))).append("\",")
                    .append("\"status\":\"")    .append(escape(rs.getString("status"))).append("\"")
                    .append("}");
                first = false;
            }
        } catch (Exception e) {
            return "[]";
        }
        json.append("]");
        return json.toString();
    }

    // ── GET one crime by ID (for pre-filling update form) ───────────────────────
    public static String getCrimeById(int id) {
        String sql = "SELECT * FROM crimes WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return "{\"success\":true,\"data\":{"
                    + "\"id\":"           + rs.getInt("id")                              + ","
                    + "\"type\":\""       + escape(rs.getString("type"))                 + "\","
                    + "\"location\":\""   + escape(rs.getString("location"))             + "\","
                    + "\"description\":\"" + escape(rs.getString("description"))         + "\","
                    + "\"status\":\""     + escape(rs.getString("status"))               + "\""
                    + "}}";
            }
            return "{\"success\":false,\"message\":\"Case not found.\"}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── UPDATE a crime ──────────────────────────────────────────────────────────
    public static String updateCrime(int id, String type, String location, String description, String status) {
        String sql = "UPDATE crimes SET type=?, location=?, description=?, status=? WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, location);
            ps.setString(3, description);
            ps.setString(4, status);
            ps.setInt(5, id);
            int rows = ps.executeUpdate();
            if (rows > 0)
                return "{\"success\":true,\"message\":\"Case #" + id + " updated successfully.\"}";
            else
                return "{\"success\":false,\"message\":\"No case found with ID " + id + ".\"}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── DELETE a crime ──────────────────────────────────────────────────────────
    public static String deleteCrime(int id) {
        String sql = "DELETE FROM crimes WHERE id = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0)
                return "{\"success\":true,\"message\":\"Case #" + id + " deleted.\"}";
            else
                return "{\"success\":false,\"message\":\"No case found with ID " + id + ".\"}";
        } catch (Exception e) {
            return "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}";
        }
    }

    // ── Escape special characters for JSON ─────────────────────────────────────
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
