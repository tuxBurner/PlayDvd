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
	

	// we need an empty option here
	$('#movieId').prepend('<option></option>');
	$('#movieId').val(selectedMovieId);
	
	 $('.def_chosen_select').select2({
		 placeholder: "Select a movie",
		 allowClear: true
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