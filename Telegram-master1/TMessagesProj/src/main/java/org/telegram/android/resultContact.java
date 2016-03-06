package org.telegram.android;

/**
 * Created by 411A on 2015-02-23.
 */
public class resultContact {
    int id;
    int month;
    int day;
    int callr;
    int calls;
    int smsr;
    int smss;
    int msgr;
    int msgs;
    double use;
    int emoji;
    double emonum;

    public resultContact() {

    }

    public resultContact(int id, int month, int day, int callr, int calls, int smsr, int smss, int msgr, int msgs, double use, int emoji, double emonum) {
        this.id = id;
        this.month = month;
        this.day = day;
        this.callr = callr;
        this.calls = calls;
        this.smsr = smsr;
        this.smss = smss;
        this.msgr = msgr;
        this.msgs = msgs;
        this.use = use;
        this.emoji = emoji;
        this.emonum = emonum;
    }

    public resultContact(int month, int day, int callr, int calls, int smsr, int smss, int msgr, int msgs, double use, int emoji, double emonum) {
        this.month = month;
        this.day = day;
        this.callr = callr;
        this.calls = calls;
        this.smsr = smsr;
        this.smss = smss;
        this.msgr = msgr;
        this.msgs = msgs;
        this.use = use;
        this.emoji = emoji;
        this.emonum = emonum;
    }

    public int getID() {
        return this.id;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return this.day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getcallr() {
        return this.callr;
    }

    public void setcallr(int callr) {
        this.callr = callr;
    }

    public int getcalls() {
        return this.calls;
    }

    public void setcalls(int calls) {
        this.calls = calls;
    }

    public int getsmsr() {
        return this.smsr;
    }

    public void setsmsr(int smsr) {
        this.smsr = smsr;
    }

    public int getsmss() {
        return this.smss;
    }

    public void setsmss(int smss) {
        this.smss = smss;
    }

    public int getmsgr() {
        return this.msgr;
    }

    public void setmsgr(int msgr) {
        this.msgr = msgr;
    }

    public int getmsgs() {
        return this.msgs;
    }

    public void setmsgs(int msgs) {
        this.msgs = msgs;
    }

    public double getUse() {
        return this.use;
    }

    public void setUse(double use) {
        this.use = use;
    }

    public int getEmoji() {
        return this.emoji;
    }

    public void setEmoji(int emoji) {
        this.emoji = emoji;
    }

    public double getEmonum() {
        return this.emonum;
    }

    public void setEmonum(double emonum) {
        this.emonum = emonum;
    }

    public String toString(){
        String newone = "";
        newone += this.month +"/" + this.day + '\n' +  '\n' + "수신 전화 : "+ this.callr + "           발신 전화 : " +this.calls + '\n' + "수신 메세지 : "+ this.smsr + "        발신 메세지 : " + this.smss + '\n' + "수신 TouchTouch : " + this.msgr + '\n' + "발신 TouchTouch : " + this.msgs + '\n';

                return newone;
    }
}
