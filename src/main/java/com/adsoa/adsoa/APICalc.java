package com.adsoa.adsoa;

import static spark.Spark.get;

import spark.Request;
import spark.Response;
import spark.Route;
import org.json.JSONObject;

public class APICalc {
    public static void main(String[] args) {
        get("/hello", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                response.type("application/json");
                return new JSONObject().put("message", "Hola ADSOA!").toString();
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
                double result = switch (operator) {
                    case "+" -> operand1 + operand2;
                    case "-" -> operand1 - operand2;
                    case "*" -> operand1 * operand2;
                    case "/" -> operand1 / operand2;
                    default -> 0;
                };
                response.type("application/json");
                return new JSONObject().put("result", result).toString();
            }
        });
    }
}
