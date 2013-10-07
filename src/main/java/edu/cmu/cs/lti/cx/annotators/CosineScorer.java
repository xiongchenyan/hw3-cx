package edu.cmu.cs.lti.cx.annotators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.deiis.types.Annotation;
import edu.cmu.deiis.types.Answer;
import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.NGram;
import edu.cmu.deiis.types.Question;


/**
 * @author cx
 *   I calculate the ngram-cosine similarity of all question-answer pairs of a input JCas.
 *   input JCas should be annotated with ngram, question and answer, defined in typesystem.
 *   there should only be on question in JCas, but could be multiple answers.
 *   I will annotate an AnswerScore type for each answer, as the cosine score results of this answer for the question.
 *   the score is assigned as \sum_n w_n \times cosine(ngram(q),ngram(a)). n's range is defined by parameter lNGramN "ngramn"
 *   	while w_n is defined by paramter lNGramWeight "ngramweight";
 */
public class CosineScorer extends JCasAnnotator_ImplBase {

	//parameters used to set n of ngrams. e.g. 1,2,3: use unigram, bigram and trigram.
	Integer[] lNGramN;	
	//parameters used to set the weight to combine cosine of different ngrams, is 1-1 corresponding to lGramN;
	Float [] lNGramWeight;
	public void initialize(UimaContext aContext) throws ResourceInitializationException {
		super.initialize(aContext);
		lNGramN = (Integer[]) aContext.getConfigParameterValue("ngramn");
		lNGramWeight = (Float [])aContext.getConfigParameterValue("ngramweight");
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.apache.uima.analysis_component.JCasAnnotator_ImplBase#process(org.apache.uima.jcas.JCas)
	 * main process of this class
	 */
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		//Calculate the language model score as p(q|a)
		
		//get question's tokens
		Question question = JCasUtil.selectSingle(aJCas, Question.class);
		List<NGram> lQNGram = new ArrayList<NGram> (JCasUtil.selectCovered(NGram.class, question));
		List<List<NGram>> llQGram = GetNGrams(lQNGram);

		
		
		
		for (Answer answer : JCasUtil.select(aJCas,Answer.class)) {
			List<NGram> lANGram = new ArrayList<NGram> (JCasUtil.selectCovered(NGram.class,answer));	
			List<List<NGram>> llAGram = GetNGrams(lANGram);
//			System.out.println(String.format("q[%s]\na[%s]", question.getCoveredText(),answer.getCoveredText()));
			Double AnswerTotalScore = 0.0;
			for (int i = 0; i < llQGram.size(); i ++)
			{
				AnswerTotalScore += lNGramWeight[i] * Cosine(llQGram.get(i),llAGram.get(i));	
//				System.out.println(String.format("[%d] gram [%.2f], cosine [%.2f]",lNGramN[i],lNGramWeight[i],Cosine(llQGram.get(i),llAGram.get(i))));
				
				
			}
			AnswerScore ansScore = new AnswerScore(aJCas);
			ansScore.setAnswer(answer);
			ansScore.setScore(AnswerTotalScore);
			ansScore.addToIndexes();
		}		
		return;		
	}

	/**
	 * 
	 * @param lNGram: the list of ngrams, with all `n' in same list
	 * @return the list of list of ngrams, while a list in returned list-list is all ngrams with same n, splited from lNGram.
	 * the n of a ngram is judged by its Elements's length.
	 */
	private List<List<NGram>> GetNGrams(List<NGram> lNGram)
	{
		List<List<NGram>> llNGram = new ArrayList<List<NGram>>();
		for (Integer n : lNGramN) {
			List<NGram> lThisNGram = new ArrayList<NGram>();
			for (NGram gram : lNGram) {
				if (gram.getElements().size() != n) {
					continue;
				}
				lThisNGram.add(gram);
			}
			llNGram.add(lThisNGram);
		}
		return llNGram;		
	}
	
	/**
	 * 
	 * @param lA the list of annotation to be calculated by cosine. could be token, ngram, etc.
	 * @param lB the other list of annotations. 
	 * @return the cosine score of cos(lA,lB)
	 * the cosine is conducted by the raw string of covered text for Annotation elements in two lists.
	 */
	private <T extends Annotation> Double Cosine(List<T> lA, List<T> lB) {
		Double Res = 0.0;		
		Map<String,Integer> hA = GetAnnotationCounts(lA);
		Map<String,Integer> hB = GetAnnotationCounts(lB);
//		System.out.println(lA);
//		System.out.println(hA);		
		for (Entry<String,Integer> TokenEntry : hA.entrySet()) {
			if (hB.containsKey(TokenEntry.getKey())) {
				Res += TokenEntry.getValue() * hB.get(TokenEntry.getKey());
			}
		}		
		Double LengthA = Length(hA);
		Double LengthB = Length(hB);
		
		return Res/ (LengthA * LengthB);
	}
	
	/**
	 * 
	 * @param the vector model. String is the dimensions (terms) in vector, Integer is the count of that dimension.
	 * @return the L2 norm of this vector.
	 */
	private Double Length(Map<String,Integer> hT) {
		double res = 0;
		for (Entry<String,Integer> entry : hT.entrySet()) {
			Integer value = entry.getValue();
			res += value * value;
		}
		return res;
	}
	
	/**
	 * 
	 * @param LT, the list of Annotations.
	 * @return the vector model of lT. Constructed only on lT's raw strings.
	 */
	private <T extends Annotation> Map<String,Integer> GetAnnotationCounts(List<T> lT) {
		if (lT.isEmpty()){
			return null;
		}
		Map<String, Integer> hAnnotation = new HashMap<String, Integer>();
		for (T key: lT) {
			String text = key.getCoveredText();
			if (!hAnnotation.containsKey(text)) {
				hAnnotation.put(text, 0);
			}			
			hAnnotation.put(text, hAnnotation.get(text) + 1);			
		}		
		return hAnnotation;		
	}

}
