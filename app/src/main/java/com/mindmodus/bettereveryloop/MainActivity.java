package com.mindmodus.bettereveryloop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindmodus.bettereveryloop.interfaces.RedditBetterEveryLoopApi;
import com.mindmodus.bettereveryloop.models.Gfycat;
import com.mindmodus.bettereveryloop.models.GfycatGifData;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    CardStackView cardStackView;
    GifCardAdapter adapter;
    boolean gifState = false;
    Observable<Boolean> myObservable;
    TextView count;
    TextView comeBack;
    int gifCount = 20;
    PendingIntent broadcast;
    AlarmManager alarmManager;
    Retrofit retrofit;
    ContentLoadingProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);

        adapter = new GifCardAdapter(getApplicationContext());

        loadingProgressBar = findViewById(R.id.loadingBar);
        cardStackView = findViewById(R.id.gifCardStack);
        count = findViewById(R.id.gifCount);
        comeBack = findViewById(R.id.comeBack);
        comeBack.setVisibility(View.GONE);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(this, AlarmReceiver.class);
        broadcast = PendingIntent.getBroadcast(this, 100, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gfycat.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();

        if (getTimeState("timeState", this)) {
            retrofit.create(RedditBetterEveryLoopApi.class).getGifs()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResults, this::handleErrors);
        } else {
            count.setText(String.valueOf(0));
            comeBack.setText("Come Back Tomorrow on 8am");
            comeBack.setVisibility(View.VISIBLE);
            loadingProgressBar.setVisibility(View.GONE);
        }

        myObservable = Observable.create(emitter -> {
            emitter.onNext(gifState);
            emitter.onComplete();
        });

    }

    private void handleResults(Gfycat gfycat) {
        adapter.addAll(gfycat.getGfycats());
        cardStackView.setAdapter(adapter);
        setup();
        loadingProgressBar.hide();
    }

    private void handleErrors(Throwable throwable) {
        Log.e("Observer", throwable.toString());
    }

    private void setup() {
        cardStackView.setCardEventListener(new CardStackView.CardEventListener() {
            @Override
            public void onCardDragging(float percentX, float percentY) {
                Log.d("CardStackView", "onCardDragging");
            }

            @Override
            public void onCardSwiped(SwipeDirection direction) {
                Log.d("CardStackView", "onCardSwiped: " + direction.toString());
                Log.d("CardStackView", "topIndex: " + cardStackView.getTopIndex());
                if (cardStackView.getTopIndex() == adapter.getCount() - 5) {
                    Log.d("CardStackView", "Paginate: " + cardStackView.getTopIndex());
                    paginate();
                }
                gifCount--;
                count.setText(String.valueOf(gifCount));
                if (gifCount == 0) {
                    setTimeState("timeState", false, MainActivity.this);
                    scheduleNotification();
                    comeBack.setText("You seen all the gifs for today");
                    comeBack.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCardReversed() {
                Log.d("CardStackView", "onCardReversed");
            }

            @Override
            public void onCardMovedToOrigin() {
                Log.d("CardStackView", "onCardMovedToOrigin");
            }

            @Override
            public void onCardClicked(int index) {
                Log.d("CardStackView", "onCardClicked: " + index);
                gifState = true;
                myObservable.subscribe(adapter.myObserver);
            }
        });
    }

    private void paginate() {
        cardStackView.setPaginationReserved();
        adapter.notifyDataSetChanged();
    }

    private void scheduleNotification() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        cal.add(Calendar.SECOND, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
    }

    public boolean getTimeState(String key, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(key, true);
    }

    public void setTimeState(String key, boolean state, Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, state);
        editor.commit();
    }

}
