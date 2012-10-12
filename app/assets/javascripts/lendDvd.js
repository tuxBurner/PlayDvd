/**
 * This is used when the lendform js is called
 */
$(function() {
  
  /*$('#userName').chosen({
    allow_single_deselect: true
  });*/
	
	 $("#lendToUser").select2({
		 placeholder: "Select a user",
		 allowClear: true
	 });
		 
  
});