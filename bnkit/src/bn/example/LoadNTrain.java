/**
 * 
 */
package bn.example;
import bn.alg.LearningAlg;
import bn.alg.EM;

import java.util.List;

import bn.*;
import bn.file.*;

/**
 * @author mikael
 *
 */
public class LoadNTrain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: LoadNTrain <bn-file> <data-file>");
			System.exit(1);
		}
		String bn_file = args[0];
		String data_file = args[1];
		BNet bn = BNBuf.load(bn_file);
		List<BNode> nodes = bn.getOrdered();
		Object[][] data = DataBuf.load(data_file, nodes);

		int maxRounds = 1000;
		int emCase = 1;

		EM em = new EM(bn);
		em.setEMOption(emCase);
		em.setPrintStatus(true);
		em.setMaxRounds(maxRounds);
		em.setThreadCount(1);
		double start = System.currentTimeMillis();
		em.train(data, nodes);
		double end = System.currentTimeMillis();
		double time = end - start;
		System.out.println("Training time = " + time);
		BNBuf.save(bn, bn_file + "_trained.new");
		
//		TestNetwork tn = new TestNetwork(bn, data, "Variance");
	}

}

//"bnkit/data/1420676232637cg.new" "bnkit/data/FD4UNR-50000.txt"