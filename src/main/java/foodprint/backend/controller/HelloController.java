// package foodprint.backend.controller;

// import java.util.Collections;
// import java.util.Map;
// import java.util.Optional;

// import org.slf4j.LoggerFactory;
// import org.slf4j.Logger;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.ResponseStatus;
// import org.springframework.web.bind.annotation.RestController;

// import foodprint.backend.model.User;

// // REST OpenAPI Swagger - http://localhost:8080/foodprint-swagger.html

// @RestController
// @RequestMapping("/api/v1/hello")
// public class HelloController {

// 	Logger logger = LoggerFactory.getLogger(HelloController.class);

//     @GetMapping({"/hello"})
// 	@ResponseStatus(code = HttpStatus.OK)
// 	public Map<String, String> hello(@RequestParam Optional<String> echoString) {
// 		if (echoString.isPresent()) {
// 			return Collections.singletonMap("response", "Hello " + echoString.get());
// 		}
// 		return Collections.singletonMap("response", "Hello World!");
// 	}

// 	@GetMapping({"/helloUser"})
// 	public User helloUser() {
// 		return new User("abc@def.com", "123123123", "Bob");
// 	}

// }