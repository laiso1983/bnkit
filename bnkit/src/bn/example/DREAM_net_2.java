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

//        EnumVariable DHS_PEAK = Predef.Boolean("dhsPeak");
//        CPT dhs_peak = new CPT(DHS_PEAK);
//        EnumVariable DHS_gFOOT = Predef.Boolean("dhsGFoot");
//        CPT dhs_gFoot = new CPT(DHS_gFOOT);
//        EnumVariable DHS_sFOOT = Predef.Boolean("dhsSFoot");
//        CPT dhs_sFoot = new CPT(DHS_sFOOT);

        EnumVariable DHS_PEAK_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsPeakLabel");
        CPT dhs_peak_l = new CPT(DHS_PEAK_L);
        EnumVariable DHS_gFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5"}, "dhsGFootLabel");
        CPT dhs_gFoot_l = new CPT(DHS_gFOOT_L);
        EnumVariable DHS_sFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsSFootLabel");
        CPT dhs_sFoot_l = new CPT(DHS_sFOOT_L);

//        EnumVariable DHS_P = Predef.Nominal(new String[]{"1", "2", "3"}, "DHS_P");
//        CPT dhs_p = new CPT(DHS_P, DHS_PEAK, DHS_PEAK_L);
//        EnumVariable DHS_G = Predef.Nominal(new String[]{"1", "2", "3"}, "DHS_G");
//        CPT dhs_g = new CPT(DHS_G, DHS_gFOOT, DHS_gFOOT_L);
//        EnumVariable DHS_S = Predef.Nominal(new String[]{"1", "2", "3"}, "DHS_S");
//        CPT dhs_s = new CPT(DHS_S, DHS_sFOOT, DHS_sFOOT_L);

        EnumVariable GFLATENT = Predef.Nominal(new String[]{"0","1","2", "3"}, "GFLatent");
        CPT gflatent = new CPT(GFLATENT);

        Variable GCOUNT = Predef.Real("dhsGFootCount");
        GDT gcount = new GDT(GCOUNT, GFLATENT);

        EnumVariable DHS = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "DHS");
//        CPT dhs = new CPT(DHS, DHS_PEAK, DHS_gFOOT, DHS_sFOOT);
//        CPT dhs = new CPT(DHS, DHS_PEAK_L, DHS_gFOOT_L, DHS_sFOOT_L);
        CPT dhs = new CPT(DHS, DHS_PEAK_L, DHS_gFOOT_L, DHS_sFOOT_L);

        EnumVariable LATENT = Predef.Nominal(new String[]{"1", "2", "3"}, "distance_latent");
//        CPT latent = new CPT(LATENT, DHS, DISTANCE);
        CPT latent = new CPT(LATENT);

        Variable DISTANCE = Predef.Real("logDistance");
        GDT distance = new GDT(DISTANCE, LATENT);

//        EnumVariable EXPR = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "ExpressionCluster");
//        CPT expr = new CPT(EXPR);
//
//        EnumVariable PROCLUS = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "PromoterState");
//        CPT proclus = new CPT(PROCLUS);

//        Variable EXPRVAL = Predef.Real("Expression");
//        GDT exprval = new GDT(EXPRVAL, LATENT);

//        EnumVariable CHIP = Predef.Nominal(new String[]{"B", "A", "U"}, "ChipLabel");
        EnumVariable CHIP = Predef.Nominal(new String[]{"0","1","2","3","4"}, "ChipCount");
        CPT chip = new CPT(CHIP, DHS, GFLATENT, LATENT);
//        CPT chip = new CPT(CHIP, LATENT, LATENTEXPR, MOTIFLATENT);
//        CPT chip = new CPT(CHIP, DHS, MOTIF);0

        EnumVariable MOTIFLATENT = Predef.Nominal(new String[]{"0","1","2","3","4"}, "motif_latent");
        CPT motiflatent = new CPT(MOTIFLATENT, CHIP);

        Variable LOGMOTIF = Predef.Real("MotifScore");
        GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);

//        Variable GCCONTENT = Predef.Real("GCWindow");
//        GDT gccontent = new GDT(GCCONTENT, MOTIFLATENT);

        BNet bn = new BNet();
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine, logmotif, motiflatent);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, expr, motiflatent, logmotif);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, motiflatent, logmotif, dhs_g, dhs_p, dhs_s, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l);
        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l, logmotif, distance, latent, motiflatent, gflatent, gcount);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, motif);

        String tf = "E2F6";
        String[] cells = {"A549","H1-hESC","HeLa-S3"};
        int repeats = 4;

        for (int c = 0; c < cells.length; c++) {
            String cell = cells[c];
            for (int r = 0; r < repeats; r++){
                String bn_file = "X:/DREAM/Training/networks/dream_net_"+tf+"_"+cell+"_m"+r+"_n6.out";
                BNBuf.save(bn, bn_file);
            }
        }

        String bn_file = "X:/DREAM/Training/networks/dream_net_spi1_6.out";
        BNBuf.save(bn, bn_file);

    }

}
