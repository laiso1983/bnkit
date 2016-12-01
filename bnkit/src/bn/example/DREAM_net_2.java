package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.GDT;
import bn.prob.EnumDistrib;
import bn.prob.GaussianDistrib;
import dat.EnumVariable;
import dat.Enumerable;
import dat.Variable;

import java.util.Arrays;

/**
 * Created by aesseb on 23-Aug-16.
 */
public class DREAM_net_2 {

    public static void main(String[] args) {

        EnumVariable DHS_PEAK_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsPeakLabel");
        CPT dhs_peak_l = new CPT(DHS_PEAK_L);
        EnumVariable DHS_gFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5"}, "dhsGFootLabel");
        CPT dhs_gFoot_l = new CPT(DHS_gFOOT_L);
        EnumVariable DHS_sFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsSFootLabel");
        CPT dhs_sFoot_l = new CPT(DHS_sFOOT_L);

        EnumVariable DHS = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "DHS");
        CPT dhs = new CPT(DHS, DHS_PEAK_L, DHS_gFOOT_L, DHS_sFOOT_L);
//        CPT dhs = new CPT(DHS, DHS_PEAK_L, DHS_gFOOT_L);

        EnumVariable GFLATENT = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "GFLatent");
        CPT gflatent = new CPT(GFLATENT);

        Variable GCOUNT = Predef.Real("dhsGFootCount");
        GDT gcount = new GDT(GCOUNT, GFLATENT);

        EnumVariable LATENT = Predef.Nominal(new String[]{"1", "2", "3"}, "distance_latent");
        CPT latent = new CPT(LATENT);

        Variable DISTANCE = Predef.Real("logDistance");
        GDT distance = new GDT(DISTANCE, LATENT);

        EnumVariable CHIP = Predef.Nominal(new String[]{"0","1","2","3","4"}, "ChipCount");
        CPT chip = new CPT(CHIP, DHS, GFLATENT, LATENT);

        EnumVariable MOTIFLATENT = Predef.Nominal(new String[]{"0","1","2","3","4"}, "motif_latent");
        CPT motiflatent = new CPT(MOTIFLATENT, CHIP);

        Variable LOGMOTIF = Predef.Real("MotifScore");
        GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);

        BNet bn = new BNet();
        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l, logmotif, distance, latent, motiflatent, gflatent, gcount);
//        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, distance, latent, gflatent, gcount);

//        String tf = "E2F6";
//        String[] cells = {"A549","H1-hESC","HeLa-S3"};
//        int repeats = 4;

//        String tf = "E2F1";
//        String[] cells = {"GM12878","HeLa-S3"};
//        int repeats = 5;

//        String tf = "Arid3a";
//        String[] cells = {"HepG2"};
//        int repeats = 10;

//        String tf = "FOXA1";
//        String[] cells = {"HepG2"};
//        int repeats = 10;

//        String tf = "Foxa2";
//        String[] cells = {"HepG2"};
//        int repeats = 10;

//        String tf = "GATA3";
//        String[] cells = {"A549", "SK-N-SH"};
//        int repeats = 5;

//        String tf = "Hnf4a";
//        String[] cells = {"HepG2"};
//        int repeats = 10;

//        String tf = "SPI1";
//        String[] cells = {"GM12878"};
//        int repeats = 10;

//        String tf = "STAT3";
//        String[] cells = {"HeLa-S3"};
//        int repeats = 10;

//        String tf = "MAFK";
//        String[] cells = {"HepG2", "HeLa-S3", "GM12878", "H1-hESC", "IMR-90"};
//        int repeats = 2;

//        String tf = "ATF2";
//        String[] cells = {"GM12878", "H1-hESC", "MCF-7"};
//        int repeats = 4;

//        String tf = "EGR1";
//        String[] cells = {"GM12878", "H1-hESC", "MCF-7", "HCT116"};
//        int repeats = 3;

//        String tf = "NANOG";
//        String[] cells = {"H1-hESC"};
//        int repeats = 10;

//        String tf = "REST";
//        String[] cells = {"H1-hESC", "HeLa-S3", "HepG2", "MCF-7", "Panc1", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "RFX5";
//        String[] cells = {"GM12878", "HeLa-S3", "MCF-7", "SK-N-SH"};
//        int repeats = 3;

//        String tf = "TCF7L2";
//        String[] cells = {"HeLa-S3", "HCT116", "Panc1"};
//        int repeats = 4;

//        String tf = "Tcf12";
//        String[] cells = {"H1-hESC", "GM12878", "MCF-7", "SK-N-SH"};
//        int repeats = 3;

//        String tf = "YY1";
//        String[] cells = {"H1-hESC", "GM12878", "HepG2", "HCT116", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "ZNF143";
//        String[] cells = {"H1-hESC", "HeLa-S3", "HepG2", "GM12878"};
//        int repeats = 3;

//        String tf = "CTCF";
//        String[] cells = {"A549", "H1-hESC", "HeLa-S3", "HepG2", "IMR-90", "K562", "MCF-7"};
//        int repeats = 2;

//        String tf = "Atf3";
//        String[] cells = {"HCT116", "H1-hESC", "HepG2", "K562"};
//        int repeats = 3;

//        String tf = "ATF7";
//        String[] cells = {"HepG2", "K562", "GM12878"};
//        int repeats = 4;

//        String tf = "CEBPB";
//        String[] cells = {"A549", "H1-hESC", "HeLa-S3", "HepG2", "IMR-90", "K562", "HCT116"};
//        int repeats = 2;

//        String tf = "JUND";
//        String[] cells = {"HepG2","HCT116", "HeLa-S3", "K562", "MCF-7", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "TEAD4";
//        String[] cells = {"A549","H1-hESC", "HepG2", "HCT116", "K562", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "EP300";
//        String[] cells = {"GM12878", "H1-hESC", "HeLa-S3", "HepG2", "K562", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "TAF1";
//        String[] cells = {"GM12878", "H1-hESC", "HeLa-S3", "K562", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "CREB1";
//        String[] cells = {"H1-hESC", "HepG2", "K562", "GM12878"};
//        int repeats = 3;

//        String tf = "SRF";
//        String[] cells = {"H1-hESC", "HepG2", "HCT116", "K562", "GM12878"};
//        int repeats = 2;

//        String tf = "MAX";
//        String[] cells = {"HepG2", "A549", "HCT116", "K562", "H1-hESC", "HeLa-S3", "GM12878", "SK-N-SH"};
//        int repeats = 2;

//        String tf = "Myc";
//        String[] cells = {"A549", "K562", "MCF-7", "HeLa-S3"};
//        int repeats = 3;

        String tf = "Gabpa";
        String[] cells = {"HepG2","H1-hESC","HeLa-S3","GM12878","MCF-7","SK-N-SH"};
        int repeats = 2;

        for (int c = 0; c < cells.length; c++) {
            String cell = cells[c];
            for (int r = 0; r < repeats; r++){
                String bn_file = "X:/DREAM/Data/"+tf+"/dream_net_"+tf+"_"+cell+"_m"+r+"_n6.out";
                BNBuf.save(bn, bn_file);
            }
        }

//        String bn_file = "X:/DREAM/Training/networks/dream_net_spi1_6.out";
//        BNBuf.save(bn, bn_file);

    }

}
