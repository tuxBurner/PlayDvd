$(function() {
	

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
	 
	createSelect2DeselectCreate("#series",avaibleSeries);
	 
	
	// enable the button for  
	var title = $('#title').val();
	if("" != title) {
		$('#grabberButton').attr('disabled',null).removeClass('disabled');
	}
	
	/**
	 * clicking on the grabber button opens an popup where the user can search for the movie online
	 */
	$('#grabberButton').click(function() {
		searchGrabber($('#title').val(),"TMDB",$('#movieId').val());
	});
	
	/**
	 * button in the popup will be always clickable
	 */
	$('#grabber_search_button').live('click',function() {
		searchGrabber($('#grabber_search_input').val(),$('#grabberType').val(),$('#movieToEditId').val());
		return false;
	});
	
	/**
	 * The user searches in the grabber poup
	 */
	$('.pickGrabberEntry').live('click',function() {
    openGrabberMoviePopup($(this).data('grabberId'),$(this).data('grabberType'),$('#movieToEditId').val());
	});


  /**
   * The user wants to refresh the data via the grabber information he used before
   */
  $('#grabberRefetchButton').live('click',function() {
    openGrabberMoviePopup($('#grabberId').val(),$('#grabberType').val(),$('#movieId').val());
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

});


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
 * This opens the grabber movie informations popup where description,poster, backdrop etc can be selected
 * @param grabberId
 * @param grabberType
 * @param movieToEditId
 */
function openGrabberMoviePopup(grabberId,grabberType,movieToEditId) {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.InfoGrabberController.getMovieById(grabberId,grabberType),
    ajaxParams : { "movieToEditId" : movieToEditId},
    title: 'Movie info from Grabber',
    onOpen: closeWaitDiaLog,
    cssClass: "grabberModal",
    buttons : {
      "Ok" : {
        icon: "icon-trash",
        cssClass: "btn-danger",
        callback: fillFormWithInfoFromGrabber
      }
    }
  });
}

/**
 * This is called when the user picked movie from grabber and wants to fill the movie form with the infos  
 */
function fillFormWithInfoFromGrabber() {
	
	showWaitDiaLog();
	var formParams = $('#grabberMovieForm').formParams();
	var movieFormMode = $('#grabberMovieForm').attr('mode');
	var grabberType = $('#grabberMovieForm').attr('grabberType');
	$('#newMovieFormWrapper').html('').hide();
	pAjax(jsRoutes.controllers.MovieController.addMovieByGrabberId(movieFormMode,grabberType),formParams,
			function(data) {
		      $('#newMovieFormWrapper').html(data).show();
		      closeWaitDiaLog();
	        },
			function(err) {
	        	closeWaitDiaLog();
				console.error(err);
			});
	
	closeDialog();
	
}

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

/**
 * This searches the grabber and returns the result via ajax and writes it to the dialog
 * @param title
 * @param movieToEditId
 * @param grabberType
 */
function searchGrabber(title,grabberType,movieToEditId) {	
	showWaitDiaLog();	
	displayAjaxDialog({
	  route: jsRoutes.controllers.InfoGrabberController.searchGrabber(title,grabberType),
	  ajaxParams : { "movieToEditId" : movieToEditId},
	  title: 'Movie info from Grabber',
	  onOpen: closeWaitDiaLog,
	  cssClass: "grabberModal"
	});
}