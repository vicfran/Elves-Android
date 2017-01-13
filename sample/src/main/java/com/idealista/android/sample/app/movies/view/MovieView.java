package com.idealista.android.sample.app.movies.view;


import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.idealista.android.sample.R;
import com.idealista.android.sample.app.model.MovieModel;

public class MovieView extends LinearView<MovieModel> {

    private TextView textViewTitle;
    private OnMovieClickListener onMovieClickListener;

    public MovieView(Context context) {
        super(context);
    }

    public MovieView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MovieView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public MovieView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private OnClickListener getOnClickListener(final MovieModel movie) {
        return new OnClickListener() {

            @Override
            public void onClick(View view) {
                if (onMovieClickListener != null) {
                    onMovieClickListener.onClick(movie);
                }
            }
        };
    }

    @Override
    public void render(final MovieModel movie) {
        textViewTitle.setText(movie.getTitle());
        setOnClickListener(getOnClickListener(movie));
    }

    public void setOnMovieClickListener(OnMovieClickListener onMovieClickListener) {
        this.onMovieClickListener = onMovieClickListener;
    }

    @Override
    public void prepare() {
        textViewTitle = (TextView) findViewById(R.id.tvTitle);
    }

    @Override
    public int getLayoutId() {
        return R.layout.view_movie;
    }

    public interface OnMovieClickListener {
        void onClick(final MovieModel movieModel);
    }
}
