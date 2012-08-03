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
	
	// load the genre menu
	pAjax(jsRoutes.controllers.Dashboard.menuGenres(), null, function(data) {
		$(data).replaceAll('#genreMenuReplace');
	}, function(){});
	
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
        id: select2GetId,
        initSelection : select2InitSelection,
        data: dataObj
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
        	  if ($(data).filter(function() { console.info(this); return this.localeCompare(term)===0; }).length===0) {
        		  return term;
        	  }
          },
	    formatSelection: select2Format,
        formatResult:  select2Format,
        id: select2GetId,
        initSelection : select2InitSelection,
        data: dataObj
	});
}


function select2Format(item) { 
	return item; 
}

function select2GetId(object) {
	return object;
}

function select2InitSelection(element) {
	return $(element).val();
} 
