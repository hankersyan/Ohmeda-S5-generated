package com.ohmeda.s5;

import java.util.concurrent.TimeUnit;

public class Program {
    public static void main(String[] args) {
        SerialPort sp = createSerialPort();

        Receiver receiver = new Receiver(new Logger(), sp, new VitalSignObserver());
        receiver.startDevice();

        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        receiver.stopDevice();
    }

    private static SerialPort createSerialPort() {
        // full crossover cable
        SerialPort sp = new SerialPort();
        sp.setBaudRate(19200);
        sp.setDataBits(8);
        sp.setHandshake(SerialPort.HANDSHAKE_RTS);
        sp.setParity(SerialPort.PARITY_EVEN);
        sp.setPortName("COM2");
        sp.setReadTimeout(3000);
        sp.setRtsEnable(true);
        sp.setStopBits(SerialPort.STOPBITS_1);
        sp.setWriteTimeout(3000);
        return sp;
    }

    static class VitalSignObserver implements IVitalSignObserver {
        @Override
        public void onDataReceived(VitalSignDto vsd) {
            System.out.println("OnDataReceived:" + vsd);
        }
    }

    static class Logger implements ILog {
        @Override
        public void d(String msg) {
            System.out.println(msg);
        }

        @Override
        public void i(String msg) {
            System.out.println(msg);
        }

        @Override
        public void e(String msg) {
            System.out.println(msg);
        }
    }
}