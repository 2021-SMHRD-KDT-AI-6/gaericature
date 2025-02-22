package com.example.project.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethod;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.project.Activity.MyGaericatureFull;
import com.example.project.Activity.MyPagePurchaseAllHistory;
import com.example.project.Activity.MyPagePurchaseCompleteHistory;
import com.example.project.Activity.MyPagePurchaseDeliveringHistory;
import com.example.project.Activity.ProfilePopup;
import com.example.project.Adapter.MyGaericatureAdapter;
import com.example.project.ExpandableHeightGridView;
import com.example.project.Loading2;
import com.example.project.R;
import com.example.project.RbPreference;
import com.example.project.VO.MyGaericatureVO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyPage extends Fragment {

    ImageView imgProfile, imgCorr;
    TextView tvNickname,
            tvPurchaseAll, tvPurchaseAllNum,
            tvPurchaseDelivering, tvPurchaseDeliveringNum,
            tvPurchaseComplete, tvPurchaseCompleteNum;
    ExpandableHeightGridView myPageGridView;
    MyGaericatureAdapter adapter;
    ArrayList<MyGaericatureVO> data = new ArrayList<>();
    Bitmap profile;
    String nick, cart, allNum, ingNum, comNum;
    Loading2 loading2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View fragment = inflater.inflate(R.layout.fragment_mypage, container, false);



        imgProfile = fragment.findViewById(R.id.imgProfile);
        imgCorr = fragment.findViewById(R.id.imgCorr);
        tvNickname = fragment.findViewById(R.id.tvNickname);
        tvPurchaseAll = fragment.findViewById(R.id.tvPurchaseAll);
        tvPurchaseAllNum = fragment.findViewById(R.id.tvPurchaseAllNum);
        tvPurchaseDelivering = fragment.findViewById(R.id.tvPurchaseDelivering);
        tvPurchaseDeliveringNum = fragment.findViewById(R.id.tvPurchaseDeliveringNum);
        tvPurchaseComplete = fragment.findViewById(R.id.tvPurchaseComplete);
        tvPurchaseCompleteNum = fragment.findViewById(R.id.tvPurchaseCompleteNum);


        imgProfile.setBackground(new ShapeDrawable(new OvalShape()));
        imgProfile.setClipToOutline(true);

        myPageGridView = fragment.findViewById(R.id.myPageGrid);

        imgCorr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), ProfilePopup.class);
                startActivity(intent);
            }
        });

        loading2 = new Loading2(fragment.getContext());
        loading2.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loading2.setCancelable(false);
        loading2.show();

//        세션에서 아이디 가져오기
        RbPreference pref = new RbPreference(getActivity().getApplicationContext());
        String url = pref.getValueUrl("url", null);
        String user_id = pref.getValue("user_id", null);

        Log.d("session", user_id);

        tvNickname.setText(user_id);

        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add("user_id", user_id).build();
        Request request = new Request.Builder().url(url + "/mygaericature")
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
                    JSONArray jsonArray2 = jsonObject.getJSONArray("charlist");
                    data = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++) {

                        byte[] b = new byte[0];
                        try {
                            b = Base64.decode(jsonArray.get(i).toString(), Base64.DEFAULT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Bitmap img = BitmapFactory.decodeByteArray(b, 0, b.length);
                        MyGaericatureVO vo = new MyGaericatureVO();
                        vo.setDeep_seq((Integer) jsonArray2.getJSONArray(i).get(2));
                        vo.setCharNick((String) jsonArray2.getJSONArray(i).get(1));
                        vo.setImg(img);
                        data.add(vo);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                adapter = new MyGaericatureAdapter(getActivity().getApplicationContext(), R.layout.gaericaturelist, data);
                MyPageThread myPageThread = new MyPageThread(adapter);
                myPageThread.start();

                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("profileimg");
                    JSONArray jsonArray2 = jsonObject.getJSONArray("cart_count");
                    byte[] b = Base64.decode(jsonArray.get(0).toString(), Base64.DEFAULT);
                    profile = BitmapFactory.decodeByteArray(b, 0, b.length);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(getResources(), R.id.imgProfile, options);

                    options.inSampleSize = calculateInSampleSize(options, 100, 100);

                    jsonArray = jsonObject.getJSONArray("nick");
                    nick = jsonArray.get(0).toString();
                    cart = jsonArray2.get(0).toString();

                    JSONArray jsonArray3 = jsonObject.getJSONArray("pur_count");
                    allNum = jsonArray3.get(0).toString();
                    ingNum = jsonArray3.get(1).toString();
                    comNum = jsonArray3.get(2).toString();

                    ProfileThread profileThread = new ProfileThread(profile, nick, cart);
                    profileThread.start();
                    loading2.dismiss();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        tvPurchaseAllNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), MyPagePurchaseAllHistory.class);
                intent.putExtra("PurchaseAllNum", String.valueOf(tvPurchaseAllNum.getText()));
                startActivity(intent);
            }
        });

        tvPurchaseDeliveringNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MyPagePurchaseDeliveringHistory.class);
                intent.putExtra("PurchaseDeliveringNum", String.valueOf(tvPurchaseDeliveringNum.getText()));
                startActivity(intent);
            }
        });

        tvPurchaseCompleteNum.setOnClickListener(new View.OnClickListener() {
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
                intent.putExtra("nick", data.get(i).getCharNick());
                intent.putExtra("deep_seq", data.get(i).getDeep_seq());
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
            tvPurchaseAllNum.setText(allNum);
            tvPurchaseDeliveringNum.setText(ingNum);
            tvPurchaseCompleteNum.setText(comNum);
        }
    };

    class ProfileThread extends Thread{

        Bitmap mProfile;
        String nick;
        String cart;

        public ProfileThread(Bitmap profile, String nick, String cart) {
            mProfile = profile;
            this.nick = nick;
            this.cart = cart;
        }

        @Override
        public void run() {
            Message message = new Message();
            handler2.sendMessage(message);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}
