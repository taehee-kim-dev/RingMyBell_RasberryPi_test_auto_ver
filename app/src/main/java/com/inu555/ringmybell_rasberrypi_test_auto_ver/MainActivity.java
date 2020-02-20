package com.inu555.ringmybell_rasberrypi_test_auto_ver;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

/*
    안드로이드의 인터넷 접근 권한 허용을 위해
    manifests/AndroidManifest.xml에
    <uses-permission android:name="android.permission.INTERNET" /> 를 추가하였다.
 */

public class MainActivity extends AppCompatActivity {

    // 포트번호
    private final int port = 7777;
    // IP주소
    //private final String ip = "13.125.8.121";
    private final String ip = "10.0.2.2";

    // Socket 객체 참조 변수
    private Socket socket;

    // 데이터를 송수신할 스트림버퍼 Reader, Writer 참조변수 선언
    private BufferedReader br;
    private BufferedWriter bw;

    private boolean isDataReady = false;

    // 데이터를 저장할 스트링 참조변수
    private String receivedDataString;
    private String dataJsonStrToSend;
    private String dataIndexStr;

    // 버튼들 참조변수
    private Button button_registerBus;
    private Button button_autoStart;
    private Button button_autoPause;
    private Button button_goToNextManually;
    private Button button_reset;


    // 버튼 클릭 여부를 체크할 boolean 변수
    private boolean is_button_autoStart_Clicked = false;
    private boolean is_button_autoPause_Clicked = true;
    private boolean is_button_goToNextManually_Clicked = false;
    private boolean is_button_reset_Clicked = false;

    private boolean busRegistered = false;

    // TextView들 참조변수
    private TextView textView_currentStopName;
    private TextView textView_currentStopIdentifier;
    private TextView textView_ringBell;
    private TextView textView_dataIndex;

    // Handler 참조변수 선언
    Handler mHandler = new Handler();

    ArrayList<RasberryPi> rasberryPiArrayList = new ArrayList<>();

    ArrayList<ToPrintLocation> toPrintLocationArrayList = new ArrayList<>();

    // String을 key값으로,
    // Object를 value값으로 담을 HashMap 생성
    // 이 데이터는 어플에서 서버로 전송할 총 데이터 묶음임.
    HashMap<String, Object> model = new HashMap<String, Object>();

    // Gson형 객체 생성
    Gson gson = new Gson();

    private String currentStopName;
    private String currentStopIdentifier;


    // 어플이 처음 켜질 때
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.setProperty("file.encoding","UTF-8");

