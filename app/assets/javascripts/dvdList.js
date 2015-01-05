$(function () {

  $('.tooltipo').tooltip();

  $(document).on('mouseenter', '.coverwrapper em', function () {
    $('.dvdInfo', this).fadeIn(150);
  }).on('mouseleave', '.coverwrapper em', function () {
    $('.dvdInfo', this).fadeOut(150);
  });


  /**
   * Clicking on a link in the navbar
   */
  $(document).on('click', '.copyNavLink', function () {
    navToPage($(this).data('idx'));
    return false;
  });

  /**
   * INFO DIALOG
   */
    // open the info dialog when the user clicks on the info button
  $(document).on('click', '.coverwrapper em, .coverwrapper_small em, a.displayCopy', function (event) {
    var copyId = $(this).data('dvdId');
    // testing style
    window.location = jsRoutes.controllers.Dashboard.displayCopyOnPage(copyId).absoluteURL(appIsInHttps);
  });
  /**
   * EO INFO DIALOG
   */


  /**
   * SEARCH FORM
   */
  // check if to display the advanced search from
  if (displayAdvancedOrder === true) {
    $('#advancedSearchForm').show();
  }

  createSelect2Deselect('#searchGenre', avaibleGenres);
  createSelect2Deselect('#searchOwner', avaibleUsers);
  createSelect2Deselect('#searchAgeRating', avaibleAgeRatings, function (item) {
    if (item === null || item === "") {
      return item;
    }
    return "<img class='flag' src='/assets/images/agerating/" + item.id + ".gif'/>";
  });
  createSelect2Deselect('#searchOrderBy', listOrderBy, null, false);
  createSelect2Deselect('#searchCopyType', avaibleCopyTypes, function (item) {
    if (item === null || item === "") {
      return item;
    }
    return "<img class='flag' src='/assets/images/copy_type/" + item.id + ".png'/>";
  });
  /**
   * EO SEARCH FORM
   */
});

/**
 * This calls the backend to add a comment
 */
var addComment = function (movieId) {
  var val = $('#movieCommentText').val().trim();
  if (val !== "") {
    pAjax(jsRoutes.controllers.CommentController.addComment(movieId), {"commentText": val}, function (data) {
      $('#comments').html(data);
    }, function (data) {
    });

  }
};

/**
 * Loads the list page for the given idx and replaces the current one
 * @param idx
 */
var navToPage = function (idx) {
  pAjax(
    jsRoutes.controllers.ListDvdsController.listCopiesJS(),
    {"pageNr": idx},
    function (data) {
      $('#copyListContainer').html(data);
      $('.tooltipo').tooltip();

      window.location.hash = idx;
      Holder.run();
    }
  );
};
