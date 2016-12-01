package bn.example;

import bn.kmx.DefaultProbSVM;
import bn.kmx.kernel.Kernel;
import bn.kmx.kernel.seq.KSequence;
import bn.kmx.kernel.seq.KernelSpectrum;
import seq.Sequence;
import seq.file.BioSeqIOException;
import seq.file.FastaReader;
import sim.CrossValidateTagged;
import sim.dataset.DataBuffer;
import sim.result.ROC;
import sim.result.ResultBoolean;
import sim.result.ResultSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by aesseb on 14-Sep-16.
 */
public class SVM_sequence {

    public static void main(String[] args) {
        Random generator = new Random();
        int seed = generator.nextInt(100);

        String pos = args[0];
        String neg = args[1];
        Integer nFold = Integer.parseInt(args[2]);
        Integer spectrum = Integer.parseInt(args[3]);
        String params = args[4];

        String[] lParams = params.split(",");
        double[] C = new double[lParams.length];
        for (int p=0; p<lParams.length; p++) {
            C[p] = Double.parseDouble(lParams[p]);
        }

        Sequence[] seqsPos = null;
        Sequence[] seqsNeg = null;
        try {
            seqsPos = new FastaReader(pos).load();
            seqsNeg = new FastaReader(neg).load();
        } catch (BioSeqIOException e) {
            System.err.println("Failed to load FASTA file");
            System.exit(2);
        }

        DataBuffer<KSequence> dbuf = new DataBuffer<>();

        int npos = 0, nneg = 0;
        for (int i = 0; i < seqsPos.length; i++) {
            dbuf.put(new KSequence(seqsPos[i]), "pos");
            npos++;
        }
        for (int i = 0; i < seqsNeg.length; i++) {
            dbuf.put(new KSequence(seqsNeg[i]), "neg");
            nneg++;
        }
        System.out.println(npos+" positives, "+nneg+" negatives in data set");

        Kernel kernel=new KernelSpectrum(spectrum);

        // step 2: create models
//        double[] C={1000,1000};
        List<DefaultProbSVM<KSequence>> model=new ArrayList<>();
        for (int i=0; i<nFold; i++)
            model.add(new DefaultProbSVM<KSequence>(kernel, C));

        // step 3: create simulation
        CrossValidateTagged<DefaultProbSVM<KSequence>, ResultBoolean<?>, KSequence> cv = new CrossValidateTagged<>(model, dbuf, seed);

        // step 4: train models
        cv.train();

        // step 5: test models
        ResultSummary<ResultBoolean<?>> eval=new ResultSummary<>(cv.test());
        double roc = ROC.getROCArea(eval, true);
        System.out.println(roc);

    }
}
