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

	
	 $('.chosen_select').chosen({
		    create_option: true,
		    // persistent_create_option decides if you can add any term, even if part of the term is also found, or only unique, not overlapping terms
		    persistent_create_option: false,
		    allow_single_deselect: true
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
	
	$('#existingDvdDialog').dialog({
		height: '450',
		width: '660',
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
	 * clicking on the existing dvd button creates a popup which lets the user select a dvd from a dropdown
	 */
	$('#existingDvdButton').click(function() {
		
		showWaitDiaLog();
		
		var dvdIdToEdit =  $('#dvdId').val();
		if(dvdIdToEdit == null) {
			dvdIdToEdit = 0;
		}
		
		
		pAjax(jsRoutes.controllers.Dashboard.listExistingMovies(dvdIdToEdit),null,
				  function(data){
					$('#existingDvdDialog .dialogContent').html(data);
					$('#existingDvdDialog').dialog( "option", "buttons",[{
					      text: 'Ok',
			              click: function() {
			            	 $('#selectExistingDvdForm').submit();
			              }
					    }]).dialog('open');
					closeWaitDiaLog();
				  },
				  function(err) {
					console.error(err);
					closeWaitDiaLog();
				});
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
            	 $('#tmdbMovieForm').submit();
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
