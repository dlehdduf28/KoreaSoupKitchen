package net.dongyeol.koreasoupkitchen;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

/**
 * 인터페이스 : Dialogue
 * 대화 객체의 기준을 마련한다.
 */
interface Dialogue {
	String speak(String line);
	String getAPIKey();
}

/**
 * 클래스 : DialogManager
 * 대화 객체의 기준으로 정의된 챗봇 대화 매니저.
 */
public class DialogManager implements Dialogue {
	private		String 		apiKey 	= 	null;
	private 	static 		DialogManager		inst	=	null;
	private		AIConfiguration 	aiConfiguration 	= 	null;
	private		AIDataService 		aiDataService 		= 	null;

	public static final String myApiKey = "ea04c6c52e5b423cb6fefdffb0086eae";
	
	/**
	 * DialogManager를 생성하고 초기화 한다
	 * @param apiKey  DialogFlow의 API key
	 */
	private DialogManager(String apiKey) {
	    if (apiKey.length() < 1 || apiKey == null) 
	      System.out.println("Please specify API key");
	    
		this.apiKey = apiKey;
		
		// aiDataService 초기화 //
		aiConfiguration = new AIConfiguration(apiKey);
		aiDataService = new AIDataService(aiConfiguration);
	}
	
	
	public static DialogManager getInstance(String apiKey) {
		if (inst == null)
			inst = new DialogManager(apiKey);
		
		return inst;
	}
			
	@Override
	public String speak(String line) {
        try {
			// 질문이 담긴 객체 //
            AIRequest request = new AIRequest(line);
			// AIDataService 객체의 request 메소드에 AIRequest 객체를 전달하고 답변 객체로 AIResponse 객체를 반환 받는다. //
			AIResponse response = aiDataService.request(request); // 네트워크 쓰레드로 진행해야 한다.

			// AIResponse 객체의 Status Code가 200이라면 답변이 반환된 것이다. //
			if (response.getStatus().getCode() == 200)
				return response.getResult().getFulfillment().getSpeech();
			else {
				System.err.println(response.getStatus().getErrorDetails());
				return null;
			}
        } catch (AIServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAPIKey() {
		// TODO Auto-generated method stub
		return apiKey;
	}
}
