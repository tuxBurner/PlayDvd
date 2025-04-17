package controllers;

import models.Movie;

public class MovieSelect2Value {

  public String title;

  public final Boolean hasPoster;

  public Long id;

  public MovieSelect2Value(final Movie movie) {
    title = movie.getTitle();
    hasPoster = movie.getHasPoster();
    id = movie.getId();
  }
}
