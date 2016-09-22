package bn.example;

import bn.BNet;
import bn.BNode;
import bn.Predef;
import bn.alg.EM;
import bn.file.BNBuf;
import bn.node.CPT;
import bn.node.DirDT;
import dat.EnumVariable;
import dat.Enumerable;
import dat.Variable;

import java.util.List;

import static bn.file.DataBuf.load;

/**
 * Created by aesseb on 24-Mar-16.
 */
public class ValTF1 {

    public static void main(String[] args) {

        long seed = 1;
        //Arguments
        //datafile, colPeaks, colKmer, clusters,

        // add data file into data folder
        String filename = args[0];
        int segments = Integer.parseInt(args[1]); //Used to figure out which columns belong to which distribution
        int ncluster = Integer.parseInt(args[2]);
        int ecluster = Integer.parseInt(args[3]);

//        int[][] data = loadData(filename);

        BNet bn = new BNet();

        EnumVariable Cluster = Predef.Number(ncluster, "Cluster");
        CPT cluster = new CPT(Cluster);

        EnumVariable EpiSig = Predef.Number(ecluster, "EpiSignal");
        CPT epiSig = new CPT(EpiSig);

        Enumerable tf = new Enumerable(segments);
        Variable TF = Predef.Distrib(tf, "TFNode");
        DirDT tfN = new DirDT(TF, Cluster, EpiSig);

        bn.add(cluster, tfN, epiSig);
        List<BNode> nodes = bn.getAlphabetical();

        Variable[] vars = new Variable[nodes.size()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = nodes.get(i).getVariable();
        }

        Object[][] data = load(filename, nodes);

        EM em = new EM(bn);
        em.train(data, nodes);
        BNBuf.save(bn, filename + ".xml");
    }


}
