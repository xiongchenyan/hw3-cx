package edu.cmu.cs.lti.cx.consumer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceProcessException;

import edu.cmu.deiis.types.AnswerScore;
import edu.cmu.deiis.types.Question;

public class Consumer extends CasConsumer_ImplBase {
	double CollectionPSum = 0.0;
	int DocNum = 0;
	
	public Consumer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processCas(CAS arg0) throws ResourceProcessException {
		// sort answer by score, and calculate p@n
		JCas aJCas = null;
		try {
			aJCas = arg0.getJCas();
		} catch (CASException e) {			
			e.printStackTrace();
		}
		List<AnswerScore> lAnswer = new ArrayList<AnswerScore>(JCasUtil.select(
				aJCas, AnswerScore.class));
		Question question = JCasUtil.selectSingle(aJCas, Question.class);
		double CorrectNum = 0.0;
		for (AnswerScore ans : lAnswer) {
			if (ans.getAnswer().getIsCorrect()) {
				CorrectNum += 1.0;
			}
		}
		// System.out.println("Answer num:");
		// System.out.println(lAnswer.size());
		Collections.sort(lAnswer, new Comparator<AnswerScore>() {
			public int compare(AnswerScore a, AnswerScore b) {
				return a.getScore() > b.getScore() ? -1 : 1;
			}
		});

		int TruePos = 0;
		for (int i = 0; i < CorrectNum; i++) {
			if (lAnswer.get(i).getAnswer().getIsCorrect()) {
				TruePos += 1;
			}
		}

		double PAtN = TruePos / CorrectNum;
		CollectionPSum += PAtN;
		DocNum += 1;

		// print result
		System.out.println(String.format("Question: %s",
				question.getCoveredText()));
		for (AnswerScore ans : lAnswer) {
			String label = ans.getAnswer().getIsCorrect() ? "+" : "-";
			System.out.println(String.format("%s %.2f %s", label,
					ans.getScore(), ans.getAnswer().getCoveredText()));
		}

		System.out.println(String.format("Precision at %d %.2f\n",
				(int) CorrectNum, PAtN));
		return;

	}

}
