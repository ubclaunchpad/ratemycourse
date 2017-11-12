package com.example.coursify;

import java.util.ArrayList;

/**
 * Created by LucyZhao on 2017/11/11.
 */

public class Utils {
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
}
