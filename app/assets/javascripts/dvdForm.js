$(function() {

  // TODO: AJAX ME
	createSelect2DeselectCreate('#box',avaibleBoxes,"span6");
	createSelect2DeselectCreate('#collection',avaibleCollections,"span6");

  createSelect2TagAjaxBox("#audioTypes", jsRoutes.controllers.DvdController.searchForCopyAttribute(),{ attrType: "AUDIO_TYPE"});
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
    onClose: function() { stopVideoCapture(); },
    cssClass: "barcodeModal"
  });

  return false;
}


/**
 * Opens the amazon lookup dialog
 */
var openAmazonLookUp = function(code) {
  openSearchAmazonPopUp($.trim(code),$('#dvdId').val());
};

/**
* Opens the amazon search popup
* @param code
*/
var openSearchAmazonPopUp = function(code,copyId) {
  if(code != null && code != "") {
    showWaitDiaLog();
    displayAjaxDialog({
      route: jsRoutes.controllers.DvdController.searchAmazonByCode(code,copyId),
      ajaxParams : null,
      title: '<i class="icon-search"></i> Lookup movie on amazon',
      onOpen: function() { createMovieDropDown('#amazonPickMovie'); closeWaitDiaLog(); },
      cssClass: "grabberModal"
    });
  }

  return false;
}

/**
 * Opens the grabber modal with the given code and the title to search for
 *
 * @param code
 * @param titleToSearch
 * @param copyId
 */
var amazonStartOnlineGrabber = function(code,titleToSearch,copyId) {
  searchGrabber(titleToSearch,"TMDB",null,code,new String(copyId));
}



/**
 * Redirects the user to the dvdform and sets the movie to the given id, also fills copytype etc to the dvd form
 * @param code
 * @param movieId
 */
var amazonPickExistingMovie = function(code, movieId) {
  if(code == null || movieId == null) {
    return;
  }
  window.location = jsRoutes.controllers.DvdController.showDvdByAmazonAndMovie(code,movieId).absoluteURL(appIsInHttps);
}

/**
 * Creates a movie selector drop down with the existing movies in the db
 * @param selector
 */
var createMovieDropDown = function(selector) {

  var cssClassToAdd = $(selector).attr("class");

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
    containerCssClass: cssClassToAdd,
    escapeMarkup: function (m) { return m; } // we do not want to escape markup since we are displaying html in results
  });
}

/**
* Formats the result if the backend returns an array of movies in the dropdown
* @param movie
* @returns {string}
*/
var movieFormatResult =  function(movie) {
    var markup = "<table class='movie-result'><tr><td class='movie-image'>";

    if (movie.hasPoster == true) {
      markup += "<img src='" + jsRoutes.controllers.Dashboard.streamImage(movie.id,'POSTER','SELECT2').url + "'/>";
    } else {
      markup += '<img data-src="holder.js/60x80/#4D99E0:#fff/text:No Poster"/>';
    }

    markup += "</td><td class='movie-info'><div class='movie-title'>" + movie.title + "</div>";
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
      return '<img class="flag" data-src="holder.js/25x25/#4D99E0:#fff/text:No Poster"/>'+movie.title;
    }
  }


