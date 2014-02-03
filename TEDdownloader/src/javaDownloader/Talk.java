package javaDownloader;

public class Talk {

	public String 
		title		= "",
		created 	= "",
		location 	= "",		
		url 		= "",
		duration 	= "",		
		downloadUrl = "";

	public Talk(String htmlData) {
		String[] htmlDataArr = htmlData.replaceAll("</td>", "").trim().split("<td>");
				
		title		= htmlDataArr[3].substring(htmlDataArr[3].indexOf(">") + 1, htmlDataArr[3].indexOf("</a>"));
		created 	= htmlDataArr[1].trim();
		location 	= htmlDataArr[2].trim();
		url		= htmlDataArr[3].substring(htmlDataArr[3].indexOf("talks/") + 6, htmlDataArr[3].indexOf(".html"));
		duration	= htmlDataArr[4].trim();
		downloadUrl 	= htmlDataArr[5].substring(htmlDataArr[5].indexOf("talks/") + 6, htmlDataArr[5].indexOf("-light"));			
	}
	
	public Talk() {
	}
}
