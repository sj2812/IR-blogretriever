package blogRetriever;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.synonym.SynonymMap.Builder;

import org.apache.lucene.wordnet.SynonymMap;

public class SynonymExtractor {

	public static Set<String> getsynm(String[] terms, Set<String> blogterms) throws FileNotFoundException, IOException{
		String[] words = terms;
		Set<String> resultingterms = new HashSet<String>();
		String[] result= {};
		 SynonymMap map = new SynonymMap(new FileInputStream("wn_s.pl"));
		 for (int i = 0; i < words.length; i++) {
			 resultingterms.add(words[i]);
		     String[] synonyms = map.getSynonyms(words[i].toLowerCase());
		     for (String synonym : synonyms) {
		    	if (blogterms.contains(synonym)) {
		    		resultingterms.add(synonym);
		    	}
				
			}
//		     System.out.println(words[i] + ":" + java.util.Arrays.asList(synonyms).toString());
		 }
		 
		return resultingterms;
		 
	}
}