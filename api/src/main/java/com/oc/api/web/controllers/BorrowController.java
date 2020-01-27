package com.oc.api.web.controllers;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import com.oc.api.dao.BorrowDao;
import com.oc.api.model.beans.Borrow;
import com.oc.api.web.exceptions.ForeignKeyNotExistsException;
import com.oc.api.web.exceptions.RessourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.PostMapping;





/**
 * BorrowController
 * 
 * RestController, handle client request and provide entity Borrow data
 */
@RestController
@Validated
public class BorrowController {

    @Autowired
    private BorrowDao borrowDao;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value="/borrows")
    public List<Borrow> getBorrows() {

        logger.info("Providing borrow resource from database: all borrow list");

        List<Borrow> borrows = borrowDao.findAll();

        return borrows;
    }

    @GetMapping(value="/borrows/{id}")
    public Borrow getBorrowById(@PathVariable @Min(value = 1) int id) {

        logger.info("Providing borrow resource from database: borrow id: " + id);

        Optional<Borrow> borrow = borrowDao.findById(id);

        if(!borrow.isPresent()) throw new RessourceNotFoundException("Le prêt n'existe pas, id: "+ id);

        return borrow.get();
    }

    @PostMapping(value="/borrows")
    public ResponseEntity<Void> addBorrow(@Valid @RequestBody Borrow borrow) {

        logger.info("Adding new borrow in database");
          
        Borrow borrowAdded;
        try {
            borrowAdded = borrowDao.save(borrow);
        } catch (Exception e) {
            throw new ForeignKeyNotExistsException("Une ou plusieurs clé étrangères n'existent pas.");
        }

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(borrowAdded.getId())
                .toUri();
  
        return ResponseEntity.created(location).build();
    }

    @PutMapping(value="/borrows/{id}")
    public ResponseEntity<Void> updateBorrow(@PathVariable @Min(value = 1) int id, @Valid @RequestBody Borrow borrowDetails) {
        
        logger.info("Updating borrow in database, id: " + id);

        try {
            borrowDao.findById(borrowDetails.getId()).get(); 
        } catch (NoSuchElementException e) {
            throw new RessourceNotFoundException("L'entité prêt demandée n'existe pas, id: " + borrowDetails.getId());
        }
        
        borrowDao.save(borrowDetails);

        return ResponseEntity.ok().build();        

    }
    
    @DeleteMapping(value="/borrows/{id}")
    public void deleteBorrow(@PathVariable @Min(value = 1) int id) {
        
        logger.info("Deleting borrow from database: id: "+ id);

        try {
            borrowDao.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RessourceNotFoundException("L'entité prêt n'existe pas, id: "+ id);
        }
        
        
    }

    @GetMapping(value="/users/{user_id}/borrows")
    public List<Borrow> getBorrowsByUserId(@PathVariable(value = "user_id") @Min(value = 1) int userId) {

        logger.info("Providing borrow resources from database by user id: " + userId);

        List<Borrow>  borrows = borrowDao.findByRegistereduserId(userId);

        return borrows;
    }  
    
}