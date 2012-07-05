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
	
	$("#genresTags").tagit({
		'allowSpaces' : true,
		'singleField' : true,
		'availableTags' : avaibleGenres,
		'singleFieldNode' : $('#genres')
	});
	
	$("#actorsTags").tagit({
		'allowSpaces' : true,
		'singleField' : true,
		'availableTags' : avaibleActors,
		'singleFieldNode' : $('#actors')
	});
	
	// enable the button for tmdb 
	var title = $('#title').val();
	if("" != title) {
		$('#tmdbButton').attr('disabled',null).removeClass('disabled');
	}
	
	$('#tmdbDialog').dialog({ 
		width: '1000',
		height: '710',
		top: 60,
		autoOpen: false,
		modal: true
	});
	
	/**
	 * clicking on the tmdb button opens an popup where the user can search for the movie online
	 */
	$('#tmdbButton').click(function() {
		searchTmdb($('#title').val(),$('#dvdId').val());
	});
	
	/**
	 * button in the popup will be always clickable
	 */
	$('#tmdb_search_button').live('click',function() {
		searchTmdb($('#tmdb_search_input').val(),$('#tmdbDvdId').val());
		return false;
	});
	
	/**
	 * The user searches in the tmdb poup
	 */
	$('.pickTmdbEntry').live('click',function() {
		showWaitDiaLog();
		
		var params = { "tmdbDvdId" : $('#tmdbDvdId').val()};
		
		pAjax(jsRoutes.controllers.Tmdb.getMovieById($(this).data('tmdbId')),params,
		  function(data){
		    $('#tmdbDialog .dialogContent').html(data);
		    $('#tmdbDialog').dialog( "option", "buttons", [{
		      text: 'Ok',
              click: function() {        	 
            	 fillFormWithInfoFromTmdb();
              }
		    }]);
			closeWaitDiaLog();
		  },
		  function(err) {
		    console.error(err);
		});
	});
		
});

/**
 * This is called when the user picked movie from tmdb and wants to fill the movie form with the infos  
 */
function fillFormWithInfoFromTmdb() {
	
	showWaitDiaLog();
	
	var formParams = $('#tmdbMovieForm').formParams();
	var movieFormMode = $('#movieFormMode').val();
	$('#newMovieFormWrapper').html('').hide();
	
	pAjax(jsRoutes.controllers.MovieController.addMovieByTmdbId(movieFormMode),formParams,
			function(data) {
		      $('#newMovieFormWrapper').html(data).show();
		      closeWaitDiaLog();
		      
	        },
			function(err) {
	        	closeWaitDiaLog();
				console.error(err);
			});
	
	$('#tmdbDialog').dialog("close"); 
	
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
 * This searches the tmdb and returns the result via ajax and writes it to the dialog
 * @param title
 */
function searchTmdb(title,dvdToEditId) {
	
	showWaitDiaLog();
	
	var params = { "tmdbDvdId" : dvdToEditId};
	
	pAjax(jsRoutes.controllers.Tmdb.searchTmdb(title),params,
	  function(data){
		$('#tmdbDialog .dialogContent').html(data);
		$('#tmdbDialog').dialog( "option", "buttons",[]).dialog('open');
		closeWaitDiaLog();
	  },
	  function(err) {
		console.error(err);
	});
	

}