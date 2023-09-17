package com.adsoa.adsoa;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.*;
import java.io.*;
import java.util.Arrays;


public class MainController {
    static class CalcClient extends Thread {
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private TextArea displayArea;

        private TextField displayField;
        private String message;

        public CalcClient(TextArea displayArea, TextField displayField) {
            this.displayArea = displayArea;
            this.displayField = displayField;
        }

        public void run() {
            try {
                startConnection("127.0.0.1", 6969);
                while (true) {
                    if (message != null) {
                        out.println(message);
                        message = null;
                    }
                    if (in.ready()) {
                        String response = in.readLine();
                        Platform.runLater(() -> displayArea.appendText("Response: \n" + response + "\n"));
                        EncoderDecoder ed = new EncoderDecoder();

                        // Response comes like this
                        // {server3=c5.0, server2=c5.0, server1=c5.0}

                        // Remove the curly braces
                        String[] servers = response.substring(1, response.length() - 1).split(", ");

                        Double[] decodedResults = new Double[servers.length];

                        // Decode the results from each server
                        for (String server : servers) {
                            String[] serverResult = server.split("=");
                            String serverName = serverResult[0];
                            String result = serverResult[1];
                            Platform.runLater(() -> displayArea.appendText("Server " + serverName + " result: " + ed.decode(result.getBytes()) + "\n"));
                            // To get the result, remove the code 99| from the beginning of the decoded result and convert it to a double
                            decodedResults[Integer.parseInt(serverName.substring(6)) - 1] = Double.parseDouble(ed.decode(result.getBytes()).substring(3));
                        }

                        // Calculate the average of the results
                        double average = 0;
                        for (double result : decodedResults) {
                            average += result;
                        }
                        average /= decodedResults.length;
                        double finalAverage = average;
                        Platform.runLater(() -> displayArea.appendText("Average: " + finalAverage + "\n"));
                        // Clear the display field and display the average
                        Platform.runLater(() -> displayField.clear());

                        Platform.runLater(() -> displayField.setText(String.valueOf(finalAverage)));

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void startConnection(String ip, int port) throws IOException {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public void sendMessage(String msg) {
            message = msg;
        }

        public void stopConnection() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }
    }


    public TextField displayField;
    public TextArea displayArea;
    private CalcClient client;

    @FXML
    private void initialize() {
        client = new CalcClient(displayArea, displayField);
        client.start();
    }
    @FXML
    private void onDigitButtonClicked(ActionEvent event) {
        Button digitButton = (Button) event.getSource();
        displayField.appendText(digitButton.getText());
    }

    @FXML
    private void onOperationButtonClicked(ActionEvent event) {
        EncoderDecoder ed = new EncoderDecoder();
        Button operationButton = (Button) event.getSource();
        String operation = operationButton.getText();
        if (operation.equals("=")) {
            String expression = displayField.getText();
            displayArea.clear();

            double result = 0;
            int ammonutOfServers = 0;
//            Make request to the /calc endpoint
            try {
                displayArea.appendText("Sending expression to middleware: " + expression + "\n");
                byte[] encodedOperation = ed.encodeOperation(expression);
                client.sendMessage(new String(encodedOperation));
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Error connecting to server");
                alert.showAndWait();
                e.printStackTrace();

            }
        } else if (operation.equals("C")) {
            displayField.clear();

        } else {
            //        Do not allow more than one operator (+, -, *, /) per expression once an operator has been entered, d
            //      do not allow the user to enter another operator.
            String expression = displayField.getText();
            if (expression.length() > 0) {
                String[] operands = expression.split(" ");
                if (operands.length == 3) {
                    displayField.setText(expression);
                } else if (operands.length == 1) {
                    displayField.appendText(" " + operation + " ");
                } else {
                    displayField.setText(expression);
                }
            }
        }
}

    public void onDecimalButtonClicked(ActionEvent actionEvent) {
        displayField.appendText(".");

    }

    public void onClearButtonClicked(ActionEvent actionEvent) {
        displayField.clear();
        displayArea.clear();
    }

    public void onBackspaceButtonClicked(ActionEvent actionEvent) {
        String expression = displayField.getText();
        if (!expression.isEmpty()) {
            displayField.setText(expression.substring(0, expression.length() - 1));
        }
    }
}
