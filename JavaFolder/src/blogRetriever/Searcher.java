package blogRetriever;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.NullFragmenter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.store.Directory;
import java.util.Iterator;
import org.apache.lucene.search.BooleanClause;

public class Searcher {
	public static void searchIndex(Set<String> qterms,String[] userQuery,Directory indexDir,int num,boolean explain) throws IOException, InvalidTokenOffsetsException, org.apache.lucene.queryparser.classic.ParseException {
        System.out.println("----------------------------------------");
        System.out.println("searching index");
        System.out.println("----------------------------------------");
        Directory index = indexDir;

        IndexReader idxReader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(idxReader);
        IndexSearcher idxSearcher = new IndexSearcher(idxReader);
        Analyzer analyser = null;
        
        ArrayList<Query> queries = new ArrayList<>();
         
         for (Iterator iterator = qterms.iterator(); iterator.hasNext();) {

            analyser = CustomAnalyzer.builder()
                    .withTokenizer(StandardTokenizerFactory.class)
                    .addTokenFilter(LowerCaseFilterFactory.class)
                    .build();
            
            TokenStream ts = analyser.tokenStream(null, (String) iterator.next());
            
            try {
                ts.reset();

                while (ts.incrementToken()) {
                    CharTermAttribute attr = ts.getAttribute(CharTermAttribute.class);
                   
                    Term term =new Term("field", new String(attr.buffer(), 0, attr.length()));
                    Query q = new FuzzyQuery(term);
                    
                    queries.add( q);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ts.end();
                ts.close();
                analyser.close();
            }

        }
         
         

        BooleanQuery.Builder bqb = new BooleanQuery.Builder();
         
        
        
        for (int i = 0; i < queries.size(); i++) {
            bqb.add(queries.get(i), BooleanClause.Occur.SHOULD);
        }
        TopDocs docs = idxSearcher.search(bqb.build(),num );

        ScoreDoc[] hits = docs.scoreDocs;
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
//            System.out.println(idxSearcher.explain(queries.get(0), docId));
            Document d = idxSearcher.doc(docId);
            String toprint=d.get("field_stored");
            for (int j = 0; j < userQuery.length; j++) {
            	QueryParser queryParser = new QueryParser("field", new StandardAnalyzer());
                Query query = queryParser.parse(userQuery[j]);
            	QueryScorer queryScorer = new QueryScorer(query, "field");
                Highlighter highlighter = new Highlighter(queryScorer); // Set the best scorer fragments
                highlighter.setTextFragmenter(new NullFragmenter());
                TokenStream tokenStream = TokenSources.getAnyTokenStream(idxReader,
                        hits[i].doc, "field", d, new StandardAnalyzer());
                String fragment = highlighter.getBestFragment(tokenStream, toprint);
                if(fragment!=null) {
                	toprint=fragment;
                }
                
			}
            
            System.out.println((i + 1) + ". " +toprint);
            if(explain) {
            	System.out.println(idxSearcher.explain(bqb.build(), docId));
            }
            
        }

        idxReader.close();
        
}
}
