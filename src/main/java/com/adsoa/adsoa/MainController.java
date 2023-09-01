package com.adsoa.adsoa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.*;
import java.io.*;


public class MainController {
    public TextField displayField;
    public TextArea displayArea;

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

            double result = 0;
//            Make request to the /calc endpoint
            try {
                CalcClient client = new CalcClient();
                client.startConnection("127.0.0.1", 6969);
                String response = client.sendMessage(expression);
//                The response is in this format: {server2=4.0, server1=4.0}
                String[] servers = response.substring(1, response.length() - 1).split(", ");

//                Show the result from each server in the text area
                for (String server : servers) {
                    String[] serverResult = server.split("=");
                    displayArea.appendText(serverResult[0] + ": " + serverResult[1] + "\n");
//                    For the result, display the average of the results from all servers
                    result += Double.parseDouble(serverResult[1]);

                }

                client.stopConnection();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error");
                alert.setContentText("Error connecting to server");
                alert.showAndWait();
                e.printStackTrace();

            }
            displayField.setText(String.format("%.2f", result / 2));
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
