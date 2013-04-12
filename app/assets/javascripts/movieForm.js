var initializeMovieForm = function() {

  Holder.run();

  // check the url stuff and change the image if it is set
	createPrevsrciewFromUrl('poster');
	createPrevsrciewFromUrl('backDrop');

	// when the user change the inputs for the images we want to change the preview
	$('#posterUrl').blur(function() {
	  if($(this).val() != null) {
	    createPrevsrciewFromUrl('poster');
	  }
	});

	createSelect2TagAjaxBox("#genres", jsRoutes.controllers.MovieController.searchForMovieAttribute(),{ attrType: "GENRE"});
	createSelect2TagAjaxBox("#actors", jsRoutes.controllers.MovieController.searchForMovieAttribute(),{ attrType: "ACTOR"});

  //TODO: AJAX ME !!!
	createSelect2DeselectCreate("#series",avaibleSeries,"span6");
	 
	
	// enable the button for  
	var title = $('#title').val();
	if("" != title) {
		$('#grabberButton').attr('disabled',null).removeClass('disabled');
	}
	
	/**
	 * clicking on the grabber button opens an popup where the user can search for the movie online
	 */
	$('#grabberButton').click(function() {
		searchGrabber($('#title').val(),"TMDB",$('#movieId').val(),null);
	});
	
  /**
   * The user wants to refresh the data via the grabber information he used before
   */
  $(document).on('click','#grabberRefetchButton',function() {
    openGrabberMoviePopup($('#grabberId').val(),$('#grabberType').val(),$('#movieId').val(),null);
  });


	
	/**
	 * Close the movie form if the user clicks on the button
	 */
	$('.movieFormCloseBtn').click(function() {
		$('#newMovieFormWrapper').html('').hide();
		$('#dvdFormWrapper').show();
	});
	
	/**
	 * user clicks on the submit edit or add button for the movie
	 */
	$('.movieFormSubmitBtn').click(function() {
		
		var formParams = $('#movieForm').formParams();
		var mode = $('#movieForm').attr('mode');

    // checks if the movie is already in the db
    if(mode == "add" && formParams['grabberId'] != null && formParams['grabberType'] != null) {
      pAjax(jsRoutes.controllers.MovieController.checkIfMovieAlreadyExists(formParams['grabberId'],formParams['grabberType']),null,
        function(data) {
          if(data == "true" || data == true) {
            // ask the user if he wants to add this movie although it aready exists
            displayDialog({
              title: 'Movie already exists in the Database',
              closeButton: true,
              content: "The movie: "+formParams['title']+" already exists in the Database, do you want to add it again ?",
              buttons : {
                "Ok" : {
                  icon: "icon-plus",
                  cssClass: "btn-danger",
                  callback: submitMovieForm               }
              }
            });
          } else {
            submitMovieForm(formParams,mode);
          }

        },
        function(err) {
          $('#newMovieFormWrapper').html(err.responseText).show();
        }
      );
      return false;
    } else {
      submitMovieForm(formParams,mode);
    }
	});

};


/**
 * Submits the movie form
 * @param formParams
 * @param mode
 */
function submitMovieForm(formParams, mode) {

  showWaitDiaLog();

  if(formParams == null && mode == null) {
    formParams = $('#movieForm').formParams();
    mode = $('#movieForm').attr('mode');
  }

  pAjax(jsRoutes.controllers.MovieController.addOrEditMovie(mode),formParams,
    function(data) {
      $('#newMovieFormWrapper').html('').hide();
      $('#dvdFormWrapper').show();

      // set the movie
      $("#movieId").select2("data", data);

      closeWaitDiaLog();
    },
    function(err) {
      closeWaitDiaLog();
      $('#newMovieFormWrapper').html(err.responseText).show();
    }
  );
};


/**
 * Creates a preview from the selected url
 * @param prevName
 */
function createPrevsrciewFromUrl(prevName) {
	var url = $('#'+prevName+'Url').val();
	if(url != "") {
		createPreview(prevName, url);
	}
}

/**
 * Changes the src of the preview image
 * @param prevName
 * @param src
 */
function createPreview(prevName, src) {
	$('#'+prevName+'_preview').attr('src',src);
}