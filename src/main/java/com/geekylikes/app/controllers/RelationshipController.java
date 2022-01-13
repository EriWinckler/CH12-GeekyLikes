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
import java.util.Optional;

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
            return new ResponseEntity<>(new MessageResponse("Invalid User")
                    , HttpStatus.BAD_REQUEST);
        }

        Developer originator =
                developerRepository.findByUser_id(currentUser.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));


        Developer recipient =
                developerRepository.findById(rId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Optional<Relationship> rel = repository.findByOriginator_idAndRecipient_id(originator.getId(), recipient.getId());

        if (rel.isPresent()) {
            return new ResponseEntity<>(new MessageResponse("Nice try!"), HttpStatus.OK);
        }

        Optional<Relationship> invRel = repository.findByOriginator_idAndRecipient_id(recipient.getId(), originator.getId());

        if (invRel.isPresent()) {
            switch (invRel.get().getType()) {
                case PENDING:
                    invRel.get().setType(ERelationship.ACCEPTED);
                    repository.save(invRel.get());
                    return new ResponseEntity<>(new MessageResponse("Success"), HttpStatus.CREATED);

                case ACCEPTED:
                    return new ResponseEntity<>(new MessageResponse("You are already friends stop taxing our system"), HttpStatus.OK);

                case BLOCKED:
                    return new ResponseEntity<>(new MessageResponse("GG"), HttpStatus.OK);

                default:
                    return new ResponseEntity<>(new MessageResponse("SERVER ERROR _ DEFAULT RELATIONSHIP"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            repository.save(new Relationship(originator, recipient, ERelationship.PENDING));
        } catch (Exception e) {
            System.out.println("error" + e.getLocalizedMessage());
            return new ResponseEntity<>(new MessageResponse("Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new MessageResponse("Success"), HttpStatus.CREATED);

    }

    @PostMapping("/block/{rId}")
    public ResponseEntity<MessageResponse> blockRecipient(@PathVariable Long rId) {
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            return new ResponseEntity<>(new MessageResponse("Invalid User"), HttpStatus.BAD_REQUEST);
        }

        Developer originator = developerRepository.findByUser_id(currentUser.getId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        Developer recipient = developerRepository.findById(rId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        Optional<Relationship> rel = repository.findByOriginator_idAndRecipient_id(originator.getId(), recipient.getId());

        if (rel.isPresent()) {
            switch (rel.get().getType()) {
                case PENDING:
                    rel.get().setType(ERelationship.BLOCKED);
                    repository.save(rel.get());
                    return new ResponseEntity<>(new MessageResponse("Blocked"), HttpStatus.ACCEPTED);
                case ACCEPTED:
                    rel.get().setType(ERelationship.BLOCKED);
                    repository.save(rel.get());
                    return new ResponseEntity<>(new MessageResponse("Blocked from Relationship"), HttpStatus.ACCEPTED);
                case BLOCKED:
                    return new ResponseEntity<>(new MessageResponse("All set"), HttpStatus.OK);
                default:
                    return new ResponseEntity<>(new MessageResponse("SERVER ERROR _ DEFAULT RELATIONSHIP"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        Optional<Relationship> invRel = repository.findByOriginator_idAndRecipient_id(recipient.getId(), originator.getId());

        if(invRel.isPresent()) {
            switch (invRel.get().getType()) {
                case PENDING:
                    invRel.get().setType(ERelationship.BLOCKED);
                    repository.save(invRel.get());
                    return new ResponseEntity<>(new MessageResponse("Blocked"), HttpStatus.ACCEPTED);
                case ACCEPTED:
                    invRel.get().setType(ERelationship.BLOCKED);
                    repository.save(invRel.get());
                    return new ResponseEntity<>(new MessageResponse("Blocked from Relationship"), HttpStatus.ACCEPTED);
                case BLOCKED:
                    return new ResponseEntity<>(new MessageResponse("All set"), HttpStatus.OK);
                default:
                    return new ResponseEntity<>(new MessageResponse("SERVER ERROR _ DEFAULT RELATIONSHIP"), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        try {
            repository.save(new Relationship(originator, recipient, ERelationship.BLOCKED));
        } catch (Exception e) {
            System.out.println("error" + e.getLocalizedMessage());
            return new ResponseEntity<>(new MessageResponse("Server error"), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(new MessageResponse("Blocking Success"), HttpStatus.OK);
    }
}


