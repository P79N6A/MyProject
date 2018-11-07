package com.sankuai.inf.octo.mns;

public class SimpleServerInfo {

    private String ip;
    private int port;
    private double weight;

    public SimpleServerInfo(String ip, int port, double weight) {
        this.ip = ip;
        this.port = port;
        this.weight = weight;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Server{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", weight=" + weight +
                '}';
    }
}