package models;


import io.ebean.*;
import jakarta.persistence.*;
import play.data.validation.Constraints.Required;

import java.util.*;

@Entity
@Table(name = "dvd")
public class Dvd extends Model {

  /**
   * The FINDER for the database for searching in the database
   */
  public static final Finder<Long, Dvd> FINDER = new Finder<>(Dvd.class);

  public static final String HULL_NR_SEARCH = "hull:";

  public static final String EAN_NR_SEARCH = "ean:";

  @Id
  private Long id;

  @ManyToOne
  private User owner;

  @OneToOne
  private User borrower;

  @ManyToMany(cascade = CascadeType.MERGE, mappedBy = "dvds")
  private Set<DvdAttribute> attributes;

  private Long borrowDate;


  /**
   * If this is set the user entered a free name which does not exists in the
   * database
   */
  private String borrowerName;

  /**
   * The number off the hull off the dvd
   */
  private Integer hullNr;

  /**
   * Ean Number of the dvd so we can find it again
   */
  private String eanNr;

  /**
   * The Amazon asin nr of the copy
   */
  private String asinNr;

  /**
   * Additional info off the copy like directors cut
   */
  private String additionalInfo;

  /**
   * The movie which is on the dvd
   */
  @ManyToOne
  @Column(nullable = false)
  private Movie movie;

  @Required
  @Column(nullable = false)
  private Long createdDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public User getBorrower() {
    return borrower;
  }

  public void setBorrower(User borrower) {
    this.borrower = borrower;
  }

  public Set<DvdAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<DvdAttribute> attributes) {
    this.attributes = attributes;
  }

  public Long getBorrowDate() {
    return borrowDate;
  }

  public void setBorrowDate(Long borrowDate) {
    this.borrowDate = borrowDate;
  }

  public String getBorrowerName() {
    return borrowerName;
  }

  public void setBorrowerName(String borrowerName) {
    this.borrowerName = borrowerName;
  }

  public Integer getHullNr() {
    return hullNr;
  }

  public void setHullNr(Integer hullNr) {
    this.hullNr = hullNr;
  }

  public String getEanNr() {
    return eanNr;
  }

  public void setEanNr(String eanNr) {
    this.eanNr = eanNr;
  }

  public String getAsinNr() {
    return asinNr;
  }

  public void setAsinNr(String asinNr) {
    this.asinNr = asinNr;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public Movie getMovie() {
    return movie;
  }

  public void setMovie(Movie movie) {
    this.movie = movie;
  }

  public Long getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Long createdDate) {
    this.createdDate = createdDate;
  }
}
