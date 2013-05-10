/**
 * Here we have some helpers we can use in play apps :)
 */
var appIsInHttps = (window.location.protocol == 'https:');


$(function () {

  // do some clean up when the modal is hidden
  $('#modal').on('hide', function () {

    $('#modalLabel').html("");
    $('#modal .modal-body').html("");

    // remove old buttons
    $('#modal .modal-footer').empty();

    var oldCssClassToAdd = $('#modal').data('cssClassToAdd');
    if (oldCssClassToAdd != null) {
      $('#modal').removeClass(oldCssClassToAdd);
    }

    checkDialogCloseFunction();

    $("html").css("overflow", "auto");
  });

});

/**
 * This function handles an ajax call to an javascript route which is configured
 */
function pAjax(controller, fnData, sucessFn, errorFn) {
  controller.ajax({
    data: fnData,
    success: function (data) {
      sucessFn(data);
    },
    error: function (data) {
      // TODO: on error and no errorFn alert here something
      errorFn(data)
    }
  });
}

/**
 * Shows the wait dialog
 */
function showWaitDiaLog() {
  $('#pleaseWaitDialog').modal('show');
}

/**
 * Closes the wait dialog
 */
function closeWaitDiaLog() {
  $("#pleaseWaitDialog").modal('hide');
}

/**
 * calls an ajax route to display the remote content
 * @param options
 */
function displayAjaxDialog(options) {
  pAjax(options.route, options.ajaxParams,
    function (data) {
      options["content"] = data;
      displayDialog(options);
    },
    function (err) {
      //TODO: do something
    });
}

/**
 * display a dialog
 * @param options
 */
function displayDialog(options) {

  //prevent scrolling off the back window
  $("html").css("overflow", "hidden");

  $('#modal').data('cssClassToAdd', options.cssClass);
  if (options.cssClass != null) {
    $('#modal').addClass(options.cssClass);
  }

  $('#modal .modal-footer').empty();

  if (options.buttons != null) {

    for (var title in options.buttons) {
      var button = options.buttons[title];
      var iconHtml = (button.icon != null) ? '<i class="' + button.icon + '"></i>' : '';
      var cssClass = (button.cssClass != null) ? ' ' + button.cssClass : '';
      var i18nTitle = Messages(title);

      $('<button class="btn' + cssClass + '">' + iconHtml + ' ' + i18nTitle + '</button>').click(button.callback).appendTo('#modal .modal-footer');
    }
  }

  if (options.closeButton == true) {
    $('#modal .modal-footer').append('<button class="btn" data-dismiss="modal" aria-hidden="true">'+Messages('btn.close')+'</button>');
  }

  $('#modalLabel').html(options.title);
  $('#modal .modal-body').html(options.content);
  $('#modal').modal('show');

  if (options.onOpen != null) {
    options.onOpen();
  }

  if(options.onClose != null) {
    $('#modal').data("onCloseFunction", options.onClose);
  }

  Holder.run();
}

/**
 * closes the dialog
 */
function closeDialog() {
  $("html").css("overflow", "auto"); // enable scrollbars on html
  $('#modal').modal('hide')
}



/**
 * Checks if the dialog has a close function set and if so calls it
 *
 */
var checkDialogCloseFunction = function() {
  var onCloseFunction = $('#modal').data("onCloseFunction");
  if(onCloseFunction != null) {
    onCloseFunction();
  }
  $('#modal').data("onCloseFunction",null);
};

/**
 * Creates a select2 select where the user can create new entrance
 * @param jqSelector
 * @param dataObj
 * @param allowClear
 * @param formatFunc
 */
var createSelect2Deselect = function(jqSelector, dataObj, formatFunc, allowClear) {

  var cssClassToAdd = $(jqSelector).attr("class");

  $(jqSelector).select2({
    allowClear: (allowClear == null || allowClear == true) ? true : false,
    escapeMarkup: function (m) { return m; }, // we do not want to escape markup since we are displaying html in results
    formatSelection: (formatFunc == null) ? select2Format : formatFunc,
    formatResult: (formatFunc == null) ? select2Format : formatFunc,
    data: prepareSelect2Data(dataObj),
    initSelection: select2InitSelection,
    containerCssClass: cssClassToAdd
  });
};

/**
 * Creates a select2 select where the user can create new entrance
 * @param jqSelector
 * @param dataObj
 */
var createSelect2DeselectCreate = function(jqSelector, dataObj) {

  var cssClassToAdd = $(jqSelector).attr("class");

  $(jqSelector).select2({
    allowClear: true,
    createSearchChoice:function(term, data) { if ($(data).filter(function() { return this.text.localeCompare(term)===0; }).length===0) {return {id:term, text:term};} },
    formatSelection: select2Format,
    formatResult: select2Format,
    initSelection: select2InitSelection,
    data: prepareSelect2Data(dataObj),
    containerCssClass: cssClassToAdd,
    matcher: function (term, text) {
      return text.toUpperCase().indexOf(term.toUpperCase()) == 0;
    }
  });
}

/**
 * Creates a tag box with select2 which queries the backend for tags via ajax
 * @param jqSelector
 * @param controllerAction
 * @param queryParams
 */
function createSelect2TagAjaxBox(jqSelector, controllerAction, queryParams) {
  var cssClassToAdd = $(jqSelector).attr("class");
  $(jqSelector).select2({
    multiple: true,
    containerCssClass: cssClassToAdd,
    minimumInputLength: 3,
    initSelection: function (element, callback) {
      var data = [];
      $(element.val().split(",")).each(function (i,obj) {
        data.push({id: obj, text: obj});
      });
      callback(data);
    },
    ajax: {
      url: controllerAction.url.substring(0, controllerAction.url.indexOf('?')),
      dataType: 'json',
      data: function (term, page) {
        queryParams["term"] = term;
        return queryParams;
      },
      results: function (data, page) {
        return {
          results: data
        };
      }
    }
  });
}

/**
 * Creates an object for the select2
 * @param dataObj
 */
// TODO: can this be done in the backend ?
function prepareSelect2Data(dataObj) {
  var select2Data = new Array();
  for (idx in dataObj) {
    select2Data[idx] = {"id": dataObj[idx], "text": dataObj[idx]};
  }

  return select2Data;
}

function select2Format(item) {
  return item.text;
}

function select2InitSelection(element, callback) {
  callback({"id": $(element).val(), "text": $(element).val()});
} 
