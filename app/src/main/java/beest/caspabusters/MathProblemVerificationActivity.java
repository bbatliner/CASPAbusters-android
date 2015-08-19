package beest.caspabusters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.ResponseCallback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static beest.caspabusters.AndroidUtil.makeToast;

public class MathProblemVerificationActivity extends Activity {

    private static Random random = new Random();

    private ObjectId requestId;
    private int correctResponse;

    @Bind(R.id.math_number1)
    TextView number1tv;
    @Bind(R.id.math_number2)
    TextView number2tv;
    @Bind(R.id.math_answer)
    EditText answer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math_problem_verification);
        ButterKnife.bind(this);

        requestId = new ObjectId(getIntent().getStringExtra(getString(R.string.EXTRA_REQUEST_ID)));

        // integers in the inclusive range 1-9
        int number1 = 1 + random.nextInt(9);
        int number2 = 1 + random.nextInt(9);
        correctResponse = number1 + number2;

        number1tv.setText(Integer.toString(number1));
        number2tv.setText(Integer.toString(number2));
    }

    @OnClick(R.id.math_submit)
    void submitMathProblem() {
        int response = Integer.parseInt(answer.getText().toString());
        // If the user answered correctly
        if (response == correctResponse) {
            CaspaBustersAPI.verifyMathProblem(requestId, new ResponseCallback() {
                @Override
                public void success(Response response) {
                    // TODO: Work on snowshoe verification
                    makeToast(getApplicationContext(), "Success! Thanks for using CASPAbusters!", Toast.LENGTH_LONG);
                    // Go back to menu
                    Intent intent = new Intent(MathProblemVerificationActivity.this, MenuActivity.class);
                    startActivity(intent);
                }

                @Override
                public void failure(RetrofitError error) {
                    // TODO: Handle the failure
                }
            });
        }
        // If the user answered incorrectly
        else {
            makeToast(getApplicationContext(), "Incorrect answer!", Toast.LENGTH_LONG);
            answer.setText("");
        }
    }
}
