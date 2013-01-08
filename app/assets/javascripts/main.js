/**
 * Here we have some helpers we can use in play apps :) 
 */

$(function() {
	
	// create the please wait dialog so we can display it when having long operations running
	$( "#pleaseWaitDialog" ).dialog( {
		closeOnEscape : false,
		draggable :  false,
		modal : true,
		resizable : false,
		autoOpen: false
	});
	
	// do some clean up when the modal is hidden
	$('#modal').on('hidden', function() {
		
		$('#modalLabel').html("");
		$('#modal .modal-body').html("");
		
		// remove old buttons
		$('#modal .modal-footer').empty();
		
		var oldCssClassToAdd = $('#modal').data('cssClassToAdd');
		if(oldCssClassToAdd != null) {
			$('#modal').removeClass(oldCssClassToAdd);
		}
	});
		
});

/**
 * This function handles an ajax call to an javascript route which is configured
 */
function pAjax(controller,fnData,sucessFn,errorFn) {
	controller.ajax({
		data :    fnData,
		success : function(data) { sucessFn(data) },
		error :   function(data) { errorFn(data) }
	});
}

/**
 * Shows the wait dialog
 */
function showWaitDiaLog() {
	$( "#pleaseWaitDialog").dialog('open');
}

/**
 * Closes the wait dialog
 */
function closeWaitDiaLog() {
	$( "#pleaseWaitDialog").dialog('close');
}


function displayDialog(content, title,cssClassToAdd,closeButton, buttonsToAdd) {
	
	$('#modal').data('cssClassToAdd',cssClassToAdd);
	if(cssClassToAdd != null) {
		$('#modal').addClass(cssClassToAdd);
	}
	
	if(closeButton == true) {
	  $('#modal .modal-footer').append('<button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>');
	}
	
	if(buttonsToAdd != null) {
		$(buttonsToAdd).each(function(i,button) {
			
		});
	}
	
	$('#modalLabel').html(title);
	$('#modal .modal-body').html(content);
    $('#modal').modal('show');
    
    
}


/**
 * Creates a select2 select where the user can create new entrance
 * @param jqSelector
 * @param dataObj
 */
function createSelect2Deselect(jqSelector,dataObj,formatFunc,allowClear) {
	
	
	$(jqSelector).select2({
		allowClear: (allowClear == null || allowClear == true) ? true : false,
	    formatSelection: (formatFunc == null) ? select2Format : formatFunc,
        formatResult:  (formatFunc == null) ? select2Format : formatFunc,
        data: prepareSelect2Data(dataObj),
        initSelection : select2InitSelection

	});
}

/**
 * Creates a select2 select where the user can create new entrance
 * @param jqSelector
 * @param dataObj
 */
function createSelect2DeselectCreate(jqSelector,dataObj) {
	$(jqSelector).select2({
		allowClear: true,
	    createSearchChoice: function(term, data) {
        	  if ($(data).filter(function() { return this.localeCompare(term)===0; }).length===0) {
        		  console.error(term);
        		  return {"id" : term, "text": term};
        	  }
          },
	    formatSelection: select2Format,
        formatResult:  select2Format,
        initSelection : select2InitSelection,
        data: prepareSelect2Data(dataObj),
        matcher: function(term, text) { return text.toUpperCase().indexOf(term.toUpperCase())==0; }
	});
}

/**
 * Creates a tag box with select2 which queries the backend for tags via ajax
 * @param jqSelector
 * @param controllerAction
 * @param queryParams
 */
function createSelect2TagAjaxBox(jqSelector,controllerAction,queryParams) {
  $(jqSelector).select2({
    multiple : true,
	minimumInputLength: 3,
	initSelection : function (element, callback) {
	  var data = [];
	  $(element.val().split(",")).each(function () {
	    data.push({id: this, text: this});
	  });
      callback(data);
    },
	ajax: {
	  url:  controllerAction.url.substring(0,controllerAction.url.indexOf('?')),
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
	var select2Data  = new Array();
	for(idx in dataObj) {
		select2Data[idx] = {"id": dataObj[idx] , "text": dataObj[idx]};
	}
	
	return select2Data;
}

function select2Format(item) {
	return item.text; 
}

function select2GetId(object) {
	return object;
}

function select2InitSelection(element,callback) {
	  callback({"id" : $(element).val() , "text" :  $(element).val()});
} 
