package com.ddf.vodsystem.controllers;

import com.ddf.vodsystem.entities.EditDTO;
import com.ddf.vodsystem.services.EditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class EditController {
    private final EditService editService;

    @Autowired
    public EditController(EditService editService) {
        this.editService = editService;
    }

    @PostMapping("edit/{uuid}")
    public ResponseEntity<String> edit(@PathVariable("uuid") String uuid, @ModelAttribute EditDTO editDTO) {
        editService.edit(uuid, editDTO);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

    @GetMapping("/process/{uuid}")
    public ResponseEntity<String> convert(@PathVariable("uuid") String uuid) {
        editService.jobReady(uuid);
        return new ResponseEntity<>(uuid, HttpStatus.OK);
    }

}
