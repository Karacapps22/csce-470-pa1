package scorer;

import ds.Document;
import ds.Query;
import utils.IndexUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


/**
 * Skeleton code for the implementation of a BM25 scorer in Task 2.
 */
public class BM25Scorer extends AScorer {

    /*
     *  TODO: You will want to tune these values
     */
    double titleweight  = 2;
    double bodyweight = .1;

    // BM25-specific weights
    double btitle = 0.1;
    double bbody = .5;

    double k1 = 0.11;
    double pageRankLambda = 1;
    double pageRankLambdaPrime = 2;

    // query -> url -> document
    Map<Query,Map<String, Document>> queryDict;

    // BM25 data structures--feel free to modify these
    // ds.Document -> field -> length
    Map<Document,Map<String,Double>> lengths;

    // field name -> average length
    Map<String,Double> avgLengths;

    // ds.Document -> pagerank score
    Map<Document,Double> pagerankScores;

    //String[] TFTYPES = {"title","body", "bfield", "fweight"};
    /**
     * Construct a scorer.BM25Scorer.
     * @param utils Index utilities
     * @param queryDict a map of query to url to document
     */
    public BM25Scorer(IndexUtils utils, Map<Query,Map<String, Document>> queryDict) {
        super(utils);
        this.queryDict = queryDict;
        this.calcAverageLengths();
    }

    /**
     * Set up average lengths for BM25, also handling PageRank.
     */
    public void calcAverageLengths() {

        /*
         * TODO : Your code here
         * Initialize any data structures needed, perform
         * any preprocessing you would like to do on the fields,
         * accumulate lengths of fields.
         * handle pagerank.
         */
        lengths = new HashMap<>();
        avgLengths = new HashMap<>();
        pagerankScores = new HashMap<>();

        double bfield = 0.0;
        for (String tfType : this.TFTYPES) {
            if(tfType =="title"){
                bfield = btitle;
            }
            else{
                bfield = bbody;
            }

            double totalLength = 0.0;
            int numOfDocuments = 0;
            /*
             * TODO : Your code here
             * Normalize lengths to get average lengths for
             * each field (body, title).
             */
            for(Query query: queryDict.keySet()){
                for (String url : queryDict.get(query).keySet()) {
                    Document doc = queryDict.get(query).get(url);
                    numOfDocuments++;
                    if(tfType =="title"){
                        totalLength += doc.title_length;
                    }
                    else{
                        totalLength += doc.body_length;
                    }
                    //double rankpage
                    pagerankScores.put(doc, Double.valueOf(doc.page_rank));
                }
            }
            double avgLength = totalLength/numOfDocuments;
            avgLengths.put(tfType, avgLength);
        }
    }

    /**
     * Get the net score.
     * @param tfs the term frequencies
     * @param q the ds.Query
     * @param tfQuery
     * @param d the ds.Document
     * @return the net score
     */
    public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {

        double score = 0.0;
        double overallWeight = 0.0;
        /*
         * TODO : Your code here
         * Use equation 3 first and then equation 4 in the writeup to compute the overall score
         * of a document d for a query q.
         */
        normalizeTFs(tfs, d, q);
        double fweight;
        for(String field: tfs.keySet()){
            if(field =="title"){
                fweight = titleweight;
            }
            else{
                fweight = bodyweight;
            }
            for(String queryWord: tfs.get(field).keySet()){
                overallWeight += fweight*tfs.get(field).get(queryWord);
                score += (overallWeight/(k1+overallWeight)) + pageRankLambda*(java.lang.Math.log(pageRankLambdaPrime+pagerankScores.get(d)));
            }
        }
        //for t in q, (W_d,t)/(k1 + W_d,t)*idft+ lambdaVj(f) where V_j can be a log/saturation/sigmoid function 



        return score;
    }

    /**
     * Do BM25 Normalization.
     * @param tfs the term frequencies
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
        /*
         * TODO : Your code here
         * Use equation 2 in the writeup to normalize the raw term frequencies
         * in fields in document d.
         */
        calcAverageLengths();

        double bfield;
        for(String field: tfs.keySet()){
            if(field =="title"){
                bfield = btitle;
            }
            else{
                bfield = bbody;
            }

            double lenDF = 0.0;
            for(String queryWord: tfs.get(field).keySet()){

                //get len of field
                if(field =="title"){
                    lenDF = d.title_length;
                }
                else{
                    lenDF = d.body_length;
                }

                double calculatedTF = 0.0;
                if(tfs.get(field).get(queryWord) < 0){
                    calculatedTF = 0.0;
                }
                else{
                    calculatedTF = (tfs.get(field).get(queryWord))/((1-bfield)+bfield*(lenDF/avgLengths.get(field)));
                }

                tfs.get(field).put(queryWord, calculatedTF);
            }
        }

    }

    // public double countWords(String field) {
    //     int count = 0;
    //     for (String okay: d.get(field)){
    //         count++;
    //     }
    //     return count;
    // }



    /**
     * Write the tuned parameters of BM25 to file.
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
                    "titleweight", "bodyweight", "btitle",
                    "bbody", "k1", "pageRankLambda", "pageRankLambdaPrime"
            };
            double[] values = {
                    this.titleweight, this.bodyweight, this.btitle,
                    this.bbody, this.k1, this.pageRankLambda,
                    this.pageRankLambdaPrime
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
    /**
     * Get the similarity score.
     * @param d the ds.Document
     * @param q the ds.Query
     * @return the similarity score
     */
    public double getSimScore(Document d, Query q) {
        Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
        this.normalizeTFs(tfs, d, q);
        Map<String,Double> tfQuery = getQueryFreqs(q);

        // Write out the tuned BM25 parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("bm25Para.txt");
        return getNetScore(tfs,q,tfQuery,d);
    }

}
