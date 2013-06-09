/**
 * LEND DIALOG
 */
$(document).on('click','.lendDvdBtn',function(event){
  displayAjaxDialog({
    route: jsRoutes.controllers.Dashboard.lendDialogContent($(this).data('dvdId')),
    title: Messages('headline.lendCopy'),
    cssClass:	'smallModal',
    closeButton: true,
    onOpen: function() {
      $("#lendToUser").select2({
        width: "element",
        allowClear: true
      });

      $("#lendToReservation").select2({
        width: "element",
        allowClear: true
      });
    },
    buttons: {
      "btn.lend" : {
        icon: "icon-share",
        cssClass: "btn-warning",
        callback: function() {
          var reservationVal = $('#lendToReservation').val();
          var userVal = $('#lendToUser').val();
          var freeVal = $('#freeName').val();
          var lendOtherInHull = $('#alsoOthersInHull').prop('checked');

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
    title: Messages('headline.unlendCopy'),
    cssClass:	'smallModal',
    closeButton: true,
    buttons: {
      "btn.unlend" : {
        icon: "icon-download",
        cssClass: "btn-warning",
        callback: function() {
          var lendOtherInHull = $('#alsoOthersInHull').prop('checked');
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
    title: Messages('headline.deleteCopy'),
    cssClass:	'smallModal',
    closeButton: true,
    buttons: {
      "btn.delete" : {
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
 * MARK AS VIEWED
 */
$(document).on('click','.markAsViewedBtn',function(event){
  displayAjaxDialog({
    route: jsRoutes.controllers.ViewedCopyController.markCopyAsViewedDialog($(this).data('copyId')),
    title: Messages('headline.markAsViewed'),
    cssClass:	'smallModal',
    closeButton: true,
    buttons: {
      "btn.ok" : {
        icon: "icon-eye-open",
        cssClass: "btn-warning",
        callback: function() {
          var copyId = $('#copyId').val();
          var remFromBookMarks = $('#removeBookmarkForViewed').prop('checked');
          pAjax(jsRoutes.controllers.ViewedCopyController.doMarkCopyAsViewed(copyId,remFromBookMarks ),null,
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
 * EO AS VIEWED
 */