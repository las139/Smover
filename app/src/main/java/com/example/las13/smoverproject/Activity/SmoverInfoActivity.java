package com.example.las13.smoverproject.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.las13.smoverproject.AsyncTask.ImageUploadTask;
import com.example.las13.smoverproject.HttpConnection.EditSmoverHttp;
import com.example.las13.smoverproject.HttpConnection.GetImgToBitmapHttp;
import com.example.las13.smoverproject.HttpConnection.GetSmoDataHttp;
import com.example.las13.smoverproject.HttpConnection.GetSmoImgAllHttp;
import com.example.las13.smoverproject.HttpConnection.LoadSmoImgHttp;
import com.example.las13.smoverproject.MySharedPreferences;
import com.example.las13.smoverproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lee.whdghks913.WebViewAllCapture.WebViewAllCapture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.example.las13.smoverproject.Activity.AddSmoverActivity.MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE;

public class SmoverInfoActivity extends AppCompatActivity {
    private static final String TAG = "#SmoverInfoActivity";
    private static final int REQUEST_CODE_IMAGE_PICK = 123;
    private int PROFILE_IMAGE_ASPECT_X = 120;
    private int PROFILE_IMAGE_ASPECT_Y = 48;
    private int PROFILE_IMAGE_OUTPUT_X = 1200;
    private int PROFILE_IMAGE_OUTPUT_Y = 480;
    private static final String TEMP_FILE_NAME = "TestImg.jpg";

    private MySharedPreferences myPreferences = new MySharedPreferences(SmoverInfoActivity.this);

    private GetSmoDataHttp gsdHttp = GetSmoDataHttp.getInstance();
    private GetImgToBitmapHttp gitbHttp = GetImgToBitmapHttp.getInstance();
    private GetSmoImgAllHttp gsiaHttp = GetSmoImgAllHttp.getInstance();
    private LoadSmoImgHttp lsiHttp = LoadSmoImgHttp.getInstance();
    private EditSmoverHttp esHttp = EditSmoverHttp.getInstance();

    private String smo_no = null;
    private String smo_area_name = null;
    private String smo_addr = null;
    private String smo_img_dir = null;

    private String file_path;
    private Uri temp_image_uri;
    private Bitmap bitmap;

    private boolean loading_flag = false;
    private int width, height;
    private int index;
    private String arr_y;
    private String arr_m;
    private String arr_d;
    private int index_y = 0;
    private int index_m = 0;
    private int index_d = 0;

    private AddressDialog dialog_addr;
    private EditDialog dialog_edit;
    private Handler handler;
    private TextView tv_addr_result;
    private EditText edit_dailog_addr;
    private EditText edit_addr1, edit_addr2;
    private ImageView image_pick;

    private String date;
    private ArrayList<String> arr_smoke_img_dir;
    private ArrayList<String> arr_smoke_date;
    private ArrayList<String> arr_smoke_time;
    private ArrayList<Bitmap> arr_bitmap;
    private ImageView image_smover;
    private TextView tv_area_name, tv_addr;
    private ProgressDialog progressDialog;
    private CameraDialog dialog_camera;
    private ImageDialog dialog_image;
    private LinearLayout smoke_list_layout;