        Field charset = null;
        try {
            charset = Charset.class.getDeclaredField("defaultCharset");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        charset.setAccessible(true);

        try {
            charset.set(null,null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        // 위젯 참조 변수에 대한 할당
        button_registerBus = (Button) findViewById(R.id.button_registerBus);
        button_autoStart = (Button) findViewById(R.id.button_autoStart);
        button_autoPause = (Button) findViewById(R.id.button_autoPause);
        button_goToNextManually = (Button) findViewById(R.id.button_goToNextManually);
        button_reset = (Button) findViewById(R.id.button_reset);

        textView_currentStopName = (TextView) findViewById(R.id.textView_currentStopName);
        textView_currentStopIdentifier = (TextView) findViewById(R.id.textView_currentStopIdentifier);
        textView_ringBell = (TextView) findViewById(R.id.textView_ringBell);
        textView_dataIndex = (TextView) findViewById(R.id.textView_dataIndex);

        // 웹소켓 데이터 수신 스레드 시작
        new Thread(new receivingData()).start();
        new Thread(new sendData1()).start();
        new Thread(new sendData2()).start();

        /*
            위에서 설정한 총 데이터 HaspMap 참조변수는 model 이다.
            클라이언트와 서버간에 데이터 전송 및 수신을 할 때,
            보통 데이터 표현 방식으로 JSON을 사용한다.
            데이터를 JSON 표현 방식으로 String화 하여
            송신 및 수신한다.

            클라이언트 : 객체 데이터 -> JSON 표현방식의 문자열 -> 전송 -> 서버
            서버 :  클라이언트 -> 수신 -> JSON 표현방식의 문자열 -> 객체 데이터

            따라서
            이 HashMap 객체 model을 JSON형식 String으로 변환한다.
            이를 위해 Gson을 사용한다.
            Gson은 Google에서 만든 JSON 활용 라이브러리이다. 매우 편리하다.
            이 라이브러리 사용을 위해 Gradle Scripts/build.gradle(Module: app)에
            implementation 'com.google.code.gson:gson:2.8.5' 로 의존성 추가를 해 주었다.
            HashMap형 객체 데이터를 JSON형식의 문자열로 간편하게 변환해준다.
            물론 반대로 변환도 가능하다.
        */

        rasberryPiArrayList.add(new RasberryPi(37.372406, 126.634462)); // OutFromSongdo
        rasberryPiArrayList.add(new RasberryPi(37.373359, 126.635420));
        rasberryPiArrayList.add(new RasberryPi(37.374428, 126.636379));
        rasberryPiArrayList.add(new RasberryPi(37.376857, 126.637040));
        rasberryPiArrayList.add(new RasberryPi(37.37845, 126.63443));
        rasberryPiArrayList.add(new RasberryPi(37.379675, 126.632919));
        rasberryPiArrayList.add(new RasberryPi(37.381205, 126.634608));
        rasberryPiArrayList.add(new RasberryPi(37.381597, 126.63508));
        rasberryPiArrayList.add(new RasberryPi(37.383198, 126.636819));
        rasberryPiArrayList.add(new RasberryPi(37.385278, 126.639129));
        rasberryPiArrayList.add(new RasberryPi(37.385753, 126.639602));
        rasberryPiArrayList.add(new RasberryPi(37.386269, 126.640197));
        rasberryPiArrayList.add(new RasberryPi(37.387213, 126.641194));
        rasberryPiArrayList.add(new RasberryPi(37.388425, 126.642514));
        rasberryPiArrayList.add(new RasberryPi(37.389053, 126.643156));
        rasberryPiArrayList.add(new RasberryPi(37.389656, 126.643818));
        rasberryPiArrayList.add(new RasberryPi(37.391446, 126.645687));
        rasberryPiArrayList.add(new RasberryPi(37.392215, 126.646542));
        rasberryPiArrayList.add(new RasberryPi(37.392636, 126.647179));
        rasberryPiArrayList.add(new RasberryPi(37.392296, 126.649317));
        rasberryPiArrayList.add(new RasberryPi(37.392907, 126.650033));
        rasberryPiArrayList.add(new RasberryPi(37.393081, 126.65025));
        rasberryPiArrayList.add(new RasberryPi(37.393524, 126.650656));
        rasberryPiArrayList.add(new RasberryPi(37.393925, 126.651213));
        rasberryPiArrayList.add(new RasberryPi(37.395039, 126.652367));
        rasberryPiArrayList.add(new RasberryPi(37.394679, 126.653037));
        rasberryPiArrayList.add(new RasberryPi(37.393656, 126.654505));
        rasberryPiArrayList.add(new RasberryPi(37.392651, 126.656173));
        rasberryPiArrayList.add(new RasberryPi(37.391766, 126.657631));
        rasberryPiArrayList.add(new RasberryPi(37.391094, 126.658685));
        rasberryPiArrayList.add(new RasberryPi(37.390255, 126.660096));
        rasberryPiArrayList.add(new RasberryPi(37.389742, 126.660889));
        rasberryPiArrayList.add(new RasberryPi(37.389124, 126.661930));
        rasberryPiArrayList.add(new RasberryPi(37.390029, 126.661028)); // InToSongdo
        rasberryPiArrayList.add(new RasberryPi(37.390619, 126.660080));
        rasberryPiArrayList.add(new RasberryPi(37.39136, 126.658853));
        rasberryPiArrayList.add(new RasberryPi(37.392062, 126.657721));
        rasberryPiArrayList.add(new RasberryPi(37.392645, 126.656776));
        rasberryPiArrayList.add(new RasberryPi(37.393545, 126.655290));
        rasberryPiArrayList.add(new RasberryPi(37.394627, 126.653622));
        rasberryPiArrayList.add(new RasberryPi(37.395205, 126.652231));
        rasberryPiArrayList.add(new RasberryPi(37.394057, 126.651104));
        rasberryPiArrayList.add(new RasberryPi(37.393610, 126.650535));
        rasberryPiArrayList.add(new RasberryPi(37.393036, 126.649946));
        rasberryPiArrayList.add(new RasberryPi(37.393251, 126.647039));
        rasberryPiArrayList.add(new RasberryPi(37.392625, 126.646311));
        rasberryPiArrayList.add(new RasberryPi(37.391037, 126.644626));
        rasberryPiArrayList.add(new RasberryPi(37.389706, 126.643334));
        rasberryPiArrayList.add(new RasberryPi(37.388703, 126.642149));
        rasberryPiArrayList.add(new RasberryPi(37.387721, 126.641151));
        rasberryPiArrayList.add(new RasberryPi(37.386874, 126.64009));
        rasberryPiArrayList.add(new RasberryPi(37.386087, 126.639353));
        rasberryPiArrayList.add(new RasberryPi(37.385522, 126.638719));
        rasberryPiArrayList.add(new RasberryPi(37.383834, 126.636882));
        rasberryPiArrayList.add(new RasberryPi(37.381458, 126.634269));
        rasberryPiArrayList.add(new RasberryPi(37.379110, 126.632617));
        rasberryPiArrayList.add(new RasberryPi(37.377993, 126.634448));
        rasberryPiArrayList.add(new RasberryPi(37.376269, 126.637321));
        rasberryPiArrayList.add(new RasberryPi(37.374979, 126.63654));
        rasberryPiArrayList.add(new RasberryPi(37.374509, 126.636293));
        rasberryPiArrayList.add(new RasberryPi(37.373899, 126.635500));
        rasberryPiArrayList.add(new RasberryPi(37.373136, 126.634746));
        rasberryPiArrayList.add(new RasberryPi(37.372128, 126.633786));


        toPrintLocationArrayList.add(new ToPrintLocation("인천대학교공과대학", "38-377")); // OutFromSongdo
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대학교자연과학대학", "38-378"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대정문", "38-385"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도 더샵마스터뷰23단지", "38-441"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대입구역", "38-396"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("투모로우시티", "38-390"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("롯데마트송도점", "38-523"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("포스코타워앤쉐라톤호텔", "38-491"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도더샵퍼스트월드B동.C동", "38-425"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("해양경찰청", "38-015"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도풍림아이원2단지\n(송도풍림아이원2단지 방면)", "38-026"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도풍림아이원2단지\n(금호아파트 방면)", "38-034"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("금호아파트", "38-349"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("신송고등학교", "38-023"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("한진아파트", "38-016"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("성지아파트", "38-017")); // InToSongdo
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("신송고등학교", "38-024"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도풍림아이원4단지", "38-032"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도풍림아이원1단지", "38-033"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도풍림아이원2단지", "38-025"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("해양경찰청", "38-014"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도푸르지오월드마크", "38-426"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("센트럴공원", "38-490"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("투모로우시티", "38-394"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대입구역", "38-395"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("송도 더샵마스터뷰23단지", "38-442"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대정문", "38-386"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대학교자연과학대학", "38-375"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));
        toPrintLocationArrayList.add(new ToPrintLocation("인천대학교공과대학", "38-376"));
        toPrintLocationArrayList.add(new ToPrintLocation("이동중", "-"));

        // 버튼에 클릭 리스너 설정
        button_registerBus.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("registerBus버튼 눌림");
                model.put("registerBus", new RasberryPi(1.1, 2.2));
                dataJsonStrToSend = gson.toJson(model);
                isDataReady = true;
                busRegistered = true;
            }
        });

        // 버튼에 클릭 리스너 설정
        button_autoStart.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("자동 운행 시작 버튼 눌림");

                if(busRegistered == false){
                    System.out.println("Register 버스를 먼저 해야 함.");
                    return;
                }

                // 버튼 클릭 시, 상태 true로 변경
                is_button_autoStart_Clicked = true;
                is_button_autoPause_Clicked = false;
            }
        });

        button_autoPause.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("자동 운행 일시정지 버튼 눌림");

                if(busRegistered == false){
                    System.out.println("Register 버스를 먼저 해야 함.");
                    return;
                }


                // 버튼 클릭 시, 상태 true로 변경
                is_button_autoPause_Clicked = true;
                is_button_autoStart_Clicked = false;
            }
        });

