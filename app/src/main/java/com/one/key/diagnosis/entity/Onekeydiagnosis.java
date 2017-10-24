package com.one.key.diagnosis.entity;

import java.io.Serializable;

/**
 * Created by 1611281 on 2017/10/17.
 */

public class Onekeydiagnosis implements Serializable {

    private String measuringPointNum;
    private String meterAddress;
    private String protocolNum;
    private String portNum;
    private String dayfrozenValue;
    private String clockpassthrough;
    private String diagnosisResult;

    public String getMeasuringPointNum() {
        return measuringPointNum;
    }

    public void setMeasuringPointNum(String measuringPointNum) {
        this.measuringPointNum = measuringPointNum;
    }

    public String getMeterAddress() {
        return meterAddress;
    }

    public void setMeterAddress(String meterAddress) {
        this.meterAddress = meterAddress;
    }

    public String getProtocolNum() {
        return protocolNum;
    }

    public void setProtocolNum(String protocolNum) {
        this.protocolNum = protocolNum;
    }

    public String getPortNum() {
        return portNum;
    }

    public void setPortNum(String portNum) {
        this.portNum = portNum;
    }

    public String getDayfrozenValue() {
        return dayfrozenValue;
    }

    public void setDayfrozenValue(String dayfrozenValue) {
        this.dayfrozenValue = dayfrozenValue;
    }

    public String getClockpassthrough() {
        return clockpassthrough;
    }

    public void setClockpassthrough(String clockpassthrough) {
        this.clockpassthrough = clockpassthrough;
    }

    public String getDiagnosisResult() {
        return diagnosisResult;
    }

    public void setDiagnosisResult(String diagnosisResult) {
        this.diagnosisResult = diagnosisResult;
    }
}
