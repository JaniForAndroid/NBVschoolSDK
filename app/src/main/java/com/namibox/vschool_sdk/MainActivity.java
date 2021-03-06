package com.namibox.vschool_sdk;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.namibox.wangxiao.bean.VsCacheEntity;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import sdk.NamiboxSDK;
import sdk.callback.NBLoginCallback;
import sdk.callback.NBResultCallback;
import sdk.model.NamiboxLoginModel;
import support.namibox.commonlib.common.ApiHandler;
import support.namibox.commonlib.dialog.DialogUtil;

public class MainActivity extends AppCompatActivity {

  private String scheduleId;
  private List<String> spinnerData;
  private List<VsCacheEntity> scheduleList;
  private ArrayAdapter<String> adapter;
  private String token;
  private Spinner spinner;
  private EditText et_token_phone;
  private EditText et_token_pwd;
  private EditText et_token_code;
  private String tokenAppCode;
  private String tokenPwd;
  private String tokenPhone;
  private String time;
  private boolean is_debug;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    et_token_phone = findViewById(R.id.et_token_phone);
    et_token_pwd = findViewById(R.id.et_token_pwd);
    et_token_code = findViewById(R.id.et_token_code);
    Switch aSwitch = findViewById(R.id.switcher);
    is_debug = BuildConfig.DEBUG
        && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("is_debug", true);
    aSwitch.setChecked(!is_debug);
    aSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          //????????????
          PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
              .edit().putBoolean("is_debug", false).apply();
        } else {
          //????????????
          PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
              .edit().putBoolean("is_debug", true).apply();
        }

        DialogUtil.showButtonDialog2(MainActivity.this, "??????", "????????????????????????APP", "??????",
            v -> System.exit(0), "",null,"",null,null);
      }
    });
    initRv();
    initSpinnner();
  }

  private void initRv() {
    List<String> items = new ArrayList<>();
    items.add("step1:??????token");
    items.add("step2:???????????????");
    items.add("step3:????????????");
    items.add("step4:????????????(?????????loading)");
    items.add("step4:????????????(?????????loading)");
    items.add("other:?????????????????????");

    RecyclerView recyclerView = findViewById(R.id.rv);
    recyclerView.setAdapter(new BaseQuickAdapter(R.layout.item_string, items) {
      @Override
      protected void convert(@NonNull BaseViewHolder baseViewHolder, final Object o) {
        baseViewHolder.setText(R.id.tv_name, (CharSequence) o);
        baseViewHolder.itemView.setOnClickListener(view -> {
          if ("step1:??????token".equals(o)) {
            getToken();
          } else if ("step2:???????????????".equals(o)) {
            NBLogin();
          } else if ("step3:????????????".equals(o)) {
            getScheduleList();
          } else if ("step4:????????????(?????????loading)".equals(o)) {
            if (TextUtils.isEmpty(scheduleId) || TextUtils.equals(scheduleId, "??????????????????id")
                || TextUtils.equals(scheduleId, "??????????????????")) {
              Toast.makeText(mContext, "??????????????????", Toast.LENGTH_SHORT).show();
              return;
            }
            new NamiboxSDK().enterClass(MainActivity.this, Long.parseLong(scheduleId),time, R.mipmap.ic_launcher,true,
                new NBResultCallback() {
                  @Override
                  public void onBefore() {
                    super.onBefore();
                    DialogUtil.showProgress(MainActivity.this, "?????????loading");
                  }

                  @Override
                  public void onSuccess() {
                    DialogUtil.hideProgress();
                  }

                  @Override
                  public void onFail(Throwable t) {
                    DialogUtil.hideProgress();
                    Toast.makeText(mContext, "??????????????????????????????????????????" + t.getMessage(), Toast.LENGTH_SHORT)
                        .show();
                  }
                });
          } else if ("step4:????????????(?????????loading)".equals(o)) {
            if (TextUtils.isEmpty(scheduleId) || TextUtils.equals(scheduleId, "??????????????????id")
                || TextUtils.equals(scheduleId, "??????????????????")) {
              Toast.makeText(mContext, "??????????????????", Toast.LENGTH_SHORT).show();
              return;
            }
            new NamiboxSDK().enterClass(MainActivity.this, Long.parseLong(scheduleId), time,R.mipmap.ic_launcher, null);
          } else if ("other:?????????????????????".equals(o)) {
            new NamiboxSDK().uploadLog(MainActivity.this);
          }
        });
      }
    });
  }

  private void initSpinnner() {
    spinner = findViewById(R.id.spinner);
    spinnerData = new ArrayList<>();
    spinnerData.add("??????????????????id");

    adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item, spinnerData);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setDropDownVerticalOffset(dp2px(this, 50));
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = spinnerData.get(position);
        String[] strs = text.split("_");
        scheduleId = strs[strs.length - 1];
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });
  }

  private void NBLogin() {
    EditText et_phone = findViewById(R.id.et_phone);
    NamiboxLoginModel model = new NamiboxLoginModel();
    if (TextUtils.isEmpty(tokenAppCode)) {
      tokenAppCode = "427542539";
    }
    model.setApp_code(tokenAppCode);
    String phone = et_phone.getText() == null ? "" : et_phone.getText().toString();
    if (TextUtils.isEmpty(phone)) {
      phone = "16601000001";
    }
    model.setPhone_enc(Base64.encodeToString(phone.getBytes(), Base64.NO_WRAP));
    if (TextUtils.isEmpty(token)) {
      Toast.makeText(this, "????????????token", Toast.LENGTH_SHORT).show();
      return;
    }
    model.setToken(token);
    new NamiboxSDK().login(MainActivity.this, model,  new NBLoginCallback() {

      @Override
      public void onSuccess() {
        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
      }

      @Override
      public void onFail(Throwable t) {
        Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void getToken() {
    if (!TextUtils.isEmpty(et_token_phone.getText())) {
      tokenPhone = et_token_phone.getText().toString();
    } else {
      tokenPhone = "16602115910";
    }
    if (!TextUtils.isEmpty(et_token_pwd.getText())) {
      tokenPwd = et_token_pwd.getText().toString();
    } else {
      tokenPwd = "123456";
    }
    if (!TextUtils.isEmpty(et_token_code.getText())) {
      tokenAppCode = et_token_code.getText().toString();
    } else {
      tokenAppCode = "427542539";
    }

    String url = getCloudBaseUrl()+"partner/login/";
    JsonObject param = new JsonObject();
    param.addProperty("login_type", "normal_login");
    param.addProperty("username", tokenPhone);
    param.addProperty("password", tokenPwd);

    ApiHandler.getBaseApi().commonJsonObjectPost(url, param)
        .flatMap((Function<JsonObject, ObservableSource<JsonObject>>) jsonObject -> {
          String retcode = jsonObject.get("retcode").getAsString();
          Log.d("MainActivity", "??????token??????????????????" + jsonObject);
          if (TextUtils.equals(retcode, "success")) {
            String sessionId = jsonObject.get("data").getAsJsonObject().get("session_id").getAsString();
            String cookie = "sessionid="+sessionId+"; Domain=.namibox.com; Path=/";
            syncCookie(getCloudBaseUrl(), cookie);
            String tokenUrl = getCloudBaseUrl()+"auth/test-get-access-token?app_code="+tokenAppCode;
            return ApiHandler.getBaseApi().commonJsonObjectPost(tokenUrl, new JsonObject());
          } else {
            return Observable.empty();
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          String retcode = jsonObject.get("retcode").getAsString();
          if (TextUtils.equals(retcode, "success")) {
            token = jsonObject.get("data").getAsJsonObject().get("access_token").getAsString();
            Log.d("MainActivity", "token>>>>>>" + token);
            Toast.makeText(MainActivity.this, "??????token??????", Toast.LENGTH_SHORT).show();
          } else {
            Log.e("MainActivity", "??????token??????:" + jsonObject.toString());
            Toast.makeText(MainActivity.this, "??????token??????", Toast.LENGTH_SHORT).show();
          }
        }, throwable -> {
          Log.e("MainActivity", "??????token??????:"+throwable.getMessage());
          Toast.makeText(MainActivity.this, "??????token??????", Toast.LENGTH_SHORT).show();
        }).isDisposed();
  }

  public static void syncCookie(String url, String cookie) {
    CookieManager cookieManager = CookieManager.getInstance();
    Log.d("NetworkUtil", "syncCookie: " + url + "\ncookie: " + cookie);
    try {
      cookieManager.setCookie(url, cookie);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void getScheduleList() {
    long userid = getSharedPreferences(getPackageName() + "_preferences", Context.MODE_PRIVATE)
        .getLong("user_id", -1);
    String user_id = String.valueOf(userid);
    if (TextUtils.equals("0", user_id)) {
      Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();
      return;
    }

    EditText et_date = findViewById(R.id.et_date);
    time = getGMT8DawnString(new Date());
    if (!TextUtils.isEmpty(et_date.getText())) {
      time = convertGMT8String(et_date.getText().toString());
    }

    DialogUtil.showProgress(this, "????????????????????????...");
    String url = String.format(Locale.US,
        getBaseHttpsUrl()+"/btob/vschool_class/student_class_schedule?user_id=%s&start_at=%s&days=%d",
        user_id, time, 30);
    ApiHandler.getBaseApi().commonJsonGet(url)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          DialogUtil.hideProgress();
          String retCode = jsonObject.get("retcode").getAsString();
          if (TextUtils.equals(retCode, "SUCC")) {
            Gson gson = new GsonBuilder()
                .registerTypeAdapter(Timestamp.class,
                    new GMT8DateTypeAdapter("yyyy-MM-dd HH:mm:ss"))
                .create();
            JsonObject data = jsonObject.get("data").getAsJsonObject();
            JsonArray dayArray = data.get("list").getAsJsonArray();
            scheduleList = new ArrayList<>();

            for (JsonElement dayData : dayArray) {
              String date = dayData.getAsJsonObject().get("date").getAsString();
              JsonArray dayScheduleArray = dayData.getAsJsonObject().get("schedules")
                  .getAsJsonArray();
              for (JsonElement scheduleJson : dayScheduleArray) {
                VsCacheEntity schedule = gson.fromJson(scheduleJson, VsCacheEntity.class);
                schedule.date = date;
//                if (isTodaySchedule(schedule)) {
                scheduleList.add(schedule);
//                }
              }
            }
            parsingSpinnerData();
          } else {
            DialogUtil.hideProgress();
            String des = jsonObject.get("description").getAsString();
            Toast.makeText(this, des, Toast.LENGTH_SHORT).show();
          }
        }, throwable -> {
          DialogUtil.hideProgress();
          Toast.makeText(this, "????????????????????????", Toast.LENGTH_SHORT).show();
        }).isDisposed();
  }

  private void parsingSpinnerData() {
    spinnerData.clear();
    spinnerData.add("??????????????????");
    for (VsCacheEntity vsCacheEntity : scheduleList) {
      spinnerData.add(vsCacheEntity.class_status + "_" + vsCacheEntity.schedule_id);
    }
    adapter.notifyDataSetChanged();
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        spinner.performClick();
      }
    }, 500);
  }

  private boolean isTodaySchedule(VsCacheEntity schedule) {
    long scheduleTime = getBlurTimeMillis(schedule.date);
    long dawnTimeMillis = getDawnTimeMillis();
    return scheduleTime == dawnTimeMillis;
  }

  public static int dp2px(Context var0, float var1) {
    float var2 = var0.getResources().getDisplayMetrics().density;
    return (int) (var1 * var2 + 0.5F);
  }

  public static String getGMT8DawnString(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
    return simpleDateFormat.format(calendar.getTime());
  }

  public static String convertGMT8String(String time) {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
    try {
      Date date = simpleDateFormat.parse(time);
      SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
      return simpleDateFormat2.format(date);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return time;
  }

  public static long getBlurTimeMillis(String date) {
    if (TextUtils.isEmpty(date)) {
      return System.currentTimeMillis();
    }
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      long timeMillis = simpleDateFormat.parse(date).getTime();
      return timeMillis / 1000 * 1000;
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return 0;
  }

  /**
   * ?????????????????????
   */
  public static long getDawnTimeMillis() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    return calendar.getTimeInMillis() / 1000 * 1000;
  }

  public  String getCloudBaseUrl() {
    String env = is_debug ? "mcloudw.namibox.com" : "mcloud.namibox.com";
    return  String.format(Locale.US, "https://%s/", env);
  }

  public String getBaseHttpsUrl() {
    if (is_debug) {
      return "https://wweb.namibox.com";
    } else {
      return "https://namibox.com";
    }
  }
}