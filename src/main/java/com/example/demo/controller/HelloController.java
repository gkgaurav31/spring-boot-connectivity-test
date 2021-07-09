package com.example.demo.controller;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.example.demo.model.Input;

@Controller
public class HelloController {
	
	@RequestMapping(path = "/download", method = RequestMethod.GET) 
    public StreamingResponseBody getSteamingFile(HttpServletResponse response,@RequestParam("path") String file) throws IOException {
		
		file = "/" + file;
		System.out.println("Trying to download: " + file);
		
        response.setContentType("application/text");
        InputStream inputStream = new FileInputStream(new File(file));
        return outputStream -> {
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                System.out.println("Writing some bytes..");
                outputStream.write(data, 0, nRead);
            }
        };
    }
	
	@RequestMapping("/")
	public ModelAndView getCases() {
		ModelAndView mav = new ModelAndView("cases");
		Input input = new Input();
		mav.addObject("input",input);
		return mav;
	}

	@RequestMapping("/process")
	public ModelAndView process(@ModelAttribute Input input) {
		ModelAndView mav = new ModelAndView("cases");

		String cmd = input.getMessage();

		String result="Try again.";

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("bash", "-c", cmd);

		try {

			Process process = processBuilder.start();

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			String line;
			
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

			int exitVal = process.waitFor();
			
			if (exitVal == 0) {
				System.out.println("Success!");
				System.out.println(output);
				result = output.toString();
			} else {
				
				String errorStream = printInputString(process.getErrorStream());
				String inputStream = printInputString(process.getInputStream());
				
				result = "ERROR_STREAM: \n" + errorStream + "\n\n" + "INPUT_STREAM: " + inputStream;
				
			}

		} catch (IOException e) {
			e.printStackTrace();
			result = e.getMessage();
		} catch (InterruptedException e) {
			e.printStackTrace();
			result = e.getMessage();
		}

		mav.addObject("output", result);
		
		return mav;
	}
	
	
	public static String printInputString(InputStream inputStream) {

	    String text = new BufferedReader(
	      new InputStreamReader(inputStream, StandardCharsets.UTF_8))
	        .lines()
	        .collect(Collectors.joining("\n"));
	    
	    return text;
	}


}
