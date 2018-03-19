package com.mindmodus.bettereveryloop;


import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.mindmodus.bettereveryloop.models.GfycatGifData;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class GifCardAdapter extends ArrayAdapter<GfycatGifData>{

    Observer myObserver;
    ControllerListener controllerListener;

    static class ViewHolder {
        @BindView(R.id.title)
        TextView name;

        @BindView(R.id.gif)
        SimpleDraweeView gifHolder;

        public ViewHolder(View contentView) {
            ButterKnife.bind(this, contentView);
        }
    }

    public GifCardAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(final int position, View contentView, final ViewGroup parent) {
        GfycatGifData gif = getItem(position);

        final ViewHolder holder;
        if (contentView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            contentView = inflater.inflate(R.layout.gif_card_view, parent, false);
            holder = new ViewHolder(contentView);
            contentView.setTag(holder);
        } else {
            holder = (ViewHolder) contentView.getTag();
        }

        holder.name.setText(gif.getTitle());

        Uri uri = Uri.parse(gif.getUrl());
        setControllerListener(position);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setControllerListener(controllerListener)
                .setAutoPlayAnimations(false)
                .build();
        holder.gifHolder.setController(controller);

        return contentView;
    }

    public void setControllerListener(final int position) {
        controllerListener = new BaseControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
                super.onSubmit(id, callerContext);
            }

            @Override
            public void onFinalImageSet(
                    String id,
                    @Nullable ImageInfo imageInfo,
                    @Nullable final Animatable anim) {
                if (anim != null) {
                    myObserver = new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(Boolean state) {
                            if (state) {
                                Log.d("position", "Position is" + position);
                                anim.start();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    };
                }
            }
        };
    }
}
