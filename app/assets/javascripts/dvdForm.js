$(function() {
	
	 $('.chosen_select').chosen({
		    create_option: true,
		    // persistent_create_option decides if you can add any term, even if part of the term is also found, or only unique, not overlapping terms
		    persistent_create_option: false,
		    allow_single_deselect: true
		  });
	
	
	
	$('#existingDvdDialog').dialog({
		height: '450',
		width: '660',
		top: 60,
		autoOpen: false,
		modal: true
	});
	
	
	/**
	 * Clicking on the new movie buttons loads the new movie form via ajax and displays it under the dvd form
	 */
	$('#newMovieInfos').click(function() {
		
		$('#newMovieFormWrapper').html('').hide();
		
		pAjax(jsRoutes.controllers.MovieController.showAddMovieForm(),null,
				function(data) {
			      $('#newMovieFormWrapper').html(data).show(); 
		        },
				function(err) {
				  console.error(err);
				});
				
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
	
	
});