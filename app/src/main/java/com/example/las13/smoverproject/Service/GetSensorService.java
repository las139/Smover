package com.example.las13.smoverproject.Service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class GetSensorService extends Service {
    private static String TAG = "#GetSensorSevice";
    public String file_path;
    public String local_path;
    public String sDate;
    public String fileName;
    public static Thread thread_data;
    private SendFCM thread_fcm;
    private String no, gas, fire;
    private String wait_flag = "ON";
    private int time = 0;

    @Override
    public IBinder onBind(Intent intent) {
        // Service 객체와 (화면단 Activity 사이에서)
        // 통신(데이터를 주고받을) 때 사용하는 메서드
        // 데이터를 전달할 필요가 없으면 return null;
        return null;
    }

    @Override
    public void onCreate() {
        // 서비스에서 가장 먼저 호출됨(최초에 한번만)
        super.onCreate();
        Log.d(TAG, "Thread== onCreate()");

        thread_data = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Log.d(TAG, "Thread== run!!!");

                        String param = "u_id=" + UserIdLoad();
                        try {
                            URL url = new URL("http://172.16.12.138:8080/GetSmoDataAll.php");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                            conn.setRequestMethod("POST");
                            conn.setDoInput(true);
                            conn.connect();

                            /* 안드로이드 -> 서버 파라미터값 전달 */
                            OutputStream outs = conn.getOutputStream();
                            outs.write(param.getBytes("UTF-8"));
                            outs.flush();
                            outs.close();

                            /* 서버 -> 안드로이드 파라미터값 전달 */
                            InputStream is = null;
                            BufferedReader in = null;
                            String data = "";

                            is = conn.getInputStream();
                            in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                            String line = null;
                            StringBuffer buff = new StringBuffer();
                            while ((line = in.readLine()) != null) {
                                buff.append(line + "\n");
                            }
                            data = buff.toString().trim();

                            // JSON 파싱해서 가스 센서값 판별
                            ParsingJsonData(data);

                            Log.d(TAG,"Thread== TIME -->> "+time);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //흡연 감지 상태가 지난후에만 time 증가
                        if(wait_flag.equals("OFF")) {
                            time++;
                        }
                        //푸시 알림 요청 후 1분 경과해야 푸시알림 받을 수 있는 상태로 변경
                        if(wait_flag.equals("OFF") && time == 20){
                            wait_flag = "ON";
                            time = 0;
                        }
                        sleep(3000);      //3초마다 데이터 갱신
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        thread_data.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
    }

    //Json Data 파싱
    public void ParsingJsonData(String result) {
        Log.d(TAG,"Thread== wait_flag -->> "+wait_flag);
        try {
            JSONArray arr = new JSONArray(result);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                //JSON 객체 가져오기
                no = obj.getString("no");
                gas = obj.getString("gas");
                fire = obj.getString("fire");

                if (Integer.parseInt(gas) >= 1100) {           //일산화탄소 값 1100 이상일 경우
                    if (Integer.parseInt(fire) == 1) {        //불꽃 감지했을 경우
                        //푸시 알림을 받을 수 있는 경우 (1분 대기 상태 지난 후)
                        if(wait_flag.equals("ON")) {
                            //흡연감지된 Smover 번호 저장
                            SmokeNoSave(no);


                            Log.d(TAG, "Thread== [" + no + "] 흡연 감지됨!");

                            //푸시알림 보냄
                            thread_fcm = new SendFCM(no);
                            thread_fcm.start();

                            wait_flag = "OFF";  //푸시 알림 받을 수 없는 상태로 변경
                        }
                    }
                }
            }
        } catch (JSONException o) {
            o.printStackTrace();
        }
    }

    //FCM 푸시알림 요청
    private class SendFCM extends Thread {
        private String no = null;

        public SendFCM(String no) {
            this.no = no;
        }

        @Override
        public void run() {
            Log.d(TAG, "Thread== FCM");
            try {
                String id = UserIdLoad();
                String title = "[흡연 감지]";
                String message = "상황을 자세히 보려면 클릭하세요";
                String param = "id=" + id + "&title=" + title + "&message=" + message;

                Log.d(TAG, "Thread== param: " + param);

                URL url = new URL("http://172.16.12.138:8080/SendFCM.php");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.connect();

                /* 안드로이드 -> 서버 파라미터값 전달 */
                OutputStream outs = conn.getOutputStream();
                outs.write(param.getBytes("UTF-8"));
                outs.flush();
                outs.close();

                /* 서버 -> 안드로이드 파라미터값 전달 */
                InputStream is = null;
                BufferedReader in = null;
                String data = "";

                is = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(is), 8 * 1024);
                String line = null;
                StringBuffer buff = new StringBuffer();
                while ((line = in.readLine()) != null) {
                    buff.append(line + "\n");
                }
                data = buff.toString().trim();

                //URL로부터 흡연 장면 캡쳐해서 다운로드
                new ImgDownThread(this.no).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //URL로부터 흡연 장면 캡쳐해서 다운로드
    private class ImgDownThread extends Thread {
        private final String SAVE_FOLDER = "/smoke";
        private String no = null;

        public ImgDownThread(String no) {
            this.no = no;
        }

        @Override
        public void run() {
            String savePath = Environment.getExternalStorageDirectory().toString() + SAVE_FOLDER;

            File dir = new File(savePath);

            //상위 디렉토리가 존재하지 않을 경우 생성
            if (!dir.exists()) {
                dir.mkdirs();
            }

            //저장할 파일용 날짜 포맷
            Date day = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);

            //DB 저장용 날짜 포맷
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA);
            sDate = String.valueOf(sdf2.format(day));

            fileName = "img_" + String.valueOf(sdf.format(day));

            //웹 서버 쪽 파일이 있는 경로
            String fileUrl = "http://169.254.56.126:8080/stream/snapshot.jpeg?delay_s=0";

            //다운로드 폴더에 동일한 파일명이 존재하는지 확인
            if (new File(savePath + "/" + fileName).exists() == false) {

            } else {

            }

            file_path = savePath + "/" + fileName + ".jpg";                           //휴대폰 저장소에 저장되는 경로
            local_path = savePath + "/" + UserIdLoad() + "/" + fileName + ".jpg";     //서버에 저장되는 경로
            try {
                URL imgUrl = new URL(fileUrl);
                //서버와 접속하는 클라이언트 객체 생성
                HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                int len = conn.getContentLength();

                byte[] tmpByte = new byte[len];

                //입력 스트림을 구한다
                InputStream is = conn.getInputStream();
                File file = new File(file_path);

                //파일 저장 스트림 생성
                FileOutputStream fos = new FileOutputStream(file);
                int read;

                //입력 스트림을 파일로 저장
                for (; ; ) {
                    read = is.read(tmpByte);
                    if (read <= 0) {
                        break;
                    }
                    fos.write(tmpByte, 0, read); //file 생성
                }
                //연결 해제
                is.close();
                fos.close();
                conn.disconnect();

                //다운받은 캡쳐파일 서버에 업로드
                new ImageUpload(this.no).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //캡쳐 다운받은 파일 서버 폴더에 업로드
    private class ImageUpload extends Thread {
        private String no = null;

        public ImageUpload(String no) {
            this.no = no;
        }

        @Override
        public void run() {
            try {
                String id = UserIdLoad();
                String fileName = file_path;
                String type = "smoke";
                String url = "http://172.16.12.138:80/web/SmoverProject/UploadImage.jsp?id=" + id + "&no=" + this.no + "&type=" + type;

                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";

                FileInputStream mFileInputStream = new FileInputStream(fileName);
                URL connectUrl = new URL(url);
                Log.d("Test", "mFileInputStream  is " + mFileInputStream);

                // open connection
                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                // write data
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                int bytesAvailable = mFileInputStream.available();
                int maxBufferSize = 5 * 1024 * 1024;   // 파일 최대 크기 5MB
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                byte[] buffer = new byte[bufferSize];
                int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                Log.d("Test", "image byte is " + bytesRead);

                // read image
                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // close streams
                Log.e("Test", "File is written");
                mFileInputStream.close();
                dos.flush(); // finish upload...
                dos.close();

                // get response
                int ch;
                InputStream is = conn.getInputStream();
                StringBuffer b = new StringBuffer();
                while ((ch = is.read()) != -1) {
                    b.append((char) ch);
                }
                String result = b.toString();

                //흡연감지 정보 DB에 업로드
                new UploadDB(this.no).run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //흡연 감지 정보 DB에 등록
    private class UploadDB extends Thread {
        String no = null;

        public UploadDB(String no) {
            this.no = no;
        }

        @Override
        public void run() {
            try {
                String no = this.no;
                String img_dir = "http://172.16.12.138:80/web/SmoverProject/image/smoke/" +
                        UserIdLoad() + "/" + no + "/" + fileName + ".jpg";
                String date = sDate;

                String link = "http://172.16.12.138:8080/SensedSmoke.php";
                String data = URLEncoder.encode("no", "UTF-8") + "=" + URLEncoder.encode(no, "UTF-8");
                data += "&" + URLEncoder.encode("img_dir", "UTF-8") + "=" + URLEncoder.encode(img_dir, "UTF-8");
                data += "&" + URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8");

                URL url = new URL(link);
                URLConnection conn = url.openConnection();

                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }
                Log.d(TAG, "Thread== DB result -> " + sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //흡연감지된 Smover 번호 저장 (SharedPreference)
    private void SmokeNoSave(String no) {
        SharedPreferences pref = getSharedPreferences("SmokeNo", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("No", no);
        editor.apply();
    }

    //사용자의 id 불러오기 (SharedPreference)
    private String UserIdLoad() {
        SharedPreferences pref = getSharedPreferences("UserId", Activity.MODE_PRIVATE);
        return pref.getString("Id", "");
    }
}