package week9;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class clientHandle extends Thread {
    private Socket clientSocket;

    public clientHandle(Socket socket) {
        this.clientSocket = socket;
    }

    private String processData(String input) {
        String[] tokens = input.trim().split("\\s+", 2);
        if (tokens.length < 2) {
            return "Error: Invalid syntax. Use 'calc <expression>' or 'prime <number>'.";
        }

        String command = tokens[0].toLowerCase();
        String data = tokens[1];

        switch (command) {
            case "calc":
                return calculate(data);
            case "prime":
                return primeFactorize(data);
            default:
                return "Error: Unknown command '" + command + "'.";
        }
    }

    private String calculate(String expression) {
        try {
            // Tim toan tu
            String operator = "";
            if (expression.contains("+")) operator = "\\+";
            else if (expression.contains("-")) operator = "-";
            else if (expression.contains("*")) operator = "\\*";
            else if (expression.contains("/")) operator = "/";
            else return "Error: Invalid expression. No operator found.";

            String[] numbers = expression.split(operator);
            if (numbers.length != 2) return "Error: Invalid expression. Must have two numbers.";

            double num1 = Double.parseDouble(numbers[0].trim());
            double num2 = Double.parseDouble(numbers[1].trim());
            double result = 0;

            switch (operator) {
                case "\\+": result = num1 + num2; break;
                case "-": result = num1 - num2; break;
                case "\\*": result = num1 * num2; break;
                case "/":
                    if (num2 == 0) return "Error: Cannot divide by zero.";
                    result = num1 / num2;
                    break;
            }
            return expression + " = " + result;
        } catch (NumberFormatException e) {
            return "Error: Invalid numbers in expression.";
        }
    }

    private String primeFactorize(String numberStr) {
        try {
            int n = Integer.parseInt(numberStr.trim());
            if (n <= 1) {
                return n + " is not a positive integer greater than 1.";
            }

            StringBuilder result = new StringBuilder(n + " = ");
            int tempN = n;
            for (int i = 2; i <= tempN; i++) {
                while (tempN % i == 0) {
                    result.append(i);
                    tempN /= i;
                    if (tempN > 1) {
                        result.append(" * ");
                    }
                }
            }
            return result.toString();
        } catch (NumberFormatException e) {
            return "Error: '" + numberStr + "' is not a valid integer.";
        }
    }


    @Override
    public void run() {
        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter responseWriter = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String inputLine;
            // Lien tuc doc du lieu client gui toi
            while ((inputLine = in.readLine()) != null) {
                System.out.println("[Client " + clientSocket.getPort() + "]: " + inputLine);

                if (inputLine.equalsIgnoreCase("exit") || inputLine.equalsIgnoreCase("quit")) {
                    System.out.println("[-] Client " + clientSocket.getPort() + " da ngat ket noi.");
                    break;
                }

                // Xu ly du lieu va tra ve ket qua
                String response = processData(inputLine);
                responseWriter.println(response);
            }
        } catch (IOException e) {
            System.err.println("Loi ket noi voi Client " + clientSocket.getPort() + ": " + e.getMessage());
        } finally {
            try {
                clientSocket.close(); // Dam bao dong socket khi thread ket thuc
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
