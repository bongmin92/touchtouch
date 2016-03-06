package org.telegram.ui;

/**
 * Created by 411A on 2015-02-23.
 */
public class Contact {
        int id;
    int type;
        Long time;

        public Contact(){

        }
    public Contact(int id, int type, Long time){
        this.id = id;
        this.type = type;
        this.time = time;
    }
    
        public Contact(int type, Long time){
            this.type = type;
            this.time = time;
        }

        public int getID(){
            return this.id;
        }

        public void setID(int id){
            this.id = id;
        }

    public int getType(){
        return this.type;
    }

    public void setType(int type){
        this.type = type;
    }
        public Long getTime(){
            return this.time;
        }

        public void setTime(Long time){
            this.time = time;
        }

}
