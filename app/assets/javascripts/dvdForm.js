$(function() {
	
	createSelect2DeselectCreate('#box',avaibleBoxes,"span6");
	createSelect2DeselectCreate('#collection',avaibleCollections,"span6");
	  /**
	   * searching for a movie
	   */
    createMovieDropDown('#movieId');

	 
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
              initializeMovieForm();
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
            initializeMovieForm();
		        },
				function(err) {
				  console.error(err);
				});
				
	});

});


/**
 * Opens the barcode scanner popup
 */
var openBarcodePopUp = function() {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.BarcodeController.displayBarcodeScaner(),
    ajaxParams : null,
    title: '<i class="icon-barcode"></i> Barcodescanner',
    onOpen: closeWaitDiaLog,
    cssClass: "barcodeModal"
  });

  return false;
}


/**
 * Opens the ean lookup dialog
 */
var openEANLookUp = function() {
  var eanNr = $.trim($('#eanNr').val());
  openSearchEanPopUp(eanNr);
};

/**
* Opens the ean search popup
* @param eanNr
*/
var openSearchEanPopUp = function(eanNr) {

  if(eanNr != null && eanNr != "") {

    showWaitDiaLog();
    displayAjaxDialog({
      route: jsRoutes.controllers.DvdController.searchEanNr(eanNr),
      ajaxParams : null,
      title: '<i class="icon-search"></i> Lookup movie on amazon',
      onOpen: function() { createMovieDropDown('#eanPickMovie'); closeWaitDiaLog(); },
      cssClass: "grabberModal"
    });
  }

  return false;
}

/**
 * Opens the grabber modal with the given ean nr and the title to search for
 *
 * @param eanNr
 * @param titleToSearch
 */
var eanStartOnlineGrabber = function(eanNr,titleToSearch) {
  searchGrabber(titleToSearch,"TMDB",null,eanNr)
}



/**
 * Redirects the user to the dvdform and sets the movie to the given id, also fills copytype etc to the dvd form
 * @param eanNr
 * @param movieId
 */
var eanPickExistingMovie = function(eanNr, movieId) {
  if(eanNr == null || movieId == null) {
    return;
  }

  window.location = jsRoutes.controllers.DvdController.showAddDvdByEanAndMovie(eanNr,movieId).absoluteURL();
}

/**
 * Creates a movie selector drop down with the existing movies in the db
 * @param selector
 */
var createMovieDropDown = function(selector) {
  $(selector).select2({
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
    initSelection : function (element,callback) {
      return callback(selectedMovie);
    },
    formatResult: movieFormatResult,
    formatSelection: movieFormatSelection,
    containerCssClass: "span6", // apply css that makes the dropdown taller
    escapeMarkup: function (m) { return m; } // we do not want to escape markup since we are displaying html in results
  });
}

/**
* Formats the result if the backend returns an array of movies in the dropdown
* @param movie
* @returns {string}
*/
var movieFormatResult =  function(movie) {
    var markup = "<table class='movie-result'><tr>";
    if (movie.hasPoster == true) {
      markup += "<td class='movie-image'><img src='" + jsRoutes.controllers.Dashboard.streamImage(movie.id,'POSTER','SELECT2').url + "'/></td>";
    }
    markup += "<td class='movie-info'><div class='movie-title'>" + movie.title + "</div>";
    markup += "</td></tr></table>"
    return markup;
  }

/**
* Formats the selection of movies when they are selected in the movie dropdown
* @param movie
* @returns {*}
*/
var movieFormatSelection =    function (movie) {
    if(movie.hasPoster == true) {
      return "<img class='flag' src='"+jsRoutes.controllers.Dashboard.streamImage(movie.id,'POSTER','TINY').url+"'/>" + movie.title;
    } else {
      return movie.title;
    }
  }


