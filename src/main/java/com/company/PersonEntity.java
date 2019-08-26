package com.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import java.util.UUID;

/**
 * Person entity with version field, which is managed by Hibernate.
 */
@Entity(name = "Person")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PersonEntity {

  @Id
  @GeneratedValue(generator = "uuid2")
  @GenericGenerator(name = "uuid2", strategy = "uuid2")
  @Column(length = 36)
  @Type(type = "uuid-char")
  private UUID id;

  @Column(name = "name")
  private String name;

  /**
   * Property which should not bump up the entity version.
   */
  @OptimisticLock(excluded = true)
  private String country;

  @Version
  private int version;


}
