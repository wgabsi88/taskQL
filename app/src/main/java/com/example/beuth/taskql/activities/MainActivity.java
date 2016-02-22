package com.example.beuth.taskql.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import com.example.beuth.tasql.R;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.UUID;

/**
 * Prototype activity for editing an text with multiple client over web socket communication
 * @author Wael Gabsi, Stefan Völkel
 */
public class MainActivity extends Activity {

    private EditText editText;
    private WebSocketClient mWebSocketClient;
    private boolean textChanged = true;
    private Handler handler;
    private int color;
    private String lockId, idex, uuid, nanomeSessionId, subProjectText;
    String[] params;
    private final static String CONTENT_TYPE_JSON = "application/json";
    private final static String NANOME_SESSIONID = "nanomeSessionId";
    private static final String ADD = "add";
    private static final String REPLACE = "replace";
    private static final String DELETE = "delete";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        editText = (EditText) findViewById(R.id.task_textfield);
        uuid = UUID.randomUUID().toString();
        color = generateRandomColor();
        editText.setTextColor(color);
        editText.setText(subProjectText);
        handler = new Handler();
        connectToWebSocket();

        // Add event listener
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (textChanged) {
                    mWebSocketClient.send(getTextChanges(start, before, count, color).toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void connectToWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://10.0.3.2:8080");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");
            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            textChanged = false;
                            JSONObject resultJson = new JSONObject(message);
                            String userId = resultJson.getString("uuid");
                            if (!userId.equals(uuid)) {
                                String editMethod = resultJson.getString("editMethod");
                                int startPos = resultJson.getInt("startPos");
                                int endPos = resultJson.getInt("endPos");
                                int textColor = convertHexColorToIntColor(resultJson.getString("textColor"));
                                String resultText = resultJson.getString("text");
                                Spannable span = new SpannableString(resultText);
                                span.setSpan(new ForegroundColorSpan(textColor), 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                if (editMethod.equals(ADD)) {
                                    editText.getText().insert(startPos, span);
                                } else if (editMethod.equals(REPLACE)) {
                                    int endPosBefore = resultJson.getInt("endPosBefore");
                                    editText.getText().replace(startPos, endPosBefore, span);
                                } else if (editMethod.equals(DELETE)) {
                                    editText.getText().replace(startPos, endPos, "");
                                }
                            }
                            textChanged = true;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    /**
     * Generates random integer color value
     * @return
     */
    private int generateRandomColor() {
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        return color;
    }

    /**
     * Convert integer color value to hexadecimal color value
     * @param intColor
     * @return
     */
    private String convertIntColorToHexColor(int intColor) {
        String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
        return hexColor;
    }

    /**
     * Convert hexadecimal color value to integer color value
     * @param hexColor
     * @return
     */
    private int convertHexColorToIntColor(String hexColor) {
        int intColor = Color.parseColor(hexColor);
        return intColor;
    }

    /**
     * Return edit text changes as json object
     * @param start
     * @param before
     * @param count
     * @param color
     * @return
     */
    private JSONObject getTextChanges(int start, int before, int count, int color) {
        int startPosition = start;
        int endPosition = 0;
        int endPositionBefore = 0;
        String text = "";
        String editMethod = "";
        JSONObject json = new JSONObject();
        String hexColorValue = convertIntColorToHexColor(color);
        if ((before == 0) && (count >= 1)) {
            editMethod = ADD;
            endPosition = startPosition + count;
            text = editText.getText().toString().substring(startPosition, endPosition);
        } else if ((count > 0) && (before > 0)) {
            endPosition = startPosition + count;
            endPositionBefore = startPosition + before;
            text = editText.getText().toString().substring(startPosition, endPosition);
            editMethod = REPLACE;
        } else {
            endPosition = startPosition + before;
            editMethod = DELETE;
        }

        try {
            json.put("uuid", uuid);
            json.put("editMethod", editMethod);
            json.put("startPos", startPosition);
            json.put("endPos", endPosition);
            json.put("endPosBefore", endPositionBefore);
            json.put("textColor", hexColorValue);
            json.put("text", text);
        } catch (JSONException e) {
            Log.i("JSON Exception", e.getStackTrace().toString());
        }
        return json;
    }
}
