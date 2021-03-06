/*
       Copyright 2018 IBM Corp All Rights Reserved
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.ibm.hybrid.cloud.sample.portfolio.controllers.datamodel;

public class FeedbackReply {
    private String message;
    private int free;
    private String sentiment;


    public FeedbackReply() {
    }

    public FeedbackReply(String initialMessage, int initialFree, String initialSentiment) {
        setMessage(initialMessage);
        setFree(initialFree);
        setSentiment(initialSentiment);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    public int getFree() {
        return free;
    }

    public void setFree(int newFree) {
        free = newFree;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String newSentiment) {
        sentiment = newSentiment;
    }

}