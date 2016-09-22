package bn.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sim.CrossValidateTagged;
import sim.Model;
import sim.dataset.DataBuffer;
import sim.model.TaggedTrainable;
import sim.result.ROC;
import sim.result.ResultBoolean;
import sim.result.ResultSummary;
import bn.BNet;
import bn.BNode;
import bn.alg.EM;
import bn.file.BNBuf;
import bn.file.DataBuf;

public class ValidateBoolean {
	int nSets; // k-fold crossvalidation
	int seed = (int)System.currentTimeMillis();
	String bn_file;
	List<BNode> order;
	int maxRounds;
	int emCase;
	String testNode;
//	String comp;
	double convergence;
	
	public ValidateBoolean(String bn_file, String data_pos, String data_neg, int fold, int maxRounds, int emCase, String testNode, double convergence) {
		
		//Load data
		this.bn_file = bn_file;
		BNet bn = BNBuf.load(bn_file);
		this.order = bn.getAlphabetical();
		this.nSets = fold;
		this.maxRounds = maxRounds;
		this.emCase = emCase;
		this.testNode = testNode;
		this.convergence = convergence;
//		this.comp = comp;
		//Load data
		List<BNode> nodes = bn.getAlphabetical();
		Object[][] dataPos = DataBuf.load(data_pos, nodes);
		Object[][] dataNeg = DataBuf.load(data_neg, nodes);
		
		// put all sequence data in a DataBuffer
		DataBuffer<Object[]> dbuf=new DataBuffer<Object[]>();
		dbuf.putAll(dataPos, "pos");
		dbuf.putAll(dataNeg, "neg");
		
		// Create models
		ArrayList<Network> model=new ArrayList<Network>();
			for (int m=0; m<nSets; m++) {
				model.add(new Network(bn));
			}
		// create cross-validation simulation
		CrossValidateTagged<Network, ResultBoolean<Object[]>, Object[]> cv=new CrossValidateTagged<>(model, dbuf, seed);
		cv.train();
//		cv.test();

		ResultSummary<ResultBoolean<Object[]>> rsum1 = new ResultSummary<ResultBoolean<Object[]>>(cv.test());
		// collect the results and compute the accuracies
		double auc1=ROC.getROCArea(rsum1, true);
		double auc1_50=ROC.getROCAreaX(rsum1, 50, true);
		System.out.print("auc="+auc1+"\t"+auc1_50+"\n");
		
	}
	
	class Network extends Model<ResultBoolean<Object[]>, Object[]> implements TaggedTrainable<Object[]> {

		private static final long serialVersionUID = 1L;
		BNet bn;
		
		public Network(BNet bn) {
			this.bn = bn;
		}
		
		public ResultBoolean<Object[]> test(Object[] sample) {
			Object[][] data = new Object[1][sample.length];
			for (int col = 0; col < sample.length; col++) {
				Object e = sample[col];
				data[0][col] = sample[col];
			}
			TestNetwork tn = new TestNetwork(bn, data, testNode);
			double out = tn.load();
			
//			double probT = 0.0;
//			for (Entry<Integer, Double> ob : jpt.table.getMapEntries()) {
//				double val = ob.getValue();
//				Integer index = ob.getKey();
//				Object[] key = jpt.table.getKey(index);
//				if (Arrays.asList(key).contains(true)) {
//					probT = val;
//				}
//			}
			
			return new ResultBoolean<Object[]>(sample, out);
		}

		public void train(Set<Object[]> sample) {
			System.out.println(sample.size());
			EM em = new EM(bn);
			em.setEMOption(emCase);
			em.setPrintStatus(true);
			em.setMaxRounds(maxRounds);
//			em.setThreadCount(threads);
			Set<Object[]> test = sample;
			Object[] c = (Object[]) test.toArray()[0];
			Object[][] data = new Object[test.size()][c.length];
			for (int row = 0; row < test.size(); row++) {
				Object[] values = (Object[]) test.toArray()[row];
				for (int col = 0; col < values.length; col++) {
					Object e = values[col];
					data[row][col] = values[col];
				}
			}
			System.out.println();
			List<BNode> nodes = bn.getAlphabetical();
			em.train(data, nodes);
//			BNBuf.save(bn, bn_file + "2.new");
		}

		@Override
		public void train(Map<Object[], String> sample) {
			System.out.println(sample.size());
			EM em = new EM(bn);
			em.setEMOption(emCase);
			em.setPrintStatus(true);
			em.setMaxRounds(maxRounds);
//			em.setThreadCount(threads);
//			em.setComparator(comp);
			em.setConvergeCrit(convergence);
			Object[][] data = new Object[sample.size()][];
			int i=0;
			for (Map.Entry<Object[], String> entry:sample.entrySet()) { 
				Object[] key = entry.getKey();
				data[i]=new Object[key.length];
				for (int j=0; j<key.length; j++)
					data[i][j]=key[j];						// put it in the data matrix
				i++;
			}
			List<BNode> nodes = bn.getAlphabetical();
			em.train(data, nodes);
			BNBuf.save(bn, System.currentTimeMillis()+".new");
		}
		
	}
}
