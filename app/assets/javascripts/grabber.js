$(function() {
  /**
   * button in the popup will be always clickable
   */
  $('#grabber_search_button').live('click',function() {
    searchGrabber($('#grabber_search_input').val(),$('#grabberType').val(),$('#movieToEditId').val(),$('#grabberEanNr').val());
    return false;
  });

  /**
   * The user searches in the grabber poup
   */
  $('.pickGrabberEntry').live('click',function() {
    openGrabberMoviePopup($(this).data('grabberId'),$(this).data('grabberType'),$('#movieToEditId').val(),$('#grabberEanNr').val());
  });
});

/**
 * This searches the grabber and returns the result via ajax and writes it to the dialog
 * @param title
 * @param movieToEditId
 * @param grabberType
 * @param eanNr
 */
var searchGrabber = function(title,grabberType,movieToEditId,eanNr) {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.InfoGrabberController.searchGrabber(title,grabberType),
    ajaxParams : { "movieToEditId" : movieToEditId, "eanNr" : eanNr},
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
 * @param eanNr
 */
var openGrabberMoviePopup = function(grabberId,grabberType,movieToEditId,eanNr) {
  showWaitDiaLog();
  displayAjaxDialog({
    route: jsRoutes.controllers.InfoGrabberController.getMovieById(grabberId,grabberType),
    ajaxParams : { "movieToEditId" : movieToEditId, "eanNr": eanNr},
    title: 'Movie info from Grabber',
    onOpen: closeWaitDiaLog,
    cssClass: "grabberModal",
    buttons : {
      "Ok" : {
        icon: "icon-ok-sign icon-white",
        cssClass: "btn-danger",
        callback: function() {
          if(eanNr != null && eanNr != "") {
            addToDbAndFillDvdFormCheck(grabberId,grabberType,eanNr);
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
 * @param eanNr
 */
var addToDbAndFillDvdFormCheck = function(grabberId,grabberType,eanNr) {

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
              callback: function() { addToDbAndFillDvdForm(grabberType,eanNr) }
            }
          }
        });
      } else {
        addToDbAndFillDvdForm(grabberType,eanNr);
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
 * @param eanNr
 */
var addToDbAndFillDvdForm = function(grabberType,eanNr) {
  showWaitDiaLog();
  var formParams = $('#grabberMovieForm').formParams();
  console.error("WTF");
  console.error(formParams);
  pAjax(jsRoutes.controllers.DvdController.addMovieByGrabber(grabberType),formParams,
    function(data) {
      window.location = jsRoutes.controllers.DvdController.showAddDvdByEanAndMovie(eanNr,data).absoluteURL();
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
