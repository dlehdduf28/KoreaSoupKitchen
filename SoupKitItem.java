package net.dongyeol.koreasoupkitchen;

import android.os.Build;

import java.io.Serializable;

/**
 * 클래스 : SoupKitItem
 * 무료급식소의 데이터를 객체로 담기 위한 클래스.
 */
public class SoupKitItem implements Serializable{
    private String  title; // 시설명
    private String  address; // 급식소 주소
    private String  location; // 급식소 장소
    private String  date; // 급식 날짜
    private String  time; // 급식 시간
    private String  contact; // 급식소 연락처
    private String  longitude; // 경도
    private String  latitude; // 위도
    private String  dataDate; // 데이터 확인 날짜

    // 시리얼/디시리얼을 할 때 이 클래스를 구분 짓기 위한 SUID //
    // OS 환경마다 시리얼 -> 디시리얼 사이에 UID가 달라지는 경우도 있다. //
    // 그러므로 SUID를 지정해두는 것이 안정적이다. //
    private static final long serialVersionUID = ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) ?
            3847988396824811857L : -2479274664736305536L;

    public SoupKitItem(String title, String address, String location,
                       String date, String time, String contact,
                       String longitude, String latitude, String dateDate) {

        this.title      =   title;
        this.address    =   address;
        this.location   =   location;
        this.date       =   date;
        this.time       =   time;
        this.contact    =   contact;
        this.longitude  =   longitude;
        this.latitude   =   latitude;
        this.dataDate   =   dateDate;

        // address 데이터에 "가 포함 되어 있다면 제거 한다. //
        if(address != null)
            this.address = quotesRemove(address);

        // contact 데이터에 "가 포함 되어 있다면 제거 한다. //
        if(contact != null)
            this.contact = quotesRemove(contact);
    }


    // 전달된 문자열에 "가 포함 되어 있는지 확인 하고 있다면 제거 한다. //
    public String quotesRemove(String str) {
        if(str.contains("\""))
            str = str.replace("\"", "");

        return str;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;

        // address 데이터에 "가 포함 되어 있다면 제거 한다. //
        this.address = quotesRemove(address);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;

        // contact 데이터에 "가 포함 되어 있다면 제거 한다. //
        this.contact = quotesRemove(contact);
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dateDate) {
        this.dataDate = dateDate;
    }

}
