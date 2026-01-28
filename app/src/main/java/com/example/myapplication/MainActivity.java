package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplayInput;
    private TextView tvDisplayResult;
    private StringBuilder currentInput = new StringBuilder();
    private boolean isResultDisplayed = false; // Kiểm tra xem vừa mới nhấn dấu = xong hay không

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplayInput = findViewById(R.id.tvDisplayInput);
        tvDisplayResult = findViewById(R.id.tvDisplayResult);

        // Nút số
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };

        View.OnClickListener numberListener = v -> {
            if (isResultDisplayed) {
                // Nếu vừa ấn bằng xong mà ấn số -> Xóa hết làm phép tính mới
                currentInput.setLength(0);
                tvDisplayResult.setText("0");
                isResultDisplayed = false;
            }
            Button b = (Button) v;
            currentInput.append(b.getText().toString());
            tvDisplayInput.setText(currentInput.toString());
        };

        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(numberListener);
        }

        // Nút phép tính
        setOperatorListener(R.id.btnAdd, "+");
        setOperatorListener(R.id.btnSub, "−");
        setOperatorListener(R.id.btnMul, "×");
        setOperatorListener(R.id.btnDiv, "÷");

        // Nút Xóa (C)
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput.setLength(0);
            tvDisplayInput.setText("0");
            tvDisplayResult.setText("0");
            isResultDisplayed = false;
        });

        // Nút Bằng (=)
        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                String expression = currentInput.toString();
                // Chặn trường hợp kết thúc bằng một dấu phép tính (ví dụ "5+6+")
                if (isOperator(expression.charAt(expression.length() - 1))) {
                    expression = expression.substring(0, expression.length() - 1);
                }

                String result = calculateResult(expression);
                tvDisplayResult.setText(result);

                // Sau khi nhấn bằng, nếu muốn tính tiếp từ kết quả này thì lưu lại
                currentInput.setLength(0);
                currentInput.append(result);
                isResultDisplayed = true;
            }
        });
    }

    private void setOperatorListener(int id, String operator) {
        findViewById(id).setOnClickListener(v -> {
            if (currentInput.length() == 0) {
                // Không cho phép bắt đầu bằng các dấu trừ dấu trừ (cho số âm)
                if (operator.equals("−")) {
                    currentInput.append(operator);
                    tvDisplayInput.setText(currentInput.toString());
                }
                return;
            }

            char lastChar = currentInput.charAt(currentInput.length() - 1);

            if (isOperator(lastChar)) {
                // Nếu ký tự cuối là dấu, thay thế bằng dấu mới (Chặn lỗi ++, +-, x+)
                currentInput.setLength(currentInput.length() - 1);
            }

            currentInput.append(operator);
            tvDisplayInput.setText(currentInput.toString());
            isResultDisplayed = false; // Tiếp tục chuỗi tính toán
        });
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '−' || c == '×' || c == '÷';
    }

    private String calculateResult(String expression) {
        try {
            String cleanExpr = expression.replace('×', '*').replace('÷', '/').replace('−', '-');

            // Logic tách số và dấu
            List<Double> numbers = new ArrayList<>();
            List<Character> operators = new ArrayList<>();
            StringBuilder tempNum = new StringBuilder();

            for (int i = 0; i < cleanExpr.length(); i++) {
                char c = cleanExpr.charAt(i);
                // Xử lý số âm ở đầu chuỗi
                if (i == 0 && c == '-') {
                    tempNum.append(c);
                    continue;
                }
                if (Character.isDigit(c) || c == '.') {
                    tempNum.append(c);
                } else {
                    numbers.add(Double.parseDouble(tempNum.toString()));
                    operators.add(c);
                    tempNum.setLength(0);
                }
            }
            numbers.add(Double.parseDouble(tempNum.toString()));

            // Nhân chia trước
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

            // Cộng trừ sau
            double finalRes = numbers.get(0);
            for (int i = 0; i < operators.size(); i++) {
                char op = operators.get(i);
                if (op == '+') finalRes += numbers.get(i + 1);
                if (op == '-') finalRes -= numbers.get(i + 1);
            }

            if (finalRes == (long) finalRes) return String.valueOf((long) finalRes);
            return String.valueOf(finalRes);
        } catch (Exception e) {
            return "Error";
        }
    }
}