    String[] str_year = {"2018"};
    String[] str_month = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"};
    String[] str_day = {"01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
            "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
            "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smover_info);

        //ArrayList 생성
        arr_smoke_img_dir = new ArrayList<String>();
        arr_smoke_date = new ArrayList<String>();
        arr_smoke_time = new ArrayList<String>();
        arr_bitmap = new ArrayList<Bitmap>();


        smoke_list_layout = (LinearLayout) findViewById(R.id.layout_smoke_list);
        image_smover = (ImageView) findViewById(R.id.image_smover);
        tv_area_name = (TextView) findViewById(R.id.tv_smover_area_name);
        tv_addr = (TextView) findViewById(R.id.tv_smover_address);

        //디바이스 해상도 가져오기
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
        width = dm.widthPixels; //디바이스 화면 너비
        height = dm.heightPixels; //디바이스 화면 높이

        PROFILE_IMAGE_ASPECT_X = width;
        PROFILE_IMAGE_ASPECT_Y = (int) ((float) (height * 2 / 6));
        PROFILE_IMAGE_OUTPUT_X = width;
        PROFILE_IMAGE_OUTPUT_Y = (int) ((float) (height * 2 / 6));

        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = new Handler();

        //주소 설정 다이얼로그 생성
        dialog_addr = new AddressDialog(this);

        //주소 설정 다이얼로그 설정
        WindowManager.LayoutParams wm = dialog_addr.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
        wm.copyFrom(dialog_addr.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
        wm.width = width;
        wm.height = height * 4 / 5;

        //메인 화면으로부터 몇번째 구역 선택했는지 값 받아오기
        Intent intent = getIntent();
        index = intent.getIntExtra("index", 0);
        Log.d(TAG, "index: " + index);

        //현재 날짜 불러와서 각 index에 저장
        setNowDate();

        //Spiner 년
        Spinner spinner_y = (Spinner) findViewById(R.id.spinner_year);
        ArrayAdapter<CharSequence> adapter_y = ArrayAdapter.createFromResource(this,
                R.array.arr_year, android.R.layout.simple_spinner_item);
        adapter_y.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_y.setAdapter(adapter_y);
        spinner_y.setSelection(index_y);
        spinner_y.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                arr_y = adapterView.getItemAtPosition(position).toString();
                //SMO_IMG 테이블 내용 DB에서 받아와서 화면 갱신
                date = arr_y + "-" + arr_m + "-" + arr_d;  //서버에 있는 값과 비교하기 위해 구분자 - 추가
                if (arr_y != null && arr_m != null && arr_d != null) {
                    new Thread() {
                        public void run() {
                            Log.d(TAG, "년 스피너 바뀜!");
                            GetSmoImgAllCallback callback = new GetSmoImgAllCallback();
                            gsiaHttp.requestWebServer(myPreferences.UserIdLoad(), Integer.toString(index), date, callback);
                        }
                    }.start();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Spiner 월
        Spinner spinner_m = (Spinner) findViewById(R.id.spinner_month);
        ArrayAdapter<CharSequence> adapter_m = ArrayAdapter.createFromResource(this,
                R.array.arr_month, android.R.layout.simple_spinner_item);
        adapter_m.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_m.setAdapter(adapter_m);
        spinner_m.setSelection(index_m);
        spinner_m.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                arr_m = adapterView.getItemAtPosition(position).toString();
                //SMO_IMG 테이블 내용 DB에서 받아와서 화면 갱신
                date = arr_y + "-" + arr_m + "-" + arr_d;  //서버에 있는 값과 비교하기 위해 구분자 - 추가
                if (arr_y != null && arr_m != null && arr_d != null) {
                    new Thread() {
                        public void run() {
                            Log.d(TAG, "월 스피너 바뀜!");
                            GetSmoImgAllCallback callback = new GetSmoImgAllCallback();
                            gsiaHttp.requestWebServer(myPreferences.UserIdLoad(), Integer.toString(index), date, callback);
                        }
                    }.start();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //Spiner 일
        Spinner spinner_d = (Spinner) findViewById(R.id.spinner_day);
        ArrayAdapter<CharSequence> adapter_d = ArrayAdapter.createFromResource(this,
                R.array.arr_day, android.R.layout.simple_spinner_item);
        adapter_d.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_d.setAdapter(adapter_d);
        spinner_d.setSelection(index_d);
        spinner_d.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                arr_d = adapterView.getItemAtPosition(position).toString();
                //SMO_IMG 테이블 내용 DB에서 받아와서 화면 갱신
                date = arr_y + "-" + arr_m + "-" + arr_d;  //서버에 있는 값과 비교하기 위해 구분자 - 추가
                if (arr_y != null && arr_m != null && arr_d != null) {
                    new Thread() {
                        public void run() {
                            Log.d(TAG, "일 스피너 바뀜!");
                            GetSmoImgAllCallback callback = new GetSmoImgAllCallback();
                            gsiaHttp.requestWebServer(myPreferences.UserIdLoad(), Integer.toString(index), date, callback);
                        }
                    }.start();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //smover ImageView 클릭 리스너
        image_smover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //카메라 웹뷰 다이얼로그 생성
                dialog_camera = new CameraDialog(SmoverInfoActivity.this);

                //다이얼로그 해상도 디바이스에 맞게 조절
                WindowManager.LayoutParams wm = dialog_camera.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(dialog_camera.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width;

                //카메라 웹뷰 다이얼로그 띄움
                dialog_camera.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!loading_flag) {
                            Toast.makeText(SmoverInfoActivity.this, "영상 재생에 실패하였습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                            progressDialog.cancel();
                            dialog_camera.cancel();
                        }
                    }
                }, 10000); // 10000ms (10초)
            }
        });

