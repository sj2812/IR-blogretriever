package blogRetriever;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

public class Indexer {
	public static Directory prepareIndexes(List<String> docs) throws IOException {
        System.out.println("----------------------------------------");
        System.out.println("creating index");
        System.out.println("----------------------------------------");

        Directory index = FSDirectory.open(Paths.get("index"));

        IndexWriterConfig idxConfig = new IndexWriterConfig();
        idxConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        IndexWriter idx = new IndexWriter(index, idxConfig);

        Analyzer analyser = null;
        try {
            for (int i = 0; i < docs.size()-1; i++) {
                try {
                    analyser = CustomAnalyzer.builder()
                            .withTokenizer(StandardTokenizerFactory.class)
                            .addTokenFilter(LowerCaseFilterFactory.class)
                            .build();

                    TokenStream ts = analyser.tokenStream(null, docs.get(i));

                    FieldType ft = new FieldType();
                    

                    ft.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                    ft.setStoreTermVectors(true);
                    ft.setStoreTermVectorPositions(true);
                    ft.setStoreTermVectorOffsets(true);
                    ft.setStoreTermVectorPayloads(true);

                    Field f = new Field("field", ts, ft);

                
                    StoredField sf = new StoredField("field_stored", docs.get(i));

                    Document d = new Document();
                    d.add(f);
                    d.add(sf);

                    idx.addDocument(d);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {

                    if (analyser != null) {
                        analyser.close();
                    }
                }
            }
        } finally {
            idx.commit();
            idx.close();
        }
		return index;

    }
}
