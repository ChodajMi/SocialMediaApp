package com.example.socialmediaapplication;

public class Comment {

    private String commentText;
    private String userId;

    // Empty constructor required for Firebase deserialization
    public Comment() {
    }

    public Comment(String commentText, String userId) {
        this.commentText = commentText;
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
