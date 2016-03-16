package bn.example;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import bn.BNet;
import bn.BNode;
import bn.Distrib;
import bn.Predef;
import bn.alg.CGTable;
import bn.alg.EM;
import bn.alg.Query;
import bn.alg.VarElim;
import bn.file.BNBuf;
import bn.file.DataBuf;
import bn.node.CPT;
import bn.node.DirDT;
import bn.prob.DirichletDistrib;
import bn.prob.EnumDistrib;
import dat.EnumVariable;
import dat.Enumerable;
import dat.Variable;

public class dirPeaks {

	public static void main(String[] args) {

		if (args.length < 1) {
			System.out.println("Usage: dirPeaks <segmentFile>");
			System.exit(1);
		}

		String[] clusNames = new String[] {"a", "b", "c"};
//		String[] clusNames = new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"};
//		String[] clusNames = new String[] {"a", "b", "c", "d"};
		
//		String[] segNames = new String[] {"s1", "s2", "s3", "s4", "s5"};
//		String[] segNames = new String[] {"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "s12", "s13", "s14", "s15", "s16", "s17", "s18", "s19", "s20"};
		String[] segNames = new String[] {"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10"};
		
		// Define variables
//		Enumerable segments = new Enumerable(new String[] {"s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11", "s12", "s13", "s14", "s15", "s16", "s17", "s18", "s19", "s20"});
//		EnumVariable C = Predef.Nominal(new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"}, "Clusters");
		Enumerable segments = new Enumerable(segNames);
		EnumVariable C = Predef.Nominal(clusNames, "Clusters");
		Variable S = Predef.Distrib(segments, "Segments");

		// Define nodes (connecting the variables into an acyclic graph, i.e. the structure)
		CPT c = new CPT(C);
		DirDT s = new DirDT(S, C);

//		// Parameterise the nodes using our "expertise"
//		s.put(new DirichletDistrib(segments, new double[] {3.0, 5.0, 7.0, 4.0, 2.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "a");
//		s.put(new DirichletDistrib(segments, new double[] {6.0, 7.0, 10.0, 9.0, 2.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "b");
//		s.put(new DirichletDistrib(segments, new double[] {10.0, 15.0, 17.0, 14.0, 12.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "c");
//		s.put(new DirichletDistrib(segments, new double[] {23.0, 25.0, 27.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "d");
//		s.put(new DirichletDistrib(segments, new double[] {21.0, 22.0, 33.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "e");
//		s.put(new DirichletDistrib(segments, new double[] {33.0, 35.0, 37.0, 34.0, 32.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "f");
//		s.put(new DirichletDistrib(segments, new double[] {10.0, 15.0, 17.0, 14.0, 12.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "g");
//		s.put(new DirichletDistrib(segments, new double[] {23.0, 25.0, 27.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "h");
//		s.put(new DirichletDistrib(segments, new double[] {21.0, 22.0, 33.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "i");
//		s.put(new DirichletDistrib(segments, new double[] {33.0, 35.0, 37.0, 34.0, 32.0, 3.0, 14.0, 25.0, 3.0, 18.0, 19.0, 12.0, 5.0, 1.0, 1.0, 8.0, 14.0, 29.0, 10.0, 1.0}), "j");
		
		s.put(new DirichletDistrib(segments, new double[] {3.0, 5.0, 7.0, 4.0, 2.0,10.0, 15.0, 17.0, 14.0, 12.0}), "a");
		s.put(new DirichletDistrib(segments, new double[] {6.0, 7.0, 10.0, 9.0, 2.0,10.0, 15.0, 17.0, 14.0, 12.0}), "b");
		s.put(new DirichletDistrib(segments, new double[] {10.0, 15.0, 17.0, 14.0, 12.0,10.0, 15.0, 17.0, 14.0, 12.0}), "c");
//		s.put(new DirichletDistrib(segments, new double[] {23.0, 25.0, 27.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0}), "d");
//		s.put(new DirichletDistrib(segments, new double[] {21.0, 22.0, 33.0, 24.0, 22.0, 3.0, 14.0, 25.0, 3.0, 18.0}), "e");
		
//		s.put(new DirichletDistrib(segments, new double[] {3.0, 5.0, 7.0, 4.0, 2.0}), "a");
//		s.put(new DirichletDistrib(segments, new double[] {6.0, 7.0, 10.0, 9.0, 2.0}), "b");
//		s.put(new DirichletDistrib(segments, new double[] {10.0, 15.0, 17.0, 14.0, 12.0}), "c");
		
//		s.put(new DirichletDistrib(segments, new double[] {23.0, 25.0, 27.0, 24.0, 22.0}), "d");
//		s.put(new DirichletDistrib(segments, new double[] {21.0, 22.0, 33.0, 24.0, 22.0}), "e");
//		s.put(new DirichletDistrib(segments, new double[] {33.0, 35.0, 37.0, 34.0, 32.0}), "f");


		BNet bn = new BNet();
		bn.add(c, s);

		String data_file = args[0];
		
		
		//////
		int clusters = clusNames.length;
		int segs = segNames.length;
		

		//Create a list of variables to pull the data from the original file
		//Hack so I could use DataBuf to load the original data
		Variable[] vars = new Variable[segs];
		for (int i=1; i < segs+1; i++) {
			Variable a = Predef.Number(1000, "s"+i);
			vars[i-1] = a;
		}
		Object[][] data = DataBuf.load(data_file, vars);

		//Create new structure to store enumDistrib data that will be passed to EM
		Object[][] toTrain = new Object[data.length][];
		//Iterate over original data and generate enumDistrib for each set of counts
		for (int k = 0; k < data.length; k++) {
			double[] line = new double[data[k].length];
			for (int j=0; j < data[k].length; j++) {
				line[j] = (double)((Integer)data[k][j]).intValue();
			}
			EnumDistrib d = new EnumDistrib(segments, line);
			toTrain[k] = new Object[] {d, null}; //null value for the latent node
		}

		EM em = new EM(bn);
		List<BNode> nodes = new ArrayList<>();
		//Hack to set node ordering for new data to pass to EM
		nodes.add(bn.getNode("Segments"));
		nodes.add(bn.getNode("Clusters"));
		em.setMaxRounds(10);
		em.train(toTrain, nodes);
		

//		System.out.println();
		
		///CODE TO ASSIGN CLUSTERS///
		
		//Same original data but now include ID
		
				
		Variable[] idVars = new Variable[segs+3];
		idVars[0] = Predef.Nominal(new String[] {"chr1","chr2","chr3","chr4","chr5","chr6","chr7","chr8","chr9","chr10","chr11","chr12","chr13","chr14","chr15","chr16","chr17","chr18","chr19","chrX"}, "Chrom");
		idVars[1] = Predef.Real("Start");
		idVars[2] = Predef.Real("End");
		for (int i=1; i < segs+1; i++) {
			Variable a = Predef.Number(1000, "s"+i);
			idVars[i+2] = a;
		}
		Object[][] data_ids = DataBuf.load(data_file, idVars);
		
		Object[][] toTrain1 = new Object[data_ids.length][];
		//Iterate over original data and generate enumDistrib for each set of counts
		for (int k = 0; k < data_ids.length; k++) {
			double[] line = new double[data_ids[k].length - 3];
			for (int j=3; j < data_ids[k].length; j++) {
				line[j-3] = (double)((Integer)data_ids[k][j]).intValue();
			}
			EnumDistrib d = new EnumDistrib(segments, line);
			toTrain1[k] = new Object[] {data_ids[k][0], data_ids[k][1], data_ids[k][2], d, null}; //null value for the latent node
		}
		
		
		
		for (int a = 0; a < toTrain1.length; a++) {
			s.setInstance(toTrain1[a][3]);
			VarElim in = new VarElim();
			in.instantiate(bn);
			Query qq = in.makeQuery(C);
			CGTable cg = (CGTable)in.infer(qq);
			Distrib cr = cg.query(C);
			Object t = cr.sample();
			toTrain1[a][4] = cg.query(C);
//			System.out.println();
		}
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter("clusterOutput1.txt");
		} catch (FileNotFoundException e) {
			System.out.println("Invalid filename");
		}
		
