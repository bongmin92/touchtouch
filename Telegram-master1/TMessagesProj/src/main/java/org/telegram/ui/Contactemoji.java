package org.telegram.ui;

/**
 * Created by 411A on 2015-03-03.
 */
public class Contactemoji {
    int id;
    String symbol;
    Long time;

    public Contactemoji(){

    }
    public Contactemoji(int id, String symbol, Long time){
        this.id = id;
        this.symbol = symbol;
        this.time = time;
    }

    public Contactemoji(String symbol, Long time){
        this.symbol = symbol;
        this.time = time;
    }

    public int getID(){
        return this.id;
    }
    public void setID(int id){
        this.id = id;
    }
    public String getSymbol(){
        return this.symbol;
    }
    public void setSymbol(String symbol){
        this.symbol = symbol;
    }
    public Long getTime(){
        return this.time;
    }
    public void setTime(Long time){
        this.time = time;
    }

}
