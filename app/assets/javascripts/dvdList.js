$(function() {
	
	/**
	 * INFO DIALOG
	 */
	$('#dvdInfoDialog').dialog({ 
		width: '1024',
		height: '710',
		autoOpen: false,
		modal: true,
		open: function() {
		  $("html").css("overflow", "hidden");
		},
		close: function() { 
		  $('#dvdInfoDialog .dialogContent').html('');
		  $("html").css("overflow", "auto"); 
	    }
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
	  return false;
		
	});
	/**
	 * EO INFO DIALOG
	 */
	
	/**
	 * LEND DIALOG
	 */
	$( "#dvdLendDialog" ).dialog({
	  resizable: false,
	  height:340,
	  width:440,
	  autoOpen: false,
	  modal: true,
	  buttons: {
			"Lend": function() {
				
				var userVal = $('#lendToUser').val();
				var freeVal = $('#freeName').val();
				var lendOtherInHull = ($('#alsoOthersInHull').attr('checked') == 'checked') ? "true" : "false" ; 
				
				
				if((userVal == null || userVal == "") && (freeVal == null || freeVal == "")) {
					return;
				}
				
				if(userVal != null && userVal != "" && freeVal != null && freeVal != "") {
					return;
				}
				
				pAjax(jsRoutes.controllers.Dashboard.lendDvd(
				  $("#lendDvdId").val()),
				  {"userName" : userVal, "freeName" : freeVal, "alsoOthersInHull" :  lendOtherInHull},
			      function(data){
				    alert('aww');
				   },
				   function(err) {
				  }
				 );
				
				$( this ).dialog( "close" );
			},
			Cancel: function() {
				$( this ).dialog( "close" );
			}
		}
	});
	
	$('.lendDvdBtn').live('click',function(event){
		pAjax(jsRoutes.controllers.Dashboard.lendDialogContent($(this).data('dvdId')),null,
				  function(data){
				    $('#dvdLendDialog .dialogContent').html(data);
				    $('#dvdLendDialog').dialog('open');
				  },
				  function(err) {
				    
				});
		
	});
	/**
	 * EO LEND DIALOG
	 */
	
});