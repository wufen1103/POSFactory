package com.citaq.util;

public class BlueToothDeviceStruct {
    String name = null;
    String address = null;
    short rssi ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRssi(short rssi) {
        this.rssi = rssi;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public short getRssi() {
        return rssi;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return '{'+
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", rssi='" + rssi + '\'' +
                '}';
    }
}
