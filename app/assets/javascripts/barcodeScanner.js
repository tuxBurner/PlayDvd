/**
 *
 * User: tuxburner
 * Date: 2/11/13
 * Time: 8:16 PM
 * This is used for capturing the barcode from a webcam
 */

var searchAfterScan = false;


// websocket
var ws = null;

$(function () {
    // open the websocket
    ws = new WebSocket(jsRoutes.controllers.BarcodeController.scanBarcode().webSocketURL(appIsInHttps));
});

/**
 * Initializes the webcam when the socket to the backend is opened :)
 */
ws.onopen = function () {


    Webcam.set({
        width: 320,
        height: 240,
        dest_width: 320,
        dest_height: 240,
        image_format: 'jpeg',
        jpeg_quality: 90,
        force_flash: false,
        enable_flash: false
    });
    Webcam.attach('#cameraWrapper');

};


/**
 * Sends the image data of the native webcam
 */
var sendNativeBC = function () {
    jQuery("#barcodeStatus").text("Checking code.");
    Webcam.snap(function (dataAsUri) {
        ws.send(dataAsUri);
    });
};


/**
 * When we recieve a message over the websocket we have to take care for ite
 * @param e
 */
ws.onmessage = function (e) {
    if (e.data == "error") {
        jQuery("#barcodeStatus").text(Messages('msg.error.barcodeNotFound'));
        jQuery("#barcodeStatus").text(Messages('msg.info.barcodeWait'));
        window.setTimeout(sendNativeBC, 1000);
    } else {
        if (searchAfterScan === true) {
            stopVideoCapture();
            openAmazonLookUp(e.data);
        } else {
            $('#eanNr').val(e.data);
            closeDialog();
        }

    }
};


/**
 * Stops the webcam access from the browser
 */
var stopVideoCapture = function () {
    Webcam.reset();
    ws.close();
};

/**
 * Triggers the barcode event
 * @param search if true the scanned code will be taken for searching @ amazon
 */
var barcodeStartCaputring = function (search) {
    searchAfterScan = search;
    sendNativeBC();
};

