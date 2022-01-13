package com.example.project.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project.Activity.MyGaericatureFull;
import com.example.project.Activity.MyPagePurchaseAllHistory;
import com.example.project.Activity.MyPagePurchaseCompleteHistory;
import com.example.project.Activity.MyPagePurchaseDeliveringHistory;
import com.example.project.Adapter.MyGaericatureAdapter;
import com.example.project.ExpandableHeightGridView;
import com.example.project.R;
import com.example.project.RbPreference;
import com.example.project.VO.MyGaericatureVO;
import com.example.project.VO.itemVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyPage extends Fragment {

    ImageView imgProfile;
    TextView tvNickname,
            tvPurchaseAll, tvPurchaseAllNum,
            tvPurchaseDelivering, tvPurchaseDeliveringNum,
            tvPurchaseComplete, tvPurchaseCompleteNum;
    ExpandableHeightGridView myPageGridView;
    MyGaericatureAdapter adapter;
    ArrayList<MyGaericatureVO> data = new ArrayList<>();
    View viewPurchaseAll, viewPurchaseDelivering, viewPurchaseComplete;
    Bitmap profile;
    String nick;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_mypage, container, false);

        imgProfile = fragment.findViewById(R.id.imgProfile);
        tvNickname = fragment.findViewById(R.id.tvNickname);
        tvPurchaseAll = fragment.findViewById(R.id.tvPurchaseAll);
        tvPurchaseAllNum = fragment.findViewById(R.id.tvPurchaseAllNum);
        tvPurchaseDelivering = fragment.findViewById(R.id.tvPurchaseDelivering);
        tvPurchaseDeliveringNum = fragment.findViewById(R.id.tvPurchaseDeliveringNum);
        tvPurchaseComplete = fragment.findViewById(R.id.tvPurchaseComplete);
        tvPurchaseCompleteNum = fragment.findViewById(R.id.tvPurchaseCompleteNum);

        viewPurchaseAll = fragment.findViewById(R.id.viewPurchaseAll);
        viewPurchaseDelivering = fragment.findViewById(R.id.viewPurchaseDelivering);
        viewPurchaseComplete = fragment.findViewById(R.id.viewPurchaseComplete);

        imgProfile.setBackground(new ShapeDrawable(new OvalShape()));
        imgProfile.setClipToOutline(true);

        tvPurchaseAll.bringToFront();
        tvPurchaseAllNum.bringToFront();
        tvPurchaseDelivering.bringToFront();
        tvPurchaseDeliveringNum.bringToFront();
        tvPurchaseComplete.bringToFront();
        tvPurchaseCompleteNum.bringToFront();

        myPageGridView = fragment.findViewById(R.id.myPageGrid);

//        세션에서 아이디 가져오기
        RbPreference pref = new RbPreference(getActivity().getApplicationContext());
        String user_id = pref.getValue("user_id", null);

        Log.d("session", user_id);

        tvNickname.setText(user_id);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("user_id", user_id).build();
        String url = "http://172.30.1.12:5000/mygaericature";
        Request request = new Request.Builder().url(url)
                                               .addHeader("Connection","close")
                                               .post(body).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                 e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response.body().string());
                    JSONArray jsonArray = jsonObject.getJSONArray("gaericature");
                    data = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++) {

                        byte[] b = new byte[0];
                        try {
                            b = Base64.decode(jsonArray.get(i).toString(), Base64.DEFAULT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Bitmap img = BitmapFactory.decodeByteArray(b, 0, b.length);
                        data.add(new MyGaericatureVO(img));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter = new MyGaericatureAdapter(getActivity().getApplicationContext(), R.layout.gaericaturelist, data);
                MyPageThread myPageThread = new MyPageThread(adapter);
                myPageThread.start();

                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("profileimg");
                    byte[] b = Base64.decode(jsonArray.get(0).toString(), Base64.DEFAULT);
                    profile = BitmapFactory.decodeByteArray(b, 0, b.length);

                    jsonArray = jsonObject.getJSONArray("nick");
                    nick = jsonArray.get(0).toString();

                    ProfileThread profileThread = new ProfileThread(profile, nick);
                    profileThread.start();


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

//        OkHttpClient client2 = new OkHttpClient();
//
//        RequestBody body2 = new FormBody.Builder()
//                .add("user_id", user_id).build();
//        String url2 = "http://172.30.1.12:5000/loadprofile";
//        Request request2 = new Request.Builder().url(url2)
//                                                .addHeader("Connection", "close")
//                                                .post(body2).build();
//
//        client2.newCall(request2).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                InputStream inputStream = response.body().byteStream();
//                profile = BitmapFactory.decodeStream(inputStream);
//                ProfileThread profileThread = new ProfileThread(profile);
//                profileThread.start();
//            }
//        });

        tvPurchaseAllNum.bringToFront();
        tvPurchaseDeliveringNum.bringToFront();
        tvPurchaseCompleteNum.bringToFront();

        viewPurchaseAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), MyPagePurchaseAllHistory.class);
                intent.putExtra("PurchaseAllNum", String.valueOf(tvPurchaseAllNum.getText()));
                startActivity(intent);
            }
        });

        viewPurchaseDelivering.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyPagePurchaseDeliveringHistory.class);
                intent.putExtra("PurchaseDeliveringNum", String.valueOf(tvPurchaseDeliveringNum.getText()));
                startActivity(intent);
            }
        });

        viewPurchaseComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyPagePurchaseCompleteHistory.class);
                intent.putExtra("PurchaseCompleteNum", String.valueOf(tvPurchaseCompleteNum.getText()));
                startActivity(intent);
            }
        });

        myPageGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                ByteArrayOutputStream stream = new ByteArrayOutputStream();

                Bitmap bitmap = data.get(i).getImg();
                float scale = (float) (1024/(float)bitmap.getWidth());
                int image_w = (int) (bitmap.getWidth() * scale);
                int image_h = (int) (bitmap.getHeight() * scale);
                Bitmap resize = Bitmap.createScaledBitmap(bitmap, image_w, image_h, true);
                resize.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Intent intent = new Intent(getActivity().getApplicationContext(), MyGaericatureFull.class);
                intent.putExtra("image", byteArray);
                startActivity(intent);

                getActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
            }
        });

        return fragment;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            myPageGridView.setExpanded(true);
            myPageGridView.setAdapter(adapter);
        }
    };

    class MyPageThread extends Thread{
        MyGaericatureAdapter adapter;

        public MyPageThread(MyGaericatureAdapter adapter){
            this.adapter = adapter;
        }

        @Override
        public void run() {
            Message message = new Message();
            handler.sendMessage(message);
        }
    }

    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            imgProfile.setImageBitmap(profile);
            tvNickname.setText(nick);
        }
    };

    class ProfileThread extends Thread{

        Bitmap mProfile;
        String nick;

        public ProfileThread(Bitmap profile, String nick) {
            mProfile = profile;
            this.nick = nick;
        }

        @Override
        public void run() {
            Message message = new Message();
            handler2.sendMessage(message);
        }
    }
}