		for (int x = 0; x < toTrain1.length; x++) {
			writer.println(Arrays.toString(toTrain1[x]));
		}
		writer.close();
		
		
		

//		c.setInstance(new EnumDistrib(segments, new double[] {0.3, 0.3, 0.4})); // primarily blue, so probably male...
//		c.setInstance("a");
		VarElim inf = new VarElim();
		
//		c.setInstance("c");
		double[] inst = new double[] {0.060, 0.089, 0.116, 0.112, 0.122, 0.125, 0.108, 0.122, 0.086, 0.060};
		s.setInstance(new EnumDistrib(segments, inst));
		inf.instantiate(bn);
		Query q = inf.makeQuery(C);
		CGTable r = (CGTable) inf.infer(q);
		r.display();
		Distrib d = r.query(C);
		
		System.out.println();
		
		Object[][] toTest = new Object[data.length][];
		//Iterate over original data and generate enumDistrib for each set of counts
		for (int k = 0; k < clusNames.length; k++) {
			toTest[k] = new Object[] {null, clusNames[k]}; //null value for the latent node
		}
		int x = 0;
		Map<Object, List<Double>> hists = new HashMap<>();
		for (int f = 0; f < segNames.length; f++) {
			hists.put(segNames[f], new ArrayList<Double>());
		}
		while (x < 250) { //sample from the distribution
			EnumDistrib dd = (EnumDistrib)d.sample();
			for(Entry<Object, List<Double>> entry  : hists.entrySet()) {
				Object key = entry.getKey();
				List<Double> val = entry.getValue();
				Double o = (Double)dd.get(key);
				val.add(o);
				hists.put(key, val);

			}				
			x++;
		}
		
		for(Entry<Object, List<Double>> entry  : hists.entrySet()) {
			Object key = entry.getKey();
			List<Double> val = entry.getValue();

			double mean_LL = val.get(0) / val.size();
			double sd_LL;
            for (int i = 1; i < val.size(); i++) {
                mean_LL += (val.get(i) / val.size());
            }
            double[] sdl_LL = new double[val.size()];
            for (int j = 0; j < val.size(); j++) {
            	sdl_LL[j] = (val.get(j) - mean_LL)*(val.get(j) - mean_LL);
            }
            sd_LL = sdl_LL[0] / sdl_LL.length;
//            for (int i = 1; i < val.size(); i++) {
//                sd_LL += (sdl_LL[i] / sdl_LL.length);
//            }
            
            for (int i = 0; i < val.size() - 1; i++) {
                sdl_LL[i] = sdl_LL[i + 1];
                sd_LL += (sdl_LL[i] / sdl_LL.length);
            }
            
            double err = sd_LL/(Math.sqrt((double)sdl_LL.length));
			
			System.out.println(key + "\t" + mean_LL + "\t" + err);
		}
		
		System.out.println();

//		BNBuf.save(bn, "testDirPeaks.new");

	}

}
