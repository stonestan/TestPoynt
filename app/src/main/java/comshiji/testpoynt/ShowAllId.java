package comshiji.testpoynt;

import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author Stones.Tan
 * @date 10:48
 */
public class ShowAllId extends Activity {
    private ListView listView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showall_actvity);

        findViewById(R.id.back_tv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        listView = findViewById(R.id.listView);
        final List<String> names = PaymentUtil.getPaymentUtil().getKey();
        ArrayAdapter<String> adapter= new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = ShowAllId.this.getIntent();
                intent.putExtra("select",names.get(position));
                setResult(Activity.RESULT_OK, intent);
                finish();
//                cm.setText(names.get(position));
//                Toast.makeText(ShowAllId.this,"Has been copied",Toast.LENGTH_SHORT).show();
            }
        });
    }
}
