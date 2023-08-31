package com.adsoa.adsoa;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class MainController {
    public TextField displayField;

    @FXML
    private void onDigitButtonClicked(ActionEvent event) {
        Button digitButton = (Button) event.getSource();
        displayField.appendText(digitButton.getText());
    }

//    @FXML
//    private void onOperationButtonClicked(ActionEvent event) {
//        Button operationButton = (Button) event.getSource();
//        String operation = operationButton.getText();
//        if (operation.equals("=")) {
//            String expression = displayField.getText();
//            String[] operands = expression.split(" ");
//            double operand1 = Double.parseDouble(operands[0]);
//            double operand2 = Double.parseDouble(operands[2]);
//            double result = 0;
//            switch (operands[1]) {
//                case "+":
//                    result = operand1 + operand2;
//                    break;
//                case "-":
//                    result = operand1 - operand2;
//                    break;
//                case "*":
//                    result = operand1 * operand2;
//                    break;
//                case "/":
//                    result = operand1 / operand2;
//                    break;
//            }
//            displayField.setText(String.format("%.4f", result));
//        } else if (operation.equals("C")) {
//            displayField.clear();
//
//        } else {
//            //        Do not allow more than one operator (+, -, *, /) per expression once an operator has been entered, d
//            //      do not allow the user to enter another operator.
//            String expression = displayField.getText();
//            if (expression.length() > 0) {
//                String[] operands = expression.split(" ");
//                if (operands.length == 3) {
//                    displayField.setText(expression);
//                } else if (operands.length == 1) {
//                    displayField.appendText(" " + operation + " ");
//                } else {
//                    displayField.setText(expression);
//                }
//            }
//        }
//    }

    @FXML
    private void onOperationButtonClicked(ActionEvent event) {
        Button operationButton = (Button) event.getSource();
        String operation = operationButton.getText();
        if (operation.equals("=")) {
            String expression = displayField.getText();
            String[] operands = expression.split(" ");
            double operand1 = Double.parseDouble(operands[0]);
            double operand2 = Double.parseDouble(operands[2]);
            String operator = operands[1];
            if (operator.equals("+")) {
                operator = "%2B";
            }

            double result = 0;
//            Make request to the /calc endpoint
            try {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://localhost:4567/calc?operator=" + operator + "&operand1=" + operand1 + "&operand2=" + operand2))
                        .method("GET", HttpRequest.BodyPublishers.noBody())
                        .build();
                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println(URI.create("http://localhost:4567/calc?operator=" + operator + "&operand1=" + operand1 + "&operand2=" + operand2));
                // Result is a JSON string, so we need to parse it to a double
//                {"result": 4.0}
                result = Double.parseDouble(response.body().split(":")[1].split("}")[0]);


            } catch (Exception e) {
                e.printStackTrace();
            }
            displayField.setText(String.format("%.4f", result));
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
    }

    public void onBackspaceButtonClicked(ActionEvent actionEvent) {
        String expression = displayField.getText();
        if (!expression.isEmpty()) {
            displayField.setText(expression.substring(0, expression.length() - 1));
        }
    }
}
