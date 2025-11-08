package org.example;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Plant {
    private double vol; //для объёма
    private double room_temperature; //температура в комнате
    private double height; //высота цветочка
    private File pic;
    private String inf;
    private String name;
    private ArrayList<String> calendar;

    public Plant(String name){
        this.name = name;
        calendar = new ArrayList<>();
    }

    public Plant(double vol, double room_temperature, double height, File pic,
                 String inf, String name, ArrayList<String> calendar){
        this.name = name;
        calendar = new ArrayList<>();
        this.calendar = calendar;
        this.vol = vol;
        this.room_temperature = room_temperature;
        this.height = height;
        this.pic = pic;
        this.inf = inf;
    }

    public void add_reminder(String reminder) {
        calendar.add(reminder);
    }

    public void set_pic (File path) throws IOException {
        pic = path;
        System.out.println("Адрес картинки: " + pic);

    }

    public void set_info(String str){
        inf = str;
        System.out.println("Информация о картинке: " + pic);
    }

    public void set_vol (double number)
    {
        vol = number;
        System.out.println("Объём цветочного горшка = " + vol);
    }

    public void set_temperature (double number)
    {
        room_temperature = number;
        System.out.println("Температура в комнате = " + room_temperature);
    }

    public void set_height (double number)
    {
        height = number;
        System.out.println("Высота цветочка = " + height);
    }

    public File get_pic () throws IOException {
        return pic;
    }

    public String get_info(){
        return inf;
    }

    public double get_vol ()
    {
        return vol;
    }

    public double get_temperature ()
    {
        return room_temperature;
    }

    public double get_height ()
    {
        return height;
    }
    public String get_name()
    {
        return name;
    }
    public ArrayList<String> get_calendar(){return calendar;}
}