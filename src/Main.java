import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("W:\\java\\SpringBoot\\Hashira\\src\\input2.json"));

        int n = root.get("keys").get("n").asInt();
        int k = root.get("keys").get("k").asInt();

        List<Point> points = new ArrayList<>();

        // Parse x, decode y based on base
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.equals("keys")) continue;

            int x = Integer.parseInt(key);
            JsonNode node = root.get(key);
            int base = Integer.parseInt(node.get("base").asText());
            String value = node.get("value").asText();

            BigInteger y = new BigInteger(value, base); // ✅ Use BigInteger for large numbers
            points.add(new Point(x, y));
        }

        // Sort points by x just for consistency
        points.sort(Comparator.comparingInt(p -> p.x));
        System.out.println("Original points: ");
        for (Point p : points) {
            System.out.println(p.x + " " + p.y+",");
        }
        if (points.size() < k) {
            throw new RuntimeException("Not enough points to reconstruct polynomial");
        }

        // Solve using BigInteger Lagrange Interpolation (more robust than double Gaussian)
        BigInteger secret = lagrangeInterpolation(points.subList(0, k));
        System.out.println("Secret (constant term): " + secret);
    }

    // Small helper class for clarity
    static class Point {
        int x;
        BigInteger y;
        Point(int x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    // Lagrange interpolation to get constant term (x=0)
    private static BigInteger lagrangeInterpolation(List<Point> points) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < points.size(); i++) {
            BigInteger term = points.get(i).y;
            for (int j = 0; j < points.size(); j++) {
                if (i != j) {
                    // Multiply term by (0 - xj) / (xi - xj)
                    term = term.multiply(BigInteger.valueOf(-points.get(j).x))
                            .divide(BigInteger.valueOf(points.get(i).x - points.get(j).x));
                }
            }
            result = result.add(term);
        }

        return result;
    }
}
