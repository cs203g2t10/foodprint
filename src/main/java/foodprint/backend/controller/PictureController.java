package foodprint.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import foodprint.backend.model.Picture;
import foodprint.backend.service.PictureService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("api/v1/picture")
@CrossOrigin("*")
public class PictureController {
    PictureService service;

    @Autowired
    public PictureController(PictureService service) {
        this.service = service;
    }


    @GetMapping({"/pictures/all"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Gets all the pictures")
    public ResponseEntity<List<Picture>> getPictures() {
        return new ResponseEntity<>(service.getAllPictures(), HttpStatus.OK);
    }


    @GetMapping({"/{pictureId}"})
    @ResponseStatus(code = HttpStatus.OK)
    @Operation(summary = "Get a picture by its id")
    public ResponseEntity<String> getPictureById(@PathVariable("pictureId") Long id) {
        return new ResponseEntity<>(service.getPictureById(id), HttpStatus.OK);
    }

    //TODO Pathing, integration and testing
    @PostMapping(
            path = "",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<Picture> savePicture(@RequestParam("title") String title,
                                         @RequestParam("description") String description,
                                         @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(service.savePicture(title, description, file), HttpStatus.CREATED);
    }

    @GetMapping(value = "{id}/image/download")
    @ResponseStatus(code = HttpStatus.OK)
    public byte[] downloadPictureImage(@PathVariable("id") Long id) {
        return service.downloadPictureImage(id);
    }

    //TODO create a delete mapping
}