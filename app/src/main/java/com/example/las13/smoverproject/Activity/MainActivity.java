package com.example.las13.smoverproject.Activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.las13.smoverproject.HttpConnection.DelDirHttp;
import com.example.las13.smoverproject.HttpConnection.DelSmoHttp;
import com.example.las13.smoverproject.HttpConnection.GetIndexHttp;
import com.example.las13.smoverproject.HttpConnection.GetSensorHttp;
import com.example.las13.smoverproject.HttpConnection.GetSmoDataAllHttp;
import com.example.las13.smoverproject.MySharedPreferences;
import com.example.las13.smoverproject.R;
import com.example.las13.smoverproject.Service.GetSensorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "#MainActivity";
    private static final int REQUEST_CODE_ADD = 456;

    private MySharedPreferences myPreferences = new MySharedPreferences(MainActivity.this);

    private GetSensorHttp gsHttp = GetSensorHttp.getInstance();
    private GetSmoDataAllHttp gsdahttp = GetSmoDataAllHttp.getInstance();
    private DelSmoHttp dsHttp = DelSmoHttp.getInstance();
    private DelDirHttp ddHttp = DelDirHttp.getInstance();
    private GetIndexHttp giHttp = GetIndexHttp.getInstance();

    private ListView smover_list;
    private CustomAdapter adapter;

    ArrayList<Integer> arr_smoke_flag;
    ArrayList<String> arr_no;
    ArrayList<String> arr_area_name;
    ArrayList<String> arr_img_dir;
    ArrayList<String> arr_gas;
    ArrayList<String> arr_fire;
    ArrayList<Bitmap> arr_bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        smover_list = (ListView) findViewById(R.id.smover_list);

        //디바이스 해상도 가져오기
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
        int width = dm.widthPixels;   //디바이스 화면 너비

        //Smover 목록 레이아웃 디바이스 해상도에 맞게 비율 조정
        smover_list.getLayoutParams().width = width * 13 / 14;

        //ArrayList 생성
        arr_smoke_flag = new ArrayList<Integer>();
        arr_no = new ArrayList<String>();
        arr_area_name = new ArrayList<String>();
        arr_img_dir = new ArrayList<String>();
        arr_gas = new ArrayList<String>();
        arr_fire = new ArrayList<String>();
        arr_bitmap = new ArrayList<Bitmap>();

        //감지된 Smover번호 초기화
        myPreferences.SmokeNoSave("");

        // 인터넷 연결 체크
        try {
            ConnectivityManager cm = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkState = cm.getActiveNetworkInfo();
            if( networkState == null || !networkState.isConnected()){
                Toast.makeText(MainActivity.this, "인터넷 연결을 확인하세요", Toast.LENGTH_LONG).show();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        // 데이터 센서값 받아와서 흡연감지 시 푸시알림 보내는 Service
        Intent intent_data = new Intent(
                getApplicationContext(),//현재제어권자
                GetSensorService.class); // 이동할 컴포넌트
        startService(intent_data); // 서비스 시작

        //SMO_DATA 테이블 내용 DB에서 받아와서 화면 갱신
        new Thread() {
            public void run() {
                GetSmoDataAllCallback callback = new GetSmoDataAllCallback();
                gsdahttp.requestWebServer(myPreferences.UserIdLoad(), callback);
                Log.d(TAG, "GetSmoDataAllCallback 실행됨!");
            }
        }.start();

        //푸시알림 클릭 시 어느 Smover에서 흡연감지가 됐는지 알기 위해서 intent로 받아옴
        Intent intent = getIntent();
        final String smokeNo = intent.getStringExtra("SmokeNo");
        Log.d(TAG, "인텐트로부터 받아온 SmokeNo: " + smokeNo);

        //흡연감지되서 푸시알림 온 후에 푸시알림 클릭할 경우
        if (smokeNo != null && !smokeNo.equals("")) {
            //감지된 Smover번호 초기화
            myPreferences.SmokeNoSave("");
            Log.d(TAG, "푸시알림 클릭한 후 SmoekNo: "+smokeNo);

            //Smover no의 index 구해서 해당 smover의 상세정보 화면으로 이동
            new Thread() {
                public void run() {
                    GetIndexCallback callback = new GetIndexCallback();
                    giHttp.requestWebServer(smokeNo, callback);
                    Log.d(TAG, "GetIndexCallback 실행됨!");
                }
            }.start();
        }
    }

    //----------------------------------------------------------------------------------------------
    //                                        #CallBack
    //----------------------------------------------------------------------------------------------
    //GetSensorCallback
    private class GetSensorCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();

            //ArrayList 초기화
            arr_gas.clear();
            arr_fire.clear();

            try {
                JSONArray arr = new JSONArray(result);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    String gas = obj.getString("gas");
                    String fire = obj.getString("fire");

                    arr_gas.add(gas);
                    arr_fire.add(fire);

                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //Smover List 갱신
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (JSONException o) {
                o.printStackTrace();
            }
        }
    }

    //GetSmoDataAllCallback
    private class GetSmoDataAllCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            Log.d(TAG,"result: "+result);
            try {
                JSONArray arr = new JSONArray(result);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);

                    //JSON 객체 가져오기
                    String no = obj.getString("no");
                    String area_name = obj.getString("name");
                    String img_dir = obj.getString("img");
                    String gas = obj.getString("gas");
                    String fire = obj.getString("fire");
                    Log.d(TAG,"no: "+no);
                    Log.d(TAG,"area_name: "+area_name);
                    Log.d(TAG,"img_dir: \n"+img_dir);
                    Log.d(TAG,"gas: \n"+gas);
                    Log.d(TAG,"fre: \n"+fire);


                    //ArrayList에 저장
                    arr_no.add(no);
                    arr_area_name.add(area_name);
                    arr_img_dir.add(img_dir);
                    arr_gas.add(gas);
                    arr_fire.add(fire);
                }

                //해당 smover 사진 경로를 url로 넘겨 bitmap으로 변환하여 arr_bitmap에 저장
                new GetImgToBitArrTask().execute(arr_img_dir);
            } catch (JSONException o) {
                o.printStackTrace();
            }
        }
    }

    //DelSmoCallback
    private class DelSmoCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            final String result = response.body().string();

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "1":
                            Toast.makeText(MainActivity.this, "삭제 성공", Toast.LENGTH_SHORT).show();
                            break;
                        case "0":
                            Toast.makeText(MainActivity.this, "삭제 실패", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "알 수 없는 에러", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //DelDirCallback
    private class DelDirCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String body = response.body().string();
            String[] str = body.split(":");
            final String result = str[1];

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    switch (result) {
                        case "success":
                            Toast.makeText(MainActivity.this, "디렉터리 삭제 성공", Toast.LENGTH_SHORT).show();
                            //화면 새로고침(메인->메인)
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        case "fail":
                            Toast.makeText(MainActivity.this, "디렉터리 삭제 실패", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(MainActivity.this, "파일이 존재하지 않습니다", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //GetIndexCallback
    private class GetIndexCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String result = response.body().string();
            int index = Integer.parseInt(result);

            Log.d(TAG, "CallBack 메서드>> result: "+result);

            //감지된 Smover 상세정보 화면으로 이동
            Intent intent = new Intent(MainActivity.this, SmoverInfoActivity.class);
            intent.putExtra("index", index);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
    }

    //----------------------------------------------------------------------------------
    //  해당 경로에 있는 사진 가져와 비트맵으로 변환하여 ArrayList에 저장하는 AsyncTask
    //-----------------------------------------------------------------------------------
    private class GetImgToBitArrTask extends AsyncTask<ArrayList<String>, Void, Void> {
        protected Void doInBackground(ArrayList<String>... params) {
            try {
                for (int i = 0; i < params[0].size(); i++) {
                    URL url = new URL(params[0].get(i));

                    // Web에서 이미지를 가져온 뒤 ImageView에 지정할 Bitmap을 만든다
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); // 서버로 부터 응답 수신
                    conn.connect();

                    InputStream is = conn.getInputStream(); // InputStream 값 가져오기
                    Bitmap bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환
                    arr_bitmap.add(bitmap);    //ArrayList에 저장
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }   //doInBackground

        protected void onPostExecute(Void result) {
            try {
                //Smover List 보여주기
                adapter = new CustomAdapter(MainActivity.this);
                smover_list = (ListView) findViewById(R.id.smover_list);
                smover_list.setAdapter(adapter);

                //데이터 받아와서 2초마다 메인UI 갱신
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                GetSensorCallback callback = new GetSensorCallback();
                                // 파라미터와 미리정의해놓은 콜백함수를 매개변수로 전달하여 호출
                                gsHttp.requestWebServer(myPreferences.UserIdLoad(), callback);
                                Log.d(TAG, "GetSensorCallback 실행됨!");

                                sleep(3000);      //3초마다 데이터 갱신
                                Log.d(TAG, "Thread== 센서값 받아오는중");
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    //                                          #Dialog
    //----------------------------------------------------------------------------------------------
    //Smover 삭제 다이얼로그
    public class DeleteDialog extends Dialog {
        public DeleteDialog(Context context, final int position) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_delete);     //다이얼로그에서 사용할 레이아웃입니다.

            // 확인 버튼 리스너
            Button btn_ok = (Button) findViewById(R.id.btn_signup_ok);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Smover 정보 DB에서 삭제
                    new Thread() {
                        public void run() {
                            DelSmoCallback callback = new DelSmoCallback();
                            dsHttp.requestWebServer(arr_no.get(position), callback);
                        }
                    }.start();

                    //서버 디렉터리 삭제
                    new Thread() {
                        public void run() {
                            DelDirCallback callback = new DelDirCallback();
                            ddHttp.requestWebServer(myPreferences.UserIdLoad(), arr_no.get(position), callback);
                        }
                    }.start();

                    dismiss();   //다이얼로그를 닫음
                }
            });

            // 취소 버튼 리스너
            Button btn_cancel = (Button) findViewById(R.id.btn_signup_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();   //다이얼로그를 닫음
                }
            });
        }
    }

    //---------------------------
    //   Smover 리스트뷰 어댑터
    //---------------------------
    private class CustomAdapter extends ArrayAdapter<String> {
        private String sense = "미감지";
        private String color = "#51A7E1";
        private Context context;

        private CustomAdapter(Context context) {
            super(context, R.layout.list_item_smover, arr_area_name);
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list_item_smover, null, true);

            ImageView image_smover = (ImageView) rowView.findViewById(R.id.image_smover);
            TextView tv_smover_area_name = (TextView) rowView.findViewById(R.id.tv_smover_area_name);
            TextView tv_smover_sense = (TextView) rowView.findViewById(R.id.tv_smover_sense);
            ImageButton btn_smover_delete = (ImageButton) rowView.findViewById(R.id.btn_smover_delete);

            //이미지뷰 디바이스 해상도 및 사진 비율에 맞게 비율 조정
            float bitmap_width = arr_bitmap.get(position).getWidth();
            float bitmap_height = arr_bitmap.get(position).getHeight();
            float rate = bitmap_width / bitmap_height;
            image_smover.getLayoutParams().height = (int) (smover_list.getLayoutParams().width / rate);
            image_smover.requestLayout();

            image_smover.setImageBitmap(arr_bitmap.get(position));
            tv_smover_area_name.setText(arr_area_name.get(position));
            int gas = Integer.parseInt(arr_gas.get(position));
            int fire = Integer.parseInt(arr_fire.get(position));
            if (gas >= 1100 && fire == 1) {
                sense = "감지";
                color = "#DE4F4F";
            } else {
                sense = "미감지";
                color = "#51A7E1";
                /***
                 new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                sense = "미감지";
                color = "#51A7E1";
                }
                }, 120000 );  //감지로 바뀐 후 2분간 대기상태
                 ***/
            }
            //메인 UI 변경
            tv_smover_sense.setText(sense);
            tv_smover_sense.setTextColor(Color.parseColor(color));

            //Smover 이미지뷰 리스너
            image_smover.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SmoverInfoActivity.class);
                    intent.putExtra("index", position);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            });

            //Smover 삭제 버튼 리스너
            btn_smover_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //삭제 다이얼로그 띄움
                    new DeleteDialog(MainActivity.this, position).show();
                }
            });

            return rowView;
        }
    }

    //기기 추가 등록 후 메인 화면으로 넘어올 때 Smover 리스트 갱신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD:
                    String addMsg = data.getStringExtra("add_msg");
                    if (addMsg.equals("성공")) {
                        arr_no.clear();
                        arr_area_name.clear();
                        arr_img_dir.clear();
                        arr_bitmap.clear();
                        arr_gas.clear();
                        arr_fire.clear();

                        Intent intent = new Intent(MainActivity.this, MainActivity.class);
                        overridePendingTransition(0, 0);   //화면 전환 효과 X
                        startActivity(intent);
                        finish();
                    }
                    break;
            }
        }
    }

    //뒤로가기 버튼 눌렀을 시 이전 액티비티로 전환
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, msg);
    }

    //--------------------------
    //     커스텀 액션바
    //--------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar actionBar = getSupportActionBar();

        // Custom Actionbar를 사용하기 위해 CustomEnabled을 true 시키고 필요 없는 것은 false 시킨다
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);            //액션바 아이콘을 업 네비게이션 형태로 표시합니다.
        actionBar.setDisplayShowTitleEnabled(false);        //액션바에 표시되는 제목의 표시유무를 설정합니다.
        actionBar.setDisplayShowHomeEnabled(false);            //홈 아이콘을 숨김처리합니다.

        //layout을 가지고 와서 actionbar에 포팅을 시킵니다.
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View actionbar = inflater.inflate(R.layout.custom_actionbar_main, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar) actionbar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        //카메라 추가 버튼 리스너
        ImageButton btn1 = (ImageButton) this.findViewById(R.id.btn_plus_camera);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddSmoverActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD);
            }
        });

        //설정 버튼 리스너
        ImageButton btn2 = (ImageButton) this.findViewById(R.id.btn_setting);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        return true;
    }
}
