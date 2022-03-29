package scorer;

import ds.Document;
import ds.Query;
import utils.IndexUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Skeleton code for the implementation of a
 * Cosine Similarity scorer in Task 1.
 */
public class VSMScorer extends AScorer {

    /*
     * TODO: You will want to tune the values for
     * the weights for each field.
     */
    double titleweight  = 5;
    double bodyweight = 0.1;
    double bodylength = 100;


    /**
     * Construct a Cosine Similarity scorer.
     * @param utils Index utilities to get term/doc frequencies
     */
    public VSMScorer(IndexUtils utils) {
        super(utils);
    }

    /**
     * Get the net score for a query and a document.
     * @param tfs the term frequencies
     * @param q the ds.Query
     * @param tfQuery the term frequencies for the query
     * @param d the ds.Document
     * @return the net score
     */
    public double getNetScore(Map<String, Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {

        double score = 0.0;

        //raw frequencies for each query term in title
        Map<String, Double> titleVector = new HashMap<>();

        //raw frequencies for each query term in body
        Map<String, Double> bodyVector = new HashMap<>();

        //frequency of query terms in the querie
        Map<String, Double> qVector = new HashMap<>();
        /*
         * TODO : Your code here
         * See Equation 1 in the handout regarding the net score
         * between a query vector and the term score vectors
         * for a document.
         */
         normalizeTFs(tfs, d, q);

         for(String tf : tfs.get("title").keySet()) {
             titleVector.put(tf, tfs.get("title").get(tf));
         }
         for(String bf : tfs.get("body").keySet()) {
             bodyVector.put(bf, tfs.get("body").get(bf));
         }
         for (String queryWord : q.queryWords) {

             queryWord = queryWord.toLowerCase();
             qVector.put(queryWord, 0.0);

             double numInQuery = countOccurrencesInList(queryWord, q.queryWords);
             qVector.put(queryWord, numInQuery);
         }
         for(String qv: qVector.keySet()){
             score+=qVector.get(qv)*(titleweight*titleVector.get(qv)+bodyweight*bodyVector.get(qv));
         }

        return score;
    }

    /**
     * Normalize the term frequencies.
     * @param tfs the term frequencies
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
        /*
         * TODO : Your code here
         * Note that we should use the length of each field
         * for term frequency normalization as discussed in the assignment handout.
         */
        //Term Frequency tf_i(t,d) = 1+ log(RF_i) if RF_i > 0 else 0
        for(String type: tfs.keySet()){
            for(String queryWord: tfs.get(type).keySet()){
                double tf = tfs.get(type).get(queryWord);
                double calculatedTF = 0.0;
                if(tf > 0){
                    calculatedTF = (1 + java.lang.Math.log10(tf));
                }
                else{
                    calculatedTF = 0.0;
                }
                //df(t) = #docs w/ term t/total # docs
                double df = (bodylength + d.body_length);
                //idf(t) = ln((1+n)/(1+df(t)))+1 n=total # docs in doc set | d(t) = #docs in doc set that contain term
                double idf = java.lang.Math.log((1 + utils.totalNumDocs())/(1.0+df));

                //Calculate weighted value: raw*IDF (dot product)
                double idfWeighted = 0.0;
                if(tf != 0){
                    idfWeighted =  calculatedTF*idf;
                }

                tfs.get(type).put(queryWord, idfWeighted);
            }

        }
    }

    public double countOccurrencesInList(String term, List<String> lst) {
        double count = 0;
        for(String str : lst ){
            str = str.toLowerCase();
            String[] words = str.split(" ");

            for (String w : words) {
                if (term.equals(w)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Write the tuned parameters of vsmSimilarity to file.
     * Only used for grading purpose, you should NOT modify this method.
     * @param filePath the output file path.
     */
    private void writeParaValues(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            String[] names = {
                    "titleweight", "bodyweight"
            };
            double[] values = {
                    this.titleweight, this.bodyweight
            };
            BufferedWriter bw = new BufferedWriter(fw);
            for (int idx = 0; idx < names.length; ++ idx) {
                bw.write(names[idx] + " " + values[idx]);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /** Get the similarity score between a document and a query.
     * @param d the ds.Document
     * @param q the ds.Query
     * @return the similarity score.
     */
    public double getSimScore(Document d, Query q) {
        Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
        this.normalizeTFs(tfs, d, q);
        Map<String,Double> tfQuery = getQueryFreqs(q);

        // Write out tuned vsmSimilarity parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("vsmPara.txt");
        return getNetScore(tfs,q,tfQuery,d);
    }
}
