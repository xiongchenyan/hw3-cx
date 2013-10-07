package edu.cmu.cs.lti.cx.annotators;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.UimaContext;
import org.apache.uima.fit.util.FSCollectionFactory;
import org.apache.uima.fit.util.JCasUtil;

import edu.cmu.deiis.types.*;

/**
 * 
 * @author cx
 *I annotate ngrams to aJcas. Specifically, only to questions and answers of aJcas.
 *there could be multiple ngram annotated. set by parameter "ngramn" and kept lNGramN.
 */

public class NGramAnnotator extends JCasAnnotator_ImplBase {
	Integer[] lNGramN;
	
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		lNGramN = (Integer[]) aContext.getConfigParameterValue("ngramn");
		
	}

	@Override
	public void process(JCas aJcas) throws AnalysisEngineProcessException {
		for (Question ques : JCasUtil.select(aJcas, Question.class)) {
			List<Token> lToken = new ArrayList<Token> (JCasUtil.selectCovered(Token.class, ques));
			
			for (int TokenSt = 0; TokenSt < lToken.size(); TokenSt ++) {
				for (int ngramn : lNGramN) {
					int TokenEd = TokenSt + ngramn - 1;
					if (TokenEd >= lToken.size()) {
						continue;
					}
					int TokenCharSt = lToken.get(TokenSt).getBegin();
					int TokenCharEd = lToken.get(TokenEd).getEnd();
					NGram aNgram = new NGram(aJcas);
					aNgram.setBegin(TokenCharSt);
					aNgram.setEnd(TokenCharEd);
					aNgram.setElements(FSCollectionFactory.createFSArray(aJcas, JCasUtil.selectCovered(Token.class,aNgram)));
					aNgram.setElementType(Token.class.getName());
					aNgram.addToIndexes();
				}
			}			
		}
		
		for (Answer ans : JCasUtil.select(aJcas, Answer.class)) {
			List<Token> lToken = new ArrayList<Token> (JCasUtil.selectCovered(Token.class, ans));
			
			for (int TokenSt = 0; TokenSt < lToken.size(); TokenSt ++) {
				for (int ngramn : lNGramN) {
					int TokenEd = TokenSt + ngramn - 1;
					if (TokenEd >= lToken.size()) {
						continue;
					}
					int TokenCharSt = lToken.get(TokenSt).getBegin();
					int TokenCharEd = lToken.get(TokenEd).getEnd();
					NGram aNgram = new NGram(aJcas);
					aNgram.setBegin(TokenCharSt);
					aNgram.setEnd(TokenCharEd);
					aNgram.setElements(FSCollectionFactory.createFSArray(aJcas, JCasUtil.selectCovered(Token.class,aNgram)));
					aNgram.setElementType(Token.class.getName());
					aNgram.addToIndexes();
				}
			}			
		}
		
		return;

	}

}
