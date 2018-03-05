package com.mindmodus.bettereveryloop;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mindmodus.bettereveryloop.interfaces.RedditBetterEveryLoopApi;
import com.mindmodus.bettereveryloop.models.Gfycat;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.SwipeDirection;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    Gfycat data;
    CardStackView cardStackView;
    GifCardAdapter adapter;
    boolean gifState = false;
    Observable<Boolean> myObservable;
    TextView count;
    TextView comeBack;
    int gifCount = 20;
    SharedPreferences prefs;
    PendingIntent broadcast;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fresco.initialize(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        adapter = new GifCardAdapter(getApplicationContext());
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

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.gfycat.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RedditBetterEveryLoopApi apiEndpoint = retrofit.create(RedditBetterEveryLoopApi.class);

        final Call<Gfycat> leagues = apiEndpoint.getGifs();
        if (checkIfTimeIsUp()) {
            leagues.enqueue(new Callback<Gfycat>() {
                @Override
                public void onResponse(Call<Gfycat> call, Response<Gfycat> response) {
                    Log.d("apiCall", "success");
                    count.setText(String.valueOf(gifCount));
                    data = response.body();
                    adapter.addAll(data.getGfycats());
                    cardStackView.setAdapter(adapter);
                    setup();

                }

                @Override
                public void onFailure(Call<Gfycat> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }

            });
        }

        myObservable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                emitter.onNext(gifState);
                emitter.onComplete();
            }
        });

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
                setCount(gifCount);
                count.setText(String.valueOf(gifCount));
                if (gifCount == 0) {
                    scheduleNotificationo();
                    Timer _timer = new Timer();
                    _timer.schedule(new MyTimeTask(), 60000);
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


    public int getCount() {
        return prefs.getInt("count", (int) System.currentTimeMillis());
    }

    public void setCount(int count) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("count", count);
        editor.commit();
    }

    public boolean checkIfTimeIsUp() {
        if (getCount() == 20) {
            return true;
        } else {
            count.setText(String.valueOf(0));
            comeBack.setText("Come Back in " + "do minuta");
            comeBack.setVisibility(View.VISIBLE);
            return false;
        }
    }

    private void paginate() {
        cardStackView.setPaginationReserved();
        adapter.notifyDataSetChanged();
    }

    private void scheduleNotificationo() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 1);
        cal.add(Calendar.SECOND, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), broadcast);
    }

    private class MyTimeTask extends TimerTask {
        public void run() {
            Log.d("task", "run");
            setCount(20);
        }
    }

}
