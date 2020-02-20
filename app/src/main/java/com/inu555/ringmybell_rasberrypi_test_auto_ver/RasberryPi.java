package com.inu555.ringmybell_rasberrypi_test_auto_ver;

public class RasberryPi {

    // 버스 차량 번호 (예:"인천74사1071")
    private String busNumPlate;
    // 버스 노선번호(예:"780-1")
    private String routeNum;
    // 버스 현재 위도
    private Double lat;
    // 버스 현재 경도
    private Double lon;

    public RasberryPi() {}

    public RasberryPi(Double lat, Double lon) {
        this.busNumPlate = "인천11가2222";
        this.routeNum = "780-1";
        this.lat = lat;
        this.lon = lon;
    }

    public String getBusNumPlate() {
        return busNumPlate;
    }

    public void setBusNumPlate(String busNumPlate) {
        this.busNumPlate = busNumPlate;
    }

    public String getRouteNum() {
        return routeNum;
    }

    public void setRouteNum(String routeNum) {
        this.routeNum = routeNum;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }
}
