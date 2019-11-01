package com.example.test_api;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    String a;
    String chatbot;
    Button btn;
     TextView lv;
    EditText et;
    Komoran komoran=new Komoran(DEFAULT_MODEL.FULL);
    final List<String> they=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=findViewById(R.id.lv);
        btn=findViewById(R.id.btn);
        et=findViewById(R.id.et);
        lv.setMovementMethod(new ScrollingMovementMethod());
        System.out.println("시작");
        set_they();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=et.getText().toString();
                a=run(text);

                new Thread() {
                    public void run() {
                        String text=et.getText().toString();
                        System.out.println(text);
                        et.setText("");
                        lv.append("민준:"+text+"\n");
                        String output=get_output(text);
                        lv.append("기분:"+a+"\n");
                        //lv.append("챗봇:"+output+"\n"+"\n");
                        //lv.add

                    }
                }.start();
            }
        });
    }

    private void set_they(){

        BufferedReader bReader=null;
        InputStream is = getBaseContext().getResources().openRawResource(R.raw.data);
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            while(br.ready()) {
                they.add(br.readLine());
            }

            for(String str : they)
                System.out.println(str);
        } catch (IOException e) {
            System.out.println("에러");
        }
    }

    private String run(String text){
        float inputs[][]=new float[1][147];
        for(int i=0;i<147;i++){
            inputs[0][i]=0;
        }

        KomoranResult result=komoran.analyze(text);
        List<Token> tokenList=new ArrayList<>();
        try {
            tokenList=result.getTokenList();
        }catch(Exception e) {
            return "에러";

        }

        for(Token token:tokenList){
            if(token.getPos().equals("EC" )||token.getPos().equals("ETM")){
                System.out.println("별로");
            }
            else {
                System.out.println(token.getMorph());
                for(int i=0;i<147;i++){
                    if(they.get(i).equals(token.getMorph()))
                        inputs[0][i]=1f;
                }
            }

        }
        for(int i=0;i<147;i++){
            System.out.println(inputs[0][i]);
        }

        float[][] output=new float[1][3];
        Interpreter tflite = getTfliteInterpreter("model.tflite");
        tflite.run(inputs,output);

        int big=0;
        for(int i=0;i<3;i++){
            System.out.println(output[0][i]);
            if(output[0][big]<output[0][i]) {

                big = i;
            }
        }
        if(big==0){text="화남";}
        else if(big==1){ text="슬픔";}
        else{text="기쁨";}

        return text;

    }


    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public String get_output(String text){
        this.chatbot="오류";
        String asdf="";
        NetworkHelper.getInstence().get_Weather_retrofit("20150910",text,"ko","민준").enqueue(new Callback<art>() {
            @Override
            public void onResponse(Call<art> call, Response<art> response) {
                if (response.isSuccessful()) {
                    System.out.println("성공");
                    System.out.println(response.body().result);

                    try {
                        JSONObject as=new JSONObject(response.body().result.get("fulfillment").toString());
                        String output=as.get("speech").toString();
                        System.out.println(output);
                        lv.append("챗봇:"+output+"\n"+"\n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    System.out.println(response.errorBody());
                    System.out.println(response.raw());
                    System.out.println(response.headers());
                    System.out.println("실패");
                    lv.append("챗봇:"+"실패"+"\n"+"\n");
                }
            }
            @Override
            public void onFailure(Call<art> call, Throwable t) {
                System.out.println(t.fillInStackTrace());
                System.out.println(t.toString());
                System.out.println("실패..");
                lv.append("챗봇:"+"실패"+"\n"+"\n");
            }
        });
        System.out.println(chatbot);
        return asdf;
    }









}
