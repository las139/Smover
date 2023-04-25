package com.example.las13.smoverproject.Activity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
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
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.las13.smoverproject.AsyncTask.ImageUploadTask;
import com.example.las13.smoverproject.HttpConnection.AddSmoverHttp;
import com.example.las13.smoverproject.HttpConnection.MakeDirHttp;
import com.example.las13.smoverproject.MySharedPreferences;
import com.example.las13.smoverproject.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class AddSmoverActivity extends AppCompatActivity {
    private static final String TAG = "#AddSmoverActivity";
    public static final int MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE = 789;
    private static final int REQUEST_CODE_IMAGE_PICK = 123;
    private int PROFILE_IMAGE_ASPECT_X = 120;
    private int PROFILE_IMAGE_ASPECT_Y = 48;
    private int PROFILE_IMAGE_OUTPUT_X = 1200;
    private int PROFILE_IMAGE_OUTPUT_Y = 480;
    private static final String TEMP_FILE_NAME = "TestImg.jpg";

    private MySharedPreferences myPreferences = new MySharedPreferences(AddSmoverActivity.this);

    private AddSmoverHttp asHttp = AddSmoverHttp.getInstance();
    private MakeDirHttp mdHttp = MakeDirHttp.getInstance();

    private String file_path;
    private Uri temp_image_uri;
    private Bitmap bitmap;

    private AddressDialog dialog_addr;
    private Handler handler;
    private TextView tv_addr_result;
    private EditText edit_dailog_addr;
    private EditText edit_addr1, edit_addr2;
    private String addr_basic, addr_detail;
    private ImageView image_pick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smover);

        image_pick = (ImageView) findViewById(R.id.image_pick);
        edit_addr1 = (EditText) findViewById(R.id.edit_add_addr1);
        edit_addr2 = (EditText) findViewById(R.id.edit_add_addr2);

        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = new Handler();

        //다이얼로그 생성
        dialog_addr = new AddressDialog(this);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics(); //디바이스 화면크기를 구하기위해
        int width = dm.widthPixels; //디바이스 화면 너비
        int height = dm.heightPixels; //디바이스 화면 높이

        //다이얼로그 설정
        WindowManager.LayoutParams wm = dialog_addr.getWindow().getAttributes();  //다이얼로그의 높이 너비 설정하기위해
        wm.copyFrom(dialog_addr.getWindow().getAttributes());  //여기서 설정한값을 그대로 다이얼로그에 넣겠다는의미
        wm.width = width;
        wm.height = height * 4 / 5;

        //Smover 사진 선택 이미지뷰 디바이스 해상도에 맞게 비율 조정
        image_pick.getLayoutParams().width = (int) ((float) (width * 7 / 10));
        image_pick.requestLayout();

        PROFILE_IMAGE_ASPECT_X = width;
        PROFILE_IMAGE_ASPECT_Y = (int) ((float) (height * 2 / 6));
        PROFILE_IMAGE_OUTPUT_X = width;
        PROFILE_IMAGE_OUTPUT_Y = (int) ((float) (height * 2 / 6));

        // 이미지 앨범에서 불러오기 버튼 리스너
        Button btn_pick_image = (Button) findViewById(R.id.btn_pick_image);
        btn_pick_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SD카드 쓰기 권한 체크
                int permissionCheck = ContextCompat.checkSelfPermission(AddSmoverActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                //권한이 없는 경우
                if (permissionCheck == PERMISSION_DENIED) {
                    ActivityCompat.requestPermissions(AddSmoverActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE);
                }
                //권한이 있는 경우
                else if (permissionCheck == PERMISSION_GRANTED) {
                    DoCropImage();
                }
            }
        });

        // 주소 찾기 버튼 리스너
        Button btn_addr = (Button) findViewById(R.id.btn_add_addr);
        btn_addr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // WebView 초기화
                init_webView();

                //이전에 입력했던 것 초기화
                tv_addr_result.setText("");
                edit_dailog_addr.setText("");

                dialog_addr.show();
            }
        });

        // 기기 추가 확인 버튼 리스너
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit_no = (EditText) findViewById(R.id.edit_add_no);
                EditText edit_name = (EditText) findViewById(R.id.edit_add_name);
                EditText edit_addr1 = (EditText) findViewById(R.id.edit_add_addr1);
                EditText edit_addr2 = (EditText) findViewById(R.id.edit_add_addr2);

                final String no = edit_no.getText().toString();
                final String name = edit_name.getText().toString();
                String addr1 = edit_addr1.getText().toString();
                String addr2 = edit_addr2.getText().toString();
                final String addr = addr1 + ", " + addr2;

                if (image_pick.getDrawable() == null) {
                    Toast.makeText(AddSmoverActivity.this, "사진을 선택해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else if (no.equals("")) {
                    Toast.makeText(AddSmoverActivity.this, "기기 번호를 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else if (name.equals("")) {
                    Toast.makeText(AddSmoverActivity.this, "구역 이름을 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else if (addr1.equals("")) {
                    Toast.makeText(AddSmoverActivity.this, "구역 주소를 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else if (addr2.equals("")) {
                    Toast.makeText(AddSmoverActivity.this, "구역 상세 주소를 입력해주세요.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // 인터넷 연결 체크
                    try {
                        ConnectivityManager cm = (ConnectivityManager) AddSmoverActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo networkState = cm.getActiveNetworkInfo();
                        if (networkState != null && networkState.isConnected()) {
                            //Smover 정보 DB에 저장
                            new Thread() {
                                public void run() {
                                    AddSmoverCallback callback = new AddSmoverCallback(no);
                                    asHttp.requestWebServer(myPreferences.UserIdLoad(), no, name, addr, callback);
                                }
                            }.start();
                        } else {
                            Toast.makeText(AddSmoverActivity.this, "인터넷 연결을 확인하세요", Toast.LENGTH_LONG).show();
                        }
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //                                        #CallBack
    //----------------------------------------------------------------------------------------------
    //AddSmoverCallback
    private class AddSmoverCallback implements Callback {
        private String no = null;
        AddSmoverCallback(String no){ this.no = no; }

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
                            Toast.makeText(AddSmoverActivity.this, "기기 추가 등록 성공",
                                    Toast.LENGTH_SHORT).show();

                            //서버에 디렉터리 생성
                            new Thread() {
                                public void run() {
                                    MakeDirCallback callback = new MakeDirCallback(no);
                                    mdHttp.requestWebServer(myPreferences.UserIdLoad(), no, callback);
                                }
                            }.start();

                            //Smover 사진 파일 저장
                            SaveBitmapToJpeg(bitmap, "smover", "img_" + no);

                            Intent intent = new Intent();
                            intent.putExtra("add_msg", "성공");
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        case "0":
                            Toast.makeText(AddSmoverActivity.this, "기기 추가 등록에 실패하였습니다.",
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //MakeDirCallback
    private class MakeDirCallback implements Callback {
        private String no = null;

        MakeDirCallback(String no) {
            this.no = no;
        }

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
                    Log.d(TAG, "id: " + myPreferences.UserIdLoad() + "\nno: " + no + "\nfile_path: " + file_path);
                    switch (result) {
                        case "success":
                            //Smover 대표사진 서버에 업로드
                            Log.d(TAG, "id: " + myPreferences.UserIdLoad() + "\nno: " + no + "\nfile_path: " + file_path);
                            new ImageUploadTask().execute(myPreferences.UserIdLoad(), no, file_path, "smover");
                            Toast.makeText(AddSmoverActivity.this, "디렉터리 생성 성공\n경로: " + file_path,
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case "fail":
                            Toast.makeText(AddSmoverActivity.this, "디렉터리 생성 실패",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            Toast.makeText(AddSmoverActivity.this, "알 수 없는 에러 "+result,
                                    Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }
    }

    //----------------------------------------------------------------------------------------------
    //                                        #Dialog
    //----------------------------------------------------------------------------------------------
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
                    addr_basic = tv_addr_result.getText().toString();
                    addr_detail = edit_dailog_addr.getText().toString();

                    if (addr_basic.equals("")) {
                        Toast.makeText(AddSmoverActivity.this, "주소를 설정해주세요.",
                                Toast.LENGTH_SHORT).show();
                    }
                    //상세주소를 입력하지 않았을 경우
                    else if (addr_detail.equals("")) {
                        Toast.makeText(AddSmoverActivity.this, "상세 주소를 입력해주세요.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        /***
                         Toast.makeText(AddSmoverActivity.this, "기본: " + addr_basic + "\n상세: " + addr_detail,
                         Toast.LENGTH_SHORT).show();
                         ***/
                        edit_addr1.setText(addr_basic);
                        edit_addr2.setText(addr_detail);

                        dialog_addr.cancel();
                    }
                }
            });
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
        webView.addJavascriptInterface(new AndroidBridge(), "SmoverProject");
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
                Toast.makeText(AddSmoverActivity.this, "디렉터리 생성",
                        Toast.LENGTH_SHORT).show();
            }

            FileOutputStream fileOutputStream = new FileOutputStream(file_path);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);

            fileOutputStream.flush();
            fileOutputStream.close();

            Toast.makeText(AddSmoverActivity.this, "이미지 저장 성공",
                    Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_IMAGE_PICK:
                File tempFile = GetTempFile();
                if (tempFile.exists()) {
                    try {
                        String img_absolute_path = tempFile.getAbsolutePath();  //절대 경로 구하기
                        //Bitmap bitmap = BitmapFactory.decodeFile(img_absolute_path);  //비트맵 파일 생성
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), temp_image_uri);
                        image_pick.setImageBitmap(bitmap);  //불러온 이미지 화면에 적용

                        image_pick.getLayoutParams().height = 1;
                        image_pick.requestLayout();
                    } catch (Exception e) {
                        Toast.makeText(AddSmoverActivity.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(AddSmoverActivity.this, "권한 부여 성공", Toast.LENGTH_SHORT).show();
                    DoCropImage();  //앨범 불러오기
                } else {
                    Toast.makeText(AddSmoverActivity.this, "권한 부여 실패", Toast.LENGTH_SHORT).show();
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
        View actionbar = inflater.inflate(R.layout.custom_actionbar_add_smover, null);

        actionBar.setCustomView(actionbar);

        //액션바 양쪽 공백 없애기
        Toolbar parent = (Toolbar) actionbar.getParent();
        parent.setContentInsetsAbsolute(0, 0);

        //------------------------------
        //   뒤로가기 버튼 리스너
        //------------------------------
        ImageButton btn = (ImageButton) this.findViewById(R.id.btn_add_smover_back);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        return true;
    }
}