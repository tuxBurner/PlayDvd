$(function() {

	// we need an empty option here
	$('#movieId').prepend('<option></option>');
	$('#movieId').val("");
	
	 $('.def_chosen_select').chosen({
       allow_single_deselect: true
     });
	
	 $('.chosen_select').chosen({
	   create_option: true,
	   // persistent_create_option decides if you can add any term, even if part of the term is also found, or only unique, not overlapping terms
	   persistent_create_option: false,
	   allow_single_deselect: true
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

	
	
});