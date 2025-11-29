package com.uniMelb.msGenerator;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class AppController {

	@RequestMapping("/generateEvents")
	public ModelAndView index() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index.html");
		return modelAndView;
	}

	@GetMapping("/generatePatterns")
	public ModelAndView test1() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index.html");
		return modelAndView;
	}

	@GetMapping("/calculateCost")
	public ModelAndView test2() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index.html");
		return modelAndView;
	}

	@GetMapping("/generateMicroservices")
	@ResponseBody
	public String test3() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index.html");
		return "index.html";
	}

	@GetMapping("/generateEvents")
	public ModelAndView generateEvents() {
		ModelAndView modelAndView = new ModelAndView();
		modelAndView.setViewName("index.html");
		return modelAndView;
	}

	@GetMapping("/uploadFile")
	public String uploadFile() {
		return "index";
	}


}
