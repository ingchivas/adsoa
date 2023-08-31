package com.adsoa.adsoa;

import static spark.Spark.get;

import spark.Request;
import spark.Response;
import spark.Route;

public class APICalc {
    public static void main(String[] args) {
        get("/hello", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                return "Hello World!";
            }
        });

//        Api route that gets a JSON with the following format {operator: "+", operand1: 1, operand2: 2}
//        and returns a JSON with the following format {result: 3}
        get("/calc", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                String operator = request.queryParams("operator");
                double operand1 = Double.parseDouble(request.queryParams("operand1"));
                double operand2 = Double.parseDouble(request.queryParams("operand2"));
                System.out.println(request.queryParams());
                double result = 0;
                switch (operator) {
                    case "+":
                        result = operand1 + operand2;
                        break;
                    case "-":
                        result = operand1 - operand2;
                        break;
                    case "*":
                        result = operand1 * operand2;
                        break;
                    case "/":
                        result = operand1 / operand2;
                        break;
                }
                return "{\"result\": " + result + "}";
            }
        });
    }
}
