$(function() {
	
	$('#dvdInfoDialog').dialog({ 
		width: '90%',
		height: '710',
		autoOpen: false,
		modal: true
	});
	
	// open the info dialog when the user clicks on the info button
	$('.displayInfoButton').live('click',function(event){
		pAjax(jsRoutes.controllers.Dashboard.displayDvd($(this).data('dvdId')),null,
				  function(data){
				    $('#dvdInfoDialog .dialogContent').html(data);
					$('#dvdInfoDialog').dialog('open');
				  },
				  function(err) {
				    console.error(err);
				});	
		
	}); 
	
});