package bn.example;

import bn.BNet;
import bn.BNode;
import bn.Predef;
import bn.alg.EM;
import bn.file.BNBuf;
import bn.file.DataBuf;
import bn.node.CPT;
import bn.node.GDT;
import bn.prob.GaussianDistrib;
import dat.EnumVariable;
import dat.Variable;

import java.util.List;

/**
 * Created by aesseb on 11-Aug-16.
 */
public class DREAM_net {

    public static void main(String[] args) {

        EnumVariable DHS_PEAK = Predef.Boolean("dhsPeak");
        CPT dhs_peak = new CPT(DHS_PEAK);
        EnumVariable DHS_gFOOT = Predef.Boolean("dhsGFoot");
        CPT dhs_gFoot = new CPT(DHS_gFOOT);
        EnumVariable DHS_sFOOT = Predef.Boolean("dhsSFoot");
        CPT dhs_sFoot = new CPT(DHS_sFOOT);
        EnumVariable DHS = Predef.Nominal(new String[]{"1", "2", "3"}, "DHS");
        CPT dhs = new CPT(DHS, DHS_PEAK, DHS_gFOOT, DHS_sFOOT);

        EnumVariable EXPR = Predef.Nominal(new String[]{"0","1","2","3"}, "ExpressionCluster");
//        EnumVariable EXPR = Predef.Nominal(new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12" }, "ExpressionCluster");
        CPT expr = new CPT(EXPR);

//        EnumVariable MOTIF = Predef.Boolean("Motif");
//        CPT motif = new CPT(MOTIF);

//        EnumVariable ONE = Predef.Boolean("500");
//        CPT one = new CPT(ONE);
//        EnumVariable TWO = Predef.Boolean("1000");
//        CPT two = new CPT(TWO);
//        EnumVariable THREE = Predef.Boolean("2000");
//        CPT three = new CPT(THREE);
//        EnumVariable FOUR = Predef.Boolean("5000");
//        CPT four = new CPT(FOUR);
//        EnumVariable FIVE = Predef.Boolean("12500");
//        CPT five = new CPT(FIVE);
//        EnumVariable SIX = Predef.Boolean("25000");
//        CPT six = new CPT(SIX);
//        EnumVariable SEVEN = Predef.Boolean("50000");
//        CPT seven = new CPT(SEVEN);
//        EnumVariable EIGHT = Predef.Boolean("100000");
//        CPT eight = new CPT(EIGHT);
//        EnumVariable NINE = Predef.Boolean("500000");
//        CPT nine = new CPT(NINE);

//        EnumVariable DISTANCE = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "Distance_latent");
//        CPT distance = new CPT(DISTANCE, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE);

        EnumVariable LATENT = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "dhs_distance_latent");
//        CPT latent = new CPT(LATENT, DHS, DISTANCE);
        CPT latent = new CPT(LATENT, DHS);

        EnumVariable LATENTEXPR = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "expr_distance_latent");
//        CPT latentexpr = new CPT(LATENTEXPR, EXPR, DISTANCE);
        CPT latentexpr = new CPT(LATENTEXPR, EXPR);

        Variable DISTANCE = Predef.Real("logDistance");
        GDT distance = new GDT(DISTANCE, LATENT, LATENTEXPR);

//        EnumVariable MOTIFLATENT = Predef.Nominal(new String[]{"1", "2", "3", "4"}, "motif_latent");
//        CPT motiflatent = new CPT(MOTIFLATENT);
//
//        Variable LOGMOTIF = Predef.Real("logMotif");
//        GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);

        EnumVariable CHIP = Predef.Nominal(new String[]{"B", "A", "U"}, "ChipLabel");
        CPT chip = new CPT(CHIP, LATENT, LATENTEXPR);
//        CPT chip = new CPT(CHIP, LATENT, LATENTEXPR, MOTIFLATENT);
//        CPT chip = new CPT(CHIP, DHS, MOTIF);

        BNet bn = new BNet();
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine, logmotif, motiflatent);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine);
        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, motif);

        String bn_file = "X:/DREAM/Training/networks/dream_net_gaussian.out";
        BNBuf.save(bn, bn_file);






//        String data_pos = "bnkit/data/test_pos.out";
//        String data_neg = "bnkit/data/test_neg.out";

//        int fold = 3;
//        int maxRounds = 1000;
//        int emCase = 1;
//        String testNode = "ChipLabel";
//        double convergence = 0.001;
//
////        ValidateBoolean v = new ValidateBoolean(bn_file, data_pos, data_neg, fold, maxRounds, emCase, testNode, convergence);
//
//
//        EM em = new EM(bn);
//        em.setEMOption(emCase);
//        em.setPrintStatus(true);
//        em.setMaxRounds(maxRounds);
//        List<BNode> nodes = bn.getAlphabetical();
//        Object[][] data = DataBuf.load("bnkit/data/CTCF_H1-hESC_ran_set_exprCluster_c13_newDist_train.out", nodes);
//        double start = System.currentTimeMillis();
//        em.train(data, nodes);
//        double end = System.currentTimeMillis();
//        double time = end - start;
//        System.out.println("Training time = " + time);
//        BNBuf.save(bn, "bnkit/data/dream_net_16_trained.out");

    }

}
