import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("W:\\java\\SpringBoot\\Hashira\\src\\input.json"));
        int n = root.get("keys").get("n").asInt();
        int k = root.get("keys").get("k").asInt();

        List<double[]> points = new ArrayList<>();

        // Parse x, decode y based on base
        Iterator<String> fieldNames = root.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (key.equals("keys")) continue;

            int x = Integer.parseInt(key);
            JsonNode node = root.get(key);
            int base = Integer.parseInt(node.get("base").asText());
            String value = node.get("value").asText();

            int y = Integer.parseInt(value, base); // decode based on base
            points.add(new double[]{x, y});
        }

        // Sort points by x just for consistency
        points.sort(Comparator.comparingDouble(arr -> arr[0]));
        for(double[] d : points) {
            System.out.println(d[0] + " " + d[1]);
        }
        if (points.size() < k) {
            throw new RuntimeException("Not enough points to reconstruct polynomial");
        }

        // Use first k points to solve system
        double[][] A = new double[k][k];
        double[] B = new double[k];

        for (int i = 0; i < k; i++) {
            double x = points.get(i)[0];
            double y = points.get(i)[1];

            // Fill row for quadratic: ax^2 + bx + c = y
            A[i][0] = x * x; // a coefficient
            A[i][1] = x;     // b coefficient
            A[i][2] = 1;     // c coefficient
            B[i] = y;
        }

        double[] solution = solveLinearSystem(A, B);
        double a = solution[0];
        double b = solution[1];
        double c = solution[2];

        System.out.println("Solved Polynomial: y = " + a + "x^2 + " + b + "x + " + c);
        System.out.println("Constant term (c): " + c);
    }

    // Gaussian elimination solver for Ax = B
    private static double[] solveLinearSystem(double[][] A, double[] B) {
        int n = B.length;

        // Forward elimination
        for (int i = 0; i < n; i++) {
            // Partial pivoting
            int maxRow = i;
            for (int k = i + 1; k < n; k++) {
                if (Math.abs(A[k][i]) > Math.abs(A[maxRow][i])) {
                    maxRow = k;
                }
            }

            // Swap rows
            double[] temp = A[i];
            A[i] = A[maxRow];
            A[maxRow] = temp;

            double t = B[i];
            B[i] = B[maxRow];
            B[maxRow] = t;

            // Normalize pivot row
            double pivot = A[i][i];
            if (pivot == 0) throw new RuntimeException("No unique solution");
            for (int j = i; j < n; j++) A[i][j] /= pivot;
            B[i] /= pivot;

            // Eliminate below
            for (int k = i + 1; k < n; k++) {
                double factor = A[k][i];
                for (int j = i; j < n; j++) A[k][j] -= factor * A[i][j];
                B[k] -= factor * B[i];
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = B[i];
            for (int j = i + 1; j < n; j++) {
                x[i] -= A[i][j] * x[j];
            }
        }
        return x;
    }
}
