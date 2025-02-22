package com.example.project.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.project.Adapter.ImageSliderAdapter;
import com.example.project.Loading2;
import com.example.project.R;
import com.example.project.RbPreference;
import com.example.project.VO.itemVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PurchaseDetail extends AppCompatActivity {

    TextView tvName, tvPrice, tvPurCnt;
//    TextView tvContent;
    Button btnPurchase, btnCart, btnPurPlus, btnPurMinus;
    ImageView imgPurchase, imgDetail;
    itemVO item = new itemVO();
    Bitmap img1,img2;
    int seq;
    Loading2 loading2;
    ViewPager2 sliderViewPager;
    LinearLayout layoutIndicator;
    Bitmap[] images;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_detail);

//        tvContent=findViewById(R.id.tvContent);
        tvName=findViewById(R.id.tvName);
        tvPrice=findViewById(R.id.tvPrice);
        btnPurchase=findViewById(R.id.btnPurchase);
        btnCart=findViewById(R.id.btnCart);
        btnPurPlus=findViewById(R.id.btnPurPlus);
        btnPurMinus=findViewById(R.id.btnPurMinus);
//        imgPurchase=findViewById(R.id.imgPurchase);
        imgDetail=findViewById(R.id.imgDetail);

        tvPurCnt = findViewById(R.id.tvPurCnt);

        sliderViewPager = findViewById(R.id.imgPurchase);
        layoutIndicator = findViewById(R.id.layoutIndicators);

        tvPurCnt.bringToFront();

        RbPreference pref = new RbPreference(getApplicationContext());
        String url = pref.getValueUrl("url", null);

        loading2 = new Loading2(this);
        loading2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading2.setCancelable(false);
        loading2.show();

        // intent로 상품번호 가져오기
        Intent intent = getIntent();

        seq = intent.getIntExtra("seq", 0);

        OkHttpClient client = new OkHttpClient.Builder().build();


        RequestBody body = new FormBody.Builder()
                .add("seq",String.valueOf(seq))
                .build();

        Request request = new Request.Builder().url(url + "/itemdetail")
                .addHeader("Connection","close").post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                try {
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray1 = jsonObject.getJSONArray("string");
                    JSONArray jsonArray2 = jsonObject.getJSONArray("img");

                    Log.i("price :: ",String.valueOf((int) jsonArray1.get(1)));
                    Log.i("name :: ",(String) jsonArray1.get(0));
                    Log.i("content :: ",(String) jsonArray1.get(2));

                    images = new Bitmap[jsonArray2.length()-1];
                    for(int i = 0 ; i < jsonArray2.length()-1 ; i++){
                        byte[] b = Base64.decode(jsonArray2.get(i).toString(), Base64.DEFAULT);
                        img1 = BitmapFactory.decodeByteArray(b,0,b.length);
                        images[i] = img1;

                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sliderViewPager.setOffscreenPageLimit(1);
                            sliderViewPager.setAdapter(new ImageSliderAdapter(getApplicationContext(), images));

                            sliderViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                                @Override
                                public void onPageSelected(int position) {
                                    super.onPageSelected(position);
                                    setCurrentIndicator(position);
                                }
                            });
                            setupIndicators(images.length);
                        }
                    }, 0);


                    byte[] b1 = Base64.decode(jsonArray2.get(4).toString(), Base64.DEFAULT);
                    img2 = BitmapFactory.decodeByteArray(b1,0,b1.length);

                    item.setItem_price((int)jsonArray1.get(1));
                    item.setItem_name((String)jsonArray1.get(0));
                    item.setItem_content((String)jsonArray1.get(2));
                    item.setItem_pic1(images[0]);
                    item.setItem_pic2(img2);

                    Log.i("2price :: ",String.valueOf(item.getItem_price()));
                    Log.i("2name :: ",item.getItem_name());
                    Log.i("2content :: ",item.getItem_content());

//                    tvName.setText(item.getItem_name());
//                    tvContent.setText(item.getItem_content());
//                    tvPrice.setText(String.valueOf(item.getItem_price()));

                    MyThread myThread = new MyThread(tvName, tvPrice,img1,img2);
                    myThread.start();
                    loading2.dismiss();


                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        });


        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int cnt = Integer.parseInt((String) tvPurCnt.getText());

                Intent intent = new Intent(getApplicationContext(), PurchaseActivity.class);
                intent.putExtra("seq",seq);
                intent.putExtra("purchaseType","1");
                intent.putExtra("cnt", cnt);
                startActivity(intent);
            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String PurCnt = tvPurCnt.getText().toString();
                RbPreference pref = new RbPreference(getApplicationContext());
                String user_id = pref.getValue("user_id", null);


                OkHttpClient client = new OkHttpClient.Builder().build();

                RequestBody body = new FormBody.Builder()
                        .add("item_seq",String.valueOf(seq))
                        .add("item_cnt", PurCnt)
                        .add("user_id", user_id)
                        .build();

                Request request = new Request.Builder().url(url + "/cartadd")
                        .addHeader("Connection","close").post(body).build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String result = response.body().string();
                        if (result.equals("1")){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("ck", 1);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        });

        btnPurPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cnt = Integer.parseInt(tvPurCnt.getText().toString());
                cnt += 1;
                tvPurCnt.setText(String.valueOf(cnt));
            }
        });

        btnPurMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cnt = Integer.parseInt(tvPurCnt.getText().toString());
                if (cnt > 1) {
                    cnt -= 1;
                    tvPurCnt.setText(String.valueOf(cnt));
                }
            }
        });
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            tvName.setText(item.getItem_name());
//            tvContent.setText(item.getItem_content());
            tvPrice.setText(item.getItem_price()+"원");
//            imgPurchase.setImageBitmap(img1);
            imgDetail.setImageBitmap(img2);
//            imgDetail2.setImageBitmap(img2);
//            imgDetail3.setImageBitmap(img2);
//            imgDetail4.setImageBitmap(img2);

        }
    };

    class MyThread extends Thread {
        TextView tvName, tvPrice;
        Bitmap img1,img2;

        public MyThread(TextView tvName, TextView tvPrice, Bitmap img1, Bitmap img2){
            this.tvName = tvName;
            this.tvPrice = tvPrice;
            this.img1=img1;
            this.img2=img2;
        }

        @Override
        public void run() {
            Message message = new Message();
            handler.sendMessage(message);
        }
    }
    private void setupIndicators(int count) {
        ImageView[] indicators = new ImageView[count];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.setMargins(16, 8, 16, 8);

        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(this);
            indicators[i].setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.bg_indicator_inactive));
            indicators[i].setLayoutParams(params);
            layoutIndicator.addView(indicators[i]);
        }
        setCurrentIndicator(0);
    }

    private void setCurrentIndicator(int position) {
        int childCount = layoutIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutIndicator.getChildAt(i);
            if (i == position) {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_active
                ));
            } else {
                imageView.setImageDrawable(ContextCompat.getDrawable(
                        this,
                        R.drawable.bg_indicator_inactive
                ));
            }
        }
    }

}