package edu.cmu.cs.lti.cx.annotators;


import java.util.Properties;
import edu.cmu.deiis.types.Token;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
/**
 * 
 * @author cx
 * I tokenize the given aJcas's text into tokens, using StanfordCoreNLP API's.
 */
public class Tokenization extends JCasAnnotator_ImplBase {

	public Tokenization() {
		// I do token, no initialization is required
	}

	@Override
	public void process(JCas aJcas) throws AnalysisEngineProcessException {
		//add token to aJcas
		String RawText = aJcas.getDocumentText();
		
		Properties props = new Properties();
		props.put("annotators","tokenize,ssplit");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation document = new Annotation(RawText);		
		pipeline.annotate(document);
//		System.out.println(document);
//		System.out.println(RawText);
//		System.out.println(SentencesAnnotation.class);
//		System.out.println(document.get(SentencesAnnotation.class));
		for (CoreMap sent : document.get(SentencesAnnotation.class)){			
			for (CoreLabel token : sent.get(TokensAnnotation.class)) {
				int begin = token.get(CharacterOffsetBeginAnnotation.class);
				int end = token.get(CharacterOffsetEndAnnotation.class);
				
				Token sToken = new Token(aJcas, begin,end);
				if (sToken.getCoveredText().equals(".") || sToken.getCoveredText().equals("?"))
				{
					continue;
				}
				sToken.addToIndexes(aJcas);				
			}		
		}		
		return;
	}

}
