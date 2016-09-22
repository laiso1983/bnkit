package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.DirDT;
import bn.node.GDT;
import dat.EnumVariable;
import dat.Variable;

/**
 * Created by aesseb on 19-Sep-16.
 */
public class DREAM_ensm {

    public static void main(String[] args) {

        EnumVariable MODELLATENT = Predef.Nominal(new String[]{"0","1","2","3","4"}, "ChipCount");
//        EnumVariable MODELLATENT = Predef.Nominal(new String[]{"0","1"}, "ChipCount");
        CPT modellatent = new CPT(MODELLATENT);

//        Variable M1 = Predef.Real("M1");
//        GDT m1 = new GDT(M1, MODELLATENT);
//
//        Variable M2 = Predef.Real("M2");
//        GDT m2 = new GDT(M2, MODELLATENT);
//
//        Variable M3 = Predef.Real("M3");
//        GDT m3 = new GDT(M3, MODELLATENT);
//
//        Variable M4 = Predef.Real("M4");
//        GDT m4 = new GDT(M4, MODELLATENT);
//
//        Variable M5 = Predef.Real("M5");
//        GDT m5 = new GDT(M5, MODELLATENT);

        Variable M1 = Predef.Distrib(new String[]{"0","1","2","3","4"}, "M1");
        DirDT m1 = new DirDT(M1, MODELLATENT);

        Variable M2 = Predef.Distrib(new String[]{"0","1","2","3","4"}, "M2");
        DirDT m2 = new DirDT(M2, MODELLATENT);

        Variable M3 = Predef.Distrib(new String[]{"0","1","2","3","4"}, "M3");
        DirDT m3 = new DirDT(M3, MODELLATENT);

        Variable M4 = Predef.Distrib(new String[]{"0","1","2","3","4"}, "M4");
        DirDT m4 = new DirDT(M4, MODELLATENT);

        Variable M5 = Predef.Distrib(new String[]{"0","1","2","3","4"}, "M5");
        DirDT m5 = new DirDT(M5, MODELLATENT);

        BNet bn = new BNet();
        bn.add(modellatent, m1, m2, m3, m4, m5);

        String bn_file = "X:/DREAM/Training/networks/dream_model_foxa1_3.out";
        BNBuf.save(bn, bn_file);

    }

}
