package com.example.tmmf5.onmyway.UserList;

import android.util.Xml;

import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UsersXMLParser {
    private static final String ns = null;

    public void parse(InputStream in, ArrayList<User> users) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readUsers(parser,users);
        } finally {
            in.close();
        }
    }

    private void readUsers(XmlPullParser parser, ArrayList<User> users) throws XmlPullParserException, IOException {


        parser.require(XmlPullParser.START_TAG, ns, "users");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the user tag
            if (name.equals("user")) {
                users.add(readUser(parser));
            } else {
                skip(parser);
            }
        }

    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private User readUser(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "user");

        int fb_id = 0;
        String gender = null;
        String first_name = null;
        String last_name = null;
        LatLng position = null;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            switch (name) {
                case "fb_id":
                    fb_id = readId(parser);
                    break;
                case "gender":
                    gender = readGender(parser);
                    break;
                case "first_name":
                    first_name = readFirstName(parser);
                    break;
                case "last_name":
                    last_name = readLastName(parser);
                    break;
                case "position":
                    position = readPosition(parser);
                    break;
                default:
                    skip(parser);
                    break;
            }
        }

        User user = new User();
        user.setId(fb_id);
        user.setGender(gender);
        user.setFirst_name(first_name);
        user.setLast_name(last_name);
        user.setLatitude((float) position.latitude);
        user.setLongitude((float) position.longitude);

        return user;
    }

    private LatLng readPosition(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "position");
        String tag = parser.getName();
        float latitude = 0f;
        float longitude = 0f;

        if (tag.equals("position")) {
            parser.nextTag();
            if (parser.getName().equals("latitude")) {
                latitude = readLatitude(parser);

                parser.nextTag();

                if(parser.getName().equals("longitude")) {
                    longitude = readLongitude(parser);
                }
            }
        }
        //parser.require(XmlPullParser.END_TAG, ns, "position");
        return new LatLng(latitude,longitude);
    }

    private float readLongitude(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "longitude");
        String longitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "longitude");
        return Float.parseFloat(longitude);
    }

    private float readLatitude(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "latitude");
        String latitude = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "latitude");
        return Float.parseFloat(latitude);
    }

    private String readLastName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "last_name");
        String last_name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "last_name");
        return last_name;
    }

    private String readFirstName(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "first_name");
        String first_name = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "first_name");
        return first_name;
    }

    private String readGender(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "gender");
        String gender = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "gender");
        return gender;
    }

    private int readId(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "fb_id");
        String id = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "fb_id");
        return Integer.parseInt(id);
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

}
