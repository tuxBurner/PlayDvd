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
 * Creates a select2 select 
 * @param jqSelector
 * @param dataObj
 */
function createSelect2Deselect(jqSelector,dataObj) {
	$(jqSelector).select2({
		allowClear: true,
	    createSearchChoice: function(term, data) { 
        	  if ($(data).filter(function() { console.info(this); return this.localeCompare(term)===0; }).length===0) {
        		  return term;
        	  }
          },
	    formatSelection: function(item) { return item; },
        formatResult:  function(item) { return item; },
        id: function(object) {
        	return object;
        },
        initSelection : function (element) {
          return $(element).val();
        },
        data: dataObj
	});
}
