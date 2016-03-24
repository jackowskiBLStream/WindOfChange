package com.blstream.windofchange;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class RSSPullService extends IntentService {
    private static final String TAG = "RSSPullService";
    private RSSInfo rssInfo;
    private String text;
    private List<RSSInfo> rssInfoList = new ArrayList<>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public RSSPullService() {
        super("RSSPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Started!");
        Intent localIntent;
        try {
            List<RSSInfo> checkList = downloadData(Constants.URL);
            if (checkList != null) {
                localIntent = new Intent(Constants.BROADCAST_ACTION)
                        // Puts the status into the Intent
                        .putParcelableArrayListExtra(Constants.EXTENDED_DATA_STATUS,
                                (ArrayList<? extends Parcelable>) checkList);
                sendBroadcast(localIntent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Broadcasts the Intent to receivers in this app.
        Log.d("RSSPullService: ", "onHandleIntent");
    }
    private List<RSSInfo> downloadData(String requestUrl) throws IOException {
        InputStream inputStream;
        HttpURLConnection urlConnection;

        /* forming th java.net.URL object */
        URL url = new URL(requestUrl);
        urlConnection = (HttpURLConnection) url.openConnection();

        /* for Get request */
        urlConnection.setRequestMethod("GET");
        int statusCode = urlConnection.getResponseCode();

        /* 200 represents HTTP OK */
        if (statusCode == 200) {
            inputStream = new BufferedInputStream(urlConnection.getInputStream());
            return parseResult(inputStream);
        } else {
            return null;
        }
    }
    private List<RSSInfo> parseResult(InputStream is) {
        boolean isItemStartTag = false;
        try {
            parseFromXML(is, isItemStartTag);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return rssInfoList;
    }

    private void parseFromXML(InputStream is, boolean isItemStartTag) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory;
        XmlPullParser parser;
        factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        parser = factory.newPullParser();

        parser.setInput(is, null);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagname = parser.getName();

            isItemStartTag = parseEachTag(parser, isItemStartTag, eventType, tagname);
            eventType = parser.next();
        }
    }

    private boolean parseEachTag(XmlPullParser parser, boolean isItemStartTag,
                                 int eventType, String tagname) {
        switch (eventType) {
            case XmlPullParser.START_TAG:
                isItemStartTag = startTagParse(isItemStartTag, tagname);
                break;
            case XmlPullParser.TEXT:
                text = parser.getText();
                break;

            case XmlPullParser.END_TAG:
                endTagParser(parser, isItemStartTag, tagname);
                break;

            default:
                break;
        }
        return isItemStartTag;
    }

    private boolean startTagParse(boolean isItemStartTag, String tagname) {
        if (tagname.equalsIgnoreCase("item")) {
            rssInfo = new RSSInfo(Parcel.obtain());
            isItemStartTag = true;
        }
        return isItemStartTag;
    }

    private void endTagParser(XmlPullParser parser, boolean isItemStartTag, String tagname) {
        if (isItemStartTag) {
            if (tagname.equalsIgnoreCase("item")) {
                rssInfoList.add(rssInfo);
            } else if (tagname.equalsIgnoreCase("title")) {
                rssInfo.setTitle(text);
            } else if (tagname.equalsIgnoreCase("description")) {
                rssInfo.setDescription(text);
            } else if (tagname.equalsIgnoreCase("pubDate")) {
                rssInfo.setPubDate(text);
            } else if (tagname.equalsIgnoreCase("enclosure")) {
                rssInfo.setImage(parser.getAttributeValue(null, "url"));
            } else if (tagname.equalsIgnoreCase("link")) {
                rssInfo.setLink(text);
            }
        }
    }


}
