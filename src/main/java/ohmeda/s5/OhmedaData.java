package com.ohmeda.s5;

import java.io.Serializable;
import java.util.Arrays;

public class OhmedaData {

    public static class DatexTxType {
        public byte[] data;

        public DatexTxType() {
            this.data = new byte[49];
        }
    }

    public static class DatexRecordType {
        public DatexHdrType hdr;
        public byte[] data;

        public DatexRecordType() {
            this.hdr = new DatexHdrType();
            this.data = new byte[1450];
        }
    }

    public static class DatexRecordReqType {
        public DatexHdrType hdr;
        public PhdbReqType phdbr;

        public DatexRecordReqType() {
            this.hdr = new DatexHdrType();
            this.phdbr = new PhdbReqType();
        }
    }

    public static class DriPhdb {
        public int time;
        public BasicPhdbType basic;
        public Ext1Phdb ext1;
        public Ext2Phdb ext2;
        public Ext3Phdb ext3;
        public byte marker;
        public byte reserved;
        public short clDrilvlSubt;

        public DriPhdb() {
            this.basic = new BasicPhdbType();
            this.ext1 = new Ext1Phdb();
            this.ext2 = new Ext2Phdb();
            this.ext3 = new Ext3Phdb();
        }
    }

    public static class BasicPhdbType {
        public EcgGroupType ecg = new EcgGroupType();
        public PGroupType p1 = new PGroupType();
        public PGroupType p2 = new PGroupType();
        public PGroupType p3 = new PGroupType();
        public PGroupType p4 = new PGroupType();
        public NibpGroupType nibp = new NibpGroupType();
        public TGroupType t1 = new TGroupType();
        public TGroupType t2 = new TGroupType();
        public TGroupType t3 = new TGroupType();
        public TGroupType t4 = new TGroupType();
        public SpO2GroupType SpO2 = new SpO2GroupType();
        public Co2GroupType co2 = new Co2GroupType();
        public O2GroupType o2 = new O2GroupType();
        public N2oGroupType n2o = new N2oGroupType();
        public AaGroupType aa = new AaGroupType();
        public FlowVolGroupType flowVol = new FlowVolGroupType();
        public CoWedgeGroupType coWedge = new CoWedgeGroupType();
        public NmtGroup nmt = new NmtGroup();
        public EcgExtraGroup ecgExtra = new EcgExtraGroup();
        public Svo2Group svo2 = new Svo2Group();
        public PGroupType p5 = new PGroupType();
        public PGroupType p6 = new PGroupType();
        public byte[] reserved = new byte[2];

        public BasicPhdbType() {
        }
    }

    public static class Ext1Phdb {
        public ArrhEcgGroup ecg;
        public Ecg12Group ecg12;
        public byte[] reserved;

        public Ext1Phdb() {
            this.ecg = new ArrhEcgGroup();
            this.ecg12 = new Ecg12Group();
            this.reserved = new byte[192];
        }
    }

    public static class Ext2Phdb {
        public Nmt2Group nmt2;
        public EegGroup eeg;
        public EegBisGroup eegBis;
        public EntropyGroup ent;
        public byte[] reserved1;
        public Eeg2Group eeg2;
        public byte[] reserved;

        public Ext2Phdb() {
            this.nmt2 = new Nmt2Group();
            this.eeg = new EegGroup();
            this.eegBis = new EegBisGroup();
            this.ent = new EntropyGroup();
            this.reserved1 = new byte[58];
            this.eeg2 = new Eeg2Group();
            this.reserved = new byte[41];
        }
    }

    public static class Ext3Phdb {
        public GasexGroup gasex;
        public FlowVolGroup2 flowVol2;
        public BalGasGroup bal;
        public TonoGroup tono;
        public Aa2Group aa2;
        public byte[] reserved;

        public Ext3Phdb() {
            this.gasex = new GasexGroup();
            this.flowVol2 = new FlowVolGroup2();
            this.bal = new BalGasGroup();
            this.tono = new TonoGroup();
            this.aa2 = new Aa2Group();
            this.reserved = new byte[154];
        }
    }

    public static class AaGroupType {
        public GroupHdrType hdr;
        public short et;
        public short fi;
        public short macSum;

        public AaGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class Aa2Group {
        public GroupHdrType hdr;
        public short macAgeSum;
        public byte[] reserved;

        public Aa2Group() {
            this.hdr = new GroupHdrType();
            this.reserved = new byte[16];
        }
    }

