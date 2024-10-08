package com.adsoa.adsoa;

import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MainController {
    static class CalcClient extends Thread {
        private Socket clientSocket;
        private OutputStream out;
        private BufferedReader in;
        private TextArea displayArea;
        private TextField displayField;
        private byte[] message;

        private ConcurrentHashMap<Short, ConcurrentLinkedQueue<byte[]>> operationQueues;
        private ExecutorService messageDispatcher;

        public CalcClient(TextArea displayArea, TextField displayField,
                          ConcurrentHashMap<Short, ConcurrentLinkedQueue<byte[]>> operationQueues,
                          ExecutorService messageDispatcher) {
            this.displayArea = displayArea;
            this.displayField = displayField;
            this.operationQueues = operationQueues;
            this.messageDispatcher = messageDispatcher;
        }

        public void run() {
            try {
                startConnection("127.0.0.1", 6969);
                while (true) {
                    if (message != null) {
                        out.write(message, 0, message.length);
                        message = null;
                    }
                    if (in.ready()) {
                        String response = in.readLine();
                        Platform.runLater(() -> displayArea.appendText("Response: \n" + response + "\n"));
                        EncoderDecoder ed = new EncoderDecoder();

                        String[] servers = response.substring(1, response.length() - 1).split(", ");

                        Double[] decodedResults = new Double[servers.length];

                        // Decode the results from each server
                        for (int i = 0; i < servers.length; i++) {
                            String[] serverResult = servers[i].split("=");
                            String serverName = serverResult[0];
                            double decodedResult = Double.parseDouble(serverResult[1]);
                            decodedResults[i] = decodedResult;

                            Platform.runLater(() -> displayArea.appendText("Server " + serverName + " result: " + decodedResult + "\n"));
                        }

                        // Calculate the average of the results
                        double average = 0;
                        for (double result : decodedResults) {
                            average += result;
                        }
                        average /= decodedResults.length;
                        double finalAverage = average;
                        Platform.runLater(() -> displayArea.appendText("Average: " + finalAverage + "\n"));

                        // Display the average
                        Platform.runLater(() -> displayField.setText(String.valueOf(finalAverage)));

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void startConnection(String ip, int port) throws IOException {
            clientSocket = new Socket(ip, port);
            out = clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }

        public void sendMessage(byte[] msg) {
            ByteBuffer buffer = ByteBuffer.wrap(msg);
            short operationType = buffer.getShort();
            operationQueues.get(operationType).add(msg);
        }

        public void sendMessageDirectly(byte[] msg) throws IOException {
            out.write(msg, 0, msg.length);
        }

        private void startMessageDispatcher() {
            messageDispatcher.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    operationQueues.forEach((operationType, queue) -> {
                        byte[] message = queue.poll();
                        if (message != null) {
                            try {
                                client.sendMessageDirectly(message);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    try {
                        Thread.sleep(100); // Avoid CPU overuse
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }



        public void stopConnection() throws IOException {
            in.close();
            out.close();
            clientSocket.close();
        }
    }


    public TextField displayField;
    public TextArea displayArea;
    private static CalcClient client;

    private ConcurrentHashMap<Short, ConcurrentLinkedQueue<byte[]>> operationQueues = new ConcurrentHashMap<>();
    private ExecutorService messageDispatcher = Executors.newSingleThreadExecutor();


    @FXML
    private void initialize() throws IOException {
        // Initialize queues for each operation type
        operationQueues.put((short) 1, new ConcurrentLinkedQueue<>());
        operationQueues.put((short) 2, new ConcurrentLinkedQueue<>());
        operationQueues.put((short) 3, new ConcurrentLinkedQueue<>());
        operationQueues.put((short) 4, new ConcurrentLinkedQueue<>());

        // Start client and dispatcher
        client = new CalcClient(displayArea, displayField, operationQueues, messageDispatcher);
        client.start();
        client.startMessageDispatcher();
    }

    @FXML
    private void onDigitButtonClicked(ActionEvent event) {
        Button digitButton = (Button) event.getSource();
        displayField.appendText(digitButton.getText());
    }

    @FXML
    public void onOperationButtonClicked(ActionEvent event) {
        EncoderDecoder ed = new EncoderDecoder();
        Button operationButton = (Button) event.getSource();
        String operation = operationButton.getText();
        if (operation.equals("=")) {
            String expression = displayField.getText();
            displayArea.clear();

            double result = 0;
            int amountOfServers = 0;
            try {
                displayArea.appendText("Sending expression to middleware: " + expression + "\n");
                byte[] encodedOperation = ed.encodeArithmeticOperation(expression, "abc");
                client.sendMessage(encodedOperation);
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
