package org.aviran.woodpeckerapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.aviran.woodpecker.Woodpecker;
import org.aviran.woodpecker.WoodpeckerError;
import org.aviran.woodpecker.WoodpeckerResponse;
import org.aviran.woodpecker.WoodpeckerSettings;
import org.aviran.woodpeckerapp.model.ItemRequest;
import org.aviran.woodpeckerapp.model.ItemResponse;
import org.aviran.woodpeckerapp.model.ListRequest;
import org.aviran.woodpeckerapp.model.LoginRequest;
import org.aviran.woodpeckerapp.model.LoginResponse;
import org.aviran.woodpeckerapp.model.ReviewRequest;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        findViewById(R.id.runButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("");
                runDemo();
            }
        });

        // Initialize woodpecker with a base url
        Woodpecker.initialize(new WoodpeckerSettings("http://woodpecker.aviran.org"));
    }

    private void runDemo() {
        // POST  login           /login?username=user&password=password
        // GET   list            /list?page=1&pageSize=10
        // GET   item            /item/{id}
        // POST  review          { itemId: id, name: Aviran, review: This is awesome }
        Woodpecker
                .begin()
                .request(new LoginRequest("aviran", "12345"))
                .then(new WoodpeckerResponse<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse response) {
                        Woodpecker.getSettings().addHeader("token", response.getToken());
                        printLog("Login successful, token: " + response.getToken());
                    }
                })
                .request(new ListRequest(1, 10))
                .then(new WoodpeckerResponse<List<ItemResponse>>() {
                    @Override
                    public void onSuccess(List<ItemResponse> response) {
                        ItemRequest itemRequest = (ItemRequest) getNextRequest();
                        itemRequest.setId(response.get(0).getId());

                        printLog("List request successful, second element is: " + response.get(1).getName());
                    }
                })
                .request(new ItemRequest(-1))
                .then(new WoodpeckerResponse<ItemResponse>() {
                    @Override
                    public void onSuccess(ItemResponse response) {
                        printLog("Item request successful, element is: "
                                + response.getName() + " "
                                + Arrays.toString(response.getValues()));
                    }
                })
                .request(new ReviewRequest(1, "Aviran", "This is awesome!"))
                .then(new WoodpeckerResponse<String>() {
                    @Override
                    public void onSuccess(String response) {
                        printLog("Review request successful, response is:\n" + response);
                    }
                })
                .error(new WoodpeckerError() {
                    @Override
                    public void onError(WoodpeckerResponse response) {
                        Log.e("WP", "ERROR");
                    }
                });
    }

    private void printLog(String log) {
        textView.setText(textView.getText().toString() + log + "\n\n");
    }
}
