package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Calculator {

    public static String calculate(String expression) {
        try {
            // Loại bỏ tất cả khoảng trắng để tránh lỗi index khi kiểm tra dấu âm
            String cleanExpr = expression.replace(" ", "");
            return calculateParentheses(cleanExpr);
        } catch (Exception e) {
            return "Error";
        }
    }

    private static String calculateParentheses(String expr) {
        String temp = expr;
        while (temp.contains("(") && temp.contains(")")) {
            int start = temp.lastIndexOf("(");
            int end = temp.indexOf(")", start);
            if (end != -1) {
                String subExpr = temp.substring(start + 1, end);
                String result = calculateCore(subExpr);
                temp = temp.substring(0, start) + result + temp.substring(end + 1);
            } else {
                break;
            }
        }
        if (temp.contains("(") || temp.contains(")")) {
            temp = temp.replace("(", "").replace(")", "");
        }
        return calculateCore(temp);
    }

    private static String calculateCore(String expr) {
        try {
            String clean = preprocess(expr);
            List<Double> numbers = new ArrayList<>();
            List<Character> operators = new ArrayList<>();

            parseTokens(clean, numbers, operators);

            if (numbers.isEmpty()) return "0";

            applyMultiplicationDivision(numbers, operators);
            double result = applyAdditionSubtraction(numbers, operators);

            return formatResult(result);
        } catch (Exception e) {
            return "Error";
        }
    }

    private static String preprocess(String expr) {
        return expr.replace("π", String.valueOf(Math.PI))
                .replace('×', '*')
                .replace('÷', '/')
                .replace('−', '-');
    }

    private static void parseTokens(String clean, List<Double> numbers, List<Character> operators) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);

            // CẢI TIẾN: Nhận diện dấu âm chính xác hơn (kể cả sau dấu trừ khác)
            if (c == '-' && (i == 0 || "+-*/".indexOf(clean.charAt(i - 1)) != -1)) {
                sb.append(c);
                continue;
            }

            if ("0123456789.√²%".indexOf(c) != -1) {
                sb.append(c);
            } else {
                if (sb.length() > 0) {
                    numbers.add(evaluateTerm(sb.toString()));
                    sb.setLength(0);
                }
                if ("+-*/".indexOf(c) != -1) operators.add(c);
            }
        }
        if (sb.length() > 0) {
            numbers.add(evaluateTerm(sb.toString()));
        }
    }

    private static void applyMultiplicationDivision(List<Double> numbers, List<Character> operators) {
        for (int i = 0; i < operators.size(); i++) {
            char op = operators.get(i);
            if (op == '*' || op == '/') {
                double n1 = numbers.get(i);
                double n2 = numbers.get(i + 1);
                double res = (op == '*') ? n1 * n2 : n1 / n2;
                
                numbers.set(i, res);
                numbers.remove(i + 1);
                operators.remove(i);
                i--; 
            }
        }
    }

    private static double applyAdditionSubtraction(List<Double> numbers, List<Character> operators) {
        double res = numbers.get(0);
        for (int i = 0; i < operators.size(); i++) {
            char op = operators.get(i);
            if (op == '+') res += numbers.get(i + 1);
            else res -= numbers.get(i + 1);
        }
        return res;
    }

    private static double evaluateTerm(String term) {
        if (term.contains("√")) {
            String val = term.replace("√", "");
            if (val.isEmpty() || val.equals("-")) return 0;
            // Xử lý trường hợp số âm trong căn (ví dụ √-16)
            double d = Double.parseDouble(val);
            return (d < 0) ? Double.NaN : Math.sqrt(d);
        } else if (term.contains("²")) {
            return Math.pow(Double.parseDouble(term.replace("²", "")), 2);
        } else if (term.contains("%")) {
            return Double.parseDouble(term.replace("%", "")) / 100.0;
        }
        return Double.parseDouble(term);
    }

    private static String formatResult(double d) {
        if (Double.isNaN(d)) return "NaN (Error)";
        if (Double.isInfinite(d)) return "Infinity";
        if (d == (long) d) return String.valueOf((long) d);
        return String.valueOf(d);
    }
}
