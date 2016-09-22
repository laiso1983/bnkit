package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.GDT;
import dat.EnumVariable;
import dat.Variable;

import java.util.Arrays;

/**
 * Created by aesseb on 22-Aug-16.
 */
public class DREAM_net_iterative {

    public static void main(String[] args) {

        EnumVariable DHS_PEAK = Predef.Boolean("dhsPeak");
        CPT dhs_peak = new CPT(DHS_PEAK);
        EnumVariable DHS_gFOOT = Predef.Boolean("dhsGFoot");
        CPT dhs_gFoot = new CPT(DHS_gFOOT);
        EnumVariable DHS_sFOOT = Predef.Boolean("dhsSFoot");
        CPT dhs_sFoot = new CPT(DHS_sFOOT);

        EnumVariable EXPR = Predef.Nominal(new String[]{"0", "1", "2", "3"}, "ExpressionCluster");
        CPT expr = new CPT(EXPR);

//        EnumVariable MOTIF = Predef.Boolean("Motif");
//        CPT motif = new CPT(MOTIF);

        EnumVariable ONE = Predef.Boolean("500");
        CPT one = new CPT(ONE);
        EnumVariable TWO = Predef.Boolean("1000");
        CPT two = new CPT(TWO);
        EnumVariable THREE = Predef.Boolean("2000");
        CPT three = new CPT(THREE);
        EnumVariable FOUR = Predef.Boolean("5000");
        CPT four = new CPT(FOUR);
        EnumVariable FIVE = Predef.Boolean("12500");
        CPT five = new CPT(FIVE);
        EnumVariable SIX = Predef.Boolean("25000");
        CPT six = new CPT(SIX);
        EnumVariable SEVEN = Predef.Boolean("50000");
        CPT seven = new CPT(SEVEN);
        EnumVariable EIGHT = Predef.Boolean("100000");
        CPT eight = new CPT(EIGHT);
        EnumVariable NINE = Predef.Boolean("500000");
        CPT nine = new CPT(NINE);
//


        String[] digits = new String[]{"0","1","2","3","4","5","6","7","8"};

        EnumVariable DHS;
        EnumVariable LATENT;
        EnumVariable LATENTEXPR;
        EnumVariable DISTANCE;
        EnumVariable MOTIFLATENT;
        CPT dhs;
        CPT latent;
        CPT latentexpr;
        CPT distance;
        CPT motiflatent;

        for (int i = 2; i < 6; i++) {
            DHS = Predef.Nominal(Arrays.copyOfRange(digits, 0, i), "DHS");
            dhs = new CPT(DHS, DHS_PEAK, DHS_gFOOT, DHS_sFOOT);
            for (int j = 2; j < 6; j++) {
                LATENT = Predef.Nominal(Arrays.copyOfRange(digits, 0, j), "dhs_distance_latent");
//        CPT latent = new CPT(LATENT, DHS, DISTANCE);
                latent = new CPT(LATENT, DHS);
                for (int k = 2; k < 6; k++) {
                    LATENTEXPR = Predef.Nominal(Arrays.copyOfRange(digits, 0, k), "expr_distance_latent");
//        CPT latentexpr = new CPT(LATENTEXPR, EXPR, DISTANCE);
                    latentexpr = new CPT(LATENTEXPR, EXPR);
                    for (int m = 2; m < 6; m++) {
                        DISTANCE = Predef.Nominal(Arrays.copyOfRange(digits, 0, m), "Distance_latent");
                        distance = new CPT(DISTANCE, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE);
                        for (int n = 2; n < 6; n++) {
                            MOTIFLATENT = Predef.Nominal(Arrays.copyOfRange(digits, 0, n), "motif_latent");
                            motiflatent = new CPT(MOTIFLATENT);

//                        Variable DISTANCE = Predef.Real("logDistance");
//                        GDT distance = new GDT(DISTANCE, LATENT, LATENTEXPR);

                            Variable LOGMOTIF = Predef.Real("logMotif");
                            GDT logmotif = new GDT(LOGMOTIF, MOTIFLATENT);

                            EnumVariable CHIP = Predef.Nominal(new String[]{"B", "A", "U"}, "ChipLabel");
                            CPT chip = new CPT(CHIP, LATENT, LATENTEXPR, MOTIFLATENT);
//        CPT chip = new CPT(CHIP, DHS, MOTIF);

                            BNet bn = new BNet();
                            bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine, logmotif, motiflatent);
//                            bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, motif);

                            String bn_file = "X:/DREAM/Training/networks/dream_net_discrete_"+i+"_"+j+"_"+k+"_"+m+"_"+n+".out";
                            BNBuf.save(bn, bn_file);
                        }
                    }
                }
            }
        }

//        Variable DISTANCE = Predef.Real("logDistance");
//        GDT distance = new GDT(DISTANCE, LATENT, LATENTEXPR);
//
//        EnumVariable CHIP = Predef.Nominal(new String[]{"B", "A", "U"}, "ChipLabel");
//        CPT chip = new CPT(CHIP, DHS, LATENT, LATENTEXPR);
////        CPT chip = new CPT(CHIP, DHS, MOTIF);
//
//        BNet bn = new BNet();
////        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr, one, two, three, four, five, six, seven, eight, nine);
//        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, distance, latent, latentexpr,expr);
////        bn.add(chip, dhs, dhs_gFoot, dhs_peak, dhs_sFoot, motif);
//
//        String bn_file = "X:/DREAM/Training/networks/dream_net_19.out";
//        BNBuf.save(bn, bn_file);

    }



}
