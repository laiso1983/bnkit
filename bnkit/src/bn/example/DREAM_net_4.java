package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.GDT;
import dat.EnumVariable;
import dat.Variable;

/**
 * Created by aesseb on 09-Sep-16.
 */
public class DREAM_net_4 {

    public static void main(String[] args) {

        EnumVariable DHS_PEAK_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsPeakLabel");
        CPT dhs_peak_l = new CPT(DHS_PEAK_L);
        EnumVariable DHS_gFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5"}, "dhsGFootLabel");
        CPT dhs_gFoot_l = new CPT(DHS_gFOOT_L);
        EnumVariable DHS_sFOOT_L = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, "dhsSFootLabel");
        CPT dhs_sFoot_l = new CPT(DHS_sFOOT_L);

        EnumVariable GFLATENT = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "GFLatent");
        CPT gflatent = new CPT(GFLATENT);

        Variable GCOUNT = Predef.Real("dhsGFootCount");
        GDT gcount = new GDT(GCOUNT, GFLATENT);

        EnumVariable DHS = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "DHS");
        CPT dhs = new CPT(DHS, DHS_PEAK_L, DHS_gFOOT_L, DHS_sFOOT_L);

//        EnumVariable BCLATENT = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "BCLatent");
//        CPT bclatent = new CPT(BCLATENT);
//
//        Variable BCOUNT = Predef.Real("BindingCount");
//        GDT bcount = new GDT(BCOUNT, BCLATENT);

        EnumVariable MIDLATENT = Predef.Nominal(new String[]{"0", "1", "2", "3", "4"}, "MidLatent");
        CPT midlatent = new CPT(MIDLATENT, DHS, GFLATENT);

//        EnumVariable EXPR = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "ExpressionCluster");
//        CPT expr = new CPT(EXPR);
//
//        EnumVariable PROCLUS = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "PromoterState");
//        CPT proclus = new CPT(PROCLUS);

        EnumVariable LATENT = Predef.Nominal(new String[]{"1", "2", "3"}, "distance_latent");
//        CPT latent = new CPT(LATENT, EXPR, PROCLUS, MIDLATENT);
        CPT latent = new CPT(LATENT, MIDLATENT);

        Variable DISTANCE = Predef.Real("logDistance");
        GDT distance = new GDT(DISTANCE, LATENT);

//        Variable EXPRVAL = Predef.Real("Expression");
//        GDT exprval = new GDT(EXPRVAL, LATENT);

        EnumVariable MOTIFLATENT = Predef.Nominal(new String[]{"0", "1", "2", "3", "4", "5"}, "motif_latent");
        CPT motiflatent = new CPT(MOTIFLATENT, MIDLATENT);

        Variable LOGMOTIF = Predef.Real("MotifScore");
        GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);

        Variable GCCONTENT = Predef.Real("GCWindow");
        GDT gccontent = new GDT(GCCONTENT, MOTIFLATENT);

        EnumVariable CHIP = Predef.Nominal(new String[]{"0", "1", "2", "3", "4"}, "ChipCount");
        CPT chip = new CPT(CHIP, MIDLATENT);

        BNet bn = new BNet();
//        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l, logmotif, distance, latent, motiflatent, gccontent, gcount, gflatent, expr, proclus, exprval, midlatent);
        bn.add(chip, dhs, dhs_gFoot_l, dhs_peak_l, dhs_sFoot_l, logmotif, distance, latent, motiflatent, gccontent, gcount, gflatent, midlatent);

        String bn_file = "X:/DREAM/Training/networks/dream_net_48.out";
        BNBuf.save(bn, bn_file);
    }
}
