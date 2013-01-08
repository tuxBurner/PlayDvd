$(function() {
	
	$('.coverwrapper em').live('mouseenter',function() {
	      $('.dvdInfo',this).fadeIn(150);
	    }).live('mouseleave',function() {
	      $('.dvdInfo',this).fadeOut(150);
	    });
	
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
	$('.coverwrapper em').live('click',function(event){
	  pAjax(jsRoutes.controllers.Dashboard.displayDvd($(this).data('dvdId')),null,
				  function(data){
		            displayDialog(data,'Is set from the displaydvdSacla','dvdInfoModal');
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
	$("#dvdLendDialog" ).dialog({
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
	
	$("#dvdUnLendDialog" ).dialog({
		  resizable: false,
		  height:340,
		  width:440,
		  autoOpen: false,
		  modal: true,
		  buttons: {
				"Unlend": function() {
					var lendOtherInHull = ($('#alsoOthersInHull').attr('checked') == 'checked') ? "true" : "false" ;
					
					pAjax(jsRoutes.controllers.Dashboard.unlendDvd(
					  $("#unlendDvdId").val()),{"alsoOthersInHull" :  lendOtherInHull},
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
	
	$('.unlendDvdBtn').live('click',function(event){
		pAjax(jsRoutes.controllers.Dashboard.unLendDialogContent($(this).data('dvdId')),null,
				  function(data){
				    $('#dvdUnLendDialog .dialogContent').html(data);
				    $('#dvdUnLendDialog').dialog('open');
				  },
				  function(err) {
				    
				});
		
	});
	/**
	 * EO LEND DIALOG
	 */
	
	/**
	 * DELETE DIALOG
	 */
	$('#deleteDvdDialog').dialog({
		  resizable: false,
		  height:340,
		  width:440,
		  autoOpen: false,
		  modal: true,
		  buttons: {
				"Delete": function() {
					var deleteDvdId = $('#deleteDvdId').val();
					pAjax(jsRoutes.controllers.Dashboard.deleteDvd(deleteDvdId),null,
							  function(data){
						       $('#deleteDvdDialog').dialog( "close" );	
						       window.location.reload();
					          }, function(err) {
					        	$('#deleteDvdDialog').dialog( "close" );
							});
				},
				Cancel: function() {
					$( this ).dialog( "close" );
				}
			}
		});
	
	$('.deleteDvdBtn').live('click',function(event){
		pAjax(jsRoutes.controllers.Dashboard.deleteDialogContent($(this).data('dvdId')),null,
				  function(data){
				    $('#deleteDvdDialog .dialogContent').html(data);
				    $('#deleteDvdDialog').dialog('open');
				  },
				  function(err) {
				    
				});
	});
	/**
	 * EO DELETE DIALOG
	 */
	
	
	/**
	 * SEARCH FORM
	 */
	
	// check if to display the advanced search from
	if(displayAdvancedOrder === true) {
		$('#advancedSearchForm').show();
	}
	
	createSelect2Deselect('#searchGenre',avaibleGenres);
	createSelect2Deselect('#searchOwner',avaibleUsers);
	createSelect2Deselect('#searchAgeRating',avaibleAgeRatings,function(item) { 
		if(item == null || item == "") {
			return item;
		}
		return "<img class='flag' src='/assets/images/agerating/" + item.id + ".gif'/>";
	});
	createSelect2Deselect('#searchOrderBy',listOrderBy,null,false);
	createSelect2Deselect('#searchCopyType',avaibleCopyTypes,function(item) { 
		if(item == null || item == "") {
			return item;
		}
		return "<img class='flag' src='/assets/images/copy_type/" + item.id + ".png'/>";
	});
	/**
	 * EO SEARCH FORM
	 */
	
});