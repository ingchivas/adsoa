package com.adsoa.adsoa;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.*;
import java.io.*;


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
                        // Response comes in this format {server3=9.0, server2=9.0, server1=9.0} so we need to parse it
                        // the result is the average of all the servers

                        double result = 0;

                        String[] operands = response.substring(response.indexOf("{") + 1, response.indexOf("}")).split(", ");
                        for (String operand : operands) {
                            result += Double.parseDouble(operand.substring(operand.indexOf("=") + 1));
                        }

                        result /= operands.length;

                        double finalResult = result;
                        Platform.runLater(() -> displayField.setText(String.valueOf(finalResult)));


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
                client.sendMessage(expression);
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
