 package scorer;

import ds.Document;
import ds.Query;
import utils.IndexUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for a scorer.
 * Needs to be extended by each specific implementation of scorers.
 */
public abstract class AScorer {

    // Map: term -> idf
    IndexUtils utils;

    // Various types of term frequencies that you will need
    String[] TFTYPES = {"title","body", "bfield"};


    /**
     * Construct an abstract scorer with a map of idfs.
     * @param utils index utility functions like map of idf scores
     */
    public AScorer(IndexUtils utils) {
        this.utils = utils;
    }

    /**
     * You can implement your own function to whatever you want for debug string
     * The following is just an example to include page information in the debug string
     * The string will be forced to be 1-line and truncated to only include the first 200 characters
     */
    public String getDebugStr(Document d, Query q)
    {
        return "Pagerank: " + Integer.toString(d.page_rank);
    }

    /**
     * Score each document for each query.
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public abstract double getSimScore(Document d, Query q);

    /**
     * Get frequencies for a query.
     * @param q the query to compute frequencies for
     */

    /*term frequency = within doc frequency

    */
    public Map<String,Double> getQueryFreqs(Query q) {


        /*
         * TODO : Your code here
         * Compute the raw term frequencies
         * Additionally weight each of the terms using the idf value
         * of the term in the query (we use the provided text corpus to
         * determine how many documents contain the query terms, which is stored
         * in this.idfs).
         */
        Map<String, Double> tfQuery = new HashMap<String, Double>();

        //1.Compute raw term frequencies: Raw TF: tf(t,d) = frequency count of term t in doc d
        for (String term : q.queryWords) {
            term = term.toLowerCase();

            if (tfQuery.containsKey(term)) {
                tfQuery.put(term, tfQuery.get(term) + 1.0);
            } else {
                tfQuery.put(term, 1.0);
            }
        }

        return tfQuery;
    }

        //Document Frequency(term t) = number of documents with the term t/ total number of documents = d(t)/n
        //Inverse Document Frequency = total number of documents / number of documents with the term t = n / d(t)

        //1.Compute raw term frequencies: Raw TF: tf(t,d) = frequency count of term t in doc d
        //2.for every word in q and use tfquerry and tf




    /*
     * TODO (Optional in case you want to do any preprocessing here) : Your code here
     * Include any initialization and/or parsing methods
     * that you may want to perform on the ds.Document fields
     * prior to accumulating counts.
     * See the ds.Document class in ds.Document.java to see how
     * the various fields are represented.
     */


    /**
     * Accumulate the various kinds of term frequencies
     * for the fields (title, body).
     * You can override this if you'd like, but it's likely
     * that your concrete classes will share this implementation.
     * @param d the ds.Document
     * @param q the ds.Query
     */

        //getDocTermsFreq is for TF
        //using log_10(rfi) not e for computing sublinear scaling
    public Map<String,Map<String, Double>> getDocTermFreqs(Document d, Query q) {

        // Map from tf type (field) -> queryWord -> score
        Map<String,Map<String, Double>> tfs = new HashMap<>();

            // Various types of term frequencies that you will need
            //String[] TFTYPES = {"title","body"};
        /*
         * TODO : Your code here
         * Initialize any variables needed
         */
        System.out.println("Get Doc Term Freqs");
        double numQueriesInURL = 0.0;
        double numQueriesInTitle = 0.0;
        double numQueriesInBodyHits = 0.0;
        double numQueriesInBodyLength = 0.0;
        double numQueriesInURLPageRank = 0.0;
        for(String type: TFTYPES){
            tfs.put(type, new HashMap<String, Double>());

            for (String queryWord : q.queryWords) {
                /*
                 * TODO: Your code here
                 * Loop through query terms and accumulate term frequencies.
                 * Note: you should do this for each type of term frequencies,
                 * i.e. for each of the different fields.
                 * Don't forget to lowercase the query word.
                 */
                queryWord = queryWord.toLowerCase();
                tfs.get(type).put(queryWord, 0.0);

                //Loop through query terms and accumulate term frequencies.
                if (type.equals("url") && d.url != null) {
                        numQueriesInURL = countNumOfOccurrencesInUrl(queryWord, d.url);
                        tfs.get(type).put(queryWord, numQueriesInURL);
                }
                if (type.equals("title") && d.title != null) {
                        numQueriesInTitle = countNumOfOccurrencesInString(queryWord, d.title);
                        tfs.get(type).put(queryWord, numQueriesInTitle);
                }
                if (type.equals("body") && d.body_hits != null) {
                    if (d.body_hits.containsKey(queryWord)) {
                        tfs.get(type).put(queryWord, (double) d.body_hits.get(queryWord).size());
                    }
                }
            }
        }
        return tfs;
    }

    public double countNumOfOccurrencesInString(String term, String str) {
        str = str.toLowerCase();
        String[] words = str.split(" ");

        double count = 0;
        for (String w : words) {
            if (term.equals(w)) {
                count++;
            }
        }
        return count;
    }

    public double countNumOfOccurrencesInUrl(String term, String url) {
        url = url.toLowerCase();
        String[] words = url.split("[/.]");

        // **** LC - Need to review here how to count words in url

        double count = 0;
        for (String w : words) {
            if (term.equals(w)) {
                count++;
            }
        }
        return count;
    }

}
