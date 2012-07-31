$(function() {
	
	$('#box').select2({		   
	    allowClear: true,
	    createSearchChoice: function(term, data) { 
        	  if ($(data).filter(function() { console.info(this); return this.localeCompare(term)===0; }).length===0) {
        		  return term;
        	  }
          },
	    formatSelection: function(item) { return item; },
        formatResult:  function(item) { return item; },
        id: function(object) {
        	return object;
        },
        initSelection : function (element) {
          return $(element).val();
        },
        data: avaibleBoxes
      });
	
	  $('#collection').select2({		   
	    allowClear: true,
	    createSearchChoice: function(term, data) { 
        	  if ($(data).filter(function() { console.info(this); return this.localeCompare(term)===0; }).length===0) {
        		  return term;
        	  }
          },
	    formatSelection: function(item) { return item; },
        formatResult:  function(item) { return item; },
        id: function(object) {
        	return object;
        },
        initSelection : function (element) {
          return $(element).val();
        },
        data: avaibleCollections
      });	
	  
	  
	  /**
	   * searching for a movie
	   */
	  $("#movieId").select2({
          placeholder: {title: "Search for a movie", id: ""},
          allowClear: true,
          minimumInputLength: 3,
          ajax: { 
              url: jsRoutes.controllers.MovieController.searchMoviesForDvdSelect().url.substring(0,jsRoutes.controllers.MovieController.searchMoviesForDvdSelect().url.indexOf('?')),
              dataType: 'json',
              data: function (term, page) {
                  return {
                      term: term
                  };
              },
              results: function (data, page) { 
                  return {results: data};
              }
          },
          initSelection : function (element) {
            return selectedMovie;
          },
          formatResult: movieFormatResult, 
          formatSelection: movieFormatSelection
      });
	
	
	 
	 $('#editMovieInfos').click(function() {
		 var selectedMovieId = $('#movieId').val();
		 if(selectedMovieId == null || selectedMovieId == "") {
			 alert("No Movie selected to edit !");
			 return;
		 }
			
			$('#newMovieFormWrapper').html('').hide();
			
			pAjax(jsRoutes.controllers.MovieController.showEditMovieForm(selectedMovieId),null,
					function(data) {
				      $('#dvdFormWrapper').hide();
				      $('#newMovieFormWrapper').html(data).show(); 
			        },
					function(err) {
					  console.error(err);
					});
					
		});
	
	
	/**
	 * Clicking on the new movie buttons loads the new movie form via ajax and displays it under the dvd form
	 */
	$('#newMovieInfos').click(function() {
		
		$('#newMovieFormWrapper').html('').hide();
		
		pAjax(jsRoutes.controllers.MovieController.showAddMovieForm(),null,
				function(data) {
			      $('#dvdFormWrapper').hide();
			      $('#newMovieFormWrapper').html(data).show(); 
		        },
				function(err) {
				  console.error(err);
				});
				
	});

	
	
});


function movieFormatResult(movie) {
    var markup = "<table class='movie-result'><tr>";
    if (movie.hasPoster == true) {
      markup += "<td class='movie-image'><img src='" + jsRoutes.controllers.Dashboard.streamImage(movie.id,'POSTER','SELECT2').url + "'/></td>";
    }
    markup += "<td class='movie-info'><div class='movie-title'>" + movie.title + "</div>";
    markup += "</td></tr></table>"
    return markup;
}

function movieFormatSelection(movie) {
	if(movie.hasPoster == true) {
	  return "<img class='flag' src='"+jsRoutes.controllers.Dashboard.streamImage(movie.id,'POSTER','TINY').url+"'/>" + movie.title;
	} else {
		return movie.title;
	}
    
}