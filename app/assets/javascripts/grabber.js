$(function() {
  /**
   * button in the popup will be always clickable
   */
  $(document).on('click','#grabber_search_button',function() {
    searchGrabber($('#grabber_search_input').val(),$('#grabberType').val(),$('#movieToEditId').val(),$('#grabberAmazonCode').val(),$('#grabberCopyId').val());
    return false;
  });

  /**
   * The user searches in the grabber poup
   */
  $(document).on('click','.pickGrabberEntry',function() {
    openGrabberMoviePopup($(this).data('grabberId'),$(this).data('grabberType'),$('#movieToEditId').val(),$('#grabberAmazonCode').val(),$('#grabberCopyId').val());
  });
});

/**
 * This searches the grabber and returns the result via ajax and writes it to the dialog
 * @param title
 * @param movieToEditId
 * @param grabberType
 * @param grabberAmazonCode
 * @param copyId
 */
var searchGrabber = function(title,grabberType,movieToEditId,grabberAmazonCode,copyId) {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.InfoGrabberController.searchGrabber(title,grabberType),
    ajaxParams : {
      "movieToEditId" : movieToEditId,
      "amazonCode" : grabberAmazonCode,
      "copyId"     : copyId
    },
    title: 'Movie info from Grabber',
    onOpen: closeWaitDiaLog,
    cssClass: "grabberModal"
  });
}



/**
 * This opens the grabber movie informations popup where description,poster, backdrop etc can be selected
 * @param grabberId
 * @param grabberType
 * @param movieToEditId
 * @param amazonCode
 * @param copyId
 */
var openGrabberMoviePopup = function(grabberId,grabberType,movieToEditId,amazonCode,copyId) {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.InfoGrabberController.getMovieById(grabberId,grabberType),
    ajaxParams : {
      "movieToEditId" : movieToEditId,
      "amazonCode": amazonCode,
      "copyId" : copyId
    },
    title: 'Movie info from Grabber',
    onOpen: closeWaitDiaLog,
    cssClass: "grabberModal",
    buttons : {
      "Ok" : {
        icon: "icon-ok-sign icon-white",
        cssClass: "btn-danger",
        callback: function() {
          if(amazonCode != null && amazonCode != "") {
            addToDbAndFillDvdFormCheck(grabberId,grabberType,amazonCode,copyId);
          } else {
            fillMovieFormWithInfoFromGrabber();
          }
        }
      }
    }
  });
}

/**
 * This checks if the given movie already exists in the database and adds it to the databse.
 * Afterwards it returns to the dvd form and fills it with the data
 *
 * @param grabberId
 * @param grabberType
 * @param amazonCode
 * @param copyId
 */
var addToDbAndFillDvdFormCheck = function(grabberId,grabberType,amazonCode,copyId) {

  // because the existing movie popup is removing the form we store them here already
  var formParams = $('#grabberMovieForm').formParams();

  pAjax(jsRoutes.controllers.MovieController.checkIfMovieAlreadyExists(grabberId,grabberType),null,
    function(data) {
      if(data == "true" || data == true) {
        // ask the user if he wants to add this movie although it aready exists
        displayDialog({
          title: 'Movie already exists in the Database',
          closeButton: true,
          content: "The movie already exists in the Database, do you want to add it again ?",
          buttons : {
            "Add" : {
              icon: "icon-plus",
              cssClass: "btn-danger",
              callback: function() { addToDbAndFillDvdForm(grabberType,amazonCode,copyId,formParams) }
            }
          }
        });
      } else {
        addToDbAndFillDvdForm(grabberType,amazonCode,copyId,formParams);
      }

    },
    function(err) {
      $('#newMovieFormWrapper').html(err.responseText).show();
    }
  );
  return false;
}

/**
 * This adds the movie to the database and returns to the dvd form and fills it with the data
 * @param grabberType
 * @param amazonCode
 * @param copyId
 * @param formParams
 */
var addToDbAndFillDvdForm = function(grabberType,amazonCode,copyId,formParams) {
  showWaitDiaLog();
  if($.trim(copyId) == "") {
    copyId = null;
  }
  pAjax(jsRoutes.controllers.DvdController.addMovieByGrabber(grabberType),formParams,
    function(data) {
      window.location = jsRoutes.controllers.DvdController.showDvdByAmazonAndMovie(amazonCode,data,copyId).absoluteURL();
      closeWaitDiaLog();
    },
    function(err) {
      closeWaitDiaLog();
      console.error(err);
    });

  closeDialog();
}

/**
 * This is called when the user picked movie from grabber and wants to fill the movie form with the infos
 */
var fillMovieFormWithInfoFromGrabber = function() {
  showWaitDiaLog();
  var formParams = $('#grabberMovieForm').formParams();
  var movieFormMode = $('#grabberMovieForm').attr('mode');
  var grabberType = $('#grabberMovieForm').attr('grabberType');
  $('#newMovieFormWrapper').html('').hide();
  pAjax(jsRoutes.controllers.MovieController.addMovieByGrabberId(movieFormMode,grabberType),formParams,
    function(data) {
      $('#newMovieFormWrapper').html(data).show();
      initializeMovieForm();
      closeWaitDiaLog();
    },
    function(err) {
      closeWaitDiaLog();
      console.error(err);
    });

  closeDialog();
}
