package com.uzumaki.naruto.instagramclone.utils;

/**
 * Created by Gaurav Bist on 06-07-2017.
 */

public class StringManipulation {
    public static String expandUserName(String userName) {
        return userName.replace(".", " ");
    }

    public static String condenseUsername(String userName) {
        return userName.replace(" ", ".");
    }

    /**
     * It will seperate out the tag from the caption
     * @param caption IN-> this is my description #tag1 #tag2 #anotherTag
     * @return      OUT -> #tag1,#tag2,#anotherTag
     */
    public static String getTagsFromCaption (String caption) {
        if(caption.indexOf("#") > 0) {
            StringBuilder sb = new StringBuilder();
            char[] charArray = caption.toCharArray();
            boolean foundWord = false;

            for(char c : charArray) {
                if(c == '#') {
                    foundWord = true;
                    sb.append(c);
                } else {
                    if(foundWord) {
                        sb.append(c);
                    }
                }

                if(c == ' ') {
                    foundWord = false;
                }
            }
            String s = sb.toString().replace(" ", "").replace("#",",#");
            return s.substring(1, s.length());
        }
        return caption;
    }
}
