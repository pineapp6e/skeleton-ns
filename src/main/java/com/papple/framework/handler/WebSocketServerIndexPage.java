package com.papple.framework.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

/**
 * 
 * @author pineapple
 * Generates the demo HTML page which is served at http://localhost:8080/
 */
public final class WebSocketServerIndexPage {
	private static final String NEWLINE = "\r\n";

    public static ByteBuf getContent(String webSocketLocation) {
        return Unpooled.copiedBuffer(
                "<html><head><title>Web Socket Test</title></head>" + NEWLINE +
                "<body>" + NEWLINE +
                "<script src=\"http://jjlstudios.mp4.s3-website.cn-north-1.amazonaws.com.cn/js/jquery.min.js\"></script>"+ NEWLINE +	
                "<script type=\"text/javascript\">" + NEWLINE +
                "var socket;" + NEWLINE +
                "if (!window.WebSocket) {" + NEWLINE +
                "  window.WebSocket = window.MozWebSocket;" + NEWLINE +
                '}' + NEWLINE +
                "if (window.WebSocket) {" + NEWLINE +
                "  socket = new WebSocket(\"" + webSocketLocation + "\");" + NEWLINE +
                "  socket.onmessage = function(event) {" + NEWLINE +
                "   console.log(event.data);"+NEWLINE +                
                "    var ta = document.getElementById('responseText');" + NEWLINE +
                "    ta.value = ta.value + '\\n' + event.data" + NEWLINE +
                 "	 //var obj =event.data; "+NEWLINE +  
                 "	 var obj =eval('('+event.data+')'); "+NEWLINE +                 
                "	 if(obj.actionId==311){"+NEWLINE +
	            "    	 $.ajax({ "+NEWLINE +
				"			type: 'POST',"+NEWLINE +
				//"			url: 'http://localhost:8080/',"+NEWLINE +		
				"			url: 'http://172.27.244.62:8082/',"+NEWLINE +
				"			data: '{\"actionInfo\":{\"actionId\":305,\"userId\":\"customer1\",\"userSource\":1,\"userType\":1}}', "+NEWLINE + 
				"			success: function (jsonResult) {"+NEWLINE +				
				"			 	var ta = document.getElementById('responseText');"+NEWLINE +
				"			 	ta.value = ta.value + '\\n' + jsonResult"+NEWLINE +			
				"			},"+NEWLINE +
				"			error: function (jsonResult) {"+NEWLINE +
				"				console.log(jsonResult);"+NEWLINE +
				"				alert(jsonResult.actionId);	"+NEWLINE +		 
				"			}"+ NEWLINE +
				"	  	});"+ NEWLINE +
				"	 }"+ NEWLINE +	
                "  };" + NEWLINE +
                "  socket.onopen = function(event) {" + NEWLINE +
                "    var ta = document.getElementById('responseText');" + NEWLINE +
                "    ta.value = \"Web Socket opened!\";" + NEWLINE +
                "  };" + NEWLINE +
                "  socket.onclose = function(event) {" + NEWLINE +
                "    var ta = document.getElementById('responseText');" + NEWLINE +
                "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE +
                "  };" + NEWLINE +
                "} else {" + NEWLINE +
                "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE +
                '}' + NEWLINE +
                NEWLINE +
                "function send(message) {" + NEWLINE +
                "  if (!window.WebSocket) { return; }" + NEWLINE +
                "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE +
                "    socket.send(message);" + NEWLINE +
                "  } else {" + NEWLINE +
                "    alert(\"The socket is not open.\");" + NEWLINE +
                "  }" + NEWLINE +
                '}' + NEWLINE +
                "</script>" + NEWLINE +
                "<form onsubmit=\"return false;\">" + NEWLINE +
                "<textarea name=\"message\" style=\"width:500px;height:150px;\">hello</textarea>" + NEWLINE +
                "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE +
                "       onclick=\"send(this.form.message.value)\" />" + NEWLINE +
                "<h3>Output</h3>" + NEWLINE +
                "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE +
                "</form>" + NEWLINE +
                "</body>" + NEWLINE +
                "</html>" + NEWLINE, CharsetUtil.US_ASCII);
    }

    private WebSocketServerIndexPage() {
        // Unused
    }
}
