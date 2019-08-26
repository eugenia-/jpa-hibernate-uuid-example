package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main class to run an example of Optimistic locking which uses entity with version field.
 */
public class OptimisticLockExample {

  private static final int PERSON_COUNT = 2;

  private static CrudUtil crudUtil = new CrudUtil();

  public static void main(String[] args) throws JsonProcessingException, InterruptedException {

    System.out.println("Insert persons...");
    int i = 0;
    while (i < PERSON_COUNT) {
      crudUtil.insertPerson(PersonEntity.builder()/*.id(UUID.randomUUID())*/.name("InitialName_" + ++i).country("InitialCounty").build());
    }

    // Update name and verify that entity versions are changed
    crudUtil.printPersons();
    crudUtil.updatePersonNames();
    crudUtil.printPersons();


    // Run threads that modify a versioned personEntity, verify that Optimistic Lock Exception is thrown.
    PersonEntity personEntity1 = crudUtil.listPersons().get(0);
    PersonEntity personEntity2 = crudUtil.listPersons().get(1);

    /* The following callable objects in list will read persons from db to update and will sleep for some milliseconds
    before committing the changes, The commit delays will result in OptimisticLock exception.*/
    List<Callable<String>> callableList = Arrays.asList(
        () -> crudUtil.editPersonCountry(500, personEntity1.getId()),
        () -> crudUtil.editPersonCountry(1000, personEntity1.getId()),
        /* Pay attention the following thread which updates country will update personEntity2 county in db,
         but the second thread which updates name will update the person country again to initial value. */
        () -> crudUtil.editPersonCountry(1000, personEntity2.getId()),
        () -> crudUtil.editPersonName(2000, personEntity2.getId()),
        () -> crudUtil.editPersonName(4000, personEntity2.getId())
    );

    ExecutorService executorService = Executors.newFixedThreadPool(callableList.size());

    executorService.invokeAll(callableList)
        .stream()
        .map(future -> {
          try {
            return future.get();
          } catch (Exception e) {
            throw new IllegalStateException(e);
          }
        })
        .forEach(System.out::println);


    executorService.shutdown();

    crudUtil.printPersons();
  }
}
