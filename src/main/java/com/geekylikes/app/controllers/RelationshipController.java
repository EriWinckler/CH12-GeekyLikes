package com.geekylikes.app.controllers;

import com.geekylikes.app.models.auth.User;
import com.geekylikes.app.models.developer.Developer;
import com.geekylikes.app.models.relationship.ERelationship;
import com.geekylikes.app.models.relationship.Relationship;
import com.geekylikes.app.payloads.response.MessageResponse;
import com.geekylikes.app.repositories.DeveloperRepository;
import com.geekylikes.app.repositories.RelationshipRepository;
import com.geekylikes.app.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.sql.SQLOutput;

@CrossOrigin
@RestController
@RequestMapping("/api/relationships")
public class RelationshipController {
    @Autowired
    private RelationshipRepository repository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeveloperRepository developerRepository;

    @PostMapping("/add/{rId}")
    public ResponseEntity<MessageResponse> addRelationship(@PathVariable Long rId) {
        // create a pending relationship between longged in users and rid (recipientId)
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return new ResponseEntity<>(new MessageResponse<>("Invalid User")
                    , HttpStatus.BAD_REQUEST);
        }

        Developer originator =
                developerRepository.findByUser_id(currentUser.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        Developer recipient =
                developerRepository.findById(rId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        //create try catch if save fails
        try{
            repository.save(new Relationship(originator, recipient,
                    ERelationship.PENDING));
        } catch (Exception e) {
            System.out.println("error" + e.getLocalizedMessage());
            return new ResponseEntity<> (new MessageResponse("Server error"),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        return new ResponseEntity<>(new MessageResponse("Success"),
                HttpStatus.CREATED);
    }
}
