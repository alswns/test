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
    Button btn;
     TextView lv,emotion;
    String all_input="";
    EditText et,get;
    Komoran komoran=new Komoran(DEFAULT_MODEL.FULL);
    final List<String> they=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lv=findViewById(R.id.lv);
        btn=findViewById(R.id.btn);
        et=findViewById(R.id.et);
        get=findViewById(R.id.get);
        emotion=findViewById(R.id.emotion);
        lv.setMovementMethod(new ScrollingMovementMethod());
        System.out.println("시작");
        set_they();
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text=et.getText().toString();
                all_input=all_input+text;
                a=emotion_predict(all_input);
                emotion.setText("기분"+a);
                new Thread() {
                    public void run() {
                        String text=et.getText().toString();
                        System.out.println(text);
                        et.setText("");
                        lv.append("민준:"+text+"\n");
                        String output=post(text);
                        lv.append("챗봇:"+output+"\n"+"\n");


                    }
                }.start();
            }
        });
    }
//전역으로 final List<String> they=new ArrayList<>(); 선언후
    //시작할때 필수로 실행

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

    //text를 입력하여 그 입력에 맞는 감정 화남 기쁨 슬픔을 반환
    private String emotion_predict(String text){
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

    // 모델 파일 인터프리터를 생성하는 공통 함수
    // loadModelFile 함수에 예외가 포함되어 있기 때문에 반드시 try, catch 블록이 필요하다.
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수로, 텐서플로 라이트 홈페이지에 있다.
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    //레트로핏 이용(미완성)애매함
    public String get_output(String text){

        final String asd;
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
                        lv.append("챗봇:"+output);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    get.setText("오류");

                } else {
                    System.out.println(response.errorBody());
                    System.out.println(response.raw());
                    System.out.println(response.headers());
                    System.out.println("실패");
                    get.setText("오류");
                }
            }
            @Override
            public void onFailure(Call<art> call, Throwable t) {
                System.out.println(t.fillInStackTrace());
                System.out.println(t.toString());
                System.out.println("실패..");
                get.setText("오류");
            }

        }
        );
        System.out.println("123"+get.getText().toString());
        String send=et.getText().toString();
        return send;

        //return re;
    }



    //챗본 반응 가져온후 반환
    //httpurlconnection 이용 완성
    public String post( String text){
        try {
            URL url = new URL("https://api.dialogflow.com/v1/query?v=20150910");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setConnectTimeout(5000); //서버에 연결되는 Timeout 시간 설정
            con.setReadTimeout(5000); // InputStream 읽어 오는 Timeout 시간 설정
            con.addRequestProperty("Authorization","Bearer 957ea642f45647e98a071eaacd6b73bf"); //key값 설정

            con.setRequestMethod("POST");

            //json으로 message를 전달하고자 할 때
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoInput(true);
            con.setDoOutput(true); //POST 데이터를 OutputStream으로 넘겨 주겠다는 설정
            con.setUseCaches(false);
            con.setDefaultUseCaches(false);

            OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
            text=text.replaceAll(" ","");
            String parameters="{lang:en,sessionId:12345,query:"+text+"}";
            System.out.println(parameters);
            wr.write(parameters);
            wr.flush();
            wr.close();

            StringBuilder sb = new StringBuilder();
            if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                //Stream을 처리해줘야 하는 귀찮음이 있음.
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream(), "utf-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                br.close();
                JSONObject js = new JSONObject(sb.toString());
                JSONObject ajs=new JSONObject(js.get("result").toString());
                JSONObject a=new JSONObject(ajs.get("fulfillment").toString());
                System.out.println(""+a.get("speech").toString());
                String answer=a.get("speech").toString();
                return answer;
            } else {
                System.out.println(con.getResponseMessage());
                return "통신 실패";
            }
        } catch (Exception e){
            System.err.println(e.toString());
            return "오류";

        }
    }

}
