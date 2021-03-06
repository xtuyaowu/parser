package net.spring.controller;

import java.io.IOException;
import java.util.ArrayList;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.bole.config.Status;
import com.bole.resumeparser.document.impl.DocumentResumeParserInterface;
import com.bole.resumeparser.document.impl.TextResumeParser;
import com.bole.resumeparser.exception.ResumeParseException;
import com.bole.resumeparser.html.impl.HtmlResumeParserInterface;
import com.bole.resumeparser.html.impl.LiePinResumeParser;
import com.bole.resumeparser.html.impl.ZhiLianResumeParser;
import com.bole.resumeparser.html.impl._51jobResumeParser;
import com.bole.resumeparser.models.ResumeData;
import com.bole.resumeparser.models.ResumeInformation;
import com.bole.resumeparser.models.TextResumeData;

@RestController
@RequestMapping("/resumeparse/")
public class HelloWorldController {

@RequestMapping(value = "/start", method = RequestMethod.POST,headers="Accept=application/json")
@ResponseBody
public String getPerson(@ModelAttribute ResumeInformation resumeInfo) throws ResumeParseException, JsonGenerationException, JsonMappingException, IOException {
	try{
		String text = resumeInfo.getText();
		text = new String(text.getBytes("ISO-8859-1"),"utf-8");
		String source = resumeInfo.getSource();
		String type = resumeInfo.getType();
		
		HtmlResumeParserInterface parser = null;
		ResumeData re=new ResumeData();
		
		String jsonValue = "";
		
		if("html".equals(type)){
			switch(source){
			case "zhilian":
				parser = new ZhiLianResumeParser(text,"","");            		
				break;
			case "51job":
				parser=new _51jobResumeParser(text,"","");
				break;
			case "liepin":
				parser = new LiePinResumeParser(text,"",""); 
				break;
			}
			re=parser.parse();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
			jsonValue = mapper.writeValueAsString(re);
//	    	jsonValue = jsonValue.replaceAll("\"_id\":\\{.*?\\}", "\"id\":\""+re.get_id().toString()+"\"");
		}else{
			DocumentResumeParserInterface docparser = null;
			TextResumeData textre = new TextResumeData();
			
			ArrayList<String> resumeContentList = new ArrayList<String>();
			String[] lines = text.split(System.getProperty("line.separator"));
			for(int i=0;i<lines.length;i++){
				String line = lines[i];
				line = line.replaceAll("\u00A0", " ").replaceAll("\u3000", " ").trim();
	        	line = line.replaceAll("：", ":");
				resumeContentList.add(line);
			}
			switch(source){
			case "zhilian":
				docparser = new com.bole.resumeparser.document.impl.ZhiLianResumeParser(resumeContentList);
				break;
			case "51job":
				docparser = new com.bole.resumeparser.document.impl._51jobResumeParser(resumeContentList);
				break;
			case "liepin":
				docparser = new com.bole.resumeparser.document.impl.LiepinResumeParser(resumeContentList);
				break;
			default:
				docparser = new com.bole.resumeparser.document.impl.TextResumeParser(resumeContentList);
				break;
			}
			textre=docparser.parse();
			
			ObjectMapper mapper = new ObjectMapper();
			mapper.getSerializationConfig().setSerializationInclusion(Inclusion.NON_NULL);
			jsonValue = mapper.writeValueAsString(textre);
		}
		String response = new String(jsonValue.getBytes("utf-8"),"ISO-8859-1");
		String result=response;
		return result;
	}catch(Exception e){
		return "{\"status\":\"failed\"}";
	}
	
}
}
