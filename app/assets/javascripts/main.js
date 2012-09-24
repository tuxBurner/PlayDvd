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
