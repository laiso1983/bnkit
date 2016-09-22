package bn.example;

import bn.BNet;
import bn.Predef;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.GDT;
import dat.EnumVariable;
import dat.Variable;

/**
 * Created by julianzaugg on 2/06/16.
 */
public class CreateNetwork {

    public CreateNetwork(){

    }

    public static void main(String args[]){
        String[] classLabels = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T"};

//        String[] classLabels = {"A", "B", "C"};
        EnumVariable SCORE = Predef.Nominal(classLabels, "Score_group");
        Variable ESELECT = Predef.Real("Eval");
        EnumVariable CUR215 = Predef.AminoAcid("cur215");
        EnumVariable CUR219 = Predef.AminoAcid("cur219");
        EnumVariable CUR244 = Predef.AminoAcid("cur244");
        EnumVariable CUR249 = Predef.AminoAcid("cur249");
        EnumVariable CUR317 = Predef.AminoAcid("cur317");
        EnumVariable CUR318 = Predef.AminoAcid("cur318");
        EnumVariable CUR349 = Predef.AminoAcid("cur349");
        EnumVariable CUR350 = Predef.AminoAcid("cur350");

        EnumVariable NEXT215 = Predef.AminoAcid("next215");
        EnumVariable NEXT219 = Predef.AminoAcid("next219");
        EnumVariable NEXT244 = Predef.AminoAcid("next244");
        EnumVariable NEXT249 = Predef.AminoAcid("next249");
        EnumVariable NEXT317 = Predef.AminoAcid("next317");
        EnumVariable NEXT318 = Predef.AminoAcid("next318");
        EnumVariable NEXT349 = Predef.AminoAcid("next349");
        EnumVariable NEXT350 = Predef.AminoAcid("next350");

        GDT eselect = new GDT(ESELECT, SCORE);
        CPT score = new CPT(SCORE);
        CPT cur215 = new CPT(CUR215, NEXT215);
        CPT cur219 = new CPT(CUR219, NEXT219);
        CPT cur244 = new CPT(CUR244, NEXT244);
        CPT cur249 = new CPT(CUR249, NEXT249);
        CPT cur317 = new CPT(CUR317, NEXT317);
        CPT cur318 = new CPT(CUR318, NEXT318);
        CPT cur349 = new CPT(CUR349, NEXT349);
        CPT cur350 = new CPT(CUR350, NEXT350);

        CPT next215 = new CPT(NEXT215, SCORE);
        CPT next219 = new CPT(NEXT219, SCORE);
        CPT next244 = new CPT(NEXT244, SCORE);
        CPT next249 = new CPT(NEXT249, SCORE);
        CPT next317 = new CPT(NEXT317, SCORE);
        CPT next318 = new CPT(NEXT318, SCORE);
        CPT next349 = new CPT(NEXT349, SCORE);
        CPT next350 = new CPT(NEXT350, SCORE);

        BNet BASICNETWORK = new BNet();

        BASICNETWORK.add(eselect,score,cur215, cur219, cur244, cur249, cur317, cur318, cur349, cur350,next215, next219,
                next244, next249, next317, next318, next349, next350);

//        BASICNETWORK.add(eselect);
//        BASICNETWORK.add(score);
//        BASICNETWORK.add(cur215, cur219, cur244, cur249, cur317, cur318, cur349, cur350);
//        BASICNETWORK.add(next215, next219, next244, next249, next317, next318, next349, next350);

        BNBuf.save(BASICNETWORK, "cur_next_network.txt");
//        BNBuf.save(BASICNETWORK, "/Users/julianzaugg/cur_next_network.txt");
    }
}
