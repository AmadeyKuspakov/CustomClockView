package study.amadey.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity implements Switch.OnCheckedChangeListener {

    private ClockView clockView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Switch switch1 = findViewById(R.id.switch1);
        switch1.setOnCheckedChangeListener(this);
        clockView = findViewById(R.id.clockView);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        clockView.setLineOrArc(b);
    }
}
