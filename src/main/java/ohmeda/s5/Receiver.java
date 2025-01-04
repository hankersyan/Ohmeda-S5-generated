package com.ohmeda.s5;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Receiver {

    private final List<String> allInvalidValues = new ArrayList<>();
    private boolean isRunning;
    private Thread thrRev;
    private boolean bLoop = true;
    public byte[] DPort_rxbuf;
    private DatexTxType DPort_txbuf;
    private int DPortBufSize;
    public List<byte[]> FrameList;
    private boolean m_bitshiftnext;
    private List<Byte> m_bList;
    private boolean m_fstart;
    private boolean m_storeend;
    private boolean m_storestart;
    private StringBuilder m_strBuilder;
    public List<DatexRecordType> RecordList;
    private DatexRecordReqType request_ptr;
    private final List<DataItem> dataItems = new ArrayList<>();

    private SerialPort com;
    private IVitalSignObserver observer;
    private ILog log;

    public Receiver(ILog log, SerialPort sp, IVitalSignObserver observer) {
        this.log = log;
        this.observer = observer;
        this.com = sp;
    }

    public void initialization() {
        init();
        for (InvalidDataValue v : InvalidDataValue.values()) {
            allInvalidValues.add(String.valueOf(v.ordinal()));
        }
    }

    public void startDevice() {
        thrRev = new Thread(this::myDataRecv);
        thrRev.setName("thrRev");
        thrRev.setPriority(Thread.MIN_PRIORITY);
        thrRev.setDaemon(true);
        initialization();
        isRunning = true;
        thrRev.start();
    }

    public void stopDevice() {
        sendDisPlayRequest(false);
        isRunning = false;
        thrRev.interrupt();
        try {
            thrRev.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        closeSerialPort();
    }

    public void openSerialPort() {
        try {
            if (!com.isOpen()) {
                com.open();
            }
        } catch (Exception exp) {
            log.e("COM error. Exception message is: " + exp.getMessage());
        }
    }

    public void sendDisPlayRequest(boolean bFlag) {
        try {
            if (!com.isOpen()) {
                com.open();
            }

            byte[] buf = {
                    0x7e, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0a, 0x00, 0x0e, 0x00, 0x00, 0x00,
                    0x00,
                    0x00, 0x49, 0x7e
            };

            log.d("Start to send display request to monitor");

            com.write(buf, 0, buf.length);

            log.d("Send display request successfully");
            bLoop = bFlag;
        } catch (Exception e) {
            log.e(String.format("SendDisPlayRequest Error. Exception message is: %s", e.getMessage()));
        }
    }

    public void myDataRecv() {
        int nCount = 0;
        byte[] _buffer = new byte[0];

        while (isRunning) {
            try {
                nCount = 0;
                openSerialPort();
                sendDisPlayRequest(true);
                List<Byte> buffer = new ArrayList<>();

                while (isRunning) {
                    byte[] byy = new byte[com.bytesToRead()];

                    log.d("try to read data from com port");
                    com.read(byy, 0, byy.length);

                    if (byy.length > 0) {
                        log.d("Read data from com port, length:" + byy.length);
                        //_logger.DumpHexData(byy.AsHex());

                        for (byte b : byy) {
                            buffer.add(b);
                        }

                        int _start = 0, _end = 0;
                        while (findNextRecord(buffer, _start, _end)) {
                            //_logger.LogTrace("Prepare to parse data...");

                            resetStatus();
                            dataItems.clear();
                            if (decode(buffer)) {
                                dataHandler();
                                //Decode成功，从当前buffer中的  _end+1位置重新进行寻找
                                buffer = buffer.subList(_end + 1, buffer.size());
                            } else {
                                //如果Decode失败，则说明这个record是不正确的   所以要考虑上个record的end其实是下一个start的情况
                                buffer = buffer.subList(_end, buffer.size());
                            }
                        }

                        //将buffer中剩余的可能能够继续匹配的内容保存下来
                        if (_start >= 0) {
                            buffer = buffer.subList(_start, buffer.size());
                        } else {
                            buffer.clear();
                        }

                        if (buffer.size() > 4096) {
                            //_logger.LogWarning("Reach the max size of buffer. Clear buffer!");
                            buffer.clear();
                        }
                    } else {
                        nCount++;
                        //if (nCount > _configurationDto.CommonParameter.MaxRetryTimes) throw new ExceedMaxRetryException();

                        log.d("Read nothing from com port");
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception exception) {
                log.e(String.format("Unexpected exception occurs! Exception message is: %s", exception.getMessage()));
                log.e(String.format("StackTrace : %s", exception.getStackTrace()));
                log.d("Try to reconnect...");
                closeSerialPort();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void dataHandler() {
        //_logger.LogTrace("DataHandler...");
        VitalSignDto dp = new VitalSignDto();
        dp.setDateTime(new Date());
        // TODO:
        for (DataItem dataItem : dataItems) {
            dp.add(dataItem.Name, dataItem.Value, "", ConfigurationItemLevel.None);
            //_logger.LogInformation($"{dataItem.Name}-{dataItem.Value}");
        }
        if (dp.getItems().size() > 0) {
            log.d("OnPhysiologicalDataReceived...");
            //Task.Run(async () => {
            //    await (new VitalSignDataService(_loggerFactory, _publishService, _dbContext)).PublishDataAsync(dp);
            //});
            observer.onDataReceived(dp);
        }
    }

    private void resetStatus() {
        m_fstart = true;
        m_storestart = false;
        m_storeend = false;
        m_strBuilder.setLength(0);
        m_bList.clear();
        FrameList.clear();
        RecordList.clear();
    }

    private void init() {
        request_ptr = new DatexRecordReqType();
        RecordList = new ArrayList<>();
        FrameList = new ArrayList<>();
        DPort_txbuf = new DatexTxType();
        m_fstart = true;
        m_storestart = false;
        m_storeend = false;
        m_bList = new ArrayList<>();
        m_strBuilder = new StringBuilder();
        DPortBufSize = 4096;
        DPort_rxbuf = new byte[DPortBufSize];
    }

    private void createFrameListFromByte(byte b) {
        if (b == 126 && m_fstart) {
            m_fstart = false;
            m_storestart = true;
        } else if (b == 126 && !m_fstart) {
            m_fstart = true;
            m_storeend = true;
            m_storestart = false;
            if (b != 126) {
                m_bList.add(b);
            }
        }

        if (m_storestart) {
            if (b == 125) {
                m_bitshiftnext = true;
                return;
            }

            if (m_bitshiftnext) {
                b |= 124;
                m_bitshiftnext = false;
                m_bList.add(b);
                return;
            }

            if (b == 126) {
                return;
            }

            m_bList.add(b);
            return;
        }

        if (m_storeend) {
            int framelen = m_bList.size();
            if (framelen != 0) {
                byte[] bArray = new byte[framelen];
                for (int i = 0; i < framelen; i++) {
                    bArray[i] = m_bList.get(i);
                }
                byte checksum = 0;
                for (int j = 0; j < framelen - 1; j++) {
                    checksum += bArray[j];
                }

                if (checksum == bArray[framelen - 1]) {
                    FrameList.add(bArray);
                }

                m_bList.clear();
                m_storeend = false;
                return;
            }

            m_storestart = true;
            m_storeend = false;
            m_fstart = false;
        }
    }

    private void createRecordList() {
        int recorddatasize = 0;
        byte[] fullrecord = new byte[1490];
        for (byte[] fArray : FrameList) {
            DatexRecordType record_dtx = new DatexRecordType();
            for (int i = 0; i < fullrecord.length; i++) {
                fullrecord[i] = 0;
            }

            recorddatasize = fArray.length;
            for (int n = 0; n < fArray.length && recorddatasize < 0x5d2; n++) {
                fullrecord[n] = fArray[n];
            }

            // In Java, we don't have direct equivalent of Marshal.PtrToStructure, so we need to manually copy the data.
            // Assuming DatexRecordType has a constructor or method to set its fields from a byte array.
            // record_dtx.setFromByteArray(fullrecord);
            RecordList.add(record_dtx);
        }
    }

    private void readSubRecords() {
        for (DatexRecordType dx_record : RecordList) {
            //_logger.LogTrace("Decode sroffArray...");
            short[] sroffArray = {
                    dx_record.hdr.srOffset1, dx_record.hdr.srOffset2,
                    dx_record.hdr.srOffset3, dx_record.hdr.srOffset4, dx_record.hdr.srOffset5,
                    dx_record.hdr.srOffset6, dx_record.hdr.srOffset7, dx_record.hdr.srOffset8
            };

            //_logger.LogTrace("Decode srtypeArray...");
            byte[] srtypeArray = {
                    dx_record.hdr.srType1, dx_record.hdr.srType2,
                    dx_record.hdr.srType3, dx_record.hdr.srType4, dx_record.hdr.srType5,
                    dx_record.hdr.srType6, dx_record.hdr.srType7, dx_record.hdr.srType8
            };

            //_logger.LogTrace("Getting unixTime...");
            int unixtime = dx_record.hdr.rTime;
            DriPhdb phdata_ptr = new DriPhdb();
            for (int i = 0; i < 8 && srtypeArray[i] != 255; i++) {
                if (srtypeArray[i] == 1 && srtypeArray[i] != 255) {
                    int offset = sroffArray[i];
                    byte[] buffer = new byte[270];
                    for (int j = 0; j < 270; j++) {
                        buffer[j] = dx_record.data[4 + j + offset];
                    }

                    // In Java, we don't have direct equivalent of Marshal.PtrToStructure, so we need to manually copy the data.
                    // Assuming DriPhdb has a constructor or method to set its fields from a byte array.
                    // phdata_ptr.setFromByteArray(buffer);
                    switch (i) {
                        case 0:
                            // phdata_ptr.basic.setFromByteArray(buffer);
                            break;
                        case 1:
                            // phdata_ptr.ext1.setFromByteArray(buffer);
                            break;
                        case 2:
                            // phdata_ptr.ext2.setFromByteArray(buffer);
                            break;
                        case 3:
                            // phdata_ptr.ext3.setFromByteArray(buffer);
                            break;
                    }
                }
            }

            Date dtDateTime = new Date(unixtime * 1000L);
            String dt = dtDateTime.toString();
            //if (!_configurationDto.CommonParameter.UseMonitorTime) dt = new Date().toString();

            submitBasicSubRecord(phdata_ptr, dt);
            submitExt1and2SubRecord(phdata_ptr, dt);
        }
    }

    private void submitExt1and2SubRecord(DriPhdb driSR, String dt) {
        StringBuilder sb = new StringBuilder();
        short so1 = driSR.ext1.ecg12.stII;
        short so2 = driSR.ext1.ecg12.stV5;
        short so3 = driSR.ext1.ecg12.stAVL;
        String result = validateAddData(so1, 0.01, false, sb);
        String s1 = validateDataFormatString(so1, 0.01, false);
        validateAddData(so2, 0.01, false, sb);
        String s2 = validateDataFormatString(so2, 0.01, false);
        validateAddData(so3, 0.01, false, sb);
        String s3 = validateDataFormatString(so3, 0.01, false);
        short so4 = driSR.ext2.ent.eegEnt;
        short so5 = driSR.ext2.ent.emgEnt;
        short so6 = driSR.ext2.ent.bsrEnt;
        short so7 = driSR.ext2.eegBis.bis;
        short so8 = driSR.ext2.eegBis.srVal;
        short so9 = driSR.ext2.eegBis.emgVal;
        short so10 = driSR.ext2.eegBis.sqiVal;
        validateAddData(so4, 1.0, true, sb);
        validateAddData(so5, 1.0, true, sb);
        validateAddData(so6, 1.0, true, sb);
        result = validateAddData(so7, 1.0, true, sb);
        if (verifyValue(result)) {
            dataItems.add(new DataItem() {{
                Name = "Bis";
                Time = dt;
                Value = result;
            }});
        }

        validateAddData(so8, 1.0, true, sb);
        validateAddData(so9, 1.0, true, sb);
        validateAddData(so10, 1.0, true, sb);
        //_logger.LogTrace(
        //    $"{nameof(so1)}:{so1},{nameof(so2)}:{so2},{nameof(so3)}:{so3},{nameof(so4)}:{so4},{nameof(so5)}:{so5},{nameof(so6)}:{so6},{nameof(so7)}:{so7},{nameof(so8)}:{so8},{nameof(so9)}:{so9},{nameof(so10)}:{so10}");
        log.d("SubmitExt1and2SubRecord:" + sb);
    }

    private String validateAddData(Object value, double decimalshift, boolean rounddata, StringBuilder sbBuilder) {
        int val = (int) value;
        double dval = (double) value * decimalshift;
        if (rounddata) {
            dval = Math.round(dval);
        }

        String str = String.valueOf(dval);
        if (val < -32001) {
            str = "-";
            sbBuilder.append(str);
            sbBuilder.append(',');
            return str;
        }

        sbBuilder.append(str);
        sbBuilder.append(',');
        return str;
    }

    private boolean validateAddData(Object value, double decimalshift, boolean rounddata) {
        int val = (int) value;
        double dval = (double) value * decimalshift;
        if (rounddata) {
            dval = Math.round(dval);
        }

        String str = String.valueOf(dval);
        if (val < -32001) {
            str = "-";
            m_strBuilder.append(str);
            m_strBuilder.append(',');
            return false;
        }

        m_strBuilder.append(str);
        m_strBuilder.append(',');
        return true;
    }

    private boolean verifyValue(String v) {
        if (v == null || v.isEmpty()) {
            return false;
        }

        if (v.contains("-")) {
            return false;
        }

        if (allInvalidValues.contains(v)) {
            return false;
        }

        return true;
    }

    private String validateDataFormatString(Object value, double decimalshift, boolean rounddata) {
        int val = (int) value;
        double dval = (double) value * decimalshift;
        if (rounddata) {
            dval = Math.round(dval);
        }

        String str = String.valueOf(dval);
        if (val < -32001) {
            str = "-";
        }

        return str;
    }

    private void submitBasicSubRecord(DriPhdb driSR, String dt) {
        short so1 = driSR.basic.ecg.hr;
        short so2 = driSR.basic.nibp.sys;
        short so3 = driSR.basic.nibp.dia;
        short so4 = driSR.basic.nibp.mean;
        short so5 = driSR.basic.SpO2.SpO2;

        short so6 = driSR.basic.co2.et;
        short so5_1 = driSR.basic.SpO2.pr;

        // ecg
        String s1 = validateDataFormatString(so1, 1.0, true);
        //_logger.LogTrace(
        //    $"{nameof(so1)}:{so1},{nameof(so2)}:{so2},{nameof(so3)}:{so3},{nameof(so4)}:{so4},{nameof(so5)}:{so5},{nameof(so6)}:{so6},{nameof(so5_1)}:{so5_1}");

        validateAddData(so1, 1.0, true);

        // sys
        String s2 = validateDataFormatString(so2, 0.01, true);
        validateAddData(so2, 0.01, true);

        // dia
        String s3 = validateDataFormatString(so3, 0.01, true);
        validateAddData(so3, 0.01, true);

        // mean
        String s4 = validateDataFormatString(so4, 0.01, true);
        validateAddData(so4, 0.01, true);

        // spo2
        String s5 = validateDataFormatString(so5, 0.01, true);
        validateAddData(so5, 0.01, true);

        // spo2_PR
        String s5_1 = validateDataFormatString(so5_1, 1.0, true);
        validateAddData(so5_1, 1.0, true);

        double et = so6 * driSR.basic.co2.ambPress;
        validateAddData(et, 1E-05, true);

        // etco2
        String s6 = validateDataFormatString(et, 1E-05, true);

        short so7 = driSR.basic.aa.et;
        short so8 = driSR.basic.aa.fi;
        short so9 = driSR.basic.aa.macSum;
        short so10 = driSR.basic.aa.hdr.labelInfo;
        validateAddData(so7, 0.01, false);
        validateAddData(so8, 0.01, false);
        validateAddData(so9, 0.01, false);
        //_logger.LogTrace($"{nameof(so7)}:{so7},{nameof(so8)}:{so8},{nameof(so9)}:{so9},{nameof(so10)}:{so10}");
        String s10 = "";
        switch (so10) {
            case 0:
                s10 = "Unknown";
                break;
            case 1:
                s10 = "None";
                break;
            case 2:
                s10 = "HAL";
                break;
            case 3:
                s10 = "ENF";
                break;
            case 4:
                s10 = "ISO";
                break;
            case 5:
                s10 = "DES";
                break;
            case 6:
                s10 = "SEV";
                break;
        }

        m_strBuilder.append(s10);
        m_strBuilder.append(',');
        double so11 = driSR.basic.o2.fi;
        double so12 = driSR.basic.n2o.fi;
        double so13 = driSR.basic.n2o.et;
        //_logger.LogTrace($"{nameof(so11)}:{so11},{nameof(so12)}:{so12},{nameof(so13)}:{so13}");

        // resp 1
        double so14 = driSR.basic.co2.rr;

        // resp 2
        double so142 = driSR.basic.ecg.impRr;

        // resp 3
        double so143 = driSR.basic.flowVol.rr;
        //_logger.LogTrace($"{nameof(so14)}:{so14},{nameof(so142)}:{so142},{nameof(so143)}:{so143}");

        double so15 = driSR.basic.t1.temp;
        double so16 = driSR.basic.t2.temp;
        double so17 = driSR.basic.p1.hr;
        double so18 = driSR.basic.p1.sys;
        double so19 = driSR.basic.p1.dia;
        double so20 = driSR.basic.p1.mean;
        double so21 = driSR.basic.p2.hr;
        double so22 = driSR.basic.p2.sys;
        double so23 = driSR.basic.p2.dia;
        double so24 = driSR.basic.p2.mean;
        double so25 = driSR.basic.flowVol.ppeak;
        double so26 = driSR.basic.flowVol.pplat;
        double so27 = driSR.basic.flowVol.tvExp;
        //_logger.LogTrace(
        //    $"{nameof(so15)}:{so15},{nameof(so16)}:{so16},{nameof(so17)}:{so17},{nameof(so18)}:{so18},{nameof(so19)}:{so19},{nameof(so20)}:{so20},{nameof(so21)}:{so21},{nameof(so22)}:{so22},{nameof(so23)}:{so23},{nameof(so24)}:{so24},{nameof(so25)}:{so25},{nameof(so26)}:{so26},{nameof(so27)}:{so27}");

        validateAddData(so11, 0.01, false);
        validateAddData(so12, 0.01, false);
        validateAddData(so13, 0.01, false);

        validateAddData(so14, 1.0, false);
        validateAddData(so142, 1.0, false);
        validateAddData(so143, 1.0, false);

        validateAddData(so15, 0.01, false);
        validateAddData(so16, 0.01, false);
        validateAddData(so17, 1.0, true);
        validateAddData(so18, 0.01, true);
        validateAddData(so19, 0.01, true);
        validateAddData(so20, 0.01, true);
        validateAddData(so21, 1.0, true);
        validateAddData(so22, 0.01, true);
        validateAddData(so23, 0.01, true);
        validateAddData(so24, 0.01, true);
        validateAddData(so25, 0.01, true);
        validateAddData(so26, 0.01, true);
        validateAddData(so27, 0.1, true);
        String s9 = validateDataFormatString(so9, 0.01, false);
        String s15 = validateDataFormatString(so15, 0.01, false);
        String s16 = validateDataFormatString(so16, 0.01, false);
        String s18 = validateDataFormatString(so18, 0.01, true);
        String s19 = validateDataFormatString(so19, 0.01, true);
        String s20 = validateDataFormatString(so20, 0.01, true);
        String s22 = validateDataFormatString(so22, 0.01, true);
        String s23 = validateDataFormatString(so23, 0.01, true);
        String s24 = validateDataFormatString(so24, 0.01, true);
        String s25 = validateDataFormatString(so25, 0.01, true);
        //_logger.LogInformation(m_strBuilder.toString());

        // s1: ect
        if (verifyValue(s1)) {
            dataItems.add(new DataItem() {{
                Name = "HeartRate";
                Time = dt;
                Value = s1;
            }});
        }

        if (verifyValue(s2)) {
            dataItems.add(new DataItem() {{
                Name = "SystolicPressure";
                Time = dt;
                Value = s2;
            }});
        }

        if (verifyValue(s3)) {
            dataItems.add(new DataItem() {{
                Name = "DiastolicPressure";
                Time = dt;
                Value = s3;
            }});
        }

        if (verifyValue(s4)) {
            dataItems.add(new DataItem() {{
                Name = "MeanPressure";
                Time = dt;
                Value = s4;
            }});
        }

        if (verifyValue(s5)) {
            dataItems.add(new DataItem() {{
                Name = "SPO2";
                Time = dt;
                Value = s5;
            }});
        }

        if (verifyValue(s6)) {
            dataItems.add(new DataItem() {{
                Name = "ETCO2";
                Time = dt;
                Value = s6;
            }});
        }

        if (verifyValue(s5_1)) {
            dataItems.add(new DataItem() {{
                Name = "PR";
                Time = dt;
                Value = s5_1;
            }});
        }

        try {
            if (!s15.contains("-")) {
                double temp1 = Double.parseDouble(s15);
                // temp
                if (temp1 > 0 && temp1 < 100) {
                    //_logger.LogInformation(string.format("Submit Temp1 for display:{0}", temp1));
                    dataItems.add(new DataItem() {{
                        Name = "Temp1";
                        Time = dt;
                        Value = String.valueOf(temp1);
                    }});
                }
            }
        } catch (Exception exp) {
            log.e(String.format("Error when try to get Temp1:%s", exp.getMessage()));
        }

        try {
            if (verifyValue(s16)) {
                double temp2 = Double.parseDouble(s16);
                if (temp2 > 0 && temp2 < 100) {
                    //_logger.LogInformation(string.format("Submit Temp2 for display:{0}", temp2));
                    dataItems.add(new DataItem() {{
                        Name = "Temp2";
                        Time = dt;
                        Value = String.valueOf(temp2);
                    }});
                }
            }
        } catch (Exception exp) {
            log.e(String.format("Error when try to get Temp2:%s", exp.getMessage()));
        }

        // resp
        double resp = Math.max(Math.max(so14, so142), so143);
        if (resp > 0) {
            dataItems.add(new DataItem() {{
                Name = "RESP";
                Time = dt;
                Value = String.valueOf(resp);
            }});
        }

        try {
            if (verifyValue(s20)) {
                double ds20 = Double.parseDouble(s20);
                dataItems.add(new DataItem() {{
                    Name = "ABPMean";
                    Time = dt;
                    Value = String.valueOf(ds20);
                }});
            }
        } catch (Exception ignored) {
        }

        try {
            if (verifyValue(s24)) {
                double ds24 = Double.parseDouble(s24);
                dataItems.add(new DataItem() {{
                    Name = "CVP";
                    Time = dt;
                    Value = String.valueOf(ds24);
                }});
            }
        } catch (Exception ignored) {
        }

        try {
            if (verifyValue(s25)) {
                double ds25 = Double.parseDouble(s25);
                dataItems.add(new DataItem() {{
                    Name = "Peak";
                    Time = dt;
                    Value = String.valueOf(ds25);
                }});
            }
        } catch (Exception exp) {
            log.e(String.format("Error when try to get Peak:%s", exp.getMessage()));
        }

        if (verifyValue(s18)) {
            dataItems.add(new DataItem() {{
                Name = "ABPSys";
                Time = dt;
                Value = s18;
            }});
        }

        if (verifyValue(s19)) {
            dataItems.add(new DataItem() {{
                Name = "ABPDia";
                Time = dt;
                Value = s19;
            }});
        }
        if (verifyValue(s22)) {
            dataItems.add(new DataItem() {{
                Name = "ABPSys";
                Time = dt;
                Value = s22;
            }});
        }

        if (verifyValue(s23)) {
            dataItems.add(new DataItem() {{
                Name = "ABPDia";
                Time = dt;
                Value = s23;
            }});
        }

        String macValue = String.valueOf(so9);
        if (verifyValue(s19)) {
            dataItems.add(new DataItem() {{
                Name = "MAC";
                Time = dt;
                Value = macValue;
            }});
        }
    }

    public boolean decode(byte[] monitorData) {
        //_logger.LogTrace("Decode...");
        //_logger.LogTrace("Create FrameList from originalData");
        for (byte b : monitorData) {
            createFrameListFromByte(b);
        }

        if (FrameList.size() > 0) {
            //_logger.LogTrace("Create RecordList");
            createRecordList();
            //_logger.LogTrace("Create ReadSubRecords");
            readSubRecords();

            //_logger.LogTrace("clear FrameList and RecordList");
            FrameList.subList(0, FrameList.size()).clear();
            RecordList.subList(0, RecordList.size()).clear();
            return true;
        }

        //_logger.LogWarning(
        //    "FrameList.Count == 0. The record found is not a correct record.Maybe the the end \'7E\' is a start of next record.");
        return false;
    }

    private void stopSerialPort() {
        sendDisPlayRequest(false);

        closeSerialPort();
    }

    public void sendStopDisplayRequest(boolean bFlag) {
        if (com != null) {
            if (com.isOpen()) {
                byte[] bufStop = {
                        0x7e, 0x31, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, 0x00, 0x31, 0x7e
                };
                try {
                    com.write(bufStop, 0, bufStop.length);
                    bLoop = bFlag;
                } catch (Exception ignored) {
                }
            } else {
                bLoop = bFlag;
            }
        } else {
            bLoop = bFlag;
        }
    }

    public void closeSerialPort() {
        try {
            com.close();
            resetStatus();
        } catch (Exception e) {
            log.e(String.format("COM口错误！ Exception message is : %s", e.getMessage()));
        }
    }

    private boolean findNextRecord(byte[] buffer, int start, int end) {
        boolean flagToReturn = false;
        String tmpStr = new String(buffer);
        start = tmpStr.indexOf((char) 0x7E);
        if (start != -1) {
            end = tmpStr.indexOf((char) 0x7E, start + 1);
            if (end != -1) {
                flagToReturn = true;
                log.d(String.format("Find the next Record.In the buffer, Start at %d and End at %d", start, end));
            } else {
                log.d("Did not find the end of next record.");
            }
        } else {
            log.d("Did not find the start of next record.");
        }

        return flagToReturn;
    }
}