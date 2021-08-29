package foodprint.backend.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	// DEMO Below
	// This mapping can be accessed from your browser at http://localhost:8080/hello
    @GetMapping({"/hello"})
	public String hello(Model model) {
		return "hello world!"; 
	}

}