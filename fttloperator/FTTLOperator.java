package net.dongyeol.koreasoupkitchen.fttloperator;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 클래스 : FTTLOperator
 * FTTL 문자열의 토큰들을 추출해서 순서대로 설정된 기능을 수행하는 클래스이다.
 * abstract 클래스이므로 execute 메소드를 구현해서 사용해야 한다. 샘플 코드 참고
 * @author mycom
 *
 */

public abstract class FTTLOperator {
	public static final String SEPERATOR = "▶";
	
	
	public FTTLOperator() {}
	
	public void operate(String instruction) {
		StringTokenizer tokenizer = new StringTokenizer(instruction, SEPERATOR);
		ArrayList<String> tokenList = new ArrayList<>();
		
		while(tokenizer.hasMoreTokens())
			tokenList.add(tokenizer.nextToken());

		execute(tokenList);
		
	}
	
	protected abstract void execute(ArrayList<String> tokenList);
}







