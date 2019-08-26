package com.company;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.OptimisticLockException;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * The util class contains CRUD operations and other common methods to manipulate entities.
 */
public class CrudUtil {

  private final SessionFactory sessionFactory;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * The default constructor.
   */
  CrudUtil() {

    java.util.Properties properties = new Properties();
    try {
      properties.load(this.getClass().getClassLoader().getResourceAsStream("hibernate-mariadb.properties"));
      //properties.load(this.getClass().getClassLoader().getResourceAsStream("hibernate-postgres.properties"));
    } catch (IOException e) {
      throw new RuntimeException(String.format("Error to initiate %s.", this.getClass().getName()), e);
    }

    sessionFactory = new Configuration()
        .configure() // configures settings from hibernate.cfg.xml
        .addProperties(properties)
        .addAnnotatedClass(PersonEntity.class)
        .buildSessionFactory();
  }

  /**
   * Edits a person name by id.
   *
   * @param sleepTime time in milliseconds to wait before committing the changes.
   * @param personId  person id.
   * @return A string result message of the transaction operations.
   */
  public String editPersonCountry(long sleepTime, UUID personId) {
    String status;
    long threadId = Thread.currentThread().getId();
    System.out.println(String.format("%s thread updating person country by %s person id", threadId, personId));
    Session session = null;

    try {
      session = sessionFactory.openSession();
      session.beginTransaction();
      PersonEntity fetchedPersonEntity = session.get(PersonEntity.class, personId);
      sleep(sleepTime);
      fetchedPersonEntity.setCountry(String.format("%s changed by %s thread", fetchedPersonEntity.getCountry(), threadId));
      status = commitPersonFragment(session);
    } finally {
      if (session != null) {
        session.close();
      }
    }
    return status;
  }

  /**
   * Edits a person name by id.
   *
   * @param sleepTime time in milliseconds to wait before committing the changes.
   * @param personId  person id.
   * @return A string result message of the transaction operations.
   */
  public String editPersonName(long sleepTime, UUID personId) {

    String status;
    long threadId = Thread.currentThread().getId();
    System.out.println(String.format("%s thread updating person name by %s person id", threadId, personId));
    Session session = null;

    try {
      session = sessionFactory.openSession();
      session.beginTransaction();
      PersonEntity fetchedPersonEntity = session.get(PersonEntity.class, personId);

      sleep(sleepTime);

      fetchedPersonEntity.setName(String.format("%s updatedBy %s thread", fetchedPersonEntity.getName(), threadId));
      status = commitPersonFragment(session);

    } finally {
      if (session != null) {
        session.close();
      }
    }
    return status;

  }

  /**
   * A reusable fragment that is used for saving {@link PersonEntity} entity.
   *
   * @param session session.
   * @return A string result message of the transaction operations.
   */
  private String commitPersonFragment(Session session) {
    long threadId = Thread.currentThread().getId();
    String status;
    try {
      session.getTransaction().commit();
      status = String.format("Tread %s, Transaction Status: %s.", threadId, session.getTransaction().getStatus());
    } catch (OptimisticLockException e) {
      status = String.format("Tread %s, Transaction Status: %s. Caused by %s", threadId, session.getTransaction().getStatus(), e.getMessage());
    }
    return status;
  }

  /**
   * Prints persons that are stored in the DB.
   *
   * @throws JsonProcessingException exception.
   */
  public void printPersons() throws JsonProcessingException {
    List<PersonEntity> result = listPersons();
    System.out.println("Persons in db:");
    for (PersonEntity personEntity : result) {
      System.out.println(OBJECT_MAPPER.writeValueAsString(personEntity));
    }
  }

  /**
   * Creates a person into the DB.
   *
   * @param personEntity an entity to insert.
   */
  public void insertPerson(PersonEntity personEntity) {
    System.out.println("Insert persons...");
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    session.save(personEntity);
    session.getTransaction().commit();
    session.close();
  }

  /**
   * Updates persons names one by one.
   */
  public void updatePersonNames() {
    System.out.println("Update persons...");
    Session session = sessionFactory.openSession();
    List<PersonEntity> personEntityList = listPersons();
    session.beginTransaction();

    for (PersonEntity personEntity : personEntityList) {
      personEntity.setName(personEntity.getName() + "1");
      session.update(personEntity);
    }
    session.getTransaction().commit();
    session.close();
  }

  /**
   * Fetch person list from DB.
   *
   * @return List of {@link PersonEntity}.
   */
  public List<PersonEntity> listPersons() {
    Session session = sessionFactory.openSession();
    session.beginTransaction();
    List<PersonEntity> result = session.createQuery("from " + PersonEntity.class.getName()).list();
    session.close();
    return result;
  }

  /**
   * Causes the currently executing thread to sleep.
   *
   * @param sleepTime time in milliseconds.
   */
  private static void sleep(long sleepTime) {
    if (sleepTime > 0) {
      try {
        System.out.println(String.format("%s thread sleep for %s ms", Thread.currentThread().getId(), sleepTime));
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }
}
