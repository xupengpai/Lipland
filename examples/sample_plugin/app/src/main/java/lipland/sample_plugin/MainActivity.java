package lipland.sample_plugin;

import android.os.Bundle;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Sample Plugin");
        ((TextView)findViewById(R.id.textView)).setText("packageName:"+getPackageName());
    }
}
