package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplayInput, tvDisplayResult;
    private StringBuilder currentInput = new StringBuilder();
    private boolean isResultDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplayInput = findViewById(R.id.tvDisplayInput);
        tvDisplayResult = findViewById(R.id.tvDisplayResult);

        // Danh sách ID của tất cả các nút để gán sự kiện chung
        int[] buttonIds = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv,
                R.id.btnPi, R.id.btnSqRoot, R.id.btnSquare, R.id.btnPercent,
                R.id.btnOpenBracket, R.id.btnCloseBracket, R.id.btnSign,
                R.id.btnDelete, R.id.btnClear, R.id.btnEqual
        };

        for (int id : buttonIds) {
            findViewById(id).setOnClickListener(v -> {
                Button b = (Button) v;
                handleAction(b.getText().toString());
            });
        }
    }

    /**
     * Hàm trung tâm xử lý mọi hành động từ bàn phím
     */
    private void handleAction(String action) {
        // Nếu vừa có kết quả mà nhập mới (không phải tính tiếp), thì xóa trắng
        if (isResultDisplayed && !"+−×÷²%".contains(action)) {
            currentInput.setLength(0);
            tvDisplayResult.setText("0");
        }
        isResultDisplayed = false;

        switch (action) {
            case "C":
                currentInput.setLength(0);
                tvDisplayResult.setText("0");
                break;
            case "⌫": // Nút Delete
                if (currentInput.length() > 0) currentInput.deleteCharAt(currentInput.length() - 1);
                break;
            case "=":
                processCalculation();
                break;
            case "+/-":
                toggleSign();
                break;
            case "√":
                appendSymbol("√("); // Yêu cầu 2: √ tự động mở ngoặc
                break;
            case "x²":
                appendSymbol("²");
                break;
            default:
                if ("+−×÷".contains(action)) {
                    appendOperator(action);
                } else {
                    appendSymbol(action);
                }
                break;
        }

        tvDisplayInput.setText(currentInput.length() == 0 ? "0" : currentInput.toString());
    }

    /**
     * Xử lý nhập số và các ký hiệu đặc biệt (π, %, ngoặc...)
     */
    private void appendSymbol(String s) {
        if (currentInput.length() > 0) {
            char last = currentInput.charAt(currentInput.length() - 1);
            // Yêu cầu 1: Tự thêm dấu nhân trước Pi hoặc ngoặc nếu trước đó là số/ký hiệu đóng
            if ((s.startsWith("π") || s.startsWith("(") || s.startsWith("√")) &&
                    (Character.isDigit(last) || last == ')' || last == '²' || last == '%')) {
                currentInput.append("×");
            }
        }
        currentInput.append(s);
    }

    /**
     * Xử lý nhập toán tử với quy tắc nghiêm ngặt
     */
    private void appendOperator(String op) {
        if (currentInput.length() == 0) {
            // Nhân/Chia không được đứng đầu, chỉ cho phép + hoặc -
            if (op.equals("+") || op.equals("−")) currentInput.append(op);
            return;
        }

        char last = currentInput.charAt(currentInput.length() - 1);
        if ("+−×÷".indexOf(last) != -1) {
            // Quy tắc: Cho phép dấu + hoặc - đứng sau x hoặc chia (ví dụ 3x-2)
            if ((op.equals("+") || op.equals("−")) && (last == '×' || last == '÷')) {
                currentInput.append(op);
            } else {
                // Thay thế toán tử cũ bằng cái mới nhất (ghi đè)
                int i = currentInput.length() - 1;
                while (i >= 0 && "+−×÷".indexOf(currentInput.charAt(i)) != -1) {
                    i--;
                }
                currentInput.setLength(i + 1);
                currentInput.append(op);
            }
        } else {
            currentInput.append(op);
        }
    }

    /**
     * Logic đảo dấu thông minh cho cụm số gần nhất
     */
    /**
     * Logic đảo dấu thông minh: Đảo dấu của con số cuối cùng đang nhập.
     * Ví dụ: "+3" -> "-3", "5+3" -> "5-3", "5×3" -> "5×-3"
     */
    private void toggleSign() {
        if (currentInput.length() == 0) {
            currentInput.append("−");
            return;
        }

        int i = currentInput.length() - 1;
        // 1. Bỏ qua các toán tử ở cuối chuỗi nếu người dùng bấm dấu xong mới bấm đảo dấu
        while (i >= 0 && "+−×÷".indexOf(currentInput.charAt(i)) != -1) i--;
        if (i < 0) i = 0;

        // 2. Tìm vị trí bắt đầu của con số hiện tại (dừng lại khi gặp bất kỳ toán tử nào)
        while (i > 0 && "+−×÷(".indexOf(currentInput.charAt(i - 1)) == -1) i--;

        // 3. i bây giờ là vị trí bắt đầu của số. Ta kiểm tra ký tự ngay trước nó (i-1)
        if (i > 0 && (currentInput.charAt(i - 1) == '−' || currentInput.charAt(i - 1) == '+')) {
            char op = currentInput.charAt(i - 1);
            // Kiểm tra xem toán tử này là dấu của số hay là phép tính nối giữa 2 số
            // Nếu phía trước nó là ×, ÷ hoặc ( thì nó là "dấu của số"
            boolean isOnlySign = (i - 1 == 0 || "×÷(".indexOf(currentInput.charAt(i - 2)) != -1);

            if (op == '−') {
                if (isOnlySign) {
                    currentInput.deleteCharAt(i - 1); // -3 -> 3
                } else {
                    currentInput.setCharAt(i - 1, '+'); // 5-3 -> 5+3
                }
            } else {
                currentInput.setCharAt(i - 1, '−'); // +3 -> -3 hoặc 5+3 -> 5-3
            }
        }
        // 4. Trường hợp đặc biệt: Dấu nằm ở ngay đầu chuỗi (index 0)
        else if (i == 0 && (currentInput.charAt(0) == '−' || currentInput.charAt(0) == '+')) {
            if (currentInput.charAt(0) == '−') {
                currentInput.deleteCharAt(0);
            } else {
                currentInput.setCharAt(0, '−');
            }
        }
        // 5. Nếu không có dấu nào phía trước, ta chèn thêm dấu trừ
        else {
            currentInput.insert(i, "−");
        }
    }

    private void processCalculation() {
        if (currentInput.length() > 0) {
            String result = Calculator.calculate(currentInput.toString());
            tvDisplayResult.setText(result);
            currentInput.setLength(0);
            currentInput.append(result);
            isResultDisplayed = true;
        }
    }
}