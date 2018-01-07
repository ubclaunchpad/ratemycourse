package com.example.coursify;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class Utils {

    public static final int RECENTLY_OPENE_LIMIT = 4;
    private static final String TAG = Utils.class.getSimpleName();

    protected static String processEmail(String email){
        int i = email.indexOf('@');
        email = email.substring(0, i) + ";at;" + email.substring(i+1);
        ArrayList<Integer> indeces = new ArrayList<Integer>();
        for(int k = 0; k < email.length(); k++){
            if(email.charAt(k) == '.'){
                email = email.substring(0, k) + ";dot;" + email.substring(k+1);
            }
        }
        return email;
    }

    protected static int convertDpToPx (Context context, double dp) {
        return (int) ((dp * context.getResources().getDisplayMetrics().density) + 0.5);
    }
}