    public static class ArrhEcgGroup {
        public GroupHdrType hdr;
        public short hr;
        public short rrTime;
        public short pvc;
        public int arrhReserved;
        public short[] reserved;

        public ArrhEcgGroup() {
            this.hdr = new GroupHdrType();
            this.reserved = new short[16];
        }
    }

    public static class BalGasGroup {
        public GroupHdrType hdr;
        public short et;
        public short fi;

        public BalGasGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class CoWedgeGroupType {
        public GroupHdrType hdr = new GroupHdrType();
        public short co;
        public short bloodTemp;
        public short rightef;
        public short pcwp;
    }

    public static class Co2GroupType {
        public GroupHdrType hdr = new GroupHdrType();
        public short et;
        public short fi;
        public short rr;
        public short ambPress;
    }

    public static class DataConstants {
        public static final byte BIT5 = 124;
        public static final byte BIT5COMPL = 95;
        public static final byte CTRLCHAR = 125;
        public static final int DATA_DISCONT = -32765;
        public static final int DATA_INVALID = -32767;
        public static final int DATA_INVALID_LIMIT = -32001;
        public static final int DATA_NOT_CALIBRATED = -32762;
        public static final int DATA_NOT_UPDATED = -32766;
        public static final int DATA_OVER_RANGE = -32763;
        public static final int DATA_UNDER_RANGE = -32764;
        public static final byte DRI_LEVEL_2000 = 6;
        public static final byte DRI_LEVEL_2001 = 7;
        public static final byte DRI_LEVEL_2003 = 8;
        public static final byte DRI_LEVEL_2005 = 9;
        public static final byte DRI_LEVEL_95 = 2;
        public static final byte DRI_LEVEL_97 = 3;
        public static final byte DRI_LEVEL_98 = 4;
        public static final byte DRI_LEVEL_99 = 5;
        public static final int DRI_MAX_PHDBRECS = 5;
        public static final int DRI_MAX_SUBRECS = 8;
        public static final short DRI_MT_ALARM = 4;
        public static final short DRI_MT_PHDB = 0;
        public static final short DRI_MT_WAVE = 1;
        public static final byte DRI_PH_10S_TREND = 2;
        public static final byte DRI_PH_60S_TREND = 3;
        public static final byte DRI_PH_DISPL = 1;
        public static final int DRI_PHDBCL_DENY_BASIC_MASK = 1;
        public static final int DRI_PHDBCL_REQ_BASIC_MASK = 0;
        public static final int DRI_PHDBCL_REQ_EXT1_MASK = 2;
        public static final int DRI_PHDBCL_REQ_EXT2_MASK = 4;
        public static final int DRI_PHDBCL_REQ_EXT3_MASK = 8;
        public static final byte FRAMECHAR = 126;
    }

    public static class DatexHdrType implements Serializable {
        public short rLen;
        public byte rNbr;
        public byte rDriLevel;
        public short plugId;
        public int rTime;
        public byte reserved1;
        public byte reserved2;
        public short reserved3;
        public short rMaintype;
        public short srOffset1;
        public byte srType1;
        public short srOffset2;
        public byte srType2;
        public short srOffset3;
        public byte srType3;
        public short srOffset4;
        public byte srType4;
        public short srOffset5;
        public byte srType5;
        public short srOffset6;
        public byte srType6;
        public short srOffset7;
        public byte srType7;
        public short srOffset8;
        public byte srType8;

        public DatexHdrType() {
        }
    }

    public static class Ecg12Group {
        public GroupHdrType hdr;
        public short stI;
        public short stII;
        public short stIII;
        public short stAVL;
        public short stAVR;
        public short stAVF;
        public short stV1;
        public short stV2;
        public short stV3;
        public short stV4;
        public short stV5;
        public short stV6;

        public Ecg12Group() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class EcgExtraGroup {
        public short hrEcg;
        public short hrMax;
        public short hrMin;

        public EcgExtraGroup() {
        }
    }

    public static class EcgGroupType {
        public GroupHdrType hdr;
        public short hr;
        public short st1;
        public short st2;
        public short st3;
        public short impRr;

