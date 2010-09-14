package com.tinkerpop.rexster;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.representation.StringRepresentation;

public class RexsterResponse {
	
	public static StringRepresentation getStringRepresentation(Request request, JSONObject response)
	{		
        List<Preference<CharacterSet>> characterSetList = request.getClientInfo().getAcceptedCharacterSets();        
        
        //sort by quality value f.e utf-8;q=0.8, iso-8859-2;q=1.0 -> iso-8859-2;q=1.0, utf-8;q=0.8 
        Collections.sort(characterSetList, new Comparator<Preference<CharacterSet>>() {
            public int compare(Preference<CharacterSet> e1, Preference<CharacterSet> e2) {
                return -1 * ((Comparable<Float>) e1.getQuality()).compareTo(e2.getQuality());
            }
        });                
        
        Preference<CharacterSet> userPreferenceCharacterSet = characterSetList.get(0);
        
        CharacterSet responseCharacterSet = null;        
        if(userPreferenceCharacterSet.getMetadata().equals(CharacterSet.ALL)){
        	responseCharacterSet = CharacterSet.DEFAULT;
        }else{
        	responseCharacterSet = userPreferenceCharacterSet.getMetadata();
        }
        
		return new StringRepresentation(response.toJSONString(), MediaType.APPLICATION_JSON, Language.ALL, responseCharacterSet);
	}
	
	
	
}
