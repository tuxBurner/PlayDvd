package helpers;

import java.util.HashMap;
import java.util.Map;

import play.mvc.Http.Request;

/**
 * This maps a collection from an request to a Collection
 * 
 * @author tuxburner
 * 
 */
public class RequestToCollectionHelper {

	
	public static Map<String, String> requestToFormMap(final Request req, final String ... multiParams) {

		Map<String, String> newData = new HashMap<String, String>();
		
		//MAPPING TOOKED FROM: Form.requestData
		
		Map<String,String[]> urlFormEncoded = new HashMap<String,String[]>();
        if(play.mvc.Controller.request().body().asFormUrlEncoded() != null) {
            urlFormEncoded = play.mvc.Controller.request().body().asFormUrlEncoded();
        }
        
        if(play.mvc.Controller.request().body().asMultipartFormData() != null) {
        	urlFormEncoded = play.mvc.Controller.request().body().asMultipartFormData().asFormUrlEncoded();
        }
        
//        Map<String,String> jsonData = new HashMap<String,String>();
//        if(play.mvc.Controller.request().body().asJson() != null) {
//            jsonData = play.libs.Scala.asJava(
//                play.api.data.FormUtils.fromJson("", 
//                    play.api.libs.json.Json.parse(
//                        play.libs.Json.stringify(play.mvc.Controller.request().body().asJson())
//                    )
//                )
//            );
//        }
		if (urlFormEncoded != null) {
			for (String key : urlFormEncoded.keySet()) {
				String[] value = urlFormEncoded.get(key);
				if (value.length == 1) {
					
					boolean multiAdded = false;
					
					// check if it is a multiple value
					for (String multiParam : multiParams) {
						if(multiParam.equals(key)) {
							multiAdded = true;
							if(value[0].indexOf(',') != -1) {
								String[] split = value[0].split(",");
								for (int i = 0; i < split.length; i++) {
									newData.put(key + "[" + i + "]", split[i].trim());
								}
							} else {
							  newData.put(key + "[0]", value[0].trim());
							}
						}
					}
					
					if(multiAdded == false) {
					  newData.put(key, value[0]);
					}
				} else if (value.length > 1) {

					for (int i = 0; i < value.length; i++) {
						newData.put(key + "[" + i + "]", value[i]);
					}
				}
			}
		}

		return newData;
	}

}
