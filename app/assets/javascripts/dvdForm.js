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
          minimumInputLength: 3,
          ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
              url: jsRoutes.controllers.MovieController.searchMoviesForDvdSelect().url.substring(0,jsRoutes.controllers.MovieController.searchMoviesForDvdSelect().url.indexOf('?')),
              dataType: 'json',
              data: function (term, page) {
                  return {
                      term: term
                  };
              },
              results: function (data, page) { // parse the results into the format expected by Select2.
                  // since we are using custom formatting functions we do not need to alter remote JSON data
            	  console.error(data);
                  return {results: data};
              }
          },
          formatResult: movieFormatResult, // omitted for brevity, see the source of this page
          formatSelection: movieFormatSelection  // omitted for brevity, see the source of this page
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
    return movie.title;
}