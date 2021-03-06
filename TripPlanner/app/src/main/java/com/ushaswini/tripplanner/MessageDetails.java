package com.ushaswini.tripplanner;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Vinnakota Venkata Ratna Ushaswini
 * MessageDetails
 * 20/04/2017
 */



public class MessageDetails {

    String text,user_name,  image_url, id;

    Date posted_time;

    ArrayList<Comment> comments;

    ArrayList<String> usersWhoDeletedThisMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getPosted_time() {
        return posted_time;
    }

    boolean post_type;

    public void setPosted_time(Date posted_time) {

        this.posted_time = posted_time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public boolean getPost_type() {
        return post_type;
    }

    public void setPost_type(boolean post_type) {
        this.post_type = post_type;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }

    public ArrayList<String> getUsersWhoDeletedThisMessage() {
        return usersWhoDeletedThisMessage;
    }

    public void setUsersWhoDeletedThisMessage(ArrayList<String> usersWhoDeletedThisMessage) {
        this.usersWhoDeletedThisMessage = usersWhoDeletedThisMessage;
    }

    public void addComment(Comment comment) {
        if(comments==null)
            comments= new ArrayList<>();

        comments.add(comment);
    }

    public MessageDetails() {
    }

    public Map<String,Object> toMap(){

        //Comment comment = new Comment();

        HashMap<String,Object> result = new HashMap<>();
        result.put("text",text);
        result.put("user_name",user_name);
        result.put("posted_time",posted_time);
        result.put("image_url",image_url);
        result.put("comments",comments);
        result.put("post_type",post_type);
        result.put("id",id);





        return result;
    }

    public void addToUserWhoDeletedThisMessage(String uid){
        if(usersWhoDeletedThisMessage == null){
            usersWhoDeletedThisMessage = new ArrayList<>();
        }
        usersWhoDeletedThisMessage.add(uid);
    }

    public MessageDetails(String text, String user_name, Date posted_time, String image_url, boolean post_type,String key) {

        this.text = text;
        this.user_name = user_name;
        this.posted_time = posted_time;
        this.image_url = image_url;
        this.post_type = post_type;
        this.id = key;
    }

    @Override
    public String toString() {
        return "MessageDetails{" +
                "text='" + text + '\'' +
                ", user_name='" + user_name + '\'' +
                ", image_url='" + image_url + '\'' +
                ", id='" + id + '\'' +
                ", posted_time=" + posted_time +
                ", comments=" + comments +
                ", post_type=" + post_type +
                '}';
    }
}

