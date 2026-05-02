import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Routes
        server.createContext("/api/crimes",  Server::handleCrimes);
        server.createContext("/",            Server::handleFrontend);

        server.setExecutor(null);
        server.start();
        System.out.println("==============================================");
        System.out.println("  Crime Data Management Server started!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("==============================================");
    }

    // ── Serve frontend static files ─────────────────────────────────────────────
    private static void handleFrontend(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path.equals("/") || path.equals("/index.html")) path = "/index.html";

        String base = new File("frontend").getAbsolutePath();
        File file   = new File(base + path);

        if (!file.exists() || !file.getCanonicalPath().startsWith(base)) {
            send(ex, 404, "text/plain", "Not Found");
            return;
        }

        String mime = "text/plain";
        if (path.endsWith(".html")) mime = "text/html";
        else if (path.endsWith(".css"))  mime = "text/css";
        else if (path.endsWith(".js"))   mime = "application/javascript";

        byte[] body = java.nio.file.Files.readAllBytes(file.toPath());
        ex.getResponseHeaders().set("Content-Type", mime);
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.getResponseBody().close();
    }

    // ── /api/crimes dispatcher ──────────────────────────────────────────────────
    private static void handleCrimes(HttpExchange ex) throws IOException {
        addCORS(ex);
        String method = ex.getRequestMethod();

        if (method.equals("OPTIONS")) { ex.sendResponseHeaders(204, -1); return; }

        String query  = ex.getRequestURI().getQuery(); // e.g. id=3
        Map<String, String> qp = parseQuery(query);

        try {
            switch (method) {

                case "GET": {
                    if (qp.containsKey("id")) {
                        // GET /api/crimes?id=5  → fetch single record for edit
                        int id = Integer.parseInt(qp.get("id"));
                        send(ex, 200, "application/json", DB.getCrimeById(id));
                    } else {
                        // GET /api/crimes  → all records
                        send(ex, 200, "application/json", DB.getAllCrimes());
                    }
                    break;
                }

                case "POST": {
                    // ADD new crime
                    Map<String, String> body = parseBody(ex);
                    String result = DB.addCrime(
                        body.getOrDefault("type", ""),
                        body.getOrDefault("location", ""),
                        body.getOrDefault("description", ""),
                        body.getOrDefault("status", "Open")
                    );
                    send(ex, 200, "application/json", result);
                    break;
                }

                case "PUT": {
                    // UPDATE crime
                    Map<String, String> body = parseBody(ex);
                    int id = Integer.parseInt(body.getOrDefault("id", "0"));
                    String result = DB.updateCrime(
                        id,
                        body.getOrDefault("type", ""),
                        body.getOrDefault("location", ""),
                        body.getOrDefault("description", ""),
                        body.getOrDefault("status", "Open")
                    );
                    send(ex, 200, "application/json", result);
                    break;
                }

                case "DELETE": {
                    // DELETE crime
                    int id = Integer.parseInt(qp.getOrDefault("id", "0"));
                    send(ex, 200, "application/json", DB.deleteCrime(id));
                    break;
                }

                default:
                    send(ex, 405, "application/json", "{\"error\":\"Method not allowed\"}");
            }
        } catch (Exception e) {
            send(ex, 500, "application/json", "{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────
    private static void send(HttpExchange ex, int code, String mime, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", mime + "; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private static void addCORS(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(decode(kv[0]), decode(kv[1]));
        }
        return map;
    }

    private static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        InputStream is   = ex.getRequestBody();
        String raw       = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        return parseQuery(raw);
    }

    private static String decode(String s) {
        try { return java.net.URLDecoder.decode(s, "UTF-8"); }
        catch (Exception e) { return s; }
    }
}
