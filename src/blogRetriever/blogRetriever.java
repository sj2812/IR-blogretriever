package blogRetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class blogRetriever {
	
	static SynonymExtractor gs=new SynonymExtractor();
	static Searcher ser=new Searcher();
	static Indexer idx=new Indexer();
	
	public static List<String> getValidBlogs() throws IOException {
		InputStreamReader isr = new InputStreamReader(new FileInputStream("textmainfinal.txt"));
		BufferedReader br = new BufferedReader(isr);
    	String str =" ";
    	List<String> docs = new ArrayList<String>();
    	while((br.readLine()) != null) {
    		
    		docs.add(br.readLine());
    	}
//    	System.out.println(docs.size());
//    	System.out.println("doneeeeeeeeeeeeeee");
    	return docs;
	}
	
	public static void main(String[] args) throws IOException, InvalidTokenOffsetsException, ParseException {
	
		File f = new File("index");
		Directory index=null;
		
		if(f.exists()) { 
			index = FSDirectory.open(Paths.get("index"));	
		 
			
		}
		else {
			
			index=idx.invertedIndexing(getValidBlogs());
		}
	 IndexReader idxReader = DirectoryReader.open(index);
     while(true) {
     System.out.println("Enter query");
     Scanner sc = new Scanner(System.in); 
     
     // String input 
     String query = sc.nextLine(); 
     String[] terms = query.split(" ");
     System.out.println("Enter number of documents to retrieve");
     Scanner s = new Scanner(System.in); 
     int num = Integer.parseInt( s.nextLine());
     System.out.println("Do you want an explanation write yes or no?");
     Scanner e = new Scanner(System.in); 
     String explainans=e.nextLine();
     boolean explain=false;
     if (explainans.toLowerCase().equalsIgnoreCase("yes")) {
    	 explain=true;
     }
     else {
    	 explain=false;
     }
     // String input 
   
		// TODO Auto-generated method stub
//	 String[] terms = {"Sunny", "Exciting"};
//      searchIndex(uniqueTerms,terms);
      Set<String> blogterms = new HashSet<String>();
      for(int g=0; g<idxReader.numDocs();g++) {
      	Terms terms2 = idxReader.getTermVector(g, "ub2");
          if(terms2!=null) {
          	TermsEnum termsEnum =terms2.iterator();
              BytesRef term;
              
      		while ((term = termsEnum.next()) != null) {
              	   blogterms.add(term.utf8ToString());
              	 }
          }
          
      }
      
		
  	
  	
  	Set<String> Qterms= gs.getsynm(terms,blogterms);
  	String synonymsstr="";
  	for (String string : Qterms) {
			if(!Arrays.asList(terms).contains(string)) {
				synonymsstr+=string+" ";
			}
		}
  	System.out.println(synonymsstr);
     ser.searchIndex(Qterms,terms, index, num, explain);
	}
	}
}
