package bn.example;

import bn.BNet;
import bn.BNode;
import bn.alg.EM;
import bn.file.BNBuf;
import dat.Variable;

import java.util.List;

import static bn.file.DataBuf.load;

/**
 * Created by aesseb on 05-Apr-16.
 */
public class TF_LNT {

    public static void main(String[] args) {
        String datafile = args[0];
        String netfile = args[1];
        BNet bn = BNBuf.load(netfile);
        List<BNode> nodes = bn.getAlphabetical();
        Variable[] vars = new Variable[nodes.size()];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = nodes.get(i).getVariable();
        }

        Object[][] data = load(datafile, nodes);

        long start = System.currentTimeMillis();
        EM em = new EM(bn);
        em.train(data, nodes);
        long end = System.currentTimeMillis();
        long time = end - start;
        BNBuf.save(bn, datafile + "_" + time + ".xml");
//
    }

}