        button_goToNextManually.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("수동 운행 버튼 눌림");

                if(busRegistered == false){
                    System.out.println("Register 버스를 먼저 해야 함.");
                    return;
                }

                if(is_button_autoStart_Clicked == true && is_button_autoPause_Clicked == false) {
                    System.out.println("일시정지를 먼저 해야 함.");
                    return;
                }

                // 버튼 클릭 시, 상태 true로 변경
                is_button_goToNextManually_Clicked = true;
            }
        });

        button_reset.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("초기화 버튼 눌림");

                if(busRegistered == false){
                    System.out.println("Register 버스를 먼저 해야 함.");
                    return;
                }

                if(is_button_autoStart_Clicked == true && is_button_autoPause_Clicked == false) {
                    System.out.println("일시정지를 먼저 해야 함.");
                    return;
                }

                    // 버튼 클릭 시, 상태 true로 변경
                is_button_reset_Clicked = true;
            }
        });
    }

    // 어플이 종료될 때
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            // 버퍼 Reader, Writer 닫기
            bw.close();
            br.close();
            // 소켓 닫기
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // 안드로이드에서 네트워킹은 무조건 별도 스레드를 사용하여야 함
    // 메인스레드는 UI작업만 하여야 함
    // 메인스레드에서 네트워킹 작업시 에러발생함
    // 소켓 데이터 송신을 위한 별도 스레드를 위한
    // Runnable sendData클래스 구현
    // 액티비티의 송신 버튼 클릭 여부를
    // 무한루프로 체크하면서
    // 사용자가 버튼을 클릭할 시,
    // 서버에 위에서 준비한 총 데이터 model을
    // 버퍼스트림으로 전송함
    private class sendData1 implements Runnable{
        public void run() {
            System.out.println("새로운 sendData1 스레드 실행");
            try {
                while(true){
                    // 만약 버튼이 클릭되지 않았으면
                    // continue
                    if(isDataReady == false){
                        continue;
                    }else{
                        // 버튼이 클릭되었으면
                        // JSON형태의 문자열 데이터를
                        // 버퍼스트림을 통해 서버로 보낸다
                        bw.write(dataJsonStrToSend);
                        // 수신측 BufferdReader의 readLine()함수를 통해 문자열 데이터를 수신하려면
                        // 송신측의 inputStream에는  반드시 개행문자가 포함되어야 한다.
                        // 자바에서의 개행문자는 "\n" 이지만,
                        // 스트림에서의 개행문자는 "\r\n"이 개행문자이다.
                        // 따라서, 보내는쪽 스트림의 의 데이터 뒤에 "\r\n"을 반드시 붙여야한다.
                        // BufferedWriter에서 이 역할을
                        // newLine()함수가 수행한다.
                        bw.newLine();
                        //남아있는 데이터를 모두 출력시킴
                        bw.flush();
                        System.out.println("전송한 데이터 : " + dataJsonStrToSend);
                        System.out.println("데이터 전송 완료");
                        // 데이터 전송 완료 후
                        // 버튼 클릭 상태 false로 변경
                        isDataReady = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class sendData2 implements Runnable{
        public void run() {
            String data;
            int i = 0;
            System.out.println("새로운 sendData2 스레드 실행");
            try {
                while(true){
                    // 만약 버튼이 클릭되지 않았으면
                    // continue
                    if(is_button_autoStart_Clicked == false && is_button_autoPause_Clicked == true){
                        if(is_button_reset_Clicked == true){
                            i = 0;
                            dataIndexStr = "현재 데이터의 인덱스 : " + i;
                            currentStopName = toPrintLocationArrayList.get(i).getStopName();
                            currentStopIdentifier = toPrintLocationArrayList.get(i).getStopIdentifier();
                            model.clear();
                            model.put("busGPSInform", rasberryPiArrayList.get(i));
                            data = gson.toJson(model);
                            bw.write(data);
                            bw.newLine();
                            //남아있는 데이터를 모두 출력시킴
                            bw.flush();
                            System.out.println("전송한 데이터 : " + data);
                            System.out.println("데이터 전송 완료");
                            mHandler.post(updateTextViewCurrentStopName);
                            mHandler.post(updateTextViewCurrentStopIdentifier);
                            mHandler.post(updateTextViewDataIndex);

                            i++;
                            if(i == 63)
                                i = 0;


                            is_button_reset_Clicked = false;
                            continue;
                        }

                        if(is_button_goToNextManually_Clicked){
                            dataIndexStr = "현재 데이터의 인덱스 : " + i;
                            currentStopName = toPrintLocationArrayList.get(i).getStopName();
                            currentStopIdentifier = toPrintLocationArrayList.get(i).getStopIdentifier();
                            model.clear();
                            model.put("busGPSInform", rasberryPiArrayList.get(i));
                            data = gson.toJson(model);
                            bw.write(data);
                            bw.newLine();
                            //남아있는 데이터를 모두 출력시킴
                            bw.flush();
                            System.out.println("전송한 데이터 : " + data);
                            System.out.println("데이터 전송 완료");
                            mHandler.post(updateTextViewCurrentStopName);
                            mHandler.post(updateTextViewCurrentStopIdentifier);
                            mHandler.post(updateTextViewDataIndex);

                            i++;
                            if(i == 63)
                                i = 0;

                            is_button_goToNextManually_Clicked = false;
                        }

                        continue;
                    }else{
                        Thread.sleep(3000);
                        dataIndexStr = "현재 데이터의 인덱스 : " + i;
                        currentStopName = toPrintLocationArrayList.get(i).getStopName();
                        currentStopIdentifier = toPrintLocationArrayList.get(i).getStopIdentifier();
                        model.clear();
                        model.put("busGPSInform", rasberryPiArrayList.get(i));
                        data = gson.toJson(model);
                        bw.write(data);
                        bw.newLine();
                        //남아있는 데이터를 모두 출력시킴
                        bw.flush();
                        System.out.println("전송한 데이터 : " + data);
                        System.out.println("데이터 전송 완료");
                        mHandler.post(updateTextViewCurrentStopName);
                        mHandler.post(updateTextViewCurrentStopIdentifier);
                        mHandler.post(updateTextViewDataIndex);

                        i++;
                        if(i == 63)
                            i = 0;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // 소켓 데이터 수신을 위한 별도 스레드를 위한
    // Runnable receivingData 클래스 구현
    // 수신되는 데이터 문자열을 무한루프로 체크
    private class receivingData implements Runnable{
        public void run() {
            System.out.println("새로운 receivingData 스레드 실행");
            try {
                // IP주소와 port번호로 소켓 객체를 생성하여
                // 소켓 참조변수에 할당
                socket = new Socket(ip, port);
                System.out.println("소켓 객체 생성 및 할당 성공");
                // utf-8형으로 문자열을 읽거나 출력하도록 버퍼스트림 설정 및 할당
                br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
                System.out.println("버퍼스트림 생성 및 할당 성공");

                System.out.println("통신 시작.");
                while (true) {
                    System.out.println("데이터 수신 대기중");
                    receivedDataString = br.readLine();
                    System.out.println("데이터 수신됨");
                    // 로그 출력을 통한 수신 데이터 확인
                    System.out.println(receivedDataString);
                    if(receivedDataString.equals("ringBell")){
                        mHandler.post(updateTextViewRingBell);
                        Thread.sleep(2000);
                        mHandler.post(updateTextViewUnRingBell);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("receivingData 스레드 종료");
        }
    }

    // 익명클래스를 사용한 Runnable 클래스 구현
    // Runnable 객체 생성 및 할당
    // receivingData의 Handler post에서 사용된다.
    private Runnable updateTextViewRingBell = new Runnable() {
        @Override
        public void run() {
            textView_ringBell.setText("버스 벨 울림!!");
            textView_ringBell.setTextColor(Color.parseColor("#FF0000"));
            System.out.println("RingBell 텍스트뷰 업데이트 완료");
        }
    };

    private Runnable updateTextViewUnRingBell = new Runnable() {
        @Override
        public void run() {
            textView_ringBell.setText("버스 벨 울림 여부");
            textView_ringBell.setTextColor(Color.parseColor("#002EFF"));
            System.out.println("RingBell 텍스트뷰 업데이트 완료");
        }
    };

    // 익명클래스를 사용한 Runnable 클래스 구현
    // Runnable 객체 생성 및 할당
    private Runnable updateTextViewCurrentStopName = new Runnable() {
        @Override
        public void run() {
            textView_currentStopName.setText(currentStopName);
            System.out.println("현재 정류장 이름 텍스트뷰 업데이트 완료");
        }
    };

    // 익명클래스를 사용한 Runnable 클래스 구현
    // Runnable 객체 생성 및 할당
    private Runnable updateTextViewCurrentStopIdentifier = new Runnable() {
        @Override
        public void run() {
            textView_currentStopIdentifier.setText(currentStopIdentifier);
            System.out.println("현재 정류장 고유번호 텍스트뷰 업데이트 완료");
        }
    };

    // 익명클래스를 사용한 Runnable 클래스 구현
    // Runnable 객체 생성 및 할당
    private Runnable updateTextViewDataIndex = new Runnable() {
        @Override
        public void run() {
            textView_dataIndex.setText(dataIndexStr);
            System.out.println("현재 데이터 인덱스 텍스트뷰 업데이트 완료");
        }
    };

}
