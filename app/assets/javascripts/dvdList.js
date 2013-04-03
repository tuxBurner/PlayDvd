$(function() {
	
	$(document).on('mouseenter','.coverwrapper em',function() {
	      $('.dvdInfo',this).fadeIn(150);
	    }).on('mouseleave','.coverwrapper em',function() {
	      $('.dvdInfo',this).fadeOut(150);
	    });
	
	/**
	 * INFO DIALOG
	 */
	// open the info dialog when the user clicks on the info button
	$(document).on('click','.coverwrapper em',function(event){
		displayAjaxDialog({
			route: jsRoutes.controllers.Dashboard.displayDvd($(this).data('dvdId')),
        	title: 'Is set from the displaydvdSacla',
        	cssClass:	'dvdInfoModal',
          onClose: function() { $('#modal .modal-body').css('background-image', 'none'); }
		});
	  return false;		
	});
	/**
	 * EO INFO DIALOG
	 */
	
	/**
	 * LEND DIALOG
	 */
	$(document).on('click','.lendDvdBtn',function(event){
		displayAjaxDialog({
			route: jsRoutes.controllers.Dashboard.lendDialogContent($(this).data('dvdId')),
	    	title: 'Lend Copy',
	    	cssClass:	'smallModal',
	    	closeButton: true,
        onOpen: function() {
          $("#lendToUser").select2({
            width: "element",
            placeholder: "Select a user",
            allowClear: true
          });

          $("#lendToReservation").select2({
            width: "element",
            placeholder: "Select a reservation",
            allowClear: true
          });
        },
	    	buttons: {
	    		"Lend" : {
	    			icon: "icon-share",
	    			cssClass: "btn-warning",
	    			callback: function() {
              var reservationVal = $('#lendToReservation').val();
	    				var userVal = $('#lendToUser').val();
	    				var freeVal = $('#freeName').val();
	    				var lendOtherInHull = ($('#alsoOthersInHull').attr('checked') == 'checked') ? "true" : "false" ;

              // any set ?
              if((userVal == null || userVal == "") && (freeVal == null || freeVal == "") && (reservationVal == null || reservationVal == "")) {
	    					return;
	    				}

              // all off them set ?
	    				if(userVal != null && userVal != "" && freeVal != null && freeVal != ""  && reservationVal != null && reservationVal != "") {
	    					return;
	    				}
	    				pAjax(jsRoutes.controllers.Dashboard.lendDvd(
	    				  $("#lendDvdId").val()),
	    				  {"userName" : userVal, "freeName" : freeVal, "alsoOthersInHull" :  lendOtherInHull, "reservation" : reservationVal},
	    			      function(data){
                    //TODO: make me load a route
	    				      window.location.reload();
	    				   },
	    				   function(err) {
	    				  });
	    				closeDialog();
					}
	    		}
	    	}
		});
	});
	
	$(document).on('click','.unlendDvdBtn',function(event){
		displayAjaxDialog({
			route: jsRoutes.controllers.Dashboard.unLendDialogContent($(this).data('dvdId')),
	    	title: 'Return DVD',
	    	cssClass:	'smallModal',
	    	closeButton: true,
	    	buttons: {
	    		"Unlend" : {
	    			icon: "icon-download",
	    			cssClass: "btn-warning",
	    			callback: function() {
	    				var lendOtherInHull = ($('#alsoOthersInHull').attr('checked') == 'checked') ? "true" : "false" ;
						  pAjax(jsRoutes.controllers.Dashboard.unlendDvd($("#unlendDvdId").val()),
                {"alsoOthersInHull" :  lendOtherInHull},
					      function(data){
                  //TODO: make me load a route
                  window.location.reload();
						    },
						    function(err) {});
	    				  closeDialog();
					  }
	    		}
	    	}
		});
	});
	/**
	 * EO LEND DIALOG
	 */
	
	/**
	 * DELETE DIALOG
	 */		
	$(document).on('click','.deleteDvdBtn',function(event){
		displayAjaxDialog({
			route: jsRoutes.controllers.Dashboard.deleteDialogContent($(this).data('dvdId')),
	    	title: 'Delete DVD',
	    	cssClass:	'smallModal',
	    	closeButton: true,
	    	buttons: {
	    		"Delete" : {
	    			icon: "icon-trash",
	    			cssClass: "btn-danger",
	    			callback: function() {
					  var deleteDvdId = $('#deleteDvdId').val();
					  pAjax(jsRoutes.controllers.Dashboard.deleteDvd(deleteDvdId),null,
					    function(data){
						  closeDialog();	
						  window.location.reload();
						}, function(err) {
							closeDialog();
						});
					}
	    		}
	    	}
		});
	});
	/**
	 * EO DELETE DIALOG
	 */

  /**
   * SHOPPING CART
   */
  $(document).on('click','.addToCartBtn',function(event){
    var button = this;
    var dvdId = $(this).data('dvdId');
    var delButton = $('a.remFromCartBtn[data-dvd-id= "'+dvdId+'"]');
    pAjax(jsRoutes.controllers.ShoppingCartController.addCopyToCart(dvdId),null,
      function(data) {
        if(data == "true" || data == true) {
          pAjax(jsRoutes.controllers.ShoppingCartController.getShoppingCartMenu(),null,
            function(data) {
              $("#shoppingCartMenu").replaceWith(data);
              $('#shoppingCartMenu').addClass('animated flash');
              $(button).hide();
              Holder.run();
              $(delButton).show();
            }
          );
        }
      }
    );
    return false;
  });

  $(document).on('click','.remFromCartBtn',function(event){
    var button = this;
    var dvdId = $(this).data('dvdId');
    var addButton = $('a.addToCartBtn[data-dvd-id= "'+dvdId+'"]');
    pAjax(jsRoutes.controllers.ShoppingCartController.remCopyFromCart(dvdId),null,
      function(data) {
        if(data == "true" || data == true) {
          pAjax(jsRoutes.controllers.ShoppingCartController.getShoppingCartMenu(),null,
            function(data) {
              $("#shoppingCartMenu").replaceWith(data);
              $('#shoppingCartMenu').addClass('animated flash');
              $(button).hide();
              $(addButton).show();
            }
          );
        }
      }
    );
    return false;
  });
  /**
   * EO SHOPPING CART
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