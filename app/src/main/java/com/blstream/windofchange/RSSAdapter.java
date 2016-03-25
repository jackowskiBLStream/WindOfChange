package com.blstream.windofchange;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RSSAdapter extends RecyclerView.Adapter<RSSAdapter.RSSViewHolder> implements Parcelable {
    protected IRecyclerViewPositionHelper listener;
    static List<Thread> currentThreadsInAsyncTask = new ArrayList<>();


    private List<RSSInfo> rssList;

    public RSSAdapter(List<RSSInfo> rssList) {
        this.rssList = rssList;
    }

    protected RSSAdapter(Parcel in) {
        rssList = in.createTypedArrayList(RSSInfo.CREATOR);
    }

    public static final Creator<RSSAdapter> CREATOR = new Creator<RSSAdapter>() {
        @Override
        public RSSAdapter createFromParcel(Parcel in) {
            return new RSSAdapter(in);
        }

        @Override
        public RSSAdapter[] newArray(int size) {
            return new RSSAdapter[size];
        }
    };

    public static boolean areAllThreadsFinished(List<Thread> threadsList) {
        for (Thread thread : threadsList) {
            if (thread.isAlive())
                return false;
        }
        return true;
    }

    public void setListener(IRecyclerViewPositionHelper listener) {
        this.listener = listener;
    }

    @Override
    public RSSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.single_item_layout, parent, false);

        return new RSSViewHolder(itemView, listener);
    }

    @Override
    public void onBindViewHolder(RSSViewHolder holder, int position) {
        RSSInfo RSSInfo = rssList.get(position);
        new ImageLoadTask(holder.mImage, RSSInfo.image).execute();
        holder.mPubDate.setText(RSSInfo.pubDate);
        holder.mDescription.setText(RSSInfo.description);
        holder.mTitle.setText(RSSInfo.title);

    }

    @Override
    public int getItemCount() {
        return rssList.size();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(rssList);
    }

    public class RSSViewHolder extends RecyclerView.ViewHolder {
        protected ImageView mImage;
        protected TextView mTitle;
        protected TextView mDescription;
        protected TextView mPubDate;

        public RSSViewHolder(View v, final IRecyclerViewPositionHelper listener) {
            super(v);
            mImage = (ImageView) v.findViewById(R.id.image);
            mTitle = (TextView) v.findViewById(R.id.title);
            mDescription = (TextView) v.findViewById(R.id.description);
            mPubDate = (TextView) v.findViewById(R.id.pubDate);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onListen(getAdapterPosition());
                    }
                }
            });
        }
    }

    public class ImageLoadTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView image;
        private String url;


        public ImageLoadTask(ImageView image, String url) {
            this.image = image;
            this.url = url;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Log.d("Current Thread Asy Id: ", String.valueOf(Thread.currentThread().getId()));
                currentThreadsInAsyncTask.add(Thread.currentThread());

                return BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            image.setImageBitmap(result);
            cancel(true);
        }
    }


}
