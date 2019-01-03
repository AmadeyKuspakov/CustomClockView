package study.amadey.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity implements Switch.OnCheckedChangeListener {

    private ClockView clockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clockView = findViewById(R.id.clockViewAttempt);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        clockView.setLineOrArc(b);
    }
}
