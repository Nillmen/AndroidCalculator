package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplayInput;
    private TextView tvDisplayResult;
    private StringBuilder currentInput = new StringBuilder();
    private boolean isResultDisplayed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplayInput = findViewById(R.id.tvDisplayInput);
        tvDisplayResult = findViewById(R.id.tvDisplayResult);

        // 1. Nhóm nút số và dấu chấm
        int[] numberButtons = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9, R.id.btnDot
        };

        View.OnClickListener numberListener = v -> {
            if (isResultDisplayed) {
                currentInput.setLength(0);
                tvDisplayResult.setText("0");
                isResultDisplayed = false;
            }
            currentInput.append(((Button) v).getText().toString());
            tvDisplayInput.setText(currentInput.toString());
        };

        for (int id : numberButtons) {
            findViewById(id).setOnClickListener(numberListener);
        }

        // 2. Nhóm phép tính cơ bản
        setOperatorListener(R.id.btnAdd, "+");
        setOperatorListener(R.id.btnSub, "−");
        setOperatorListener(R.id.btnMul, "×");
        setOperatorListener(R.id.btnDiv, "÷");

        // Nút đảo dấu (+/-): Đảo dấu của con số cuối cùng đang nhập
        findViewById(R.id.btnSign).setOnClickListener(v -> {
            if (isResultDisplayed) isResultDisplayed = false;
            
            if (currentInput.length() == 0) {
                currentInput.append("−");
            } else {
                // Tìm vị trí bắt đầu của con số cuối cùng (sau toán tử cuối cùng)
                int lastOpIndex = -1;
                for (int i = currentInput.length() - 1; i >= 0; i--) {
                    char c = currentInput.charAt(i);
                    // Nếu gặp toán tử, nhưng phải đảm bảo nó không phải là dấu âm của chính số đó
                    if ("+−×÷(".indexOf(c) != -1) {
                        if (i > 0 && "+−×÷(".indexOf(currentInput.charAt(i - 1)) != -1) {
                            continue; // Đây là dấu âm đứng trước số (ví dụ *−5), bỏ qua để tìm toán tử thực sự
                        }
                        lastOpIndex = i;
                        break;
                    }
                }

                // Đảo dấu: Nếu đã có dấu trừ thì xóa, nếu chưa thì thêm
                int signPos = lastOpIndex + 1;
                if (signPos < currentInput.length() && currentInput.charAt(signPos) == '−') {
                    currentInput.deleteCharAt(signPos);
                } else {
                    currentInput.insert(signPos, "−");
                }
            }
            tvDisplayInput.setText(currentInput.toString());
        });

        // 3. Nhóm chức năng đặc biệt
        findViewById(R.id.btnPi).setOnClickListener(v -> addSymbol("π"));
        findViewById(R.id.btnSqRoot).setOnClickListener(v -> addSymbol("√"));
        findViewById(R.id.btnSquare).setOnClickListener(v -> addSymbol("²"));
        findViewById(R.id.btnPercent).setOnClickListener(v -> addSymbol("%"));
        findViewById(R.id.btnOpenBracket).setOnClickListener(v -> addSymbol("("));
        findViewById(R.id.btnCloseBracket).setOnClickListener(v -> addSymbol(")"));

        // 4. Nút Xóa lùi (⌫)
        findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                currentInput.deleteCharAt(currentInput.length() - 1);
                tvDisplayInput.setText(currentInput.length() == 0 ? "0" : currentInput.toString());
            }
        });

        // 5. Nút Xóa hết (C)
        findViewById(R.id.btnClear).setOnClickListener(v -> {
            currentInput.setLength(0);
            tvDisplayInput.setText("0");
            tvDisplayResult.setText("0");
            isResultDisplayed = false;
        });

        // 6. Nút Bằng (=)
        findViewById(R.id.btnEqual).setOnClickListener(v -> {
            if (currentInput.length() > 0) {
                String result = Calculator.calculate(currentInput.toString());
                tvDisplayResult.setText(result);
                currentInput.setLength(0);
                currentInput.append(result);
                isResultDisplayed = true;
            }
        });
    }

    private void addSymbol(String symbol) {
        if (isResultDisplayed) {
            if (symbol.equals("√")) {
                currentInput.insert(0, "√");
                isResultDisplayed = false;
                tvDisplayInput.setText(currentInput.toString());
                return;
            } else if (symbol.equals("(")) {
                currentInput.setLength(0);
            }
            isResultDisplayed = false;
        }
        
        // Tự động thêm dấu nhân trước Pi hoặc dấu ngoặc
        if ((symbol.equals("π") || symbol.equals("(")) && currentInput.length() > 0) {
            char last = currentInput.charAt(currentInput.length() - 1);
            if (Character.isDigit(last) || last == ')' || last == '²' || last == '%') {
                currentInput.append("×");
            }
        }
        currentInput.append(symbol);
        tvDisplayInput.setText(currentInput.toString());
    }

    private void setOperatorListener(int id, String operator) {
        findViewById(id).setOnClickListener(v -> {
            if (isResultDisplayed) isResultDisplayed = false;

            if (currentInput.length() > 0) {
                char last = currentInput.charAt(currentInput.length() - 1);
                
                if (isOperator(last)) {
                    // Cho phép viết dấu trừ sau dấu nhân/chia (ví dụ: 5 × −3)
                    if (operator.equals("−") && (last == '×' || last == '÷')) {
                        currentInput.append(operator);
                    } else {
                        // Nếu không, thay thế toán tử cũ (hoặc cụm toán tử như ×−) bằng toán tử mới
                        int i = currentInput.length() - 1;
                        while (i >= 0 && isOperator(currentInput.charAt(i))) {
                            i--;
                        }
                        currentInput.setLength(i + 1);
                        currentInput.append(operator);
                    }
                } else {
                    currentInput.append(operator);
                }
            } else if (operator.equals("−")) {
                currentInput.append(operator);
            }
            tvDisplayInput.setText(currentInput.toString());
        });
    }

    private boolean isOperator(char c) {
        return "+−×÷".indexOf(c) != -1;
    }
}
