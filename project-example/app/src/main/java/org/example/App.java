package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Stack;
import java.util.StringTokenizer;

public class App {

    private static String SERVER_IP = "127.0.0.1";
    private static int SERVER_PORT = 23;

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java -jar app.jar <server_ip> <server_port>");
            System.exit(1);
        }

        SERVER_IP = args[0];
        SERVER_PORT = Integer.parseInt(args[1]);

        System.out.println("Client running. Server IP: " + SERVER_IP + ", Port: " + SERVER_PORT);

        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String formula = "";
            while ((formula = in.readLine()) != null) {
                System.out.println("Received formula: " + formula);

                double result = evaluateFormula(formula);
                System.out.println("Calculated result: " + result);

                out.println(result);
                System.out.println("Sent result back to the server.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static double evaluateFormula(String formula) {
        Stack<Double> values = new Stack<>();
        Stack<Character> ops = new Stack<>();
        StringTokenizer tokens = new StringTokenizer(formula, "+-", true);

        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (token.isEmpty())
                continue;

            if (isNumber(token)) {
                values.push(Double.parseDouble(token));
            } else if (isOperator(token.charAt(0))) {
                while (!ops.isEmpty() && hasPrecedence(token.charAt(0), ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                }
                ops.push(token.charAt(0));
            }
        }

        while (!ops.isEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private static boolean isNumber(String token) {
        try {
            Double.parseDouble(token);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isOperator(char token) {
        return token == '+' || token == '-';
    }

    private static boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') {
            return false;
        }
        return true;
    }

    private static double applyOp(char op, double b, double a) {
        switch (op) {
            case '+':
                return a + b;
            case '-':
                return a - b;
        }
        return 0;
    }

}
