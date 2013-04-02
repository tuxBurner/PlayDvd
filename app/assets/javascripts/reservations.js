$(function() {

  // select first tab in reservations
  $('#reservationsTab a:first').tab('show');

  /**
   * Changing the all checkbox value
   */
  $('input.reservationsCheckAll').change(function() {
    var isChecked = $(this).is(":checked");
    var borrowerId = $(this).data('borrower');
    // get all sub and check or uncheck them
    $('#resTab'+borrowerId+' tbody input:checkbox').prop('checked', isChecked);

    reservationsUpdateButtons(borrowerId);
  });

  /**
   * changing a copy checkbox
   */
  $('input.reservationsCopyCheckBox').change(function() {
    var borrowerId = $(this).data('borrower');
    var checkedLenght = $('#resTab'+borrowerId+' tbody input:checkbox:checked').length;
    var totalLenght = $('#resTab'+borrowerId+' tbody input:checkbox').length;

    if(totalLenght == checkedLenght) {
      $('#resTab'+borrowerId+' thead input.reservationsCheckAll').prop('checked', true);
    } else {
      $('#resTab'+borrowerId+' thead input.reservationsCheckAll').prop('checked', false);
    }

    reservationsUpdateButtons(borrowerId);
  });
});

/**
 * Checks if any checkboxes are checked in the reservations borrower copy tab and enables or disables the buttons
 * @param borrowerId
 */
var reservationsUpdateButtons = function(borrowerId) {

  var checkedLenght = $('#resTab'+borrowerId+' tbody input:checkbox:checked').length;

  if(checkedLenght > 0) {
    $('#resTab'+borrowerId+' tfoot button').removeClass('disabled');
    return;
  }

  $('#resTab'+borrowerId+' tfoot button').addClass('disabled');
}

/**
 * Removes a reservation a user made himself
 * @param reservationId
 */
var removeOwnReservation = function(reservationId) {
  displayDialog({
    title: "Remove reservation ?",
    closeButton: true,
    content: "Remove reservation ? <br />"+$('#ownReservation'+reservationId+" .media").html(),
    buttons : {
      "Ok" : {
        icon: "icon-trash",
        cssClass: "btn-danger",
        callback: function()  {
          pAjax(jsRoutes.controllers.ReservationsController.deleteOwnReservation(reservationId),null,
            function(data) {
              $('#ownReservation'+reservationId).remove();
              closeDialog();
            },
            function(err) {
              console.error(err);
              closeDialog();
            });
        }
      }
    }

  });
}