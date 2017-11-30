package com.impicode.fraction_calculator;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import io.github.kexanie.library.MathView;

public class MainActivity extends AppCompatActivity {
    //TODO dodac scrolla
    //TODO parse bigDecimal -> co jak w wyniku przeksztalcenia powstanie big cecimal np. (NWW (10^8 i 10^8 + 1)
    //dodać loga
    Button buttonCompute;
    ColorStateList defaultTextViewColors;
    ColorStateList defaultEditTextColors;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCompute = findViewById(R.id.button_compute);

        buttonCompute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                computeAndShowResult();
            }
        });
        defaultTextViewColors = ((TextView)findViewById(R.id.text_result)).getTextColors();
        defaultEditTextColors = getEditTextControl(R.id.edit_right_denominator).getTextColors();

        getEditTextControl(R.id.edit_right_denominator).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    computeAndShowResult();
                    return true;
                }
                return false;
            }
        });
    }


    public static class Fraction {
        public int numerator;
        public int denominator;

        public Fraction(int n, int d) {
            numerator = n;
            denominator = d;
        }
    }


    private static int GCD(int a, int b) {
        if (b == 0) {
            return a;
        }
        return GCD(b, a % b);
    }

    public static int LCM(int a, int b) {
        return a * b / GCD(a, b);
    }

    private String getEditText(int id) {
        EditText editText = findViewById(id);
        String ret = editText.getText().toString();
        return ret.equals("")
            ? editText.getHint().toString()
            : ret;
    }

    private EditText getEditTextControl(int id) {
        return ((EditText)findViewById(id));
    }

    private void makeBold(int id) {
        ((EditText)findViewById(id)).setTextColor(getResources().getColor(R.color.color_red));
    }

    private void setAlertMessage(String message) {
        TextView result = findViewById(R.id.text_result);
        result.setVisibility(View.VISIBLE);
        result.setTextColor(getResources().getColor(R.color.color_red));
        result.setText(message);
    }

    private void computeAndShowResult() {
        hideKeyboard();
        cleanUIState();
        Fraction left = buildFraction(
                getEditText(R.id.edit_left_numerator), getEditText(R.id.edit_left_denominator));
        Fraction right = buildFraction(
                getEditText(R.id.edit_right_numerator), getEditText(R.id.edit_right_denominator));
        if (left.denominator == 0 || right.denominator == 0) {
            if (left.denominator == 0) {
                makeBold(R.id.edit_left_denominator);
            }
            if (right.denominator == 0) {
                makeBold(R.id.edit_right_denominator);
            }
            setAlertMessage(getResources().getString(R.string.cannot_divide_by_zero));
            return;
        }

        String resultMsg = "";
        int commonDenominator = LCM(left.denominator, right.denominator);

        resultMsg += printFraction(left) + "+" + printFraction(right);
        resultMsg += "=";
        int leftFactor = commonDenominator / left.denominator;
        int rightFactor = commonDenominator / right.denominator;
        if (leftFactor != 1 || rightFactor != 1) {
            resultMsg += printFactor(left, leftFactor) + "+" +
                    printFactor(right, rightFactor);
            left.numerator *= leftFactor;
            right.numerator *= rightFactor;
            left.denominator = commonDenominator;
            right.denominator = commonDenominator;
            resultMsg += "=";
            resultMsg += printFraction(left) + "+" + printFraction(right);
            resultMsg += " = ";
        }

        resultMsg += printFraction(left.numerator + " + " + right.numerator, commonDenominator + "");
        Fraction result = new Fraction(left.numerator + right.numerator, commonDenominator);
        resultMsg += "=";
        resultMsg += printFraction(result);
        int gcd = GCD(result.denominator, result.numerator);
        if (gcd > 1 && result.numerator != 0) {
            resultMsg += " = " + printFraction(result.numerator + "\\div " + getColored(gcd),
                    result.denominator + "\\div " + getColored(gcd));
            result.numerator /= gcd;
            result.denominator /= gcd;
            resultMsg += " = " + printFraction(result);
        }

        if (result.numerator == 0) {
            resultMsg += " = 0";
        } else {
            if (result.denominator <= result.numerator) {
                int big = result.numerator / result.denominator;
                resultMsg += " = ";
                if (result.denominator != 1) {
                    resultMsg += printFraction(
                            big + "\\cdot " + getColored(result.denominator + "") + " + "
                                    + (result.numerator - result.denominator * big),
                            getColored(result.denominator + ""));
                    resultMsg += " = ";
                }
                resultMsg += big;
                result.numerator -= result.denominator * big;
                if (result.numerator != 0) {
                    resultMsg += printFraction(result);
                }
            }
        }
        showResultInMathView(resultMsg);
    }

    private void showResultInMathView(String msg) {
        ((MathView)findViewById(R.id.text_result_mathview)).setEngine(MathView.Engine.MATHJAX);
        ((MathView)findViewById(R.id.text_result_mathview)).config(
                "MathJax.Hub.Config({\n"+
                        "  CommonHTML: { linebreaks: { automatic: true } },\n"+
                        "  \"HTML-CSS\": { linebreaks: { automatic: true } },\n"+
                        "         SVG: { linebreaks: { automatic: true } }\n"+
                        "});");;
        ((MathView)findViewById(R.id.text_result_mathview)).setText("\\begin{equation}" + msg + "\\end{equation}");

    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void cleanUIState() {
        TextView textViewResult = findViewById(R.id.text_result);
        textViewResult.setTextColor(defaultTextViewColors);
        textViewResult.setVisibility(View.INVISIBLE);
        getEditTextControl(R.id.edit_left_numerator).setTextColor(defaultEditTextColors);
        getEditTextControl(R.id.edit_left_denominator).setTextColor(defaultEditTextColors);
        getEditTextControl(R.id.edit_right_numerator).setTextColor(defaultEditTextColors);
        getEditTextControl(R.id.edit_right_denominator).setTextColor(defaultEditTextColors);
    }

    private static String printFactor(Fraction fraction, int factor) {
        return factor == 1
            ? printFraction(fraction)
            : printFraction(
                fraction.numerator + " \\cdot " + getColored(factor),
                fraction.denominator + " \\cdot " + getColored(factor)
        );
    }

    private static String getColored(String s) {
        return "\\color{green}{" + s + "}";
    }


    private static String getColored(int s) {
        return getColored(s + "");
    }

    private static String printFraction(String left, String right) {
        return "\\frac{" + left + "}{" + right + "}";
    }

    private static String printFraction(int left, int right) {
        return printFraction(Integer.toString(left), Integer.toString(right));
    }

    private static String printFraction(Fraction f) {
        return printFraction(f.numerator, f.denominator);
    }


    private Fraction buildFraction(String nominator, String denominator) {
        return new Fraction(Integer.parseInt(nominator), Integer.parseInt(denominator));
    }
}