        public EcgGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class EegBisGroup {
        public GroupHdrType hdr;
        public short bis;
        public short sqiVal;
        public short emgVal;
        public short srVal;
        public short reserved;

        public EegBisGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class EegChannel {
        public short ampl;
        public short sef;
        public short mf;
        public short deltaProc;
        public short thetaProc;
        public short alphaProc;
        public short betaProc;
        public short bsr;

        public EegChannel() {
        }
    }

    public static class EegGroup {
        public GroupHdrType hdr;
        public short femg;
        public EegChannel eeg1;
        public EegChannel eeg2;
        public EegChannel eeg3;
        public EegChannel eeg4;

        public EegGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class Eeg2Group {
        public GroupHdrType hdr;
        public byte commonReference;
        public byte montageLabelCh1M;
        public byte montageLabelCh1P;
        public byte montageLabelCh2M;
        public byte montageLabelCh2P;
        public byte montageLabelCh3M;
        public byte montageLabelCh3P;
        public byte montageLabelCh4M;
        public byte montageLabelCh4P;
        public short[] reserved;

        public Eeg2Group() {
            this.hdr = new GroupHdrType();
            this.reserved = new short[8];
        }
    }

    public static class EntropyGroup {
        public GroupHdrType hdr;
        public short eegEnt;
        public short emgEnt;
        public short bsrEnt;
        public short[] reserved;

        public EntropyGroup() {
            this.hdr = new GroupHdrType();
            this.reserved = new short[8];
        }
    }

    public static class FlowVolGroupType {
        public GroupHdrType hdr;
        public short rr;
        public short ppeak;
        public short peep;
        public short pplat;
        public short tvInsp;
        public short tvExp;
        public short compliance;
        public short mvExp;

        public FlowVolGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class FlowVolGroup2 {
        public GroupHdrType hdr;
        public short ipeep;
        public short pmean;
        public short raw;
        public short mvInsp;
        public short epeep;
        public short mvSpont;
        public short ieRatio;
        public short inspTime;
        public short expTime;
        public short staticCompliance;
        public short staticPplat;
        public short staticPeepe;
        public short staticPeepl;
        public short[] reserved;

        public FlowVolGroup2() {
            this.hdr = new GroupHdrType();
            this.reserved = new short[7];
        }
    }

    public static class GasexGroup {
        public GroupHdrType hdr;
        public short vo2;
        public short vco2;
        public short ee;
        public short rq;

        public GasexGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class GroupHdrType {
        public int statusBits;
        public short labelInfo;
    }

    public static class N2oGroupType {
        public GroupHdrType hdr;
        public short et;
        public short fi;

        public N2oGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class NibpGroupType {
        public GroupHdrType hdr;
        public short sys;
        public short dia;
        public short mean;
        public short hr;

        public NibpGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class NmtGroup {
        public GroupHdrType hdr;
        public short t1;
        public short tratio;
        public short ptc;

        public NmtGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class Nmt2Group {
        public GroupHdrType hdr;
        public short reserved;
        public short nmtT1;
        public short nmtT2;
        public short nmtT3;
        public short nmtT4;
        public short nmtResv1;
        public short nmtResv2;
        public short nmtResv3;
        public short nmtResv4;

        public Nmt2Group() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class O2GroupType {
        public GroupHdrType hdr;
        public short et;
        public short fi;

        public O2GroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class PGroupType {
        public GroupHdrType hdr;
        public short sys;
        public short dia;
        public short mean;
        public short hr;

        public PGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class PhdbReqType {
        public byte phdbRcrdType;
        public short txInterval;
        public int phdbClassBf;
        private short reserved;

        public PhdbReqType() {
        }
    }

    public static class SpO2GroupType {
        public GroupHdrType hdr;
        public short SpO2;
        public short pr;
        public short irAmp;
        public short svo2;

        public SpO2GroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class SrDescType {
        public short srOffset;
        public byte srType;

        public SrDescType() {
        }
    }

    public static class Svo2Group {
        public GroupHdrType hdr;
        public short svo2;

        public Svo2Group() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class TGroupType {
        public GroupHdrType hdr;
        public short temp;

        public TGroupType() {
            this.hdr = new GroupHdrType();
        }
    }

    public static class TonoGroup {
        public GroupHdrType hdr;
        public short prco2;
        public short prEt;
        public short prPa;
        public short paDelay;
        public short phi;
        public short phiDelay;
        public short ambPress;
        public short cpma;

        public TonoGroup() {
            this.hdr = new GroupHdrType();
        }
    }

    public enum InvalidDataValue {
        DataInvalid = -32767,
        DataNotUpdated = -32766,
        DataUnderRange = -32764,
        DataOverRange = -32763,
        DataNotCalibrated = -32762
    }

    public static class DataItem {
        public String Name;
        public String Time;
        public String Value;
    }
}