        //SMO_DATA 테이블 내용 DB에서 받아와서 화면 갱신
        new Thread() {
            public void run() {
                GetSmoDataCallback callback = new GetSmoDataCallback();
                gsdHttp.requestWebServer(myPreferences.UserIdLoad(), Integer.toString(index), callback);
            }
        }.start();
    }

    //----------------------------------------------------------------------------------------------
    //                                        #CallBack
    //----------------------------------------------------------------------------------------------
    //GetSmoDataCallback
    private class GetSmoDataCallback implements Callback {
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
                    try {
                        JSONArray arr = new JSONArray(result);
                        JSONObject obj = arr.getJSONObject(0);

                        //JSON 객체 가져오기
                        smo_area_name = obj.getString("name");
                        smo_no = obj.getString("no");
                        smo_addr = obj.getString("addr");
                        smo_img_dir = obj.getString("img");

                        //메인화면 UI 갱신
                        tv_area_name.setText(smo_area_name);
                        tv_addr.setText(smo_addr);

                        //해당 smover 사진 경로를 url로 넘겨 bitmap으로 변환하여 저장
                        new Thread() {
                            public void run() {
                                GetImgToBitmapCallback callback = new GetImgToBitmapCallback();
                                gitbHttp.requestWebServer(smo_img_dir, callback);
                            }
                        }.start();
                    } catch (JSONException o) {
                        o.printStackTrace();
                    }
                }
            });
        }
    }

    // GetImgToBitmapCallback
    private class GetImgToBitmapCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            InputStream is = response.body().byteStream();
            final Bitmap bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //메인화면 UI 갱신
                    image_smover.setImageBitmap(bitmap);

                    float rate = (float) bitmap.getWidth() / (float) bitmap.getHeight();

                    int img_width = (int) (width * (float) 6 / 7);
                    int img_height = (int) ((float) img_width / rate);

                    //Smover 이미지뷰 디바이스 해상도에 맞게 비율 조정
                    image_smover.getLayoutParams().width = img_width * 7 / 8;
                    image_smover.getLayoutParams().height = img_height * 7 / 8;
                    image_smover.requestLayout();
                }
            });
        }
    }

    //GetSmoImgAllCallback
    private class GetSmoImgAllCallback implements Callback {
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
                    //흡연감지 목록이 없는 경우
                    if (result.equals("0") || result.equals("[]")) {
                        smoke_list_layout.setVisibility(View.VISIBLE);

                        //ArrayList 기존에 있던 다른 날짜 목록 초기화
                        arr_smoke_img_dir.clear();
                        arr_smoke_date.clear();
                        arr_smoke_time.clear();
                    }

                    //흡연감지 목록이 있는 경우
                    else {
                        smoke_list_layout.setVisibility(View.INVISIBLE);
                        try {
                            //ArrayList 기존에 있던 다른 날짜 목록 초기화
                            arr_smoke_img_dir.clear();
                            arr_smoke_date.clear();
                            arr_smoke_time.clear();

                            JSONArray arr = new JSONArray(result);
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject obj = arr.getJSONObject(i);

                                //JSON 객체 가져오기
                                String strings[] = obj.getString("date").split(" ");
                                String date = strings[0];
                                String time = strings[1];
                                String img_dir = obj.getString("img");

                                //ArrayList에 저장
                                arr_smoke_img_dir.add(img_dir);
                                arr_smoke_date.add(date);
                                arr_smoke_time.add(time);
                            }

                            //해당 smover 사진 경로를 url로 넘겨 bitmap으로 변환하여 ArrayList에 저장
                            new GetBitToImgArrTask().execute((arr_smoke_img_dir));
                        } catch (JSONException o) {
                            o.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    //LoadSmoImgCallback
    private class LoadSmoImgCallback implements Callback {
        @Override
        public void onFailure(Call call, IOException e) {
            Log.d(TAG, "HTTP 콜백오류:" + e.getMessage());
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            InputStream is = response.body().byteStream();
            bitmap = BitmapFactory.decodeStream(is); // Bitmap으로 변환

            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    //메인화면 UI 갱신
                    image_pick.setImageBitmap(bitmap);

                    //기존 ImageView 틀에 맞게 조정
                    image_pick.getLayoutParams().height = 1;
                    image_pick.requestLayout();
                }
            });
        }
    }

    //EditSmoverCallback
    private class EditSmoverCallback implements Callback {
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
                            Toast.makeText(SmoverInfoActivity.this, "편집 성공", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(SmoverInfoActivity.this, "편집 실패", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //---------------------------------------------------------------------------------
    //  흡연 감지 목록 사진 가져와 비트맵으로 변환하여 ArrayList에 저장하는 AsyncTask
    //---------------------------------------------------------------------------------
    private class GetBitToImgArrTask extends AsyncTask<ArrayList<String>, Void, Void> {
        protected Void doInBackground(ArrayList<String>... params) {
            try {
                for (int i = 0; i < params[0].size(); i++) {
                    URL url = new URL(params[0].get(i));
                    Log.d(TAG,"URL: "+params[0].get(i));

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
                //흡연 감지 목록 List 보여주기
                CustomAdapter adapter = new CustomAdapter(SmoverInfoActivity.this);
                ListView smoke_list = (ListView) findViewById(R.id.smoke_list);
                smoke_list.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }   //onPostExecute
    }

    //----------------------------------------------------------------------------------------------
    //                                        #Dialog
    //----------------------------------------------------------------------------------------------
    //흡연감지 사진 확대 dialog
    private class ImageDialog extends Dialog {
        private WebView imageWebView;

        public ImageDialog(Context context, String url) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_image);     //다이얼로그에서 사용할 레이아웃입니다.

            imageWebView = (WebView) findViewById(R.id.imageView);
            imageWebView.getLayoutParams().height = (int) (height / 3 * (float) 8 / 10);
            imageWebView.requestLayout();

            WebSettings webSettings = imageWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            webSettings.setPluginState(WebSettings.PluginState.ON);
            //html 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            //확대,축소
            webSettings.setBuiltInZoomControls(true);
            webSettings.setSupportZoom(true);
            imageWebView.setInitialScale(10);

            imageWebView.loadUrl(url);  // 접속 URL
        }
    }

    //Smover DB 정보 편집 dialog
    private class EditDialog extends Dialog {
        public EditDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_edit);     //다이얼로그에서 사용할 레이아웃입니다.

            EditText edit_area_name = (EditText) findViewById(R.id.edit_area_name);
            edit_addr1 = (EditText) findViewById(R.id.edit_addr1);
            edit_addr2 = (EditText) findViewById(R.id.edit_addr2);
            image_pick = (ImageView) findViewById(R.id.image_pick);

            //서버에 있는 smover 대표 사진 불러와서 적용
            new Thread() {
                public void run() {
                    LoadSmoImgCallback callback = new LoadSmoImgCallback();
                    lsiHttp.requestWebServer(smo_img_dir, callback);
                }
            }.start();

            //Smover 사진 선택 이미지뷰 디바이스 해상도에 맞게 비율 조정
            image_pick.getLayoutParams().width = (int) ((float) (width * 6 / 10));
            image_pick.requestLayout();

            //기존 정보 불러와서 화면에 적용
            edit_area_name.setText(smo_area_name);
            String[] addrs = smo_addr.split(", ");
            edit_addr1.setText(addrs[0]);
            edit_addr2.setText(addrs[1]);

            // 주소 찾기 버튼 리스너
            Button btn_search = (Button) findViewById(R.id.btn_search_addr);
            btn_search.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // WebView 초기화
                    init_webView();

                    dialog_addr.show();
                }
            });

            // 이미지 앨범에서 불러오기 버튼 리스너
            Button btn_pick_image = (Button) findViewById(R.id.btn_pick_image);
            btn_pick_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //SD카드 쓰기 권한 체크
                    int permissionCheck = ContextCompat.checkSelfPermission(SmoverInfoActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    //권한이 없는 경우
                    if (permissionCheck == PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(SmoverInfoActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                    }
                    //권한이 있는 경우
                    else if (permissionCheck == PERMISSION_GRANTED) {
                        DoCropImage();
                    }
                }
            });

            // 확인 버튼 리스너
            Button btn_ok = (Button) findViewById(R.id.btn_signup_ok);
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText edit_name = (EditText) findViewById(R.id.edit_area_name);
                    edit_addr1 = (EditText) findViewById(R.id.edit_addr1);
                    edit_addr2 = (EditText) findViewById(R.id.edit_addr2);

                    final String name = edit_name.getText().toString();
                    String addr1 = edit_addr1.getText().toString();
                    String addr2 = edit_addr2.getText().toString();
                    final String addr = addr1 + ", " + addr2;

                    if (image_pick.getDrawable() == null) {
                        Toast.makeText(SmoverInfoActivity.this, "사진을 선택해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else if (edit_name.equals("")) {
                        Toast.makeText(SmoverInfoActivity.this, "구역 이름을 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else if (edit_addr1.equals("")) {
                        Toast.makeText(SmoverInfoActivity.this, "구역 주소를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else if (edit_addr2.equals("")) {
                        Toast.makeText(SmoverInfoActivity.this, "구역 상세 주소를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //비트맵을 이미지 파일로 저장
                        SaveBitmapToJpeg(bitmap, "smover", "img_" + smo_no);

                        // 인터넷 연결 체크
                        try {
                            ConnectivityManager cm = (ConnectivityManager) SmoverInfoActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo networkState = cm.getActiveNetworkInfo();
                            if (networkState != null && networkState.isConnected()) {
                                // Smover 정보 DB 수정
                                new Thread() {
                                    public void run() {
                                        EditSmoverCallback callback = new EditSmoverCallback();
                                        esHttp.requestWebServer(myPreferences.UserIdLoad(), smo_no, name, addr, callback);
                                    }
                                }.start();

                                // Smover 대표 사진 서버에 업로드
                                new ImageUploadTask().execute(myPreferences.UserIdLoad(), smo_no, file_path, "smover");

                                dismiss(); //다이얼로그 닫음

                                Intent intent = new Intent(SmoverInfoActivity.this, MainActivity.class);
                                overridePendingTransition(0, 0);   //화면 전환 효과 X
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(SmoverInfoActivity.this, "인터넷 연결을 확인하세요", Toast.LENGTH_LONG).show();
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });

            // 취소 버튼 리스너
            Button btn_cancel = (Button) findViewById(R.id.btn_signup_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();   //다이얼로그를 닫는 메소드입니다.
                }
            });
        }
    }

    //실시간 영상 dialog
    private class CameraDialog extends Dialog {
        private int count = 0;
        private Button btn_pause;
        private WebView cameraWebView;

        public CameraDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_camera);     //다이얼로그에서 사용할 레이아웃입니다.

            cameraWebView = (WebView) findViewById(R.id.cameraView);
            WebSettings webSettings = cameraWebView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webSettings.setMediaPlaybackRequiresUserGesture(false);
            webSettings.setPluginState(WebSettings.PluginState.ON);
            //html 컨텐츠가 웹뷰보다 클 경우 스크린 크기에 맞게 조정
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            cameraWebView.setInitialScale(10);

            String url = "http://169.254.56.126:8080/stream";
            cameraWebView.loadUrl(url);  // 접속 URL
            cameraWebView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressDialog = new ProgressDialog(SmoverInfoActivity.this);
                    progressDialog.setMessage("영상 로딩 중...");
                    progressDialog.setCancelable(true);
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressDialog.show();
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    long nowTime = System.currentTimeMillis();
                    //System.
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    try {
                        loading_flag = true;
                        if (progressDialog.isShowing()) {
                            progressDialog.cancel();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            cameraWebView.setWebChromeClient(new WebChromeClient());
            cameraWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);   // 뷰 하드웨어 가속
            //webView.setRotation(180);  // 뷰 180도 회전

            cameraWebView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            });

            // 일시정지&재생 버튼 리스너
            btn_pause = (Button) findViewById(R.id.btn_camera_pause);
            btn_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 버튼 누를때마다 '일시정지' or '재생' 텍스트 변환
                    if (count % 2 == 1) {
                        btn_pause.setText("일시정지");
                    } else {
                        btn_pause.setText("재생");
                    }
                    count++;
                }
            });

            // 캡쳐 버튼 리스너
            Button btn_capture = (Button) findViewById(R.id.btn_camera_capture);
            btn_capture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/smoke_capture/";

                    //저장할 파일용 날짜 포맷
                    Date day = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
                    String date = String.valueOf(sdf.format(day));

                    String mScreenShotName = "Capture_" + date + ".jpg";

                    WebViewAllCapture mAllCapture = new WebViewAllCapture();
                    Boolean result = mAllCapture.onWebViewAllCapture(cameraWebView, mFilePath, mScreenShotName);
                    if (result) {
                        Toast.makeText(SmoverInfoActivity.this, "캡쳐되었습니다\n경로:" + mFilePath
                                + mScreenShotName, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(SmoverInfoActivity.this, "캡쳐 실패", Toast.LENGTH_LONG).show();
                    }
                }
            });

            // 닫기 버튼 리스너
            Button btn_cancel = (Button) findViewById(R.id.btn_camera_cancel);
            btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();   //다이얼로그를 닫는 메소드입니다.
                }
            });
        }
    }


    //주소 설정 dialog
    private class AddressDialog extends Dialog {
        public AddressDialog(Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);   //다이얼로그의 타이틀바를 없애주는 옵션입니다.
            setContentView(R.layout.dialog_set_addr);     //다이얼로그에서 사용할 레이아웃입니다.

            tv_addr_result = (TextView) findViewById(R.id.address_result);
            edit_dailog_addr = (EditText) findViewById(R.id.edit_address);

            //입력된 것 초기화 (재실행 시)
            tv_addr_result.setText("");
            edit_dailog_addr.setText("");

            // 주소 입력후 확인 버튼 리스너
            Button btn = (Button) findViewById(R.id.btn_set_address);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String addr_basic = tv_addr_result.getText().toString();
                    String addr_detail = edit_dailog_addr.getText().toString();

                    if (addr_basic.equals("")) {
                        Toast.makeText(SmoverInfoActivity.this, "주소를 설정해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                    //상세주소를 입력하지 않았을 경우
                    else if (addr_detail.equals("")) {
                        Toast.makeText(SmoverInfoActivity.this, "상세 주소를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SmoverInfoActivity.this, "기본: " + addr_basic + "\n상세: " + addr_detail,
                                Toast.LENGTH_SHORT).show();
                        edit_addr1.setText(addr_basic);
                        edit_addr2.setText(addr_detail);

                        dialog_addr.cancel();
                    }
                }
            });
        }
    }

    //갤러리 실행 후 이미지 선택 후 크롭
    private void DoCropImage() {
        temp_image_uri = Uri.fromFile(GetTempFile());

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", PROFILE_IMAGE_ASPECT_X);
        intent.putExtra("aspectY", PROFILE_IMAGE_ASPECT_Y);
        intent.putExtra("outputX", PROFILE_IMAGE_OUTPUT_X);
        intent.putExtra("outputY", PROFILE_IMAGE_OUTPUT_Y);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, temp_image_uri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK);
    }

    //임시 파일 생성
    private File GetTempFile() {
        File file = new File(Environment.getExternalStorageDirectory(), TEMP_FILE_NAME);
        try {
            file.createNewFile();
        } catch (Exception e) {
        }
        return file;
    }

    //비트맵 가져와서 이미지 저장
    public void SaveBitmapToJpeg(Bitmap bitmap, String folder, String name) {
        try {
            String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();  //외장메모리 절대경로
            String folder_name = "/" + folder + "/";
            String folder_path = ex_storage + folder_name;
            String file_name = name + ".jpg";
            file_path = folder_path + file_name;

            File newFile = new File(folder_path);
            if (!newFile.isDirectory()) {
                newFile.mkdirs();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file_path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(SmoverInfoActivity.this, "이미지 저장 성공",
                    Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //웹뷰 초기화
    public void init_webView() {
        // WebView 설정
        WebView webView = (WebView) dialog_addr.findViewById(R.id.webView);
        // JavaScript 허용
        webView.getSettings().setJavaScriptEnabled(true);
        // JavaScript의 window.open 허용
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        // 두 번째 파라미터는 사용될 php에도 동일하게 사용해야함
        webView.addJavascriptInterface(new SmoverInfoActivity.AndroidBridge(), "SmoverProject");
        // web client 를 chrome 으로 설정
        webView.setWebChromeClient(new WebChromeClient());
        // webview url load
        webView.loadUrl("http://172.16.12.138:8080/SearchAddr.php");
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    tv_addr_result.setText(String.format("%s %s", arg2, arg3));
                    // WebView를 초기화 하지않으면 재사용할 수 없음
                    init_webView();
                }
            });
        }
    }

    // 현재 날짜 불러오기
    private String getNowDate() {
        long now = System.currentTimeMillis();  // 현재 시간 가져오기
        Date date = new Date(now);  // Date 생성하기
        SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd"); //가져오고 싶은 형식으로 가져오기
        return mFormat.format(date);
    }

    // 현재 날짜 불러와서 적용
    private void setNowDate() {
        String getDate = getNowDate();
        String strings[] = getDate.split("-");
        String year = strings[0];
        String month = strings[1];
        String day = strings[2];
        for (int i = 0; i < str_year.length; i++) {
            if (str_year[i].equals(year)) {
                index_y = i;
            }
        }
        for (int i = 0; i < str_month.length; i++) {
            if (str_month[i].equals(month)) {
                index_m = i;
            }
        }
        for (int i = 0; i < str_day.length; i++) {
            if (str_day[i].equals(day)) {
                index_d = i;
            }
        }
    }

    //----------------------------------
    //  흡연 감지 목록 리스트뷰 어댑터
    //----------------------------------
    private class CustomAdapter extends ArrayAdapter<String> {
        Context context;

        public CustomAdapter(Context context) {
            super(context, R.layout.list_smoke_sense, arr_smoke_date);
            this.context = context;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list_smoke_sense, null, true);

            LinearLayout layout_date = (LinearLayout) rowView.findViewById(R.id.layout_smoke_date);
            ImageView image_smoke = (ImageView) rowView.findViewById(R.id.image_smoke);
            TextView tv_smoke_date = (TextView) rowView.findViewById(R.id.tv_smoke_date);
            TextView tv_smoke_time = (TextView) rowView.findViewById(R.id.tv_smoke_time);

            //흡연 이미지뷰 디바이스 해상도에 맞게 비율 조정
            image_smoke.getLayoutParams().width = width * 2 / 7;
            image_smoke.getLayoutParams().height = height * 1 / 10;
            image_smoke.requestLayout();

            //흡연 날짜&시간 레이아웃 디바이스 해상도에 맞게 비율 조정
            layout_date.getLayoutParams().width = width * 3 / 7;
            layout_date.requestLayout();

            //메인 화면 UI에 적용
            image_smoke.setImageBitmap(arr_bitmap.get(position));
            tv_smoke_date.setText(arr_smoke_date.get(position));
            tv_smoke_time.setText(arr_smoke_time.get(position));

            //흡연 이미지뷰 리스너
            image_smoke.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //이미지 웹뷰 다이얼로그 생성
                    dialog_image = new ImageDialog(SmoverInfoActivity.this, arr_smoke_img_dir.get(position));

                    //다이얼로그 해상도 디바이스에 맞게 조절
                    WindowManager.LayoutParams wm = dialog_image.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                    wm.copyFrom(dialog_image.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                    wm.width = width;
                    //wm.height = height / 2;

                    //이미지 웹뷰 다이얼로그 띄움
                    dialog_image.show();
                }
            });

            return rowView;
        }
    }

    //Smover 편집 화면에서 앨범 불러오기후 사진 선택 시 화면 갱신
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_IMAGE_PICK:
                    File tempFile = GetTempFile();
                    if (tempFile.exists()) {
                        try {
                            int img_width = image_pick.getLayoutParams().width; // 이미지 적용 되기 전 뷰 너비

                            String img_absolute_path = tempFile.getAbsolutePath();  //절대 경로 구하기
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), temp_image_uri);
                            image_pick.setImageBitmap(bitmap);  //불러온 이미지 화면에 적용

                            image_pick.getLayoutParams().width = img_width;
                            image_pick.getLayoutParams().height = 1;
                            image_pick.requestLayout();
                        } catch (Exception e) {
                            Toast.makeText(SmoverInfoActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(SmoverInfoActivity.this, "권한 부여 성공", Toast.LENGTH_SHORT).show();
                    DoCropImage();  //앨범 불러오기
                } else {
                    Toast.makeText(SmoverInfoActivity.this, "권한 부여 실패", Toast.LENGTH_SHORT).show();
                }
                return;
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
        View actionbar = inflater.inflate(R.layout.custom_actionbar_smover_info, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar) actionbar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        //------------------------------
        //   뒤로가기 버튼 리스너
        //------------------------------
        ImageButton btn_back = (ImageButton) this.findViewById(R.id.btn_smover_info_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //----------------------
        //   편집 버튼 리스너
        //----------------------
        ImageButton btn_edit = (ImageButton) this.findViewById(R.id.btn_smover_info_edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //편집 다이얼로그 생성
                dialog_edit = new EditDialog(SmoverInfoActivity.this);

                //편집 다이얼로그 설정
                WindowManager.LayoutParams wm = dialog_edit.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
                wm.copyFrom(dialog_edit.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
                wm.width = width;
                wm.height = height * 97 / 100;

                image_pick.setImageResource(0);  //이미지 뷰 초기화
                //Smover 사진 선택 이미지뷰 디바이스 해상도에 맞게 비율 재 조정
                image_pick.getLayoutParams().width = (int) ((float) (width * 6 / 10));
                image_pick.requestLayout();

                dialog_edit.show();   //편집 다이얼로그 띄움
            }
        });

        return true;
    }
}