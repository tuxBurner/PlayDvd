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
	
	 $("#genres").select2({tags: avaibleGenres,  tokenSeparators: [","]});	
	 $("#actors").select2({ tags : avaibleActors });
	 $("#series").select2({
		 placeholder: "Select a movieseries",
		 allowClear: true
	 });
	 
	 $('.select2-container-multi').addClass("span5")
	
	// enable the button for  
	var title = $('#title').val();
	if("" != title) {
		$('#grabberButton').attr('disabled',null).removeClass('disabled');
	}
	
	$('#grabberDialog').dialog({ 
		width: '1000',
		height: '710',
		top: 60,
		autoOpen: false,
		modal: true,
		close:  function() {
		  $('#grabberDialog .dialogContent').html('');
		  $("html").css("overflow", "auto");
		},
	    open: function() {
		  $("html").css("overflow", "hidden");
		}
	});
	
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
		showWaitDiaLog();
		
		var params = { "movieToEditId" : $('#movieToEditId').val()};
		
		pAjax(jsRoutes.controllers.InfoGrabberController.getMovieById($(this).data('grabberId'),$(this).data('grabberType')),params,
		  function(data){
		    $('#grabberDialog .dialogContent').html(data);
		    $('#grabberDialog').dialog( "option", "buttons", [{
		      text: 'Ok',
              click: function() {        	 
            	 fillFormWithInfoFromGrabber();
              }
		    }]);
			closeWaitDiaLog();
		  },
		  function(err) {
		    console.error(err);
		});
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
		
		showWaitDiaLog();
		
		var formParams = $('#movieForm').formParams();
		var mode = $('#movieForm').attr('mode')
		
		pAjax(jsRoutes.controllers.MovieController.addOrEditMovie(mode),formParams,
				function(data) {
			      $('#newMovieFormWrapper').html('').hide();
			      $('#dvdFormWrapper').show();
			      
			      // set the movie
			      $("#movieId").select2("val", data);
			      
			  	  
			      closeWaitDiaLog();
		        },
				function(err) {
		        	closeWaitDiaLog();
		        	$('#newMovieFormWrapper').html(err.responseText).show();
				}
		);
				
		
	});
		
});

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
	
	$('#grabberDialog').dialog("close"); 
	
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
 */
function searchGrabber(title,grabberType,movieToEditId) {
	
	showWaitDiaLog();
	
	var params = { "movieToEditId" : movieToEditId};
	
	pAjax(jsRoutes.controllers.InfoGrabberController.searchGrabber(title,grabberType),params,
	  function(data){
		$('#grabberDialog .dialogContent').html(data);
		$('#grabberDialog').dialog( "option", "buttons",[]).dialog('open');
		closeWaitDiaLog();
	  },
	  function(err) {
		console.error(err);
	});
	

}