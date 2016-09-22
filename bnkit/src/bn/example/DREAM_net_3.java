package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.GDT;
import bn.prob.GaussianDistrib;
import dat.EnumVariable;
import dat.Variable;

/**
 * Created by aesseb on 30-Aug-16.
 */
public class DREAM_net_3 {

    public static void main(String[] args) {

        EnumVariable DHS_PEAK_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsPeakLabel");
        CPT dhs_peak_l = new CPT(DHS_PEAK_L);
        EnumVariable DHS_gFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5"}, "dhsGFootLabel");
        CPT dhs_gFoot_l = new CPT(DHS_gFOOT_L);
        EnumVariable DHS_sFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsSFootLabel");
        CPT dhs_sFoot_l = new CPT(DHS_sFOOT_L);

        EnumVariable GFLATENT = Predef.Nominal(new String[]{"0","1","2","3"}, "GFLatent");
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
//        distance.randomize(System.currentTimeMillis());

//        EnumVariable EXPR = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "ExpressionCluster");
////        EnumVariable EXPR = Predef.Nominal(new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12" }, "ExpressionCluster");
//        CPT expr = new CPT(EXPR, CHIP);

//        EnumVariable MOTIFDIST = Predef.Nominal(new String[]{"0","1","2","3","4"}, "Index");
//        CPT motifdist = new CPT(MOTIFDIST);

        //        EnumVariable CHIP = Predef.Nominal(new String[]{"B", "A", "U"}, "ChipLabel");
        EnumVariable CHIP = Predef.Nominal(new String[]{"0","1","2","3","4","5","6","7"}, "ChipCount");
        CPT chip = new CPT(CHIP, LATENT, DHS, GFLATENT);
//        CPT chip = new CPT(CHIP, LATENT, LATENTEXPR, MOTIFLATENT);
//        CPT chip = new CPT(CHIP, DHS, MOTIF);0

        EnumVariable MOTIFLATENT = Predef.Nominal(new String[]{"0","1","2","3","4","5"}, "motif_latent");
        CPT motiflatent = new CPT(MOTIFLATENT,CHIP);

        Variable LOGMOTIF = Predef.Real("MotifScore");
        GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);
//        logmotif.randomize(System.currentTimeMillis());
//        logmotif.put(new GaussianDistrib(10, 2), "0");
//        logmotif.put(new GaussianDistrib(8, 2), "1");
//        logmotif.put(new GaussianDistrib(4, 2), "2");
//        logmotif.put(new GaussianDistrib(0, 2), "3");
//        logmotif.put(new GaussianDistrib(-2, 2), "4");
//        logmotif.put(new GaussianDistrib(5, 2), "5");

        Variable GCCONTENT = Predef.Real("GCMotif");
        GDT gccontent = new GDT(GCCONTENT, MOTIFLATENT);
//        gccontent.put(new GaussianDistrib(55, 0.1), "0");
//        gccontent.put(new GaussianDistrib(52, 0.1), "1");
//        gccontent.put(new GaussianDistrib(48, 0.1), "2");
//        gccontent.put(new GaussianDistrib(46, 0.1), "3");
//        gccontent.put(new GaussianDistrib(44, 0.1), "4");
//        gccontent.put(new GaussianDistrib(50, 0.1), "5");

//        Variable GCWINDOW = Predef.Real("GCWindow");
//        GDT gcwindow = new GDT(GCWINDOW, MOTIFLATENT);



        BNet bn = new BNet();
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, motiflatent, logmotif, dhs_g, dhs_p, dhs_s, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l);
        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l, logmotif, distance, latent, motiflatent, gccontent, gcount, gflatent);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, motif);

        String bn_file = "X:/DREAM/Training/networks/dream_net_41.out";
        BNBuf.save(bn, bn_file);

    }


}
